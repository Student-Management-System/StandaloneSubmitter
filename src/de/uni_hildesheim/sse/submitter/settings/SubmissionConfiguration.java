package de.uni_hildesheim.sse.submitter.settings;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import net.ssehub.exercisesubmitter.protocol.frontend.Assignment;
import net.ssehub.exercisesubmitter.protocol.utils.JsonUtils;
import net.ssehub.studentmgmt.backend_api.JSON;

/**
 * Stores all information which is needed for uploading a exercise.
 * 
 * @author El-Sharkawy
 * 
 */
public class SubmissionConfiguration {
    public static final String CONFIG_FILE_NAME = "submission.conf";

    private static JSON jsonParser = JsonUtils.createParser();
    
    private String user;
    /*
     * Transient avoids (de-)serialization of the password: https://stackoverflow.com/a/5889590
     */
    private transient String pw;
    private Assignment exercise;
    private String projectFolder;

    /**
     * Sole constructor for this class.
     * 
     * @param user
     *            Then (RZ-) user name.
     * @param pw
     *            The password.
     * @param exercise
     *            The exercise to upload.
     */
    public SubmissionConfiguration(String user, String pw, Assignment exercise) {
        this.user = user;
        this.pw = pw;
        this.exercise = exercise;
    }

    /**
     * Getter for the user name.
     * 
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * Getter for the password.
     * 
     * @return the pw
     */
    public String getPW() {
        return pw;
    }

    /**
     * Getter for the exercise.
     * 
     * @return the exercise
     */
    public Assignment getExercise() {
        return exercise;
    }
    
    /**
     * Setter for the user.
     * 
     * @param user the user
     */
    public void setUser(String user) {
        this.user = user;
        save();
    }
    
    /**
     * Setter for the PW.
     * 
     * @param password the password
     */
    public void setPW(String password) {
        this.pw = password;
    }
    
    /**
     * Setter for the exercise.
     * 
     * @param exercise the exercise
     */
    public void setExercise(Assignment exercise) {
        this.exercise = exercise;
    }
    
    /**
     * Stores the folder last time used to submit/replay exercises.
     * @return the local project folder
     */
    public File getProjectFolder() {
        return null != projectFolder ? new File(projectFolder) : null;
    }

    /**
     * Stores the folder last time used to submit/replay exercises.
     * @param projectFolder the local project folder
     */
    public void setProjectFolder(File projectFolder) {
        this.projectFolder = projectFolder.getPath();
        save();
    }
    
    /**
     * Writes user name and group to saveFile.
     */
    private void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE_NAME)) {
            String serialized = jsonParser.serialize(this);
            writer.write(serialized);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Loads the locally saved configuration.
     * @return The loaded configuration or an empty, new instance if there was nothing to load.
     */
    public static SubmissionConfiguration load() {
        SubmissionConfiguration result = null;
        File file = new File(CONFIG_FILE_NAME);
        if (file.exists()) {
            try {
                String content = Files.readString(file.toPath());
                result = jsonParser.deserialize(content, SubmissionConfiguration.class);
            } catch (IOException e) {
                // TODO SE: Log
            }
        }
        
        if (null == result) {
            result = new SubmissionConfiguration("", "", null);
        }
        
        return result;
    }
    
}
