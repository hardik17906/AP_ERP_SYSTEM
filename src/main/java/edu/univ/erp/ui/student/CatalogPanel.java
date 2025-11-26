package edu.univ.erp.ui.student;

import edu.univ.erp.auth.UserSession;
import edu.univ.erp.service.CatalogService;
import edu.univ.erp.service.SettingsService;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.service.dto.CatalogEntry;
import edu.univ.erp.ui.common.UITheme; // Import Theme
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CatalogPanel extends JPanel {

    private static final Logger log = LoggerFactory.getLogger(CatalogPanel.class);

    private final UserSession session;
    private final CatalogService catalogService;
    private final StudentService studentService;
    private final SettingsService settingsService;
    private final Runnable onRegisterSuccess;

    private JTable catalogTable;
    private DefaultTableModel tableModel;
    private JButton registerButton;
    private JButton refreshButton;

    public CatalogPanel(UserSession session, CatalogService catalogService,
                        StudentService studentService, SettingsService settingsService,
                        Runnable onRegisterSuccess) {
        this.session = session;
        this.catalogService = catalogService;
        this.studentService = studentService;
        this.settingsService = settingsService;
        this.onRegisterSuccess = onRegisterSuccess;

        initComponents();
        loadCatalog();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Course Catalog");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(UITheme.PRIMARY_COLOR); // Theme Title
        add(titleLabel, BorderLayout.NORTH);

        //  Table 
        String[] columnNames = {"ID", "Code", "Title", "Credits", "Instructor", "Time", "Room", "Seats"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        catalogTable = new JTable(tableModel);
        catalogTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        catalogTable.getColumnModel().getColumn(0).setMinWidth(0);
        catalogTable.getColumnModel().getColumn(0).setMaxWidth(0);

        // Apply Theme
        UITheme.styleTable(catalogTable);

        add(new JScrollPane(catalogTable), BorderLayout.CENTER);

        //  Bottom Panel 
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        refreshButton = new JButton("Refresh");
        registerButton = new JButton("Register for Selected Section");

        // Apply Theme to Buttons
        UITheme.styleButton(refreshButton);
        UITheme.styleButton(registerButton);

        buttonPanel.add(refreshButton);
        buttonPanel.add(registerButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        registerButton.addActionListener(e -> onRegister());
        refreshButton.addActionListener(e -> loadCatalog());
    }

    public void loadCatalog() {
        tableModel.setRowCount(0);
        tableModel.addRow(new Object[]{0, "Loading...", "", 0, "", "", "", ""});
        registerButton.setEnabled(false);

        SwingWorker<List<CatalogEntry>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<CatalogEntry> doInBackground() throws Exception {
                String semester = settingsService.getSettingValue("current_semester");
                int year = Integer.parseInt(settingsService.getSettingValue("current_year"));
                return catalogService.getCourseCatalog(semester, year);
            }
            @Override
            protected void done() {
                tableModel.setRowCount(0);
                try {
                    List<CatalogEntry> catalog = get();
                    for (CatalogEntry entry : catalog) {
                        tableModel.addRow(new Object[]{
                                entry.getSectionId(), entry.getCourseCode(), entry.getCourseTitle(),
                                entry.getCredits(), entry.getInstructorName(), entry.getDayTime(),
                                entry.getRoom(), entry.getCurrentEnrollment() + " / " + entry.getCapacity()
                        });
                    }
                } catch (Exception e) {
                    tableModel.addRow(new Object[]{0, "Error", "", 0, "", "", "", ""});
                }
                registerButton.setEnabled(true);
            }
        };
        worker.execute();
    }

    private void onRegister() {
        int selectedRow = catalogTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a section.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int sectionId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String courseCode = (String) tableModel.getValueAt(selectedRow, 1);

        if(JOptionPane.showConfirmDialog(this, "Register for " + courseCode + "?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        registerButton.setEnabled(false);
        registerButton.setText("Registering...");

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() { return studentService.registerForSection(session.getUserId(), sectionId); }
            @Override
            protected void done() {
                try {
                    String res = get();
                    JOptionPane.showMessageDialog(CatalogPanel.this, res);
                    if (res.startsWith("Success")) onRegisterSuccess.run();
                } catch (Exception e) { JOptionPane.showMessageDialog(CatalogPanel.this, "Error: " + e.getMessage()); }
                registerButton.setEnabled(true);
                registerButton.setText("Register for Selected Section");
                loadCatalog();
            }
        };
        worker.execute();
    }
}