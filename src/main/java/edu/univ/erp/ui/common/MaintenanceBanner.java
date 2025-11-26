package edu.univ.erp.ui.common;

import javax.swing.*;
import java.awt.*;

/*
  A simple, reusable JPanel that displays a highly visible "Maintenance Mode"
  warning.
  It is invisible by default and should be made visible by the main
  application frame when maintenance mode is active.
 */
public class MaintenanceBanner extends JPanel {

    private final JLabel textLabel;

    public MaintenanceBanner() {
        // Set a bright, unmissable background color
        setBackground(new Color(255, 180, 0)); // Strong yellow/orange
        setForeground(Color.BLACK);
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
        setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

        // Use a built-in warning icon for a professional look
        Icon icon = UIManager.getIcon("OptionPane.warningIcon");
        if (icon != null) {
            add(new JLabel(icon));
        }

        textLabel = new JLabel("System is in MAINTENANCE MODE. All changes are disabled. View-only access.");
        textLabel.setForeground(Color.BLACK);
        textLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        add(textLabel);

        // Start hidden by default
        setVisible(false);
    }

    /**
     * Sets the text of the banner.
     * @param text The new text to display.
     */
    public void setText(String text) {
        textLabel.setText(text);
    }
}