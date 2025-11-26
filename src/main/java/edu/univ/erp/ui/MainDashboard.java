package edu.univ.erp.ui;

import edu.univ.erp.access.AccessService;
import edu.univ.erp.auth.AuthService;
import edu.univ.erp.auth.UserSession;
import edu.univ.erp.auth.store.AuthDAO;
import edu.univ.erp.data.db.DatabaseManager;
import edu.univ.erp.service.*;
import edu.univ.erp.ui.admin.*;
import edu.univ.erp.ui.common.*; // Import UITheme
import edu.univ.erp.ui.instructor.*;
import edu.univ.erp.ui.student.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class MainDashboard extends JFrame {

    private static final Logger log = LoggerFactory.getLogger(MainDashboard.class);
    private final UserSession session;
    private final AccessService accessService;
    private final SettingsService settingsService;
    private final CatalogService catalogService;
    private final StudentService studentService;
    private final InstructorService instructorService;
    private final AdminService adminService;
    private final DatabaseManager dbManager;

    private JPanel mainCardPanel;
    private CardLayout cardLayout;
    private JLabel statusLabel;
    private MaintenanceBanner maintenanceBanner;

    private MyRegistrationsPanel myRegistrationsPanel;
    private TimetablePanel timetablePanel;
    private MyGradesPanel myGradesPanel;
    private GradebookPanel gradebookPanel;

    public MainDashboard(UserSession session, AccessService accessService, SettingsService settingsService,
                         CatalogService catalogService, StudentService studentService,
                         InstructorService instructorService, AdminService adminService, DatabaseManager dbManager) {
        this.session = session; this.accessService = accessService; this.settingsService = settingsService;
        this.catalogService = catalogService; this.studentService = studentService;
        this.instructorService = instructorService; this.adminService = adminService; this.dbManager = dbManager;
        initComponents();
        setupRoleBasedUI();
    }

    private void initComponents() {
        setTitle("IIITD ERP System");
        setSize(1200, 800);
        setMinimumSize(new Dimension(900, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        setJMenuBar(createMenuBar());

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        // 1. Maintenance Banner (Hidden by default)
        maintenanceBanner = new MaintenanceBanner();
        maintenanceBanner.setAlignmentX(Component.CENTER_ALIGNMENT);
        topContainer.add(maintenanceBanner);

        // 2. Global Logo Header (Top Center)
        JPanel logoHeader = UITheme.createGlobalHeader();
        logoHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        // Set size constraints so it doesn't shrink
        logoHeader.setMaximumSize(new Dimension(Short.MAX_VALUE, 90));

        topContainer.add(logoHeader);

        add(topContainer, BorderLayout.NORTH);

        statusLabel = new JLabel("Logged in as: " + session.getUsername() + " (" + session.getRole() + ")");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(60, 60, 60));
        statusLabel.setForeground(Color.WHITE);
        add(statusLabel, BorderLayout.SOUTH);

        cardLayout = new CardLayout();
        mainCardPanel = new JPanel(cardLayout);
        add(mainCardPanel, BorderLayout.CENTER);

        checkMaintenanceState();
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem changePassItem = new JMenuItem("Change Password");
        changePassItem.addActionListener(e -> new ChangePasswordDialog(this, new AuthService(new AuthDAO(dbManager)), session).setVisible(true));
        fileMenu.add(changePassItem);
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> {
            dispose();
            new LoginWindow(new AuthService(new AuthDAO(dbManager)), dbManager).setVisible(true);
        });
        fileMenu.add(logoutItem);
        menuBar.add(fileMenu);
        return menuBar;
    }

    private void setupRoleBasedUI() {
        JMenuBar menuBar = getJMenuBar();
        if (session.isStudent()) {
            Runnable onDataChanged = () -> { myRegistrationsPanel.loadRegistrations(); timetablePanel.loadTimetable(); myGradesPanel.loadGrades(); };
            mainCardPanel.add(new StudentDashboardPanel(session, studentService), "STUDENT_HOME");
            mainCardPanel.add(new CatalogPanel(session, catalogService, studentService, settingsService, onDataChanged), "CATALOG");
            myRegistrationsPanel = new MyRegistrationsPanel(session, studentService, settingsService, onDataChanged);
            mainCardPanel.add(myRegistrationsPanel, "MY_REGISTRATIONS");
            myGradesPanel = new MyGradesPanel(session, studentService);
            mainCardPanel.add(myGradesPanel, "MY_GRADES");
            timetablePanel = new TimetablePanel(session, studentService, settingsService);
            mainCardPanel.add(timetablePanel, "TIMETABLE");
            JMenu menu = new JMenu("Student");
            addItem(menu, "Dashboard", "STUDENT_HOME"); menu.addSeparator();
            addItem(menu, "Course Catalog", "CATALOG"); addItem(menu, "My Registrations", "MY_REGISTRATIONS");
            addItem(menu, "My Grades", "MY_GRADES"); addItem(menu, "My Timetable", "TIMETABLE");
            menuBar.add(menu);
            cardLayout.show(mainCardPanel, "STUDENT_HOME");
        } else if (session.isInstructor()) {
            gradebookPanel = new GradebookPanel(instructorService, session);
            mainCardPanel.add(new InstructorDashboardPanel(), "INSTRUCTOR_HOME");
            mainCardPanel.add(new MySectionsPanel(instructorService, session, settingsService, s -> { gradebookPanel.loadSection(s); cardLayout.show(mainCardPanel, "GRADEBOOK"); }), "MY_SECTIONS");
            mainCardPanel.add(gradebookPanel, "GRADEBOOK");
            JMenu menu = new JMenu("Instructor");
            addItem(menu, "Dashboard", "INSTRUCTOR_HOME"); addItem(menu, "My Sections", "MY_SECTIONS");
            menuBar.add(menu);
            cardLayout.show(mainCardPanel, "INSTRUCTOR_HOME");
        } else if (session.isAdmin()) {
            mainCardPanel.add(new AdminDashboardPanel(), "ADMIN_HOME");
            mainCardPanel.add(new ManageUsersPanel(adminService), "USERS");
            mainCardPanel.add(new ManageCoursesPanel(adminService), "COURSES");
            mainCardPanel.add(new SystemSettingsPanel(adminService, accessService, this::checkMaintenanceState), "SETTINGS");
            JMenu menu = new JMenu("Admin");
            addItem(menu, "Dashboard", "ADMIN_HOME"); menu.addSeparator();
            addItem(menu, "Manage Users", "USERS"); addItem(menu, "Manage Courses", "COURSES"); menu.addSeparator();
            addItem(menu, "System Settings", "SETTINGS");
            menuBar.add(menu);
            cardLayout.show(mainCardPanel, "ADMIN_HOME");
        }
    }
    private void addItem(JMenu m, String t, String c) { JMenuItem i = new JMenuItem(t); i.addActionListener(e -> cardLayout.show(mainCardPanel, c)); m.add(i); }
    private void checkMaintenanceState() { maintenanceBanner.setVisible(accessService.isMaintenanceModeOn()); }
}