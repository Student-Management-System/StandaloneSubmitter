package de.uni_hildesheim.sse.submitter.svn;

import java.io.File;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.ISVNStatusHandler;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusType;
import org.tmatesoft.svn.core.wc.SVNWCClient;

/**
 * Status handler for detecting file system changes before file will be submitted.
 * See <a href="http://svnkit.com/javadoc/org/tmatesoft/svn/core/wc/ISVNStatusHandler.html">http://svnkit.com/</a>
 * @author El-Sharkawy
 *
 */
class SVNStatusHandler implements ISVNStatusHandler {
    private SVNWCClient client;

    /**
     * Sole constructor for this class.
     * @param client A client for handling changes inside the local working copy.
     */
    SVNStatusHandler(SVNWCClient client) {
        this.client = client;
    }

    @Override
    public void handleStatus(SVNStatus status) throws SVNException {

        SVNStatusType type = status.getNodeStatus();
        File file = status.getFile();

        if (SVNStatusType.STATUS_UNVERSIONED.equals(type)) {
            client.doAdd(file, true, false, false, SVNDepth.EMPTY, false, false);
        } else if (SVNStatusType.STATUS_MISSING.equals(type) || SVNStatusType.STATUS_DELETED.equals(type)
            || (SVNStatusType.STATUS_NORMAL.equals(type) && !file.exists())) {
            
            client.doDelete(file, true, false, false);
        }
    }

}
