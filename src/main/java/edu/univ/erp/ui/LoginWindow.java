package edu.univ.erp.ui;

import edu.univ.erp.access.AccessService;
import edu.univ.erp.auth.AuthService;
import edu.univ.erp.auth.UserSession;
import edu.univ.erp.auth.store.AuthDAO;
import edu.univ.erp.data.*;
import edu.univ.erp.data.db.DatabaseManager;
import edu.univ.erp.service.*;
// --- CRITICAL IMPORT BELOW ---
import edu.univ.erp.ui.common.UITheme;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.net.URL;

public class LoginWindow extends JFrame {

    private static final Logger log = LoggerFactory.getLogger(LoginWindow.class);

    private final AuthService authService;
    private final DatabaseManager dbManager;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel errorLabel;

    public LoginWindow(AuthService authService, DatabaseManager dbManager) {
        this.authService = authService;
        this.dbManager = dbManager;
        initComponents();
    }

    private void initComponents() {
        setTitle("IIITD ERP - Login");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Layout configuration
        JPanel panel = new JPanel(new MigLayout("wrap 1, fillx, insets 20", "[center]", "[]20[]10[]10[]20[]"));

        // --- 1. ADD LOGO (iiitd_logo.png) ---
        // Using loadScaledLogo to ensure it is NOT stretched
        // --- 1. ADD LOGO ---
        ImageIcon logoIcon = UITheme.getIcon("/images/iiitd_logo.png", 120);

        if (logoIcon != null) {
            JLabel logoLabel = new JLabel(logoIcon);
            panel.add(logoLabel, "gapbottom 20");
        } else {
            // Fallback text if image is missing
            JLabel textLogo = new JLabel("IIITD ERP");
            textLogo.setFont(new Font("SansSerif", Font.BOLD, 24));
            // Uses UITheme here. If import is missing, this line crashes.
            textLogo.setForeground(UITheme.PRIMARY_COLOR);
            panel.add(textLogo, "gapbottom 20");
        }

        // --- 2. INPUTS ---
        JPanel formPanel = new JPanel(new MigLayout("wrap 2, fillx, insets 0", "[right]10[fill,grow]"));

        formPanel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        formPanel.add(usernameField);

        formPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        formPanel.add(passwordField);

        panel.add(formPanel, "growx");

        // Error Label
        errorLabel = new JLabel(" ");
        errorLabel.setForeground(Color.RED);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(errorLabel, "growx");

        // --- 3. STYLED BUTTON ---
        loginButton = new JButton("Login");
        // Uses UITheme here too
        UITheme.styleButton(loginButton);
        panel.add(loginButton, "growx, h 35!");

        loginButton.addActionListener(this::onLoginClicked);
        passwordField.addActionListener(this::onLoginClicked);

        add(panel);
        pack();
        setMinimumSize(new Dimension(350, 400));
        setLocationRelativeTo(null);
    }

    // Helper to resize image without stretching (Maintain Aspect Ratio)

    private ImageIcon loadScaledLogo(String path, int maxWidth) {
        try {
            URL imgUrl = getClass().getResource(path);
            if (imgUrl == null) return null;

            BufferedImage img = ImageIO.read(imgUrl);
            int originalWidth = img.getWidth();
            int originalHeight = img.getHeight();

            // Calculate height to keep ratio
            int newWidth = maxWidth;
            int newHeight = (originalHeight * maxWidth) / originalWidth;

            Image scaled = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            log.error("Could not load logo", e);
            return null;
        }
    }

    private void onLoginClicked(ActionEvent e) {
        String username = usernameField.getText();
        char[] password = passwordField.getPassword();

        if (username.isBlank() || password.length == 0) {
            showError("Username and password are required.");
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");
        errorLabel.setText(" ");

        SwingWorker<AuthService.AuthResult, Void> worker = new SwingWorker<>() {
            @Override
            protected AuthService.AuthResult doInBackground() {
                return authService.login(username, new String(password));
            }

            @Override
            protected void done() {
                try {
                    AuthService.AuthResult result = get();
                    if (result.isSuccess()) {
                        log.info("Login successful for: {}", username);
                        launchDashboard(result.getSession());
                        dispose();
                    } else {
                        showError(result.getErrorMessage());
                    }
                } catch (Exception ex) {
                    log.error("Login exception", ex);
                    showError("Error: " + ex.getMessage());
                }
                loginButton.setEnabled(true);
                loginButton.setText("Login");
            }
        };
        worker.execute();
    }

    private void launchDashboard(UserSession session) {
        StudentDAO studentDAO = new StudentDAO(dbManager);
        InstructorDAO instructorDAO = new InstructorDAO(dbManager);
        CourseDAO courseDAO = new CourseDAO(dbManager);
        SectionDAO sectionDAO = new SectionDAO(dbManager);
        EnrollmentDAO enrollmentDAO = new EnrollmentDAO(dbManager);
        GradeDAO gradeDAO = new GradeDAO(dbManager);
        SettingsDAO settingsDAO = new SettingsDAO(dbManager);
        AuthDAO authDAO = new AuthDAO(dbManager);
        AssessmentDAO assessmentDAO = new AssessmentDAO(dbManager);

        AccessService accessService = new AccessService(session, settingsDAO, sectionDAO, enrollmentDAO);
        SettingsService settingsService = new SettingsService(settingsDAO);
        CatalogService catalogService = new CatalogService(sectionDAO, courseDAO, instructorDAO, enrollmentDAO, authDAO);
        StudentService studentService = new StudentService(enrollmentDAO, sectionDAO, gradeDAO, settingsDAO, accessService, courseDAO, session, studentDAO);
        InstructorService instructorService = new InstructorService(sectionDAO, enrollmentDAO, gradeDAO, accessService, studentDAO, authDAO, assessmentDAO);
        AdminService adminService = new AdminService(authDAO, studentDAO, instructorDAO, courseDAO, sectionDAO, settingsDAO, accessService);

        MainDashboard dashboard = new MainDashboard(session, accessService, settingsService, catalogService, studentService, instructorService, adminService, dbManager);
        dashboard.setVisible(true);
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }
}