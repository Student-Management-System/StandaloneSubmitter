package de.uni_hildesheim.sse.submitter.ui;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import de.uni_hildesheim.sse.submitter.i18n.I18nProvider;
import de.uni_hildesheim.sse.submitter.settings.SubmissionConfiguration;
import de.uni_hildesheim.sse.submitter.settings.ToolSettings;
import net.ssehub.exercisesubmitter.protocol.backend.ServerNotFoundException;
import net.ssehub.exercisesubmitter.protocol.backend.UnknownCredentialsException;
import net.ssehub.exercisesubmitter.protocol.frontend.SubmitterProtocol;

@DisabledIf("java.awt.GraphicsEnvironment#isHeadless")
public class StandaloneSubmitterWindowTest {

    private static final File TEST_SVN_REPO = new File("src/test/resources/svnRepo");
    
    private FrameFixture fixture;
    
    private StandaloneSubmitter model;
    
    private boolean throwServerNotFound;
    
    @Test
    @DisplayName("has correct title")
    public void testTitle() {
        assertEquals(ToolSettings.getConfig().getProgramName() + ' ' + ToolSettings.getConfig().getProgramVersion(),
                fixture.target().getTitle());
    }
    
    @Test
    @DisplayName("browseButton exists")
    public void testBrwoseButton() {
        var browseBtn = fixture.button("browseButton");
        
        assertTrue(browseBtn.isEnabled());
        assertEquals(I18nProvider.getText("gui.elements.browse"), browseBtn.text());
    }
    
    @Test
    @DisplayName("replayButton exists")
    public void testReplayButton() {
        var replayBtn = fixture.button("replayButton");
        
        assertTrue(replayBtn.isEnabled());
        assertEquals(I18nProvider.getText("gui.elements.replay"), replayBtn.text());
    }
    
    @Test
    @DisplayName("submitButton exists")
    public void testSubmitButton() {
        var submitBtn = fixture.button("submitButton");
        
        assertTrue(submitBtn.isEnabled());
        assertEquals(I18nProvider.getText("gui.elements.submit"), 
                submitBtn.text());  
    }
    
    @Test
    @DisplayName("historyButton exists")
    public void testHistoryButton() {
        var historyBtn = fixture.button("historyButton");
        
        assertTrue(historyBtn.isEnabled());
        assertEquals(I18nProvider.getText("gui.elements.history"), historyBtn.text());
    }
    
    @Test
    @DisplayName("reviewButton exists")
    public void testReviewButton() {
        var reviewBtn = fixture.button("reviewButton");
        
        assertTrue(reviewBtn.isEnabled());
        assertEquals(I18nProvider.getText("gui.elements.review"), reviewBtn.text());
    }
    
    @BeforeEach
    public void createDialogFixture() {
        this.model = new StandaloneSubmitter(new TestConfiguration(), new TestProtocol());
        StandaloneSubmitterWindow window = GuiActionRunner.execute(() -> new StandaloneSubmitterWindow(model));
        fixture = new FrameFixture(window);
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
            super("preset User value", "preset PW".toCharArray(), null);
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
