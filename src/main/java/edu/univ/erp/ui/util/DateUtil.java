package edu.univ.erp.ui.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/**
 * A static utility class for common date and time operations.
 * Uses the modern java.time API.
 */
public final class DateUtil {

    private static final Logger log = LoggerFactory.getLogger(DateUtil.class);

    // Standard formatters
    public static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");
    public static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private DateUtil() {
    }

    /**
     * Formats a LocalDate into a standard string (e.g., "25-12-2025").
     *
     * @param date The LocalDate to format.
     * @return The formatted string, or an empty string if date is null.
     */
    public static String formatLocalDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DATE_FORMATTER);
    }

    /**
     * Formats a LocalDateTime into a standard string (e.g., "25-12-2025 14:30:00").
     *
     * @param dateTime The LocalDateTime to format.
     * @return The formatted string, or an empty string if dateTime is null.
     */
    public static String formatLocalDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    /**
     * Parses a string (e.g., "25-12-2025") into a LocalDate.
     *
     * @param dateString The string to parse.
     * @return An Optional containing the LocalDate, or Optional.empty() if parsing fails.
     */
    public static Optional<LocalDate> parseLocalDate(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(dateString, DATE_FORMATTER));
        } catch (DateTimeParseException e) {
            log.warn("Could not parse date string: {}", dateString, e);
            return Optional.empty();
        }
    }

    /**
     * Converts a java.sql.Timestamp (from JDBC) to a user-friendly string.
     *
     * @param timestamp The timestamp from the database.
     * @return The formatted string, or an empty string if timestamp is null.
     */
    public static String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) {
            return "";
        }
        // Convert Timestamp to LocalDateTime and format it
        return timestamp.toLocalDateTime().format(DATE_TIME_FORMATTER);
    }
    
    /**
     * Converts a java.sql.Timestamp to a LocalDate.
     *
     * @param timestamp The timestamp from the database.
     * @return The LocalDate, or null if timestamp is null.
     */
    public static LocalDate toLocalDate(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime().toLocalDate();
    }
}   