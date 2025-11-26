package edu.univ.erp.data;

import edu.univ.erp.data.db.DatabaseManager;
import edu.univ.erp.domain.Instructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InstructorDAO {

    private static final Logger log = LoggerFactory.getLogger(InstructorDAO.class);
    private final DatabaseManager dbManager;

    public InstructorDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public boolean createInstructor(Instructor instructor) {
        String sql = "INSERT INTO instructors (user_id, department, title) VALUES (?, ?, ?)";
        try (Connection conn = dbManager.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, instructor.getUserId());
            pstmt.setString(2, instructor.getDepartment());
            pstmt.setString(3, instructor.getTitle());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Error creating instructor", e);
            return false;
        }
    }

    public Optional<Instructor> getInstructorById(int userId) {
        String sql = "SELECT * FROM instructors WHERE user_id = ?";
        try (Connection conn = dbManager.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Instructor i = new Instructor(
                            rs.getInt("user_id"),
                            rs.getString("department"),
                            rs.getString("title")
                    );
                    return Optional.of(i);
                }
            }
        } catch (SQLException e) {
            log.error("Error finding instructor", e);
        }
        return Optional.empty();
    }

    public Map<Integer, Instructor> getAllInstructorsAsMap() {
        Map<Integer, Instructor> map = new HashMap<>();
        String sql = "SELECT * FROM instructors";
        try (Connection conn = dbManager.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Instructor i = new Instructor(rs.getInt("user_id"), rs.getString("department"), rs.getString("title"));
                map.put(i.getUserId(), i);
            }
        } catch (SQLException e) {
            log.error("Error getting instructors map", e);
        }
        return map;
    }

    // --- THIS IS THE METHOD USED BY ADMIN SERVICE ---
    public List<Instructor> getAllInstructors() {
        List<Instructor> list = new ArrayList<>();
        String sql = "SELECT * FROM instructors";
        try (Connection conn = dbManager.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Instructor(rs.getInt("user_id"), rs.getString("department"), rs.getString("title")));
            }
        } catch (SQLException e) {
            log.error("Error getting all instructors list", e);
        }
        return list;
    }
}