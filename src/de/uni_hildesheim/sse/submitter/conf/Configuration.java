package de.uni_hildesheim.sse.submitter.conf;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import net.ssehub.exercisesubmitter.protocol.frontend.Assignment;

/**
 * Stores all information which is needed for uploading a exercise.
 * 
 * @author El-Sharkawy
 * 
 */
public class Configuration {

    private String group;
    private String user;
    private String pw;
    private Assignment exercise;
    private File saveFile;

    /**
     * Sole constructor for this class.
     * 
     * @param group
     *            The group where the user belongs to.
     * @param user
     *            Then (RZ-) user name.
     * @param pw
     *            The password.
     * @param exercise
     *            The exercise to upload.
     * @param saveFile
     *            The file to save this configuration to.
     *            Maybe <tt>null</tt> in this case changes will not be saved.
     */
    public Configuration(String group, String user, String pw, Assignment exercise, File saveFile) {
        this.group = group;
        this.user = user;
        this.pw = pw;
        this.exercise = exercise;
        this.saveFile = saveFile;
    }

    /**
     * Getter for the group.
     * 
     * @return the group
     */
    public String getGroup() {
        return group;
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
     * Setter for the group.
     * 
     * @param group the group
     */
    public void setGroup(String group) {
        this.group = group;
        save();
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
     * Writes user name and group to saveFile.
     */
    private void save() {
        if (saveFile != null) {
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(saveFile);
                
                writer.println(ConfigReader.GROUP + "=" + group);
                writer.println(ConfigReader.USER + "=" + user);
                
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        }
    }
    
}
