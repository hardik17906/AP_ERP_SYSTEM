package edu.univ.erp.data;

import edu.univ.erp.data.db.DatabaseManager;
import edu.univ.erp.domain.Grade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GradeDAO {

    private static final Logger log = LoggerFactory.getLogger(GradeDAO.class);
    private final DatabaseManager dbManager;

    public GradeDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public List<Grade> getGradesForEnrollment(int enrollmentId) {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT * FROM grades WHERE enrollment_id = ?";
        try (Connection conn = dbManager.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, enrollmentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Grade grade = new Grade();
                    grade.setGradeId(rs.getInt("grade_id"));
                    grade.setEnrollmentId(rs.getInt("enrollment_id"));
                    grade.setComponentName(rs.getString("component_name"));
                    grade.setScore(rs.getBigDecimal("score"));
                    grades.add(grade);
                }
            }
        } catch (SQLException e) {
            log.error("Error retrieving grades", e);
        }
        return grades;
    }

    /**
     * ROBUST SAVE: Explicitly checks if a grade exists to handle Updates correctly.
     */
    public boolean saveOrUpdateGrade(int enrollmentId, String component, BigDecimal score) {
        Integer existingId = null;

        // 1. Check if grade exists
        String checkSql = "SELECT grade_id FROM grades WHERE enrollment_id = ? AND component_name = ?";
        try (Connection conn = dbManager.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
            pstmt.setInt(1, enrollmentId);
            pstmt.setString(2, component);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) existingId = rs.getInt("grade_id");
            }
        } catch (SQLException e) {
            log.error("Check failed", e);
            return false;
        }

        // 2. Update or Insert
        if (existingId != null) {
            String updateSql = "UPDATE grades SET score = ? WHERE grade_id = ?";
            try (Connection conn = dbManager.getErpConnection();
                 PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setBigDecimal(1, score);
                pstmt.setInt(2, existingId);
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                log.error("Update failed", e);
                return false;
            }
        } else {
            String insertSql = "INSERT INTO grades (enrollment_id, component_name, score) VALUES (?, ?, ?)";
            try (Connection conn = dbManager.getErpConnection();
                 PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setInt(1, enrollmentId);
                pstmt.setString(2, component);
                pstmt.setBigDecimal(3, score);
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                log.error("Insert failed", e);
                return false;
            }
        }
    }
}