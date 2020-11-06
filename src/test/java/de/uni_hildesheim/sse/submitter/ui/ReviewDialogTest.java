package de.uni_hildesheim.sse.submitter.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.DialogFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import de.uni_hildesheim.sse.submitter.i18n.I18nProvider;
import de.uni_hildesheim.sse.submitter.settings.SubmissionConfiguration;
import net.ssehub.exercisesubmitter.protocol.backend.ServerNotFoundException;
import net.ssehub.exercisesubmitter.protocol.backend.UnknownCredentialsException;
import net.ssehub.exercisesubmitter.protocol.frontend.Assignment;
import net.ssehub.exercisesubmitter.protocol.frontend.Assignment.State;
import net.ssehub.exercisesubmitter.protocol.frontend.SubmitterProtocol;

@DisabledIf("java.awt.GraphicsEnvironment#isHeadless")
@Disabled("requires user input")
public class ReviewDialogTest {
    
    private DialogFixture fixture;
    
    private StandaloneSubmitter model;
    
    private boolean throwServerNotFound;
    
    @Test
    @DisplayName("has correct title")
    public void testTitle() {
        assertEquals(I18nProvider.getText("gui.elements.select_correction_replay"), fixture.target().getTitle());
    }
    
    @BeforeEach
    public void createDialogFixture() {
        this.model = new StandaloneSubmitter(new TestConfiguration(), new TestProtocol());
        List<Assignment> reviewedAssignments = new ArrayList<Assignment>();
        Assignment assignment = new Assignment("TestAssignment 001", "assignmentID", State.REVIEWED, false, 100);
        reviewedAssignments.add(assignment);
        ReviewDialog dialog = GuiActionRunner.execute(() -> new ReviewDialog(null, model, reviewedAssignments));
        fixture = new DialogFixture(dialog);
        fixture.show();
    }
    
    @AfterEach
    public void cleanupFixture() {
        fixture.cleanUp();
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
        public boolean login(String userName, String password) throws UnknownCredentialsException,
        ServerNotFoundException {
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
