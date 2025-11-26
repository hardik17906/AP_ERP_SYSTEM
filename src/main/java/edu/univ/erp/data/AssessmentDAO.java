package edu.univ.erp.data;

import edu.univ.erp.data.db.DatabaseManager;
import edu.univ.erp.domain.AssessmentComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AssessmentDAO {
    private static final Logger log = LoggerFactory.getLogger(AssessmentDAO.class);
    private final DatabaseManager dbManager;

    public AssessmentDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public boolean createComponent(AssessmentComponent comp) {
        String sql = "INSERT INTO assessment_schema (section_id, component_name, max_marks, weightage) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbManager.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, comp.getSectionId());
            pstmt.setString(2, comp.getName());
            pstmt.setBigDecimal(3, comp.getMaxMarks());
            pstmt.setBigDecimal(4, comp.getWeightage());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Error creating assessment component", e);
            return false;
        }
    }

    public List<AssessmentComponent> getSchemaForSection(int sectionId) {
        List<AssessmentComponent> list = new ArrayList<>();
        String sql = "SELECT * FROM assessment_schema WHERE section_id = ?";
        try (Connection conn = dbManager.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sectionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    AssessmentComponent c = new AssessmentComponent();
                    c.setSchemaId(rs.getInt("schema_id"));
                    c.setSectionId(rs.getInt("section_id"));
                    c.setName(rs.getString("component_name"));
                    c.setMaxMarks(rs.getBigDecimal("max_marks"));
                    c.setWeightage(rs.getBigDecimal("weightage"));
                    list.add(c);
                }
            }
        } catch (SQLException e) {
            log.error("Error fetching schema", e);
        }
        return list;
    }
    
    public void clearSchema(int sectionId) {
        String sql = "DELETE FROM assessment_schema WHERE section_id = ?";
        try (Connection conn = dbManager.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sectionId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error clearing schema", e);
        }
    }
}