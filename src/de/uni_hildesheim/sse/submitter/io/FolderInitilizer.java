package de.uni_hildesheim.sse.submitter.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

/**
 * Prepares temporary folder for committing Java project.
 * 
 * @author El-Sharkawy
 * 
 */
public class FolderInitilizer {

    public static final String CLASSPATH_FILE_NAME = ".classpath";
    public static final String PROJECT_FILE_NAME = ".project";

    private static final IOFileFilter NO_SVN_FILES_FILTER = FileFilterUtils.makeSVNAware(null);
    
    private File sourceFolder;
    private File destFolder;

    /**
     * Sole constructor for this class.
     * @param sourceFolder The source files (which shall be committed).
     * @param destFolder The temporary destination folder which will be used for committing files.
     */
    public FolderInitilizer(File sourceFolder, File destFolder) {
        this.sourceFolder = sourceFolder;
        this.destFolder = destFolder;
    }

    /**
     * Prepares the destination folder for submission.
     * <ul>
     * <li>Copies files of the current project to the destination</li>
     * <li>Removes deleted files from the working copy</li>
     * <li>Creates eclipse project settings</li>
     * </ul>
     * 
     * @param projectName
     *            The name of the project, which shall be submitted
     * @throws IOException
     *             If files could not be written to the temp folder.
     */
    public void init(String projectName) throws IOException {
        File[] oldContent = destFolder.listFiles(new SVNFilter());
        if (null != oldContent) {
            for (int i = 0; i < oldContent.length; i++) {
                FileUtils.deleteQuietly(oldContent[i]);
            }
        }
        
        FileUtils.copyDirectory(sourceFolder, destFolder, NO_SVN_FILES_FILTER);
        createEclipseProjectFiles(projectName);
    }

    /**
     * Created eclipse project settings, which are needed by the SVNSubmitHooks.
     * <ul>
     * <li>Creates the classpath file (where to find java source files, libraries, ...)</li>
     * <li>Creates the project (name of the project and natures/builders, ...)</li>
     * </ul>
     * 
     * @param projectName
     *            The name of the project, which shall be submitted
     * @throws IOException
     *             Will be thrown if one of the files could not be created.
     */
    private void createEclipseProjectFiles(String projectName) throws IOException {
        // Create classpath settings
        File cpFile = new File(destFolder, CLASSPATH_FILE_NAME);
        if (!cpFile.exists()) {
            InputStream input = getClass().getResourceAsStream("/" + CLASSPATH_FILE_NAME);
            FileUtils.copyInputStreamToFile(input, cpFile);
        }

        // Create Eclipse project settings
        File projectFile = new File(destFolder, PROJECT_FILE_NAME);
        if (!projectFile.exists()) {
            InputStream input = getClass().getResourceAsStream("/" + PROJECT_FILE_NAME);
            String projectContents = IOUtils.toString(input);
            projectContents = projectContents.replaceAll("\\$projectName", projectName);
            FileUtils.write(projectFile, projectContents);
        }

    }
}
