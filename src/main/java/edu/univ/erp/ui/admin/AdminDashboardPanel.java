package edu.univ.erp.ui.admin;

import javax.swing.*;
import java.awt.*;

/**
 * A simple welcome panel for the Admin dashboard.
 */
public class AdminDashboardPanel extends JPanel {

    public AdminDashboardPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JLabel welcomeLabel = new JLabel("Welcome, Admin!");
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel instructionsLabel = new JLabel("Use the 'Admin' menu to manage users, courses, and system settings.");
        instructionsLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel centerPanel = new JPanel(new GridLayout(2, 1));
        centerPanel.add(welcomeLabel);
        centerPanel.add(instructionsLabel);

        add(centerPanel, BorderLayout.CENTER);
    }
}