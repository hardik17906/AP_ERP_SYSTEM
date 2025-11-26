package edu.univ.erp.ui.student;

import edu.univ.erp.auth.UserSession;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import java.awt.*;

/**
 * A simple welcome panel for the Student dashboard.
 */
public class StudentDashboardPanel extends JPanel {

    private final UserSession session;
    // We keep studentService here, even if not used in this panel,
    // in case you add more dashboard features later.
    private final StudentService studentService;

    private JLabel welcomeLabel;

    public StudentDashboardPanel(UserSession session, StudentService studentService) {
        this.session = session;
        this.studentService = studentService;
        initComponents();
        loadStudentName(); // This is now much simpler
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        welcomeLabel = new JLabel("Welcome!");
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel instructionsLabel = new JLabel("Use the 'Student' menu to browse courses, manage your registrations, and view grades.");
        instructionsLabel.setHorizontalAlignment(SwingConstants.CENTER);

        centerPanel.add(welcomeLabel);
        centerPanel.add(instructionsLabel);

        add(centerPanel, BorderLayout.CENTER);
    }

    /**
     * Sets the welcome label using the username from the session.
     * No SwingWorker is needed!
     */
    private void loadStudentName() {
        // The student's name IS their username, which we already have
        // in the session object.
        welcomeLabel.setText("Welcome, " + session.getUsername() + "!");
    }
}