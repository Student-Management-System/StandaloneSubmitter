package de.uni_hildesheim.sse.submitter.svn;

/**
 * Thrown when unable to connect to the server.
 * 
 * @author Adam Krafczyk
 */
public class ServerNotFoundException extends Exception {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 3655318432262797101L;
    
    private String address;
    
    /**
     * Creates an exception for the server with the given address.
     * @param address address of the server that we couldn't connect to
     */
    public ServerNotFoundException(String address) {
        this.address = address;
    }
    
    /**
     * Returns the address of the server that we couldn't connect to.
     * @return address of the server
     */
    public String getAddress() {
        return address;
    }
    
}
