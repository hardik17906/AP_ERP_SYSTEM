package edu.univ.erp.data;

import edu.univ.erp.data.db.DatabaseManager;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Student;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO for the 'enrollments' table.
 * Handles all database interactions regarding student course registrations.
 */
public class EnrollmentDAO {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentDAO.class);
    private final DatabaseManager dbManager;

    public EnrollmentDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Gets the current number of actively enrolled students in a section.
     * Only counts 'REGISTERED' status.
     */
    public int getEnrollmentCount(int sectionId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE section_id = ? AND status = 'REGISTERED'";
        
        try (Connection conn = dbManager.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sectionId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1); 
                }
            }
        } catch (SQLException e) {
            log.error("Error getting enrollment count for section: {}", sectionId, e);
        }
        return 0; 
    }

    /**
     * Retrieves all enrollment records for a single student for a specific term.
     * Used by MyRegistrationsPanel and TimetablePanel.
     */
    public List<Enrollment> getEnrollmentsByStudentAndTerm(int studentId, String semester, int year) {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT e.* FROM enrollments e " +
                     "JOIN sections s ON e.section_id = s.section_id " +
                     "WHERE e.student_id = ? AND s.semester = ? AND s.year = ? " +
                     "ORDER BY e.enrollment_id DESC";
        
        try (Connection conn = dbManager.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            pstmt.setString(2, semester);
            pstmt.setInt(3, year);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    enrollments.add(mapRowToEnrollment(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error retrieving enrollments for student {} in term {}-{}", studentId, semester, year, e);
        }
        return enrollments;
    }

    /**
     * Checks if a student is already enrolled in a section.
     * Returns true ONLY if the status is 'REGISTERED'.
     */
    public boolean hasEnrollment(int studentId, int sectionId) {
        String sql = "SELECT 1 FROM enrollments WHERE student_id = ? AND section_id = ? AND status = 'REGISTERED' LIMIT 1";
        try (Connection conn = dbManager.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            pstmt.setInt(2, sectionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // true if a row was found
            }
        } catch (SQLException e) {
            log.error("Error checking enrollment for student: {}, section: {}", studentId, sectionId, e);
        }
        return false;
    }

    /**
     * Creates a new enrollment record OR re-activates a dropped one.
     * Uses MySQL "ON DUPLICATE KEY UPDATE" syntax.
     *
     * @param studentId The student's user_id.
     * @param sectionId The section's ID.
     * @return true if successful, false otherwise.
     */
    public boolean createEnrollment(int studentId, int sectionId) {
        // FIX: Use UPSERT logic.
        // If the row exists (e.g., status was 'DROPPED'), update it back to 'REGISTERED'.
        // Also reset final_grade to NULL in case they are retaking a class.
        String sql = "INSERT INTO enrollments (student_id, section_id, status, final_grade) " +
                     "VALUES (?, ?, 'REGISTERED', NULL) " +
                     "ON DUPLICATE KEY UPDATE status = 'REGISTERED', final_grade = NULL";
                     
        try (Connection conn = dbManager.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            pstmt.setInt(2, sectionId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            log.error("Error creating/updating enrollment: student: {}, section: {}", studentId, sectionId, e);
            return false;
        }
    }

    /**
     * Finds a single enrollment record by its primary key.
     */
    public Optional<Enrollment> getEnrollmentById(int enrollmentId) {
        String sql = "SELECT * FROM enrollments WHERE enrollment_id = ?";
        
        try (Connection conn = dbManager.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, enrollmentId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToEnrollment(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding enrollment by id: {}", enrollmentId, e);
        }
        return Optional.empty();
    }

    /**
     * Retrieves a list of all Students who are actively registered in a specific section.
     * Used by Instructors to see their class list.
     */
    public List<Student> getEnrolledStudents(int sectionId) {
        List<Student> students = new ArrayList<>();
        // Join with students table to get their profile information
        String sql = "SELECT s.* FROM students s " +
                     "JOIN enrollments e ON s.user_id = e.student_id " +
                     "WHERE e.section_id = ? AND e.status = 'REGISTERED' " +
                     "ORDER BY s.roll_number";

        try (Connection conn = dbManager.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sectionId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    students.add(mapRowToStudent(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error retrieving enrolled students for section: {}", sectionId, e);
        }
        return students;
    }

    /**
     * Updates the 'status' field for a specific enrollment record.
     * Used to "drop" a course.
     */
    public boolean updateEnrollmentStatus(int enrollmentId, String newStatus) {
        String sql = "UPDATE enrollments SET status = ? WHERE enrollment_id = ?";
        
        try (Connection conn = dbManager.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus);
            pstmt.setInt(2, enrollmentId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            log.error("Error updating enrollment status for ID: {}", enrollmentId, e);
            return false;
        }
    }

    /**
     * Retrieves all enrollment records for a single student (Active and Dropped).
     * Used by MyGradesPanel (Transcript).
     */
    public List<Enrollment> getEnrollmentsByStudent(int studentId) {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT * FROM enrollments WHERE student_id = ? ORDER BY enrollment_id DESC";
        
        try (Connection conn = dbManager.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    enrollments.add(mapRowToEnrollment(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error retrieving enrollments for student: {}", studentId, e);
        }
        return enrollments;
    }

    /**
     * Retrieves all active enrollments for a single section.
     * Used by Instructor Gradebook to calculate grades.
     */
    public List<Enrollment> getEnrollmentsBySection(int sectionId) {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT * FROM enrollments WHERE section_id = ? AND status = 'REGISTERED'";
        
        try (Connection conn = dbManager.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sectionId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    enrollments.add(mapRowToEnrollment(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error retrieving enrollments for section: {}", sectionId, e);
        }
        return enrollments;
    }

    /**
     * Updates the final_grade column for a specific enrollment.
     */
    public boolean setFinalGrade(int enrollmentId, String finalGrade) {
        String sql = "UPDATE enrollments SET final_grade = ? WHERE enrollment_id = ?";
        
        try (Connection conn = dbManager.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, finalGrade);
            pstmt.setInt(2, enrollmentId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            log.error("Error setting final grade for enrollment: {}", enrollmentId, e);
            return false;
        }
    }

    // --- Private Helpers ---

    private Student mapRowToStudent(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setUserId(rs.getInt("user_id"));
        student.setRollNumber(rs.getString("roll_number"));
        student.setProgram(rs.getString("program"));
        student.setYear(rs.getInt("year"));
        return student;
    }

    private Enrollment mapRowToEnrollment(ResultSet rs) throws SQLException {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentId(rs.getInt("enrollment_id"));
        enrollment.setStudentId(rs.getInt("student_id"));
        enrollment.setSectionId(rs.getInt("section_id"));
        enrollment.setStatus(rs.getString("status"));
        enrollment.setFinalGrade(rs.getString("final_grade"));
        return enrollment;
    }
}