package edu.univ.erp.data;

import edu.univ.erp.data.db.DatabaseManager;
import edu.univ.erp.domain.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList; // Standard import
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class StudentDAO {

    private static final Logger log = LoggerFactory.getLogger(StudentDAO.class);
    private final DatabaseManager dbManager;

    public StudentDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public boolean createStudent(Student student) {
        String sql = "INSERT INTO students (user_id, roll_number, program, year) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbManager.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, student.getUserId());
            pstmt.setString(2, student.getRollNumber());
            pstmt.setString(3, student.getProgram());
            pstmt.setInt(4, student.getYear());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Error creating student profile", e);
            return false;
        }
    }

    public Optional<Student> getStudentById(int userId) {
        String sql = "SELECT * FROM students WHERE user_id = ?";
        try (Connection conn = dbManager.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Student s = new Student();
                    s.setUserId(rs.getInt("user_id"));
                    s.setRollNumber(rs.getString("roll_number"));
                    s.setProgram(rs.getString("program"));
                    s.setYear(rs.getInt("year"));
                    return Optional.of(s);
                }
            }
        } catch (SQLException e) {
            log.error("Error finding student", e);
        }
        return Optional.empty();
    }

    public Map<Integer, Student> getStudentsByIds(List<Integer> userIds) {
        Map<Integer, Student> map = new HashMap<>();
        if (userIds == null || userIds.isEmpty()) return map;

        String placeholders = userIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT * FROM students WHERE user_id IN (" + placeholders + ")";

        try (Connection conn = dbManager.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < userIds.size(); i++) pstmt.setInt(i + 1, userIds.get(i));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Student s = new Student(
                            rs.getInt("user_id"),
                            rs.getString("roll_number"),
                            rs.getString("program"),
                            rs.getInt("year")
                    );
                    map.put(s.getUserId(), s);
                }
            }
        } catch (SQLException e) {
            log.error("Error getting students map", e);
        }
        return map;
    }

    // --- THIS IS THE CRITICAL METHOD FOR THE ADMIN LIST ---
    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>(); // Simple, standard ArrayList
        String sql = "SELECT * FROM students ORDER BY roll_number";

        try (Connection conn = dbManager.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Student s = new Student();
                s.setUserId(rs.getInt("user_id"));
                s.setRollNumber(rs.getString("roll_number"));
                s.setProgram(rs.getString("program"));
                s.setYear(rs.getInt("year"));
                list.add(s);
            }
        } catch (SQLException e) {
            log.error("Error getting all students", e);
        }
        return list;
    }
}