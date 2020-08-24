package de.uni_hildesheim.sse.submitter.ui;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.DialogFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import de.uni_hildesheim.sse.submitter.i18n.I18nProvider;
import de.uni_hildesheim.sse.submitter.settings.SubmissionConfiguration;
import de.uni_hildesheim.sse.submitter.settings.ToolSettings;
import net.ssehub.exercisesubmitter.protocol.backend.ServerNotFoundException;
import net.ssehub.exercisesubmitter.protocol.backend.UnknownCredentialsException;
import net.ssehub.exercisesubmitter.protocol.frontend.SubmitterProtocol;

public class LoginDialogTest {

    private static final File TEST_SVN_REPO = new File("src/test/resources/svnRepo");
    
    private DialogFixture fixture;
    
    private boolean throwServerNotFound;
    
    @Test
    @DisplayName("has correct title")
    public void title() {
        assertEquals("Login", fixture.target().getTitle());
    }
    
    @Test
    @DisplayName("has username and password pre-filled")
    public void prefilledData() {
        var nameField = fixture.textBox("nameField");
        var pwField = fixture.textBox("passwordField");
        
        assertAll(
            () -> nameField.requireText("preset User value"),
            () -> pwField.requireText("preset PW")
        );
    }
    
    @Test
    @DisplayName("shows error for wrong login credentials")
    public void invalidCredentials() throws InterruptedException {
        var nameField = fixture.textBox("nameField");
        var loginButton = fixture.button("loginButton");
        var errorMessage = fixture.textBox("errorMessageLabel");
        
        nameField.setText("invalid");
        
        loginButton.click();
        Thread.sleep(1000);
        
        assertAll(
            () -> errorMessage.requireText(I18nProvider.getText("gui.error.unknown_credentials")),
            () -> assertTrue(fixture.target().isVisible(), "dialog should remain open")
        );
    }
    
    @Test
    @DisplayName("shows error for server not found")
    public void serverNotFound() throws InterruptedException {
        var loginButton = fixture.button("loginButton");
        var errorMessage = fixture.textBox("errorMessageLabel");
        
        throwServerNotFound = true;
        
        loginButton.click();
        Thread.sleep(1000);
        
        assertAll(
            () -> errorMessage.requireText(I18nProvider.getText("gui.error.system_unreachable")),
            () -> assertTrue(fixture.target().isVisible(), "dialog should remain open")
        );
    }
    
    @Test
    @DisplayName("shows error for SVN server not found")
    public void svnServerNotFound() throws InterruptedException {
        var nameField = fixture.textBox("nameField");
        var pwField = fixture.textBox("passwordField");
        var loginButton = fixture.button("loginButton");
        var errorMessage = fixture.textBox("errorMessageLabel");
        
        ToolSettings.getConfig().setRepositoryURL("file:///some/url");
        
        nameField.setText("student1");
        pwField.setText("some_pw");
        
        loginButton.click();
        Thread.sleep(1000);
        
        assertAll(
                () -> errorMessage.requireText(I18nProvider.getText("gui.error.login_wrong_repository",
                        ToolSettings.getConfig().getCourse().getTeamName(),
                        ToolSettings.getConfig().getCourse().getTeamMail())),
                () -> assertTrue(fixture.target().isVisible(), "dialog should remain open")
        );
    }
    
    @Test
    @DisplayName("shows error for invalid SVN URL")
    public void invalidSvnUrl() throws InterruptedException {
        var nameField = fixture.textBox("nameField");
        var pwField = fixture.textBox("passwordField");
        var loginButton = fixture.button("loginButton");
        var errorMessage = fixture.textBox("errorMessageLabel");
        
        ToolSettings.getConfig().setRepositoryURL("invalid_url");
        
        nameField.setText("student1");
        pwField.setText("some_pw");
        
        loginButton.click();
        Thread.sleep(1000);
        
        assertAll(
            () -> errorMessage.requireText(I18nProvider.getText("gui.error.server_not_found") + " invalid_url"),
            () -> assertTrue(fixture.target().isVisible(), "dialog should remain open")
        );
    }
    
    @Test
    @DisplayName("disposes for valid credentials")
    public void correctCredentials() throws InterruptedException {
        var nameField = fixture.textBox("nameField");
        var pwField = fixture.textBox("passwordField");
        var loginButton = fixture.button("loginButton");
        var errorMessage = fixture.textBox("errorMessageLabel");
        
        nameField.setText("student1");
        pwField.setText("some_pw");
        
        loginButton.click();
        Thread.sleep(1000);
        
        LoginDialog dialog = (LoginDialog) fixture.target();
        
        assertAll(
            () -> errorMessage.requireText(""),
            () -> assertFalse(fixture.target().isVisible(), "dialog should be closed"),
            () -> assertNotNull(dialog.getRepository(), "should have created repository instance")
        );
    }
    
    
    @BeforeEach
    public void createDialogFixture() {
        LoginDialog dialog = GuiActionRunner.execute(() -> new LoginDialog(null, new TestConfiguration(), new TestProtocol()));
        fixture = new DialogFixture(dialog);
        fixture.show();
    }
    
    @AfterEach
    public void cleanupFixture() {
        fixture.cleanUp();
    }
    
    @BeforeEach
    public void initSettings() throws IOException {
        ToolSettings.INSTANCE.init();
        ToolSettings.getConfig().setRepositoryURL("file:///" + TEST_SVN_REPO.getAbsolutePath());
    }
    
    private class TestConfiguration extends SubmissionConfiguration {

        public TestConfiguration() {
            super("preset User value", "preset PW", null);
        }
        
    }
    
    private class TestProtocol extends SubmitterProtocol {

        public TestProtocol() {
            super(null, null, null, null);
        }
        
        @Override
        public boolean login(String userName, String password) throws UnknownCredentialsException, ServerNotFoundException {
            if (throwServerNotFound) {
                throw new ServerNotFoundException("", "");
            }
            
            if (userName.equals("student1") && password.equals("some_pw")) {
                return true;
            }
            
            throw new UnknownCredentialsException("some message");
        }
        
    }
    
}
