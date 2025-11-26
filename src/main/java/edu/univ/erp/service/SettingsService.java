package edu.univ.erp.service;

import edu.univ.erp.data.SettingsDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple service layer for reading application-wide settings.
 * This provides a clean API for the UI and other services
 * to get configuration values.
 */
public class SettingsService {

    private static final Logger log = LoggerFactory.getLogger(SettingsService.class);

    private final SettingsDAO settingsDAO;

    // Constructor
    public SettingsService(SettingsDAO settingsDAO) {
        this.settingsDAO = settingsDAO;
    }

    /**
     * Gets the value of a specific setting from the database.
     *
     * @param key The key of the setting (e.g., "current_semester").
     * @return The string value of the setting, or null if not found.
     */
    public String getSettingValue(String key) {
        try {
            return settingsDAO.getSettingValue(key);
        } catch (Exception e) {
            log.error("Error retrieving setting: {}", key, e);
            return null; // Return null on error
        }
    }

    /**
     * A convenience method to get the current academic year.
     *
     * @return The year as an int, or a sensible default (e.g., 2025) on error.
     */
    public int getCurrentYear() {
        try {
            String yearStr = getSettingValue("current_year");
            return Integer.parseInt(yearStr);
        } catch (Exception e) {
            log.warn("Could not parse 'current_year' setting. Defaulting to 2025.", e);
            return 2025; // A sensible fallback
        }
    }

    /**
     * A convenience method to get the current semester.
     *
     * @return The semester as a String (e.g., "FALL"), or a default on error.
     */
    public String getCurrentSemester() {
        try {
            String semester = getSettingValue("current_semester");
            return (semester != null) ? semester : "FALL"; // A sensible fallback
        } catch (Exception e) {
            log.warn("Could not retrieve 'current_semester' setting. Defaulting to FALL.", e);
            return "FALL";
        }
    }
}