package de.uni_hildesheim.sse.submitter.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

/**
 * Text Area for logging messages.
 * @author Jonathan Lechner
 *
 */
public class LogArea extends JPanel {

    private static final long serialVersionUID = -7516986866802637007L;
    
    private JTextPane textPane;
    private JScrollPane scrollPane;
    private StyledDocument document;
    private Style style;
    
    /**
     * Constructor sets the content of this Pane.
     */
    public LogArea() {
        textPane = new JTextPane();
        
        scrollPane = new JScrollPane();
        this.setLayout(new BorderLayout());
        scrollPane.getViewport().add(textPane); 
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setAutoscrolls(true);
        textPane.setEditable(false);
        
        textPane.setPreferredSize(new Dimension(500, 500));
        document = textPane.getStyledDocument();
        style = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        
        this.add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Appends default layouted text to textPane.
     * @param message text that should get appended.
     */
    public void append(String message) {
        
        // gets default Style
        getStyle("", false);
        
        try {
            document.insertString(document.getLength(), message, style);
        } catch (BadLocationException exc) {
            //TODO Errorreport
        }
    }
    
    /**
     * Appends default layouted text to textPane.
     * @param message text that should get appended.
     * @param color color that should get applyed.
     * @param bold true if text should displayed bold.
     */
    public void append(String message, String color, boolean bold) {
        
        getStyle(color, bold);
        
        try {
            document.insertString(document.getLength(), message, style);
        } catch (BadLocationException exc) {
            //TODO Errorreport
        }
    }
    
    /**
     * Empty the TextArea.
     */
    public void clear() {
        textPane.setText("");
    }
    
    /**
     * Returns Style for message type.
     * @param color color that should get applyed.
     * @param bold true if text should displayed bold.
     */
    private void getStyle(String color, boolean bold) {
        if (color.equals("")) {
            color = "#000000";
        }
        StyleConstants.setForeground(style, Color.decode(color));
        StyleConstants.setBold(style, bold);
    }
}
