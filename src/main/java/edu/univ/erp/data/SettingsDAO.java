package edu.univ.erp.data;

import edu.univ.erp.data.db.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO for the 'settings' table (Key-Value store).
 */
public class SettingsDAO {

    private static final Logger log = LoggerFactory.getLogger(SettingsDAO.class);
    private final DatabaseManager dbManager;

    public SettingsDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Retrieves the value for a specific setting key.
     *
     * @param settingKey The key (e.g., "maintenance_mode").
     * @return The string value of the setting, or null if not found.
     */
    public String getSettingValue(String settingKey) {
        String sql = "SELECT setting_value FROM settings WHERE setting_key = ?";
        try (Connection conn = dbManager.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, settingKey);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("setting_value");
                }
            }
        } catch (SQLException e) {
            log.error("Error retrieving setting: {}", settingKey, e);
        }
        // Return a safe default if not found
        return null;
    }

    /**
     * Sets the value for a specific setting key.
     *
     * @param settingKey The key (e.g., "maintenance_mode").
     * @param settingValue The new value (e.g., "true" or "false").
     * @return true if the update was successful, false otherwise.
     */
    public boolean setSettingValue(String settingKey, String settingValue) {
        // This is an "upsert" command: UPDATE if key exists, INSERT if it doesn't.
        String sql = "INSERT INTO settings (setting_key, setting_value) VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value)";
        
        try (Connection conn = dbManager.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, settingKey);
            pstmt.setString(2, settingValue);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            log.error("Error setting value for: {}", settingKey, e);
            return false;
        }
    }
}