package edu.univ.erp.ui.util;
import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * A static utility class for exporting data to CSV format.
 * Uses the OpenCSV library.
 */
public final class CsvExporter {

    private static final Logger log = LoggerFactory.getLogger(CsvExporter.class);

    // Private constructor to prevent instantiation
    private CsvExporter() {
    }

    /**
     * Exports the full contents of a JTable (including headers) to a CSV file.
     *
     * @param table The JTable to export.
     * @param file  The file to write to.
     * @return A success or error message string.
     */
    public static String exportJTableToCSV(JTable table, File file) {
        TableModel model = table.getModel();

        try (FileWriter fileWriter = new FileWriter(file);
             CSVWriter csvWriter = new CSVWriter(fileWriter,
                     CSVWriter.DEFAULT_SEPARATOR,
                     CSVWriter.NO_QUOTE_CHARACTER, // or DEFAULT_QUOTE_CHARACTER
                     CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                     CSVWriter.DEFAULT_LINE_END)) {
            // 1. Write Header Row
            int colCount = model.getColumnCount();
            String[] headers = new String[colCount];
            for (int i = 0; i < colCount; i++) {
                headers[i] = model.getColumnName(i);
            }
            csvWriter.writeNext(headers);
            // 2. Write Data Rows
            int rowCount = model.getRowCount();
            for (int r = 0; r < rowCount; r++) {
                String[] rowData = new String[colCount];
                for (int c = 0; c < colCount; c++) {
                    Object value = model.getValueAt(r, c);
                    // Handle nulls and convert all data to String
                    rowData[c] = (value == null) ? "" : value.toString();
                }
                csvWriter.writeNext(rowData);
            }
            log.info("Successfully exported JTable data to: {}", file.getAbsolutePath());
            return "Success: Data exported to " + file.getName();

        } catch (IOException e) {
            log.error("Error exporting JTable to CSV", e);
            return "Error: Could not write file. " + e.getMessage();
        }
    }
}