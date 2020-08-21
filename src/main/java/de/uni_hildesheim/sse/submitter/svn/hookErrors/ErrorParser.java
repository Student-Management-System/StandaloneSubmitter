package de.uni_hildesheim.sse.submitter.svn.hookErrors;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * XML parser for the messages created by the SVN hook.
 * 
 * @author Adam
 */
public class ErrorParser {

    private static final String ROOT_NODENAME = "submitResults";
    private static final String MESSAGE_NODENAME = "message";
    private static final String ATTRIBUTE_FILE = "file";
    private static final String ATTRIBUTE_LINE = "line";
    private static final String ATTRIBUTE_SEVERITY = "type";
    private static final String ATTRIBUTE_TYPE = "tool";
    private static final String ATTRIBUTE_MESSAGE = "message";
    // private static final String NESTED_NODE_CODE = "example";

    private List<ErrorDescription> errorDescriptions = new LinkedList<>();
    
    /**
     * Parses the given XML response from the hook.
     * Expects an XML error description of the following form (example):
     * <pre>
     * &lt;submitResults&gt;
     *     &lt;message tool="javac" type="error" file="Test4.java" line="4" message="error: ) expected"&gt;
     *         &lt;example position="27"&gt;
     *             System.out.println("Test";;
     *         &lt;/example&gt;
     *     &lt;/message&gt;
     * &lt;/submitResults&gt;
     * </pre>
     * 
     * @param xmlInput The error message to parse.
     * 
     * @throws InvalidErrorMessagesException If the XML message is invalid.
     */
    public void parse(String xmlInput) throws InvalidErrorMessagesException {
        try {
            Document document = parseXml(xmlInput);
            
            Node root = document.getDocumentElement();
            
            
            if (!root.getNodeName().equals(ROOT_NODENAME)) {
                throw new InvalidErrorMessagesException("Invalid root element: " + root.getNodeName());
            }
            
            readMessages(root);
            
        } catch (SAXException e) {
            throw new InvalidErrorMessagesException("Invalid XML", e);
        }
    }
    
    /**
     * Reads the message elements from a given submitResults root node. Fills {@link #errorDescriptions}.
     * 
     * @param root The submitResults root node.
     * 
     * @throws InvalidErrorMessagesException If any invalid elements are encountered.
     */
    private void readMessages(Node root) throws InvalidErrorMessagesException {
        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node messageNode = children.item(i);
            if (messageNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            
            if (!messageNode.getNodeName().equals(MESSAGE_NODENAME)) {
                throw new InvalidErrorMessagesException("Invalid node found: " + messageNode.getNodeName());
            }
            
            ErrorDescription error = convertMessageNode(messageNode);
            this.errorDescriptions.add(error);
        }
    }
    
    /**
     * Converts a single message node into an {@link ErrorDescription}.
     * 
     * @param messageNode The XML message node to convert.
     * 
     * @return The {@link ErrorDescription} created from the message node.
     * 
     * @throws InvalidErrorMessagesException If the XML node is malformed.
     */
    private ErrorDescription convertMessageNode(Node messageNode) throws InvalidErrorMessagesException {
        NamedNodeMap attributes = messageNode.getAttributes();
        
        Tool type = Tool.getByToolName(getRequiredAttribute(attributes, ATTRIBUTE_TYPE));
        if (type == null) {
            type = Tool.UNKNOWN;
        }
        
        Severity severity = Severity.getByName(getRequiredAttribute(attributes, ATTRIBUTE_SEVERITY));
        
        String message = getRequiredAttribute(attributes, ATTRIBUTE_MESSAGE);
        
        ErrorDescription result = new ErrorDescription();
        result.setTool(type);
        result.setSeverity(severity);
        result.setMessage(message);
        result.setFile(getAttribute(attributes, ATTRIBUTE_FILE));
        
        String lineAttribute = getAttribute(attributes, ATTRIBUTE_LINE);
        if (lineAttribute != null) {
            try {
                result.setLine(Integer.parseInt(lineAttribute));
            } catch (NumberFormatException e) {
                throw new InvalidErrorMessagesException("Invalid line number: " + lineAttribute, e);
            }
        }
        
        return result;
    }
    
    /**
     * Parses the given XML string.
     * 
     * @param xml The XML string to parse.
     * 
     * @return The parsed XML {@link Document}.
     * 
     * @throws SAXException If the XML parsing failed.
     */
    public static Document parseXml(String xml) throws SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
        DocumentBuilder parser;
        try {
            parser = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e); // cannot happen
        }
        parser.setErrorHandler(null);
        Document result;
        try {
            result = parser.parse(IOUtils.toInputStream(xml));
        } catch (IOException e) {
            throw new SAXException(e);
        }
        result.normalize();
        return result;
    }

    /**
     * Returns an array of {@link ErrorDescription}s.
     * For each message tag of the XML Input, a errorDescription will be created.
     * @return A maybe empty list of error descriptions.
     */
    public ErrorDescription[] getErrors() {
        return errorDescriptions.toArray(new ErrorDescription[errorDescriptions.size()]);
    }

    /**
     * Returns the value of an attribute of the given XML tag attributes.
     * 
     * @param attributes The attributes of the current XML tag.
     * @param attributeName The name of one of the attributes.
     * 
     * @return The value of the given attribute, never <code>null</code>.
     * 
     * @throws InvalidErrorMessagesException If the given attribute name does not appear in the attributes of the node.
     */
    private static String getRequiredAttribute(NamedNodeMap attributes, String attributeName)
            throws InvalidErrorMessagesException {
        String value = getAttribute(attributes, attributeName);
        if (value == null) {
            throw new InvalidErrorMessagesException("Missing attribute " + attributeName);
        }
        return value;
    }
    
    /**
     * Returns the value of an attribute of the given XML tag attributes.
     * 
     * @param attributes The attributes of the current XML tag.
     * @param attributeName The name of one of the attributes.
     * 
     * @return The value of the given attribute or <code>null</code> if not found.
     */
    private static String getAttribute(NamedNodeMap attributes, String attributeName) {
        String result = null;
        Node attrNode = attributes.getNamedItem(attributeName);
        if (null != attrNode) {
            result = attrNode.getNodeValue();
        }

        return result;
    }
    
}
