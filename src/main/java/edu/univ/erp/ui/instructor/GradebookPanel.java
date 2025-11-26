package edu.univ.erp.ui.instructor;

import edu.univ.erp.auth.UserSession;
import edu.univ.erp.domain.*;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.service.dto.EnrollmentDetailDTO;
import edu.univ.erp.ui.common.UITheme; // Import Theme
import edu.univ.erp.ui.util.CsvExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class GradebookPanel extends JPanel {

    private static final Logger log = LoggerFactory.getLogger(GradebookPanel.class);
    private final InstructorService instructorService;
    private final UserSession session;
    private Section currentSection;

    private JTable studentTable;
    private DefaultTableModel studentModel;
    private JButton gradeStudentButton;
    private JTextArea statsArea;

    public GradebookPanel(InstructorService instructorService, UserSession session) {
        this.instructorService = instructorService;
        this.session = session;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Gradebook");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(UITheme.PRIMARY_COLOR); // Theme Title
        header.add(title, BorderLayout.WEST);

        JPanel topButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton configureSchemaButton = new JButton("1. Configure Assessments");
        JButton computeAllButton = new JButton("3. Compute All Final Grades");
        JButton exportCsvButton = new JButton("Export CSV");

        // Theme Buttons
        UITheme.styleButton(configureSchemaButton);
        UITheme.styleButton(computeAllButton);
        UITheme.styleButton(exportCsvButton);

        configureSchemaButton.addActionListener(e -> openSchemaDialog());
        computeAllButton.addActionListener(e -> onComputeAll());
        exportCsvButton.addActionListener(e -> onExportCsv());

        topButtons.add(configureSchemaButton);
        topButtons.add(computeAllButton);
        topButtons.add(exportCsvButton);
        header.add(topButtons, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Main Split View
        JPanel listPanel = createStudentListPanel();
        JPanel statsPanel = createStatsPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listPanel, statsPanel);
        splitPane.setDividerLocation(600);
        splitPane.setResizeWeight(0.7);
        add(splitPane, BorderLayout.CENTER);

        // Bottom Panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        gradeStudentButton = new JButton("2. Grade Selected Student");

        UITheme.styleButton(gradeStudentButton); // Theme Button

        gradeStudentButton.addActionListener(e -> onGradeStudent());
        bottomPanel.add(gradeStudentButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createStudentListPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Student List"));
        studentModel = new DefaultTableModel(new String[]{"Roll No", "Name", "Final Grade", "EnrollmentObj"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        studentTable = new JTable(studentModel);
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentTable.removeColumn(studentTable.getColumnModel().getColumn(3));

        UITheme.styleTable(studentTable); // Theme Table

        studentTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) { if (evt.getClickCount() == 2) onGradeStudent(); }
        });
        p.add(new JScrollPane(studentTable), BorderLayout.CENTER);
        return p;
    }

    private JPanel createStatsPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Live Class Statistics"));
        statsArea = new JTextArea("Loading...");
        statsArea.setEditable(false);
        statsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        statsArea.setMargin(new Insets(10, 10, 10, 10));
        p.add(new JScrollPane(statsArea), BorderLayout.CENTER);
        return p;
    }

