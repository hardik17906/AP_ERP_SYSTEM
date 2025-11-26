package edu.univ.erp.ui.student;

import edu.univ.erp.auth.UserSession;
import edu.univ.erp.domain.Grade;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.service.dto.RegistrationDetailDTO;
import edu.univ.erp.ui.common.UITheme; // Import Theme
import edu.univ.erp.ui.util.TranscriptCsvExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyGradesPanel extends JPanel {

    private static final Logger log = LoggerFactory.getLogger(MyGradesPanel.class);
    private final UserSession session;
    private final StudentService studentService;

    private JTable enrollmentsTable;
    private DefaultTableModel enrollmentsModel;
    private JTable gradesTable;
    private DefaultTableModel gradesModel;
    private Map<RegistrationDetailDTO, List<Grade>> gradesMap;

    public MyGradesPanel(UserSession session, StudentService studentService) {
        this.session = session;
        this.studentService = studentService;
        initComponents();
        loadGrades();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top Table
        String[] enrCols = {"DTO", "Course", "Title", "Term", "Final Grade"};
        enrollmentsModel = new DefaultTableModel(enrCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        enrollmentsTable = new JTable(enrollmentsModel);
        enrollmentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        enrollmentsTable.getColumnModel().getColumn(0).setMinWidth(0);
        enrollmentsTable.getColumnModel().getColumn(0).setMaxWidth(0);

        // Theme Table 1
        UITheme.styleTable(enrollmentsTable);

        JScrollPane enrScrollPane = new JScrollPane(enrollmentsTable);
        enrScrollPane.setBorder(BorderFactory.createTitledBorder("My Courses"));

        // Bottom Table
        String[] grCols = {"Component", "Score"};
        gradesModel = new DefaultTableModel(grCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        gradesTable = new JTable(gradesModel);

        // Theme Table 2
        UITheme.styleTable(gradesTable);

        JScrollPane grScrollPane = new JScrollPane(gradesTable);
        grScrollPane.setBorder(BorderFactory.createTitledBorder("Grade Details"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, enrScrollPane, grScrollPane);
        splitPane.setDividerLocation(250);
        add(splitPane, BorderLayout.CENTER);

        // Bottom Buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("Refresh");
        JButton exportButton = new JButton("Download Transcript (CSV)");

        // Theme Buttons
        UITheme.styleButton(refreshButton);
        UITheme.styleButton(exportButton);

        bottomPanel.add(refreshButton);
        bottomPanel.add(exportButton);
        add(bottomPanel, BorderLayout.SOUTH);

        enrollmentsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onEnrollmentSelected();
        });
        refreshButton.addActionListener(e -> loadGrades());
        exportButton.addActionListener(e -> onExport());
    }

    public void loadGrades() {
        enrollmentsModel.setRowCount(0);
        gradesModel.setRowCount(0);
        SwingWorker<Map<RegistrationDetailDTO, List<Grade>>, Void> worker = new SwingWorker<>() {
            @Override
            protected Map<RegistrationDetailDTO, List<Grade>> doInBackground() { return studentService.getGrades(session.getUserId()); }
            @Override
            protected void done() {
                try {
                    gradesMap = get();
                    for (RegistrationDetailDTO reg : gradesMap.keySet()) {
                        enrollmentsModel.addRow(new Object[]{
                                reg, reg.getCourseCode(), reg.getCourseTitle(),
                                reg.getSemester() + " " + reg.getYear(),
                                reg.getFinalGrade() != null ? reg.getFinalGrade() : "In Progress"
                        });
                    }
                } catch (Exception e) {}
            }
        };
        worker.execute();
    }

    private void onEnrollmentSelected() {
        gradesModel.setRowCount(0);
        int selectedRow = enrollmentsTable.getSelectedRow();
        if (selectedRow == -1 || gradesMap == null) return;
        RegistrationDetailDTO selectedReg = (RegistrationDetailDTO) enrollmentsModel.getValueAt(selectedRow, 0);
        if (selectedReg != null) {
            List<Grade> grades = gradesMap.get(selectedReg);
            for (Grade g : grades) gradesModel.addRow(new Object[]{g.getComponentName(), g.getScore()});
        }
    }

    private void onExport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("transcript.csv"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getAbsolutePath().toLowerCase().endsWith(".csv")) file = new File(file.getAbsolutePath() + ".csv");
            File f = file;
            SwingWorker<String, Void> w = new SwingWorker<>() {
                @Override
                protected String doInBackground() {
                    var s = studentService.getStudentDetails(session.getUserId());
                    if(s.isEmpty()) return "Error";
                    s.get().setName(session.getUsername());
                    return TranscriptCsvExporter.exportTranscript(f, s.get(), new ArrayList<>(studentService.getGrades(session.getUserId()).keySet()));
                }
                @Override
                protected void done() { try { JOptionPane.showMessageDialog(MyGradesPanel.this, get()); } catch(Exception e){} }
            };
            w.execute();
        }
    }
}