package edu.univ.erp.ui.admin;

import edu.univ.erp.access.AccessService;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.ui.common.UITheme;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class SystemSettingsPanel extends JPanel {

    private static final Logger log = LoggerFactory.getLogger(SystemSettingsPanel.class);

    private final AdminService adminService;
    private final AccessService accessService;
    private final Runnable onStateChangedCallback;

    // UI Components
    private JToggleButton maintenanceToggle;
    private JTextField deadlineField;      // For Registration
    private JTextField dropDeadlineField;  // For Drops
    private JLabel feedbackLabel;

    public SystemSettingsPanel(AdminService adminService, AccessService accessService, Runnable onStateChangedCallback) {
        this.adminService = adminService;
        this.accessService = accessService;
        this.onStateChangedCallback = onStateChangedCallback;

        initComponents();
        loadCurrentSettings();
    }

    private void initComponents() {
        setLayout(new MigLayout("wrap 2, insets 20, fillx", "[right]15[left, grow]", "[]20[]10[]10[]20[]"));

        JLabel titleLabel = new JLabel("System Settings");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(UITheme.PRIMARY_COLOR);
        add(titleLabel, "span, center, gapbottom 10");

        //  1. Maintenance Mode Section 
        add(new JLabel("Maintenance Mode:"));
        maintenanceToggle = new JToggleButton("Turn ON");
        maintenanceToggle.addActionListener(e -> toggleMaintenance());
        add(maintenanceToggle, "growx, wmin 150");

        add(new JLabel("Effect:"), "skip 1");
        JLabel helpText = new JLabel("<html><small>Blocks all students/instructors from making changes.</small></html>");
        helpText.setForeground(Color.GRAY);
        add(helpText, "wrap");

        add(new JSeparator(), "span, growx, gapbottom 10");

        //  2. Registration Deadline Section 
        add(new JLabel("Registration Deadline:"));
        deadlineField = new JTextField();
        deadlineField.setToolTipText("Format: yyyy-MM-ddTHH:mm");
        add(deadlineField, "growx");

        JButton saveRegButton = new JButton("Update Reg. Deadline");
        UITheme.styleButton(saveRegButton); // Theme
        saveRegButton.addActionListener(e -> saveDeadline("registration_deadline", deadlineField.getText()));
        add(saveRegButton, "skip 1, growx");

        //  3. Drop Deadline Section 
        add(new JLabel("Drop Deadline:"));
        dropDeadlineField = new JTextField();
        dropDeadlineField.setToolTipText("Format: yyyy-MM-ddTHH:mm");
        add(dropDeadlineField, "growx");

        JButton saveDropButton = new JButton("Update Drop Deadline");
        UITheme.styleButton(saveDropButton); // Theme
        saveDropButton.addActionListener(e -> saveDeadline("drop_deadline", dropDeadlineField.getText()));
        add(saveDropButton, "skip 1, growx");

        // Common Format Help Text
        add(new JLabel("Format:"), "skip 1");
        JLabel formatHelp = new JLabel("yyyy-MM-ddTHH:mm (e.g., 2025-12-31T23:59)");
        formatHelp.setForeground(Color.GRAY);
        add(formatHelp, "wrap");

        //  Feedback Section 
        feedbackLabel = new JLabel(" ");
        feedbackLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        add(feedbackLabel, "span, center, gaptop 20");
    }

    private void loadCurrentSettings() {
        boolean isMaintenanceOn = accessService.isMaintenanceModeOn();
        updateToggleButton(isMaintenanceOn);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            String regDate, dropDate;
            @Override
            protected Void doInBackground() {
                regDate = adminService.getRegistrationDeadline();
                dropDate = adminService.getDropDeadline();
                return null;
            }
            @Override
            protected void done() {
                deadlineField.setText(regDate);
                dropDeadlineField.setText(dropDate);
            }
        };
        worker.execute();
    }

    private void updateToggleButton(boolean isOn) {
        maintenanceToggle.setSelected(isOn);
        if (isOn) {
            maintenanceToggle.setText("ENABLED (System Locked)");
            maintenanceToggle.setForeground(Color.RED);
        } else {
            maintenanceToggle.setText("DISABLED (System Normal)");
            maintenanceToggle.setForeground(new Color(0, 128, 0)); // Dark Green
        }
    }

    private void toggleMaintenance() {
        boolean newState = maintenanceToggle.isSelected();

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                return adminService.toggleMaintenanceMode(newState);
            }
            @Override
            protected void done() {
                try {
                    String result = get();
                    if (result.startsWith("Success")) {
                        updateToggleButton(newState);
                        if (onStateChangedCallback != null) onStateChangedCallback.run();
                        setFeedback(Color.BLUE, "Maintenance mode updated.");
                    } else {
                        setFeedback(Color.RED, result);
                        updateToggleButton(!newState); // Revert on failure
                    }
                } catch (Exception e) {
                    log.error("Error toggling maintenance", e);
                    setFeedback(Color.RED, "Error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void saveDeadline(String type, String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            setFeedback(Color.RED, "Date cannot be empty.");
            return;
        }

        setFeedback(Color.BLACK, "Saving...");

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                if (type.equals("registration_deadline")) {
                    return adminService.updateRegistrationDeadline(dateStr.trim());
                } else {
                    return adminService.updateDropDeadline(dateStr.trim());
                }
            }
            @Override
            protected void done() {
                try {
                    String result = get();
                    if (result.startsWith("Success")) {
                        setFeedback(new Color(0, 128, 0), result);
                    } else {
                        setFeedback(Color.RED, result);
                    }
                } catch (Exception e) {
                    setFeedback(Color.RED, "Error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void setFeedback(Color color, String message) {
        feedbackLabel.setForeground(color);
        feedbackLabel.setText(message);
    }
}