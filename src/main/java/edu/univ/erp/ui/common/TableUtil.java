package edu.univ.erp.ui.common;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;

/**
 * A static utility class to apply consistent, modern styling to JTables.
 */
public final class TableUtil {

    // Private constructor to prevent instantiation
    private TableUtil() {
    }

    /**
     * Applies a standard, modern style to a JTable and its ScrollPane.
     *
     * @param scrollPane The JScrollPane containing the table.
     * @param table      The JTable to style.
     */
    public static void applyCustomStyling(JScrollPane scrollPane, JTable table) {
        //  Table Styling 
        table.setRowHeight(28);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(new Color(220, 220, 220)); // Light gray grid
        table.setShowVerticalLines(false); // Modern look

        //  Header Styling 
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        table.getTableHeader().setOpaque(false);
        table.getTableHeader().setBackground(new Color(245, 245, 245)); // Light gray header
        table.getTableHeader().setForeground(Color.BLACK);
        table.getTableHeader().setReorderingAllowed(false); // Often good to disable

        //  ScrollPane Styling 
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        //  Row Styling (Alternating Colors) 
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private final Color ODD_ROW_COLOR = new Color(250, 250, 250);
            private final Color EVEN_ROW_COLOR = Color.WHITE;

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? EVEN_ROW_COLOR : ODD_ROW_COLOR);
                }
                
                // Add padding to cells
                ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
                return c;
            }
        });
    }

    /**
     * Hides a specific column in a JTable.
     * Useful for hiding ID columns.
     *
     * @param table     The table.
     * @param colIndex  The index of the column to hide.
     */
    public static void hideColumn(JTable table, int colIndex) {
        TableColumn col = table.getColumnModel().getColumn(colIndex);
        col.setMinWidth(0);
        col.setMaxWidth(0);
        col.setPreferredWidth(0);
    }
}