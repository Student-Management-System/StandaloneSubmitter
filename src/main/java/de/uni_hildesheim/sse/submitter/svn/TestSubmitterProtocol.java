package de.uni_hildesheim.sse.submitter.svn;

import java.util.Arrays;
import java.util.List;

import net.ssehub.exercisesubmitter.protocol.backend.NetworkException;
import net.ssehub.exercisesubmitter.protocol.backend.ServerNotFoundException;
import net.ssehub.exercisesubmitter.protocol.backend.UnknownCredentialsException;
import net.ssehub.exercisesubmitter.protocol.frontend.Assignment;
import net.ssehub.exercisesubmitter.protocol.frontend.Assignment.State;
import net.ssehub.exercisesubmitter.protocol.frontend.SubmissionTarget;
import net.ssehub.exercisesubmitter.protocol.frontend.SubmitterProtocol;

/**
 * A {@link SubmitterProtocol} that does not actually talk to a student management system.
 * 
 * @author Adam
 */
public class TestSubmitterProtocol extends SubmitterProtocol {

    /**
     * Creates this non-communicating submitter protocol.
     * 
     * @param authenticationURL Passed to super.
     * @param stdMgmtURL Passed to super.
     * @param courseName Passed to super.
     * @param submissionServer Passed to super.
     */
    public TestSubmitterProtocol(String authenticationURL, String stdMgmtURL, String courseName,
            String submissionServer) {
        super(authenticationURL, stdMgmtURL, courseName, submissionServer);
    }
    
    @Override
    public boolean login(String userName, String password) throws UnknownCredentialsException, ServerNotFoundException {
        return true;
    }
    
    @Override
    public List<Assignment> getOpenAssignments() throws NetworkException {
        return Arrays.asList(new Assignment("Testblatt01Aufgabe01", "Testblatt01Aufgabe01", State.SUBMISSION, true, 0));
    }
    
    @Override
    public List<Assignment> getReviewableAssignments() throws NetworkException {
        return Arrays.asList();
    }
    
    @Override
    public List<Assignment> getReviewedAssignments() throws NetworkException {
        return Arrays.asList(new Assignment("Testblatt01Aufgabe01", "Testblatt01Aufgabe01", State.REVIEWED, false, 0));
    }
    
    @Override
    public SubmissionTarget getPathToSubmission(Assignment assignment) throws NetworkException {
        return getPathToSubmission(assignment, "JP001");
    }
    
}
