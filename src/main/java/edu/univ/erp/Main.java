package edu.univ.erp;

import com.formdev.flatlaf.FlatDarkLaf; // Using a modern dark theme
import edu.univ.erp.auth.AuthService;
import edu.univ.erp.auth.store.AuthDAO;
import edu.univ.erp.data.db.DatabaseManager;
import edu.univ.erp.ui.LoginWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * Main entry point for the University ERP application.
 *
 * This class is responsible for:
 * 1. Setting the global Look and Feel (FlatLaf).
 * 2. Initializing the core DatabaseManager.
 * 3. Building and launching the initial LoginWindow on the Event Dispatch Thread (EDT).
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        // 1. Set Look and Feel (L&F)
        // This *must* be done before any Swing components are created.
        try {
            FlatDarkLaf.setup();
            log.info("FlatLaf Dark L&F initialized successfully.");
        } catch (Exception e) {
            log.error("Failed to initialize FlatLaf L&F. Defaulting to standard Swing.", e);
        }
        // 2. Start the application on the Swing Event Dispatch Thread (EDT)
        // This is the standard, safe way to start any Swing application.
        SwingUtilities.invokeLater(() -> {
            try {
                // 3. Initialize the core DatabaseManager
                // This single instance will manage pools for *both* databases.
                // It will be passed to all DAOs.
                DatabaseManager dbManager = new DatabaseManager();
                dbManager.init(); // Loads properties, starts Hikari pools
                log.info("DatabaseManager initialized.");

                // 4. Build the *minimum* services needed for login
                // We are manually doing "Dependency Injection".
                // AuthDAO needs the dbManager.
                // AuthService needs the authDAO.
                AuthDAO authDAO = new AuthDAO(dbManager);
                AuthService authService = new AuthService(authDAO);
                log.info("Auth service layer created.");

                // 5. Show the LoginWindow
                // We pass the dbManager and authService.
                // The LoginWindow will use the dbManager to build the *rest*
                // of the DAOs and services (Student, Admin, etc.)
                // *after* a user successfully logs in.
                new LoginWindow(authService, dbManager).setVisible(true);

            } catch (Exception e) {
                log.error("CRITICAL: Failed to initialize application services", e);
                
                // If we fail here (e.g., config file not found, DB down),
                // the app can't run. Show a fatal error.
                JOptionPane.showMessageDialog(null,"Fatal Error: Could not start application.\n" +"Please check database connection and configuration files.\n\n" +"Error: " + e.getMessage(),"Application Startup Error",JOptionPane.ERROR_MESSAGE);
                System.exit(1); // Exit on fatal error
            }
        });
    }
}