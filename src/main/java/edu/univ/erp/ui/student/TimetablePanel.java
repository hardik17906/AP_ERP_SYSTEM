package edu.univ.erp.ui.student;

import edu.univ.erp.auth.UserSession;
import edu.univ.erp.service.SettingsService;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.service.dto.RegistrationDetailDTO;
import edu.univ.erp.ui.common.UITheme;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TimetablePanel extends JPanel {

    private final UserSession session;
    private final StudentService studentService;
    private final SettingsService settingsService;
    private JPanel cardBox;

    public TimetablePanel(UserSession session, StudentService studentService, SettingsService settingsService) {
        this.session = session;
        this.studentService = studentService;
        this.settingsService = settingsService;
        initUI();
        loadTimetable();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header
        JPanel top = new JPanel(new BorderLayout());
        JLabel title = new JLabel("My Class Schedule");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(UITheme.PRIMARY_COLOR);
        top.add(title, BorderLayout.WEST);

        JButton refresh = new JButton("Refresh");
        UITheme.styleButton(refresh);
        refresh.addActionListener(e -> loadTimetable());
        top.add(refresh, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // Cards Grid
        cardBox = new JPanel(new MigLayout("wrap 4, insets 0", "[grow]", "[]"));
        JScrollPane scroll = new JScrollPane(cardBox);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);
    }

    public void loadTimetable() {
        cardBox.removeAll();
        cardBox.add(new JLabel("Loading..."));
        cardBox.revalidate(); cardBox.repaint();

        new SwingWorker<List<RegistrationDetailDTO>, Void>() {
            @Override
            protected List<RegistrationDetailDTO> doInBackground() {
                String sem = settingsService.getSettingValue("current_semester");
                int year = Integer.parseInt(settingsService.getSettingValue("current_year"));
                return studentService.getMyCurrentRegistrations(session.getUserId(), sem, year);
            }
            @Override
            protected void done() {
                cardBox.removeAll();
                try {
                    List<RegistrationDetailDTO> list = get();
                    if (list.isEmpty()) cardBox.add(new JLabel("No scheduled classes."));
                    else for (RegistrationDetailDTO r : list) cardBox.add(createCard(r), "growx, wmin 200");
                } catch (Exception e) { cardBox.add(new JLabel("Error loading data.")); }
                cardBox.revalidate(); cardBox.repaint();
            }
        }.execute();
    }

    private JPanel createCard(RegistrationDetailDTO r) {
        JPanel card = new JPanel(new MigLayout("insets 10, fillx", "[grow][]"));

        // RESTORED: Use Theme-aware background color (Correct for Dark Mode)
        Color bg = UIManager.getColor("Panel.background");
        card.setBackground(bg != null ? bg.brighter() : Color.GRAY);
        card.setBorder(BorderFactory.createLineBorder(UITheme.PRIMARY_COLOR, 1));

        // Course Code
        JLabel code = new JLabel(r.getCourseCode());
        code.setFont(new Font("SansSerif", Font.BOLD, 14));
        code.setForeground(UITheme.PRIMARY_COLOR);
        card.add(code, "wrap");

        // Course Title
        JLabel title = new JLabel(r.getCourseTitle());
        title.setFont(new Font("SansSerif", Font.PLAIN, 11));
        card.add(title, "wrap, gapbottom 5");

        // Time (Bold)
        String timeText = (r.getDayTime() != null) ? r.getDayTime() : "TBA";
        JLabel time = new JLabel(timeText);
        time.setFont(new Font("SansSerif", Font.BOLD, 12));
        card.add(time, "wrap");

        // Room (Normal)
        String roomText = (r.getRoom() != null) ? r.getRoom() : "TBA";
        JLabel room = new JLabel(roomText);
        room.setFont(new Font("SansSerif", Font.PLAIN, 12));
        card.add(room, "growx");

        return card;
    }
}