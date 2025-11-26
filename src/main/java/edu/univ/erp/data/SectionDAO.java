package edu.univ.erp.data;

import edu.univ.erp.data.db.DatabaseManager;
import edu.univ.erp.domain.Section;
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
import java.util.stream.Collectors;

public class SectionDAO {

    private static final Logger log = LoggerFactory.getLogger(SectionDAO.class);
    private final DatabaseManager dbManager;

    public SectionDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    // --- READ METHODS ---

    public List<Section> getAllSections() {
        List<Section> list = new ArrayList<>();
        String sql = "SELECT * FROM sections ORDER BY year DESC, semester ASC";
        try (Connection conn = dbManager.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while(rs.next()) list.add(mapRowToSection(rs));
        } catch(SQLException e) { log.error("Error getting all sections", e); }
        return list;
    }

    public Optional<Section> getSectionById(int sectionId) {
        String sql = "SELECT * FROM sections WHERE section_id = ?";
        try (Connection conn = dbManager.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sectionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRowToSection(rs));
            }
        } catch (SQLException e) {
            log.error("Error finding section by id: {}", sectionId, e);
        }
        return Optional.empty();
    }

    public List<Section> getSectionsByTerm(String semester, int year) {
        List<Section> sections = new ArrayList<>();
        String sql = "SELECT * FROM sections WHERE semester = ? AND year = ? ORDER BY course_code";
        try (Connection conn = dbManager.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, semester);
            pstmt.setInt(2, year);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) sections.add(mapRowToSection(rs));
            }
        } catch (SQLException e) {
            log.error("Error retrieving sections for term", e);
        }
        return sections;
    }
    
    public List<Section> getSectionsByInstructor(int instructorId, String semester, int year) {
        List<Section> sections = new ArrayList<>();
        // Only fetching sections for a specific instructor in a specific term
        String sql = "SELECT * FROM sections WHERE instructor_id = ? AND semester = ? AND year = ?";
        try (Connection conn = dbManager.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, instructorId);
            pstmt.setString(2, semester);
            pstmt.setInt(3, year);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) sections.add(mapRowToSection(rs));
            }
        } catch (SQLException e) {
            log.error("Error retrieving sections for instructor conflict check", e);
        }
        return sections;
    }

    public Map<Integer, Section> getSectionsByIds(List<Integer> sectionIds) {
        Map<Integer, Section> sectionMap = new HashMap<>();
        if (sectionIds == null || sectionIds.isEmpty()) return sectionMap;

        String placeholders = sectionIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT * FROM sections WHERE section_id IN (" + placeholders + ")";

        try (Connection conn = dbManager.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < sectionIds.size(); i++) pstmt.setInt(i + 1, sectionIds.get(i));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Section section = mapRowToSection(rs);
                    sectionMap.put(section.getSectionId(), section);
                }
            }
        } catch (SQLException e) {
            log.error("Error retrieving sections by list of IDs", e);
        }
        return sectionMap;
    }

    // --- WRITE METHODS ---

    public boolean createSection(Section section) {
        String sql = "INSERT INTO sections (course_code, instructor_id, day_time, room, capacity, semester, year) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, section.getCourseCode());
            if (section.getInstructorId() > 0) {
                pstmt.setInt(2, section.getInstructorId());
            } else {
                pstmt.setNull(2, java.sql.Types.INTEGER);
            }
            pstmt.setString(3, section.getDayTime());
            pstmt.setString(4, section.getRoom());
            pstmt.setInt(5, section.getCapacity());
            pstmt.setString(6, section.getSemester());
            pstmt.setInt(7, section.getYear());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Error creating new section", e);
            return false;
        }
    }
    
    public boolean updateSection(Section s) {
        String sql = "UPDATE sections SET instructor_id=?, day_time=?, room=?, capacity=?, semester=?, year=? WHERE section_id=?";
        try (Connection conn = dbManager.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            if(s.getInstructorId() > 0) pstmt.setInt(1, s.getInstructorId());
            else pstmt.setNull(1, java.sql.Types.INTEGER);
            
            pstmt.setString(2, s.getDayTime());
            pstmt.setString(3, s.getRoom());
            pstmt.setInt(4, s.getCapacity());
            pstmt.setString(5, s.getSemester());
            pstmt.setInt(6, s.getYear());
            pstmt.setInt(7, s.getSectionId());
            
            return pstmt.executeUpdate() > 0;
        } catch(SQLException e) {
            log.error("Error updating section", e);
            return false;
        }
    }

    public boolean assignInstructor(int sectionId, int instructorId) {
        String sql = "UPDATE sections SET instructor_id = ? WHERE section_id = ?";
        try (Connection conn = dbManager.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, instructorId);
            pstmt.setInt(2, sectionId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Error assigning instructor", e);
            return false;
        }
    }

    private Section mapRowToSection(ResultSet rs) throws SQLException {
        Section section = new Section();
        section.setSectionId(rs.getInt("section_id"));
        section.setCourseCode(rs.getString("course_code"));
        section.setInstructorId(rs.getInt("instructor_id"));
        section.setDayTime(rs.getString("day_time"));
        section.setRoom(rs.getString("room"));
        section.setCapacity(rs.getInt("capacity"));
        section.setSemester(rs.getString("semester"));
        section.setYear(rs.getInt("year"));
        return section;
    }
}