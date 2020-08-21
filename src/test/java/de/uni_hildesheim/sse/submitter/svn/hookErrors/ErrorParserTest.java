package de.uni_hildesheim.sse.submitter.svn.hookErrors;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import de.uni_hildesheim.sse.submitter.settings.ToolSettings;

public class ErrorParserTest {

    @Test
    @DisplayName("returns no ErrorDescriptions for empty <submitResults>")
    public void noMessages() {
        String xml = "<submitResults/>\n";
        
        ErrorParser parser = new ErrorParser();
        
        assertDoesNotThrow(() -> parser.parse(xml));
        
        assertEquals(0, parser.getErrors().length, "should return no ErrorDescriptions");
    }
    
    @Test
    @DisplayName("returns an ErrorDescription for a single message")
    public void singleMessage() {
        String xml = "<submitResults>\n"
                + "    <message tool=\"javac\" type=\"error\" message=\"Doesn't compile\"/>\n"
                + "</submitResults>\n";
        
        ErrorParser parser = new ErrorParser();
        
        assertDoesNotThrow(() -> parser.parse(xml));

        ErrorDescription expected = new ErrorDescription();
        expected.setTool(Tool.JAVAC);
        expected.setSeverity(Severity.ERROR);
        expected.setMessage("Doesn't compile");
        
        assertArrayEquals(new ErrorDescription[] {expected}, parser.getErrors(), "should return correct error descriptions");
    }
    
    @Test
    @DisplayName("correctly sets all (optional) parameters for the ErrorDescription")
    public void allParameters() {
        String xml = "<submitResults>\n"
                + "    <message tool=\"checkstyle\" type=\"warning\" file=\"some/File.java\" line=\"245\" message=\"Missing space\">\n"
                + "        <example position=\"12\"/>\n"
                + "    </message\n>"
                + "</submitResults>\n";
        
        ErrorParser parser = new ErrorParser();
        
        assertDoesNotThrow(() -> parser.parse(xml));

        ErrorDescription expected = new ErrorDescription();
        expected.setTool(Tool.CHECKSTYLE);
        expected.setSeverity(Severity.WARNING);
        expected.setMessage("Missing space");
        expected.setLine(245);
        expected.setFile("some/File.java");
        
        assertArrayEquals(new ErrorDescription[] {expected}, parser.getErrors(), "should return correct error descriptions");
    }
    
    @Test
    @DisplayName("throws if the XML is not parseable")
    public void invalidXml() {
        String xml = "<a><b></a></b>";
        
        ErrorParser parser = new ErrorParser();
        
        assertThrows(InvalidErrorMessagesException.class,
                () -> parser.parse(xml)
        );
    }
    
    @Test
    @DisplayName("throws if the XML root element is not submitResults")
    public void invalidRoot() {
        String xml = "<other_root></other_root>";
        
        ErrorParser parser = new ErrorParser();
        
        assertThrows(InvalidErrorMessagesException.class,
                () -> parser.parse(xml)
        );
    }
    
    @Test
    @DisplayName("throws if there is a non-message element")
    public void nonMessage() {
        String xml = "<submitResults><unexpected/></submitResults>";
        
        ErrorParser parser = new ErrorParser();
        
        assertThrows(InvalidErrorMessagesException.class,
                () -> parser.parse(xml)
        );
    }
    
    @Test
    @DisplayName("throws if a message element does not contain a tool attribute")
    public void missingTool() {
        String xml = "<submitResults><message type=\"error\" message=\"test.\"/></submitResults>";
        
        ErrorParser parser = new ErrorParser();
        
        assertThrows(InvalidErrorMessagesException.class,
                () -> parser.parse(xml)
        );
    }
    
    @Test
    @DisplayName("throws if a message element does not contain a type attribute")
    public void missingType() {
        String xml = "<submitResults><message tool=\"javac\" message=\"test.\"/></submitResults>";
        
        ErrorParser parser = new ErrorParser();
        
        assertThrows(InvalidErrorMessagesException.class,
                () -> parser.parse(xml)
        );
    }
    
    @Test
    @DisplayName("throws if a message element does not contain a message attribute")
    public void missingMessage() {
        String xml = "<submitResults><message tool=\"javac\" type=\"error\"/></submitResults>";
        
        ErrorParser parser = new ErrorParser();
        
        assertThrows(InvalidErrorMessagesException.class,
                () -> parser.parse(xml)
        );
    }
    
    @Test
    @DisplayName("uses tool UNKNOWN if tool name is not known")
    public void unknownTool() {
        String xml = "<submitResults>\n"
                + "    <message tool=\"doesnt_exist\" type=\"error\" message=\"Testmessage\" />\n"
                + "</submitResults>\n";
        
        ErrorParser parser = new ErrorParser();
        
        assertDoesNotThrow(() -> parser.parse(xml));

        ErrorDescription expected = new ErrorDescription();
        expected.setTool(Tool.UNKNOWN);
        expected.setSeverity(Severity.ERROR);
        expected.setMessage("Testmessage");
        
        assertArrayEquals(new ErrorDescription[] {expected}, parser.getErrors(), "should return correct error descriptions");
    }
    
    @Test
    @DisplayName("uses severity UNKNOWN if severity name is not known")
    public void unknownSeverity() {
        String xml = "<submitResults>\n"
                + "    <message tool=\"javac\" type=\"doesnt_exist\" message=\"Testmessage\" />\n"
                + "</submitResults>\n";
        
        ErrorParser parser = new ErrorParser();
        
        assertDoesNotThrow(() -> parser.parse(xml));

        ErrorDescription expected = new ErrorDescription();
        expected.setTool(Tool.JAVAC);
        expected.setSeverity(Severity.UNKNOWN);
        expected.setMessage("Testmessage");
        
        assertArrayEquals(new ErrorDescription[] {expected}, parser.getErrors(), "should return correct error descriptions");
    }
    
    @Test
    @DisplayName("returns a ErrorDescriptions for each message")
    public void multipleMessages() {
        String xml = "<submitResults>\n"
                + "    <message tool=\"javac\" type=\"error\" message=\"Doesn't compile\"/>\n"
                + "    <message tool=\"checkstyle\" type=\"warning\" message=\"Missing space\"/>\n"
                + "    <message tool=\"encoding\" type=\"error\" message=\"Wrong encoding\"/>\n"
                + "</submitResults>\n";
        
        ErrorParser parser = new ErrorParser();
        
        assertDoesNotThrow(() -> parser.parse(xml));

        ErrorDescription expected1 = new ErrorDescription();
        expected1.setTool(Tool.JAVAC);
        expected1.setSeverity(Severity.ERROR);
        expected1.setMessage("Doesn't compile");
        
        ErrorDescription expected2 = new ErrorDescription();
        expected2.setTool(Tool.CHECKSTYLE);
        expected2.setSeverity(Severity.WARNING);
        expected2.setMessage("Missing space");
        
        ErrorDescription expected3 = new ErrorDescription();
        expected3.setTool(Tool.ENCODING);
        expected3.setSeverity(Severity.ERROR);
        expected3.setMessage("Wrong encoding");
        
        assertArrayEquals(new ErrorDescription[] {expected1, expected2, expected3}, parser.getErrors(),
                "should return correct error descriptions");
    }
    
    @BeforeAll
    public static void initToolSettings() {
        assertDoesNotThrow(() -> ToolSettings.INSTANCE.init());
    }
    
}
