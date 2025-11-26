package edu.univ.erp.ui.admin;

import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.UserAuth;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.ui.common.UITheme;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class ManageUsersPanel extends JPanel {

    private static final Logger log = LoggerFactory.getLogger(ManageUsersPanel.class);

    private static final String STUDENT_PANEL = "Student";
    private static final String INSTRUCTOR_PANEL = "Instructor";
    private static final String ADMIN_PANEL = "Admin";

    private final AdminService adminService;

    private JComboBox<String> userTypeComboBox;
    private JPanel cardsPanel;
    private CardLayout cardLayout;
    private JLabel feedbackLabel;

    // Input Fields
    private JTextField studentUsernameField, rollNumberField, programField;
    private JPasswordField studentPasswordField;
    private JSpinner yearSpinner;
    private JTextField instructorUsernameField, departmentField, titleField;
    private JPasswordField instructorPasswordField;
    private JTextField adminUsernameField;
    private JPasswordField adminPasswordField;

    // List Components
    private JTable userListTable;
    private DefaultTableModel userListModel;

    public ManageUsersPanel(AdminService adminService) {
        this.adminService = adminService;
        initComponents();
        loadUserList(STUDENT_PANEL);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // 1. Top Half: Creation Form
        JPanel creationPanel = createCreationPanel();

        // 2. Bottom Half: User List
        JPanel listPanel = createListPanel();

        // 3. Split Pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, creationPanel, listPanel);
        splitPane.setDividerLocation(350);
        splitPane.setResizeWeight(0.0);
        splitPane.setBorder(BorderFactory.createEmptyBorder());

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createCreationPanel() {
        JPanel topPanel = new JPanel(new MigLayout("insets 20, fillx", "[grow]", "[][][grow][]"));

        JLabel titleLabel = new JLabel("Manage Users");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(UITheme.PRIMARY_COLOR);
        topPanel.add(titleLabel, "center, wrap, gapbottom 10");

        // Role Selector
        JPanel selectorPanel = new JPanel(new MigLayout("fillx", "[right]15[left, grow]"));
        selectorPanel.add(new JLabel("Select Role to Manage:"));
        userTypeComboBox = new JComboBox<>(new String[]{STUDENT_PANEL, INSTRUCTOR_PANEL, ADMIN_PANEL});
        selectorPanel.add(userTypeComboBox, "growx");
        topPanel.add(selectorPanel, "wrap, growx");

        // Card Layout for Forms
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.add(createStudentPanel(), STUDENT_PANEL);
        cardsPanel.add(createInstructorPanel(), INSTRUCTOR_PANEL);
        cardsPanel.add(createAdminPanel(), ADMIN_PANEL);
        topPanel.add(cardsPanel, "wrap, growx");

        // Create Button
        JButton createUserButton = new JButton("Create New User");
        UITheme.styleButton(createUserButton); // Apply Theme

        feedbackLabel = new JLabel(" ");
        topPanel.add(createUserButton, "split 2, growx");
        topPanel.add(feedbackLabel, "growx");

        userTypeComboBox.addActionListener(e -> {
            String selected = (String) userTypeComboBox.getSelectedItem();
            cardLayout.show(cardsPanel, selected);
            loadUserList(selected);
        });

        createUserButton.addActionListener(e -> onCreateUser());

        return topPanel;
    }

    private JPanel createListPanel() {
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                "Registered Users List"
        ));

        JButton refreshButton = new JButton("Refresh List");
        UITheme.styleButton(refreshButton); // Apply Theme

        refreshButton.addActionListener(e -> loadUserList((String) userTypeComboBox.getSelectedItem()));
        JPanel header = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        header.add(refreshButton);
        listPanel.add(header, BorderLayout.NORTH);

        userListModel = new DefaultTableModel();
        userListTable = new JTable(userListModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        UITheme.styleTable(userListTable); // Apply Theme

        JScrollPane scrollPane = new JScrollPane(userListTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        listPanel.add(scrollPane, BorderLayout.CENTER);
        return listPanel;
    }

    private JPanel createStudentPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 2, fillx", "[right]15[left, grow]"));
        panel.add(new JLabel("Username:"));
        studentUsernameField = new JTextField();
        panel.add(studentUsernameField, "growx");
        panel.add(new JLabel("Password:"));
        studentPasswordField = new JPasswordField();
        panel.add(studentPasswordField, "growx");
        panel.add(new JLabel("Roll Number:"));
        rollNumberField = new JTextField();
        panel.add(rollNumberField, "growx");
        panel.add(new JLabel("Program:"));
        programField = new JTextField();
        panel.add(programField, "growx");
        panel.add(new JLabel("Year:"));
        yearSpinner = new JSpinner(new SpinnerNumberModel(2025, 2010, 2050, 1));
        panel.add(yearSpinner);
        return panel;
    }

    private JPanel createInstructorPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 2, fillx", "[right]15[left, grow]"));
        panel.add(new JLabel("Username:"));
        instructorUsernameField = new JTextField();
        panel.add(instructorUsernameField, "growx");
        panel.add(new JLabel("Password:"));
        instructorPasswordField = new JPasswordField();
        panel.add(instructorPasswordField, "growx");
        panel.add(new JLabel("Department:"));
        departmentField = new JTextField();
        panel.add(departmentField, "growx");
        panel.add(new JLabel("Title:"));
        titleField = new JTextField();
        panel.add(titleField, "growx");
        return panel;
    }

    private JPanel createAdminPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 2, fillx", "[right]15[left, grow]"));
        panel.add(new JLabel("Username:"));
        adminUsernameField = new JTextField();
        panel.add(adminUsernameField, "growx");
        panel.add(new JLabel("Password:"));
        adminPasswordField = new JPasswordField();
        panel.add(adminPasswordField, "growx");
        return panel;
    }

    private void loadUserList(String role) {
        userListModel.setRowCount(0);
        userListModel.setColumnCount(0);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            List<Student> students;
            List<Instructor> instructors;
            List<UserAuth> admins;
            Map<Integer, String> usernames;

            @Override
            protected Void doInBackground() {
                try {
                    usernames = adminService.getAllUsernames();

                    if (STUDENT_PANEL.equals(role)) {
                        students = adminService.getAllStudents();
                    } else if (INSTRUCTOR_PANEL.equals(role)) {
                        instructors = adminService.getAllInstructors();
                    } else {
                        admins = adminService.getAdmins();
                    }
                } catch (Exception e) {
                    log.error("Error fetching data in background", e);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    if (STUDENT_PANEL.equals(role)) {
                        userListModel.setColumnIdentifiers(new String[]{"Roll No", "Username", "Program", "Year"});
                        if (students != null) {
                            for (Student s : students) {
                                userListModel.addRow(new Object[]{
                                        s.getRollNumber(),
                                        usernames.getOrDefault(s.getUserId(), "Unknown"),
                                        s.getProgram(),
                                        s.getYear()
                                });
                            }
                        }
                    } else if (INSTRUCTOR_PANEL.equals(role)) {
                        userListModel.setColumnIdentifiers(new String[]{"ID", "Username", "Title", "Department"});
                        if (instructors != null) {
                            for (Instructor i : instructors) {
                                String formattedId = String.format("FAC-%03d", i.getUserId());
                                userListModel.addRow(new Object[]{
                                        formattedId,
                                        i.getUsernameForDisplay(),
                                        i.getTitle(),
                                        i.getDepartment()
                                });
                            }
                        }
                    } else {
                        userListModel.setColumnIdentifiers(new String[]{"Username", "Role", "Status"});
                        if (admins != null) {
                            for (UserAuth a : admins) {
                                userListModel.addRow(new Object[]{
                                        a.getUsername(),
                                        a.getRole(),
                                        a.getStatus()
                                });
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("Error updating UI table", e);
                }
            }
        };
        worker.execute();
    }

    private void onCreateUser() {
        String userType = (String) userTypeComboBox.getSelectedItem();
        String username;
        String password;

        if (STUDENT_PANEL.equals(userType)) {
            username = studentUsernameField.getText();
            password = new String(studentPasswordField.getPassword());
        } else if (INSTRUCTOR_PANEL.equals(userType)) {
            username = instructorUsernameField.getText();
            password = new String(instructorPasswordField.getPassword());
        } else {
            username = adminUsernameField.getText();
            password = new String(adminPasswordField.getPassword());
        }

        if (username.isBlank() || password.isBlank()) {
            setFeedback(Color.RED, "Username and Password cannot be empty.");
            return;
        }

        setFeedback(Color.BLUE, "Creating user...");

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                if (STUDENT_PANEL.equals(userType)) {
                    Student student = new Student();
                    student.setRollNumber(rollNumberField.getText());
                    student.setProgram(programField.getText());
                    student.setYear((Integer) yearSpinner.getValue());
                    return adminService.createStudent(username, password, student);
                } else if (INSTRUCTOR_PANEL.equals(userType)) {
                    Instructor instructor = new Instructor();
                    instructor.setDepartment(departmentField.getText());
                    instructor.setTitle(titleField.getText());
                    return adminService.createInstructor(username, password, instructor);
                } else {
                    return adminService.createAdmin(username, password);
                }
            }

            @Override
            protected void done() {
                try {
                    String result = get();
                    if (result.startsWith("Success")) {
                        setFeedback(new Color(0, 128, 0), result);
                        clearForm();
                        loadUserList(userType); // Refresh list automatically
                    } else {
                        setFeedback(Color.RED, result);
                    }
                } catch (Exception ex) {
                    log.error("Error creating user", ex);
                    setFeedback(Color.RED, "Error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void setFeedback(Color color, String message) {
        feedbackLabel.setForeground(color);
        feedbackLabel.setText(message);
    }

    private void clearForm() {
        studentUsernameField.setText("");
        studentPasswordField.setText("");
        rollNumberField.setText("");
        programField.setText("");
        instructorUsernameField.setText("");
        instructorPasswordField.setText("");
        departmentField.setText("");
        titleField.setText("");
        adminUsernameField.setText("");
        adminPasswordField.setText("");
    }
}