package edu.univ.erp.ui.util;

import com.opencsv.CSVWriter;
import edu.univ.erp.domain.Student;
import edu.univ.erp.service.dto.RegistrationDetailDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Utility to export student transcript to CSV.
 */
public final class TranscriptCsvExporter {

    private static final Logger log = LoggerFactory.getLogger(TranscriptCsvExporter.class);

    private TranscriptCsvExporter() {}

    public static String exportTranscript(File file, Student student, List<RegistrationDetailDTO> completedCourses) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
            
            // 1. Write Student Info Header
            writer.writeNext(new String[]{"--- Academic Transcript ---"});
            writer.writeNext(new String[]{"Student Name", student.getName()});
            writer.writeNext(new String[]{"Roll Number", student.getRollNumber()});
            writer.writeNext(new String[]{"Program", student.getProgram()});
            writer.writeNext(new String[]{}); // Empty row for spacing

            // 2. Write Table Headers
            writer.writeNext(new String[]{"Course Code", "Course Title", "Semester", "Year", "Grade"});

            // 3. Write Course Data
            for (RegistrationDetailDTO reg : completedCourses) {
                String grade = (reg.getFinalGrade() != null) ? reg.getFinalGrade() : "N/A";
                writer.writeNext(new String[]{
                        reg.getCourseCode(),
                        reg.getCourseTitle(),
                        reg.getSemester(),
                        String.valueOf(reg.getYear()),
                        grade
                });
            }

            log.info("CSV transcript exported to: {}", file.getAbsolutePath());
            return "Success: Transcript saved as CSV.";

        } catch (IOException e) {
            log.error("Error writing CSV transcript", e);
            return "Error: Could not write CSV file. " + e.getMessage();
        }
    }
}