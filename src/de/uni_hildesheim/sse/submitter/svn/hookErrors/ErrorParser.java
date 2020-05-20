package de.uni_hildesheim.sse.submitter.svn.hookErrors;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.uni_hildesheim.sse.submitter.settings.ToolSettings;

/**
 * XML PArser for an error description created by the SVN HOOK.
 * @author El-Sharkawy
 *
 */
public class ErrorParser {

    private static final String MESSAGE_NODES = "message";
    private static final String ATTRIBUTE_FILE = "file";
    private static final String ATTRIBUTE_LINE = "line";
    private static final String ATTRIBUTE_SEVERITY = "type";
    private static final String ATTRIBUTE_TYPE = "tool";
    private static final String ATTRIBUTE_SOLUTION = "message";
    private static final String END_OF_MESSAGE = "</submitResults>";
    private static final String BEGIN_OF_MESSAGE = "<submitResults>";
    // private static final String NESTED_NODE_CODE = "example";

    private Document doc;
    private String fallback;

    /**
     * Sole constructor of this class. Expects an XML error description of the following form (example):
     * <pre>
     * {@code
     * <submitResults>
     *     <message tool="javac" type="error" file="Test4.java" line="4" message="error: ) expected">
     *         <example position="27">
     *             System.out.println(&quot;Test&quot;;}
     *         &lt;/example>
     *     &lt;/message>
     * &lt;/submitResults>
     * </pre>
     * 
     * 
     * @param xmlInput The error message to parse.
     */
    public ErrorParser(String xmlInput) {
        // Avoid errors if result is not well-formated:
        if (ToolSettings.getConfig().isRepositorySendsXmlAnswers()) {
            int bPos = xmlInput.indexOf(BEGIN_OF_MESSAGE);
            int ePos = xmlInput.indexOf(END_OF_MESSAGE);
            if (bPos >= 0 && ePos > 0) {
                int lastValidCharacter = ePos + END_OF_MESSAGE.length();
                if (xmlInput.length() > lastValidCharacter) {
                    xmlInput = xmlInput.substring(0, lastValidCharacter);
                }
                xmlInput = xmlInput.substring(xmlInput.indexOf(BEGIN_OF_MESSAGE));
                
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder;
                try {
                    dBuilder = dbFactory.newDocumentBuilder();
                    InputStream xml = IOUtils.toInputStream(xmlInput);
                    doc = dBuilder.parse(xml);
                    doc.getDocumentElement().normalize();
                } catch (SAXException | IOException | ParserConfigurationException e) {
                    fallback = e.getMessage();
                }
            } else {
                fallback = ToolSettings.getConfig().getRepositoryConnectionError();
            }
        } else {
            fallback = xmlInput;
        }
    }

    /**
     * Returns an array of {@link ErrorDescription}s.
     * For each message tag of the XML Input, a errorDescription will be created.
     * @return A maybe empty list of error descriptions.
     */
    public ErrorDescription[] getErrors() {
        ErrorDescription[] errors = null;
        if (null != doc) {
            NodeList nodes = doc.getElementsByTagName(MESSAGE_NODES);
            errors = new ErrorDescription[nodes.getLength()];
            for (int i = 0; i < nodes.getLength(); i++) {
                errors[i] = new ErrorDescription();
                Node node = nodes.item(i);
    
                NamedNodeMap attributes = node.getAttributes();
                errors[i].setFile(getAttribute(attributes, ATTRIBUTE_FILE));
                errors[i].setLines(getAttribute(attributes, ATTRIBUTE_LINE));
                errors[i].setSolution(getAttribute(attributes, ATTRIBUTE_SOLUTION));
                errors[i].setSeverity(SeverityType.getByName(getAttribute(attributes, ATTRIBUTE_SEVERITY)));
                errors[i].setType(ErrorType.getByToolName(getAttribute(attributes, ATTRIBUTE_TYPE)));
    
                Node nestedNode = node.getFirstChild();
                if (null != nestedNode) {
                    nestedNode = nestedNode.getNextSibling();
                    if (null != nestedNode) {
                        nestedNode = nestedNode.getFirstChild();
                        if (null != nestedNode
                            && null != nestedNode.getTextContent()) {
                            
                            String data = nestedNode.getTextContent();
                            errors[i].setCode(data.trim());
                        }
                    }
                }
            }
        } else {
            errors = new ErrorDescription[1];
            errors[0] = new ErrorDescription();
            errors[0].setSeverity(SeverityType.ERROR);
            errors[0].setSolution("Unerwarteter Fehler " 
                + (null != fallback ? ": " + fallback : ".") + "\n"
                + "Bitte kontaktieren Sie einen Tutor unter Angabe von "
                + "Datum/Zeit bzw. Abgabe-Id dieser Abgabe.");
        }

        return errors;
    }

    /**
     * Returns the value of an attribute of the given XML tag.
     * @param attributes The attributes of the current XML tag.
     * @param attributeName The name of one of the attributes.
     * @return The value of the given attribute or <tt>null</tt> if not found.
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
