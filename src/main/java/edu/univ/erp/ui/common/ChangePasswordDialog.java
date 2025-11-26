package edu.univ.erp.ui.common;

import edu.univ.erp.auth.AuthService;
import edu.univ.erp.auth.UserSession;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class ChangePasswordDialog extends JDialog {

    private static final Logger log = LoggerFactory.getLogger(ChangePasswordDialog.class);

    private final AuthService authService;
    private final UserSession session;

    private JPasswordField oldPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JLabel feedbackLabel;
    private JButton saveButton;

    public ChangePasswordDialog(JFrame parent, AuthService authService, UserSession session) {
        super(parent, "Change Password", true); // true = modal
        this.authService = authService;
        this.session = session;

        initComponents();
    }

    private void initComponents() {
        setLayout(new MigLayout(
                "wrap 2, fillx, insets 20, wmin 400",
                "[right]15[fill,grow]",
                "[]10[]10[]20[]15[]"
        ));

        add(new JLabel("Old Password:"));
        oldPasswordField = new JPasswordField();
        add(oldPasswordField, "growx");

        add(new JLabel("New Password:"));
        newPasswordField = new JPasswordField();
        add(newPasswordField, "growx");

        add(new JLabel("Confirm New:"));
        confirmPasswordField = new JPasswordField();
        add(confirmPasswordField, "growx");

        feedbackLabel = new JLabel(" ");
        feedbackLabel.setForeground(Color.RED);
        add(feedbackLabel, "span, center");

        saveButton = new JButton("Save Changes");
        JButton cancelButton = new JButton("Cancel");

        // Theme Buttons
        UITheme.styleButton(saveButton);
        UITheme.styleButton(cancelButton);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        add(buttonPanel, "span, growx, align right");

        saveButton.addActionListener(e -> onSave());
        cancelButton.addActionListener(e -> dispose());

        pack();
        setResizable(true); 
        setLocationRelativeTo(getParent());
    }

    private void onSave() {
        String oldPass = new String(oldPasswordField.getPassword());
        String newPass = new String(newPasswordField.getPassword());
        String confirmPass = new String(confirmPasswordField.getPassword());

        if (oldPass.isBlank() || newPass.isBlank() || confirmPass.isBlank()) {
            setFeedback(Color.RED, "All fields are required.");
            return;
        }
        if (!newPass.equals(confirmPass)) {
            setFeedback(Color.RED, "New passwords do not match.");
            return;
        }

        if (newPass.equals(oldPass)) {
            setFeedback(Color.RED, "New password must be different from the old one.");
            return;
        }

        setFeedback(new Color(0, 100, 0), "Saving...");
        saveButton.setEnabled(false);

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                return authService.changePassword(session.getUserId(), oldPass, newPass);
            }

            @Override
            protected void done() {
                try {
                    String result = get();
                    if (result.startsWith("Success")) {
                        JOptionPane.showMessageDialog(ChangePasswordDialog.this,
                                "Password changed successfully.", "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    } else {
                        setFeedback(Color.RED, result);
                    }
                } catch (Exception ex) {
                    log.error("Error changing password", ex);
                    setFeedback(Color.RED, "System error.");
                }
                saveButton.setEnabled(true);
            }
        };
        worker.execute();
    }

    private void setFeedback(Color color, String text) {
        feedbackLabel.setForeground(color);
        feedbackLabel.setText(text);
        pack();
    }
}