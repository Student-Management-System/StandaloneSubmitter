package de.uni_hildesheim.sse.submitter.io;

import java.io.File;
import java.io.FileFilter;

/**
 * {@link FileFilter} for omitting SVN settings folder.
 * @author El-Sharkawy
 *
 */
public class SVNFilter implements FileFilter {

    @Override
    public boolean accept(File pathname) {
        return !(pathname.isDirectory() && pathname.getName().equalsIgnoreCase(".svn"));
    }

}
