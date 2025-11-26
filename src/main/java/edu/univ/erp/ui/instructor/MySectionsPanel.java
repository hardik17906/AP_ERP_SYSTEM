package edu.univ.erp.ui.instructor;

import edu.univ.erp.auth.UserSession;
import edu.univ.erp.domain.Section;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.service.SettingsService;
import edu.univ.erp.ui.common.UITheme; // Import Theme
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class MySectionsPanel extends JPanel {

    private static final Logger log = LoggerFactory.getLogger(MySectionsPanel.class);
    private final InstructorService instructorService;
    private final UserSession session;
    private final SettingsService settingsService;
    private final Consumer<Section> onOpenGradebook;

    private JTable sectionTable;
    private DefaultTableModel tableModel;

    public MySectionsPanel(InstructorService instructorService, UserSession session,
                           SettingsService settingsService, Consumer<Section> onOpenGradebook) {
        this.instructorService = instructorService;
        this.session = session;
        this.settingsService = settingsService;
        this.onOpenGradebook = onOpenGradebook;
        initComponents();
        loadSections();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("My Assigned Sections");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(UITheme.PRIMARY_COLOR); // Theme Title
        add(titleLabel, BorderLayout.NORTH);

        String[] columnNames = {"Section ID", "Course Code", "Time", "Room", "Capacity"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        sectionTable = new JTable(tableModel);
        sectionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Apply Theme Table
        UITheme.styleTable(sectionTable);

        add(new JScrollPane(sectionTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("Refresh");
        JButton openButton = new JButton("Open Gradebook");

        // Apply Theme Buttons
        UITheme.styleButton(refreshButton);
        UITheme.styleButton(openButton);

        bottomPanel.add(refreshButton);
        bottomPanel.add(openButton);
        add(bottomPanel, BorderLayout.SOUTH);

        openButton.addActionListener(e -> openSelectedGradebook());
        refreshButton.addActionListener(e -> loadSections());
    }

    private void openSelectedGradebook() {
        int selectedRow = sectionTable.getSelectedRow();
        if (selectedRow == -1) { JOptionPane.showMessageDialog(this, "Select a section."); return; }
        Section section = (Section) tableModel.getValueAt(selectedRow, 0);
        onOpenGradebook.accept(section);
    }

    private void loadSections() {
        tableModel.setRowCount(0);
        tableModel.addRow(new Object[]{null, "Loading...", "", "", ""});
        SwingWorker<List<Section>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Section> doInBackground() {
                String semester = settingsService.getSettingValue("current_semester");
                int year = Integer.parseInt(settingsService.getSettingValue("current_year"));
                return instructorService.getMySections(session.getUserId(), semester, year);
            }
            @Override
            protected void done() {
                tableModel.setRowCount(0);
                try {
                    List<Section> sections = get();
                    for (Section s : sections) {
                        tableModel.addRow(new Object[]{s, s.getCourseCode(), s.getDayTime(), s.getRoom(), s.getCapacity()});
                    }
                    sectionTable.getColumnModel().getColumn(0).setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
                        return new JLabel(String.valueOf(((Section) value).getSectionId()));
                    });
                } catch (Exception e) { tableModel.addRow(new Object[]{null, "Error", "", "", ""}); }
            }
        };
        worker.execute();
    }
}