public void loadSection(Section section) {
        this.currentSection = section;
        studentModel.setRowCount(0);
        
        // 1. Explicitly define the worker with types
        SwingWorker<List<EnrollmentDetailDTO>, Void> worker = new SwingWorker<List<EnrollmentDetailDTO>, Void>() {
            @Override
            protected List<EnrollmentDetailDTO> doInBackground() throws Exception {
                // Guard against nulls to prevent crashes
                if (section == null || session == null) {
                    return new ArrayList<>();
                }
                return instructorService.getEnrollmentsForSection(session.getUserId(), section.getSectionId());
            }

            @Override
            protected void done() {
                try {
                    // 2. Call get() and store in a typed variable first
                    List<EnrollmentDetailDTO> results = get();
                    
                    if (results != null) {
                        for (EnrollmentDetailDTO dto : results) {
                            Vector<Object> row = new Vector<>();
                            row.add(dto.getStudentRollNumber());
                            row.add(dto.getStudentName());
                            row.add((dto.getFinalGrade() == null) ? "-" : dto.getFinalGrade());
                            row.add(dto);
                            studentModel.addRow(row);
                        }
                    }
                    refreshStats();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupted state
                    log.error("Gradebook loading interrupted", e);
                } catch (Exception e) {
                    log.error("Error loading gradebook data", e);
                    JOptionPane.showMessageDialog(GradebookPanel.this, "Error loading data: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void refreshStats() {
        statsArea.setText("Calculating...");
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() { return instructorService.getClassStatistics(currentSection.getSectionId()); }
            @Override
            protected void done() { try { statsArea.setText(get()); statsArea.setCaretPosition(0); } catch (Exception e) { statsArea.setText("Error"); } }
        };
        worker.execute();
    }

    private void onGradeStudent() {
        int row = studentTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a student."); return; }
        openGradingPopup((EnrollmentDetailDTO) studentModel.getValueAt(row, 3));
    }

    private void onExportCsv() {
        if (studentModel.getRowCount() == 0) return;
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("Section_" + currentSection.getCourseCode() + "_Grades.csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            if (!f.getName().endsWith(".csv")) f = new File(f.getAbsolutePath() + ".csv");
            JOptionPane.showMessageDialog(this, CsvExporter.exportJTableToCSV(studentTable, f));
        }
    }

    private void onComputeAll() {
        if(JOptionPane.showConfirmDialog(this, "Calculate final grades?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override protected String doInBackground() { return instructorService.computeAndSaveAllFinalGrades(session.getUserId(), currentSection.getSectionId()); }
            @Override protected void done() { try { JOptionPane.showMessageDialog(GradebookPanel.this, get()); loadSection(currentSection); } catch (Exception e) {} }
        };
        worker.execute();
    }

    // --- POPUP LOGIC ---
    private void openGradingPopup(EnrollmentDetailDTO dto) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Grade: " + dto.getStudentName(), true);
        dialog.setLayout(new BorderLayout(10, 10)); dialog.setSize(600, 400); dialog.setLocationRelativeTo(this);

        DefaultTableModel gm = new DefaultTableModel(new String[]{"Component", "Max", "Weight%", "Score"}, 0) {
            public boolean isCellEditable(int r, int c) { return c == 3; }
        };
        JTable gt = new JTable(gm);
        gt.setRowHeight(25);
        gt.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        UITheme.styleTable(gt); // Theme

        SwingWorker<Void, Void> loader = new SwingWorker<>() {
            List<AssessmentComponent> schema; List<Grade> existing;
            @Override protected Void doInBackground() {
                schema = instructorService.getAssessmentSchema(currentSection.getSectionId());
                existing = instructorService.getGradesForEnrollment(session.getUserId(), dto.getEnrollmentId());
                return null;
            }
            @Override protected void done() {
                if (schema == null || schema.isEmpty()) gm.addRow(new Object[]{"No Schema", "-", "-", "-"});
                else for (AssessmentComponent c : schema) {
                    BigDecimal score = BigDecimal.ZERO;
                    for(Grade g : existing) if(g.getComponentName().equalsIgnoreCase(c.getName())) { score = g.getScore(); break; }
                    gm.addRow(new Object[]{c.getName(), c.getMaxMarks(), c.getWeightage(), score});
                }
            }
        };
        loader.execute();

        JButton saveBtn = new JButton("Save & Close");
        UITheme.styleButton(saveBtn); // Theme

        saveBtn.addActionListener(e -> {
            if(gt.isEditing()) gt.getCellEditor().stopCellEditing();
            List<Grade> list = new ArrayList<>();
            try {
                for(int i=0; i<gm.getRowCount(); i++) {
                    if("No Schema".equals(gm.getValueAt(i, 0))) continue;
                    list.add(new Grade(dto.getEnrollmentId(), (String)gm.getValueAt(i,0), new BigDecimal(gm.getValueAt(i,3).toString())));
                }
                instructorService.saveAllGrades(session.getUserId(), currentSection.getSectionId(), dto.getEnrollmentId(), list);
                dialog.dispose(); loadSection(currentSection);
            } catch(Exception ex) { JOptionPane.showMessageDialog(dialog, "Invalid Input"); }
        });
        dialog.add(new JScrollPane(gt), BorderLayout.CENTER); dialog.add(saveBtn, BorderLayout.SOUTH); dialog.setVisible(true);
    }

    private void openSchemaDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Configure Assessments", true);
        dialog.setLayout(new BorderLayout()); dialog.setSize(500, 400); dialog.setLocationRelativeTo(this);
        DefaultTableModel sm = new DefaultTableModel(new String[]{"Name", "Max", "Weight%"}, 0);
        JTable st = new JTable(sm);

        UITheme.styleTable(st); // Theme

        JPanel bp = new JPanel();
        JButton addRow = new JButton("Add Row");
        JButton save = new JButton("Save Schema");

        UITheme.styleButton(addRow);
        UITheme.styleButton(save);

        bp.add(addRow); bp.add(save);

        addRow.addActionListener(e -> sm.addRow(new Object[]{"New", "100", "0"}));
        save.addActionListener(e -> {
            if(st.isEditing()) st.getCellEditor().stopCellEditing();
            List<AssessmentComponent> list = new ArrayList<>();
            try {
                for(int i=0; i<sm.getRowCount(); i++) list.add(new AssessmentComponent(currentSection.getSectionId(), sm.getValueAt(i,0).toString(), new BigDecimal(sm.getValueAt(i,1).toString()), new BigDecimal(sm.getValueAt(i,2).toString())));
                String res = instructorService.saveAssessmentSchema(session.getUserId(), currentSection.getSectionId(), list);
                JOptionPane.showMessageDialog(dialog, res);
                if(res.startsWith("Success")) { dialog.dispose(); refreshStats(); }
            } catch(Exception ex) { JOptionPane.showMessageDialog(dialog, "Error"); }
        });

        SwingWorker<Void, Void> l = new SwingWorker<>() {
            List<AssessmentComponent> ex;
            @Override protected Void doInBackground() { ex = instructorService.getAssessmentSchema(currentSection.getSectionId()); return null; }
            @Override protected void done() {
                for(AssessmentComponent c:ex) sm.addRow(new Object[]{c.getName(), c.getMaxMarks(), c.getWeightage()});
                if(ex.isEmpty()) sm.addRow(new Object[]{"Quiz", "20", "20"});
            }
        };
        l.execute();
        dialog.add(new JScrollPane(st), BorderLayout.CENTER); dialog.add(bp, BorderLayout.SOUTH); dialog.setVisible(true);
    }
}