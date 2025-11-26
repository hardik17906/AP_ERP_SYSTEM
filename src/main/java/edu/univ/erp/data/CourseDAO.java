package edu.univ.erp.data;

import edu.univ.erp.data.db.DatabaseManager;
import edu.univ.erp.domain.Course;
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

public class CourseDAO {

    private static final Logger log = LoggerFactory.getLogger(CourseDAO.class);
    private final DatabaseManager dbManager;

    public CourseDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM courses ORDER BY course_code";
        
        try (Connection conn = dbManager.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Course course = new Course();
                course.setCourseCode(rs.getString("course_code"));
                course.setTitle(rs.getString("title"));
                course.setCredits(rs.getInt("credits"));
                courses.add(course);
            }
        } catch (SQLException e) {
            log.error("Error retrieving all courses as list", e);
        }
        return courses;
    }

    public Map<String, Course> getAllCoursesAsMap() {
        Map<String, Course> courseMap = new HashMap<>();
        String sql = "SELECT * FROM courses";
        
        try (Connection conn = dbManager.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Course course = new Course();
                course.setCourseCode(rs.getString("course_code"));
                course.setTitle(rs.getString("title"));
                course.setCredits(rs.getInt("credits"));
                courseMap.put(course.getCourseCode(), course);
            }
        } catch (SQLException e) {
            log.error("Error retrieving all courses as map", e);
        }
        return courseMap;
    }

    public boolean createCourse(Course course) {
        String sql = "INSERT INTO courses (course_code, title, credits) VALUES (?, ?, ?)";
        try (Connection conn = dbManager.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, course.getCourseCode());
            pstmt.setString(2, course.getTitle());
            pstmt.setInt(3, course.getCredits());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            log.error("Error creating course: {}", course.getCourseCode(), e);
            return false;
        }
    }

    // --- NEW METHOD FOR EDITING ---
    public boolean updateCourse(Course course) {
        String sql = "UPDATE courses SET title = ?, credits = ? WHERE course_code = ?";
        try (Connection conn = dbManager.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, course.getTitle());
            pstmt.setInt(2, course.getCredits());
            pstmt.setString(3, course.getCourseCode());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            log.error("Error updating course: {}", course.getCourseCode(), e);
            return false;
        }
    }
}