package edu.univ.erp.ui.admin;

import edu.univ.erp.access.AccessService;
import edu.univ.erp.service.AdminService;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

//Panel for toggling system-wide Maintenance Mode.

public class MaintenancePanel extends JPanel {

    private static final Logger log = LoggerFactory.getLogger(MaintenancePanel.class);

    private final AdminService adminService;
    private final AccessService accessService;
    private final Runnable onStateChangedCallback; // Callback to update MainDashboard's banner

    private JToggleButton toggleButton;
    private JButton saveButton;
    private JLabel feedbackLabel;

    /**
     * @param adminService  Service to change the setting.
     * @param accessService Service to read the current setting.
     * @param onStateChangedCallback A callback to run on success, typically to update the UI.
     */
    public MaintenancePanel(AdminService adminService, AccessService accessService, Runnable onStateChangedCallback) {
        this.adminService = adminService;
        this.accessService = accessService;
        this.onStateChangedCallback = onStateChangedCallback;

        initComponents();
        loadCurrentState();
    }

    private void initComponents() {
        setLayout(new MigLayout(
                "insets 20, fillx, wrap 2", // Layout constraints
                "[right]15[left, grow]",    // Column constraints
                "[]20[]10[]"              // Row constraints
        ));

        JLabel titleLabel = new JLabel("Maintenance Mode");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(titleLabel, "span, center, gapbottom 10");

        add(new JLabel("Mode Status:"));
        toggleButton = new JToggleButton("Click to Enable");
        toggleButton.addActionListener(e -> updateButtonText());
        add(toggleButton, "growx");

        saveButton = new JButton("Save Changes");
        add(saveButton, "skip 1, split 2, growx");

        feedbackLabel = new JLabel(" ");
        add(feedbackLabel, "growx");

        // Action Listener for the save button
        saveButton.addActionListener(e -> onSave());
    }

    //Reads the current state from the AccessService and sets the button.
     
    private void loadCurrentState() {
        boolean isMaintenanceOn = accessService.isMaintenanceModeOn();
        toggleButton.setSelected(isMaintenanceOn);
        updateButtonText();
    }

    //Updates the text of the toggle button based on its state.
     
    private void updateButtonText() {
        if (toggleButton.isSelected()) {
            toggleButton.setText("MAINTENANCE ON");
            toggleButton.setForeground(Color.RED);
        } else {
            toggleButton.setText("Maintenance Off");
            toggleButton.setForeground(Color.GREEN);
        }
    }

    /*
      Called when the "Save Changes" button is clicked.
      Runs the update on a SwingWorker.
     */
    private void onSave() {
        boolean newState = toggleButton.isSelected();
        log.info("Attempting to set maintenance mode to: {}", newState);

        feedbackLabel.setText("Saving...");
        saveButton.setEnabled(false);

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
                        feedbackLabel.setForeground(Color.GREEN);
                        feedbackLabel.setText(result);
                        // Trigger the callback to update the MainDashboard
                        if (onStateChangedCallback != null) {
                            onStateChangedCallback.run();
                        }
                    } else {
                        feedbackLabel.setForeground(Color.RED);
                        feedbackLabel.setText(result);
                    }
                } catch (Exception ex) {
                    log.error("Error saving maintenance state", ex);
                    feedbackLabel.setForeground(Color.RED);
                    feedbackLabel.setText("Error: " + ex.getMessage());
                }
                saveButton.setEnabled(true);
            }
        };
        worker.execute();
    }
}