package edu.univ.erp.ui.admin;

import com.formdev.flatlaf.FlatClientProperties;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Section;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.ui.common.UITheme;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ManageCoursesPanel extends JPanel {

    private static final Logger log = LoggerFactory.getLogger(ManageCoursesPanel.class);
    private final AdminService adminService;

    //  Data Cache 
    private List<Course> allCourses = new ArrayList<>();
    private List<Instructor> allInstructors = new ArrayList<>();
    private List<Section> allSections = new ArrayList<>();

    //  Components 
    private JTextField courseCodeField, courseTitleField;
    private JSpinner creditsSpinner;
    private JLabel courseFeedbackLabel;
    private JTable courseTable;
    private DefaultTableModel courseModel;

    private JComboBox<Course> courseComboBox;
    private JComboBox<Instructor> instructorComboBox;
    private JTextField dayTimeField, roomField, searchField;
    private JSpinner capacitySpinner, yearSpinner;
    private JComboBox<String> semesterComboBox;
    private JTable sectionTable;
    private DefaultTableModel sectionModel;
    private JLabel sectionFeedbackLabel;

    public ManageCoursesPanel(AdminService adminService) {
        this.adminService = adminService;
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("1. Master Course Catalog", createCatalogPanel());
        tabbedPane.addTab("2. Class Schedule (Sections)", createSchedulePanel());

        add(tabbedPane, BorderLayout.CENTER);
        loadData();
    }

    private JPanel createCatalogPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel(new MigLayout("wrap 4, fillx, insets 10", "[right]10[fill, grow][right]10[fill, grow]"));
        formPanel.setBorder(BorderFactory.createTitledBorder("Add New Course"));

        formPanel.add(new JLabel("Course Code:"));
        courseCodeField = new JTextField();
        formPanel.add(courseCodeField);

        formPanel.add(new JLabel("Course Title:"));
        courseTitleField = new JTextField();
        formPanel.add(courseTitleField);

        formPanel.add(new JLabel("Credits:"));
        creditsSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 10, 1));
        formPanel.add(creditsSpinner, "wrap");

        JButton addCourseBtn = new JButton("Add to Catalog");
        UITheme.styleButton(addCourseBtn); // Theme
        addCourseBtn.addActionListener(e -> onCreateCourse());
        formPanel.add(addCourseBtn, "skip 1, span 2, growx");

        courseFeedbackLabel = new JLabel(" ");
        formPanel.add(courseFeedbackLabel, "growx");

        panel.add(formPanel, BorderLayout.NORTH);

        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder("Existing Courses (Double-click Title/Credits to Edit)"));

        courseModel = new DefaultTableModel(new String[]{"Code", "Title", "Credits"}, 0) {
            public boolean isCellEditable(int row, int col) { return col > 0; }
        };
        courseTable = new JTable(courseModel);
        UITheme.styleTable(courseTable); // Theme

        courseModel.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                if (row >= 0) saveCourseChange(row);
            }
        });

        listPanel.add(new JScrollPane(courseTable), BorderLayout.CENTER);
        panel.add(listPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSchedulePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel(new MigLayout("wrap 4, fillx, insets 10", "[right]10[fill, 100:150, grow][right]10[fill, 100:150, grow]"));
        formPanel.setBorder(BorderFactory.createTitledBorder("Schedule New Section"));

        courseComboBox = new JComboBox<>();
        formPanel.add(new JLabel("Course:")); formPanel.add(courseComboBox);

        instructorComboBox = new JComboBox<>();
        formPanel.add(new JLabel("Instructor:")); formPanel.add(instructorComboBox);

        dayTimeField = new JTextField();
        formPanel.add(new JLabel("Time:")); formPanel.add(dayTimeField);

        roomField = new JTextField();
        formPanel.add(new JLabel("Room:")); formPanel.add(roomField);

        semesterComboBox = new JComboBox<>(new String[]{"FALL", "WINTER", "SPRING", "SUMMER"});
        formPanel.add(new JLabel("Sem:")); formPanel.add(semesterComboBox);

        yearSpinner = new JSpinner(new SpinnerNumberModel(2025, 2020, 2030, 1));
        formPanel.add(new JLabel("Year:")); formPanel.add(yearSpinner);

        capacitySpinner = new JSpinner(new SpinnerNumberModel(50, 1, 300, 1));
        formPanel.add(new JLabel("Cap:")); formPanel.add(capacitySpinner);

        JButton createBtn = new JButton("Add Section");
        UITheme.styleButton(createBtn); // Theme
        createBtn.addActionListener(e -> onCreateSection());

        formPanel.add(createBtn, "skip 1, span 2, growx");

        panel.add(formPanel, BorderLayout.NORTH);

        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder("Scheduled Sections (Double-click cells to Edit)"));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(new JLabel("Search:"));
        searchField = new JTextField(15);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });
        toolbar.add(searchField);

        JButton refreshBtn = new JButton("Refresh");
        UITheme.styleButton(refreshBtn); // Theme
        refreshBtn.addActionListener(e -> loadData());
        toolbar.add(refreshBtn);

        sectionFeedbackLabel = new JLabel(" ");
        sectionFeedbackLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(sectionFeedbackLabel);

        listPanel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"ID", "Code", "Title", "Instructor", "Time", "Room", "Sem", "Year", "Cap"};
        sectionModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return col > 2; }
        };
        sectionTable = new JTable(sectionModel);
        UITheme.styleTable(sectionTable); // Theme

        sectionTable.getColumnModel().getColumn(0).setMinWidth(0);
        sectionTable.getColumnModel().getColumn(0).setMaxWidth(0);

        JComboBox<Instructor> editorCombo = new JComboBox<>();
        sectionTable.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(editorCombo));

        sectionModel.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                if (row >= 0 && e.getColumn() >= 0) saveSectionChange(row);
            }
        });

        listPanel.add(new JScrollPane(sectionTable), BorderLayout.CENTER);
        panel.add(listPanel, BorderLayout.CENTER);

        return panel;
    }

    private void loadData() {
        sectionFeedbackLabel.setText("Loading...");
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                allCourses = adminService.getAllCourses();
                allInstructors = adminService.getAllInstructors();
                allSections = adminService.getAllSections();
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    courseModel.setRowCount(0);
                    courseComboBox.removeAllItems();
                    for(Course c : allCourses) {
                        courseModel.addRow(new Object[]{c.getCourseCode(), c.getTitle(), c.getCredits()});
                        courseComboBox.addItem(c);
                    }
                    instructorComboBox.removeAllItems();
                    JComboBox<Instructor> editor = (JComboBox<Instructor>) ((DefaultCellEditor) sectionTable.getColumnModel().getColumn(3).getCellEditor()).getComponent();
                    editor.removeAllItems();
                    for(Instructor i : allInstructors) {
                        instructorComboBox.addItem(i);
                        editor.addItem(i);
                    }
                    updateSectionTable(allSections);
                    sectionFeedbackLabel.setText("Ready.");
                    sectionFeedbackLabel.setForeground(Color.BLACK);
                } catch (Exception e) {
                    log.error("Error loading data", e);
                    sectionFeedbackLabel.setText("Error loading data.");
                }
            }
        };
        worker.execute();
    }

    private void updateSectionTable(List<Section> sections) {
        sectionModel.setRowCount(0);
        for(Section s : sections) {
            String title = allCourses.stream()
                    .filter(c -> c.getCourseCode().equals(s.getCourseCode()))
                    .map(Course::getTitle).findFirst().orElse("Unknown");
            Instructor inst = allInstructors.stream()
                    .filter(i -> i.getUserId() == s.getInstructorId())
                    .findFirst().orElse(null);
            sectionModel.addRow(new Object[]{
                    s, s.getCourseCode(), title, inst, s.getDayTime(),
                    s.getRoom(), s.getSemester(), s.getYear(), s.getCapacity()
            });
        }
    }

    private void filterTable() {
        String query = searchField.getText().toLowerCase();
        if (query.isBlank()) { updateSectionTable(allSections); return; }
        List<Section> filtered = allSections.stream().filter(s -> {
            String title = allCourses.stream()
                    .filter(c -> c.getCourseCode().equals(s.getCourseCode()))
                    .map(Course::getTitle).findFirst().orElse("").toLowerCase();
            return s.getCourseCode().toLowerCase().contains(query) || title.contains(query);
        }).collect(Collectors.toList());
        updateSectionTable(filtered);
    }

    private void onCreateCourse() {
        String code = courseCodeField.getText().trim().toUpperCase();
        String title = courseTitleField.getText().trim();
        int credits = (Integer) creditsSpinner.getValue();
        if(code.isBlank() || title.isBlank()) {
            courseFeedbackLabel.setText("Code and Title required.");
            return;
        }
        Course c = new Course(code, title, credits);
        courseFeedbackLabel.setText("Saving...");
        SwingWorker<String, Void> w = new SwingWorker<>() {
            @Override protected String doInBackground() { return adminService.createCourse(c); }
            @Override protected void done() {
                try {
                    String res = get();
                    courseFeedbackLabel.setText(res);
                    if(res.startsWith("Success")) { loadData(); courseCodeField.setText(""); courseTitleField.setText(""); }
                } catch(Exception e) {}
            }
        };
        w.execute();
    }

    private void saveCourseChange(int row) {
        String code = (String) courseModel.getValueAt(row, 0);
        String title = (String) courseModel.getValueAt(row, 1);
        int credits = Integer.parseInt(courseModel.getValueAt(row, 2).toString());
        Course c = new Course(code, title, credits);
        SwingWorker<String, Void> w = new SwingWorker<>() {
            @Override protected String doInBackground() { return adminService.updateCourse(c); }
            @Override protected void done() {
                try { String res = get(); if(!res.startsWith("Success")) JOptionPane.showMessageDialog(ManageCoursesPanel.this, res); else loadData(); } catch(Exception e) {}
            }
        };
        w.execute();
    }

    private void onCreateSection() {
        Course c = (Course) courseComboBox.getSelectedItem();
        Instructor i = (Instructor) instructorComboBox.getSelectedItem();
        if(c == null) return;
        Section s = new Section();
        s.setCourseCode(c.getCourseCode());
        if(i != null) s.setInstructorId(i.getUserId());
        s.setDayTime(dayTimeField.getText());
        s.setRoom(roomField.getText());
        s.setSemester((String) semesterComboBox.getSelectedItem());
        s.setYear((Integer) yearSpinner.getValue());
        s.setCapacity((Integer) capacitySpinner.getValue());

        sectionFeedbackLabel.setText("Creating...");
        SwingWorker<String, Void> w = new SwingWorker<>() {
            @Override protected String doInBackground() { return adminService.createSection(s); }
            @Override protected void done() {
                try {
                    String res = get();
                    sectionFeedbackLabel.setText(res);
                    if(res.startsWith("Success")) loadData();
                    else sectionFeedbackLabel.setForeground(Color.RED);
                } catch(Exception e) {}
            }
        };
        w.execute();
    }

    private void saveSectionChange(int row) {
        try {
            Section s = (Section) sectionModel.getValueAt(row, 0);
            Instructor inst = (Instructor) sectionModel.getValueAt(row, 3);
            if(inst != null) s.setInstructorId(inst.getUserId());
            s.setDayTime((String) sectionModel.getValueAt(row, 4));
            s.setRoom((String) sectionModel.getValueAt(row, 5));
            s.setSemester((String) sectionModel.getValueAt(row, 6));
            s.setYear(Integer.parseInt(sectionModel.getValueAt(row, 7).toString()));
            s.setCapacity(Integer.parseInt(sectionModel.getValueAt(row, 8).toString()));

            sectionFeedbackLabel.setText("Saving...");
            SwingWorker<String, Void> w = new SwingWorker<>() {
                @Override protected String doInBackground() { return adminService.updateSection(s); }
                @Override protected void done() {
                    try {
                        String res = get();
                        sectionFeedbackLabel.setText(res);
                        if(res.startsWith("Success")) sectionFeedbackLabel.setForeground(new Color(0, 128, 0));
                        else sectionFeedbackLabel.setForeground(Color.RED);
                    } catch(Exception e) {}
                }
            };
            w.execute();
        } catch(Exception e) {
            sectionFeedbackLabel.setText("Invalid Input.");
        }
    }
}