package edu.univ.erp.ui.student;

import edu.univ.erp.auth.UserSession;
import edu.univ.erp.service.SettingsService;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.service.dto.RegistrationDetailDTO;
import edu.univ.erp.ui.common.UITheme; // Import Theme
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MyRegistrationsPanel extends JPanel {

    private static final Logger log = LoggerFactory.getLogger(MyRegistrationsPanel.class);
    private final UserSession session;
    private final StudentService studentService;
    private final SettingsService settingsService;
    private final Runnable onDropSuccess;

    private JTable regTable;
    private DefaultTableModel tableModel;
    private JButton dropButton;
    private JButton refreshButton;

    public MyRegistrationsPanel(UserSession session, StudentService studentService,
                                SettingsService settingsService, Runnable onDropSuccess) {
        this.session = session;
        this.studentService = studentService;
        this.settingsService = settingsService;
        this.onDropSuccess = onDropSuccess;
        initComponents();
        loadRegistrations();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("My Current Registrations");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(UITheme.PRIMARY_COLOR); // Theme Title
        add(titleLabel, BorderLayout.NORTH);

        String[] columnNames = {"Enrollment ID", "Code", "Title", "Time", "Room", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        regTable = new JTable(tableModel);
        regTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        regTable.getColumnModel().getColumn(0).setMinWidth(0);
        regTable.getColumnModel().getColumn(0).setMaxWidth(0);

        // Apply Theme
        UITheme.styleTable(regTable);

        add(new JScrollPane(regTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        refreshButton = new JButton("Refresh");
        dropButton = new JButton("Drop Selected Section");

        // Apply Theme
        UITheme.styleButton(refreshButton);
        UITheme.styleButton(dropButton);

        buttonPanel.add(refreshButton);
        buttonPanel.add(dropButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        dropButton.addActionListener(e -> onDrop());
        refreshButton.addActionListener(e -> loadRegistrations());
    }

    public void loadRegistrations() {
        tableModel.setRowCount(0);
        tableModel.addRow(new Object[]{0, "Loading...", "", "", "", ""});
        dropButton.setEnabled(false);

        SwingWorker<List<RegistrationDetailDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<RegistrationDetailDTO> doInBackground() {
                String semester = settingsService.getSettingValue("current_semester");
                int year = Integer.parseInt(settingsService.getSettingValue("current_year"));
                return studentService.getMyCurrentRegistrations(session.getUserId(), semester, year);
            }
            @Override
            protected void done() {
                tableModel.setRowCount(0);
                try {
                    List<RegistrationDetailDTO> registrations = get();
                    for (RegistrationDetailDTO reg : registrations) {
                        tableModel.addRow(new Object[]{
                                reg.getEnrollmentId(), reg.getCourseCode(), reg.getCourseTitle(),
                                reg.getDayTime(), reg.getRoom(), reg.getStatus()
                        });
                    }
                } catch (Exception e) {}
                dropButton.setEnabled(true);
            }
        };
        worker.execute();
    }

    private void onDrop() {
        int selectedRow = regTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a section to drop.");
            return;
        }
        int enrollmentId = (Integer) tableModel.getValueAt(selectedRow, 0);
        if(JOptionPane.showConfirmDialog(this, "Confirm drop?", "Drop", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        dropButton.setEnabled(false);
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() { return studentService.dropSection(session.getUserId(), enrollmentId); }
            @Override
            protected void done() {
                try {
                    String result = get();
                    JOptionPane.showMessageDialog(MyRegistrationsPanel.this, result);
                    if(result.startsWith("Success")) onDropSuccess.run();
                } catch (Exception e) {}
                dropButton.setEnabled(true);
                loadRegistrations();
            }
        };
        worker.execute();
    }
}