package de.uni_hildesheim.sse.submitter.ui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import net.ssehub.exercisesubmitter.protocol.frontend.Assignment;

/**
 * Cell renderer for a combobox that allows the selection of an {@link Assignment}.
 * This is required to avoid creating an {@link Object#toString()} method for {@link Assignment}s, which may have a
 * different purpose.
 * @author El-Sharkawy
 *
 */
public class AssignmentComboxRenderer extends DefaultListCellRenderer {

    /**
     * Generated ID.
     */
    private static final long serialVersionUID = 7586544960156926641L;

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
        boolean cellHasFocus) {
        
        Component result = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof Assignment) {
            setText(((Assignment) value).getName());
        }
        return result;
    }

}
