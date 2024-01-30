package com.calo.cmpp.module;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class CustomTableCellRenderer extends DefaultTableCellRenderer {

    private static final Color SELECTION_BORDER_COLOR = new Color(180, 180, 180);
    private static final int BORDER_THICKNESS = 2;  // Replace with your desired thickness
    private static final Border bottomBorder = BorderFactory.createMatteBorder(0, 0, BORDER_THICKNESS, 0, SELECTION_BORDER_COLOR);

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        this.setHorizontalAlignment(SwingConstants.CENTER);

        // Set the selection background color
        if (isSelected) {
            c.setBackground(SELECTION_BORDER_COLOR);
        } else {
            c.setBackground(table.getBackground());
        }

        // Remove the border for unselected cells
        if (!isSelected) {
            ((JComponent) c).setBorder(BorderFactory.createEmptyBorder());
        } else {
            // Set the custom border for the bottom of the cell
            ((JComponent) c).setBorder(BorderFactory.createCompoundBorder(((JComponent) c).getBorder(), bottomBorder));
        }
        return c;
    }
}
