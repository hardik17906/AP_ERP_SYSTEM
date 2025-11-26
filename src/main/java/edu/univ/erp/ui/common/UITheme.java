package edu.univ.erp.ui.common;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.net.URL;

public class UITheme {
    public static final Color PRIMARY_COLOR = new Color(63, 173, 168); // Teal
    public static final Color TEXT_WHITE = Color.WHITE;
    public static final Color ACCENT_COLOR = new Color(230, 245, 245);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);

    public static JPanel createGlobalHeader() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, PRIMARY_COLOR));

        // Simple Icon Loading
        JLabel headerLabel = new JLabel("IIITD ERP");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        headerLabel.setForeground(PRIMARY_COLOR);
        
        // Optional: Try to load logo, strictly if available
        ImageIcon logo = getIcon("/images/iiitd_logo.png", 80);
        if (logo != null) {
            headerLabel.setText("");
            headerLabel.setIcon(logo);
        }

        header.add(headerLabel);
        return header;
    }

    public static void styleButton(JButton button) {
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(TEXT_WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setFont(FONT_REGULAR);
        table.setSelectionBackground(ACCENT_COLOR);
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(new Color(220, 220, 220));
        JTableHeader header = table.getTableHeader();
        header.setBackground(PRIMARY_COLOR);
        header.setForeground(TEXT_WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
    }

    // Simplified helper using standard Swing
    public static ImageIcon getIcon(String path, int height) {
        URL imgUrl = UITheme.class.getResource(path);
        if (imgUrl == null) return null;
        ImageIcon icon = new ImageIcon(imgUrl);
        Image img = icon.getImage();
        // Maintain aspect ratio logic simplified
        float ratio = (float) icon.getIconWidth() / icon.getIconHeight();
        int width = (int) (height * ratio);
        return new ImageIcon(img.getScaledInstance(width, height, Image.SCALE_SMOOTH));
    }
}