package edu.univ.erp.service;

import edu.univ.erp.auth.store.AuthDAO; // <-- 1. ADD THIS IMPORT
import edu.univ.erp.data.CourseDAO;
import edu.univ.erp.data.EnrollmentDAO;
import edu.univ.erp.data.InstructorDAO;
import edu.univ.erp.data.SectionDAO;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Section;
import edu.univ.erp.service.dto.CatalogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service layer for browsing the course catalog.
 * This service is "read-only" and coordinates DAOs to build a
 * user-friendly catalog view.
 */
public class CatalogService {

    private static final Logger log = LoggerFactory.getLogger(CatalogService.class);

    // Dependencies on the DAOs
    private final SectionDAO sectionDAO;
    private final CourseDAO courseDAO;
    private final InstructorDAO instructorDAO;
    private final EnrollmentDAO enrollmentDAO;
    private final AuthDAO authDAO; // <-- 2. ADD THIS FIELD

    // Constructor for Dependency Injection
    public CatalogService(SectionDAO sectionDAO, CourseDAO courseDAO,
                          InstructorDAO instructorDAO, EnrollmentDAO enrollmentDAO,
                          AuthDAO authDAO) { // <-- 3. ADD AuthDAO to the parameters
        this.sectionDAO = sectionDAO;
        this.courseDAO = courseDAO;
        this.instructorDAO = instructorDAO;
        this.enrollmentDAO = enrollmentDAO;
        this.authDAO = authDAO; // <-- 3. AND ASSIGN IT HERE
    }

    /**
     * Retrieves the full course catalog for a given term.
     * This method assembles data from Courses, Sections, Instructors,
     * Auth (for usernames), and Enrollment counts into a list of CatalogEntry DTOs.
     *
     * @param semester The semester (e.g., "FALL").
     * @param year     The year (e.g., 2025).
     * @return A List of CatalogEntry objects.
     */
    public List<CatalogEntry> getCourseCatalog(String semester, int year) {
        log.debug("Assembling course catalog for {}-{}"+semester+ year);

        // 1. Get all base data in as few queries as possible
        List<Section> sections = sectionDAO.getSectionsByTerm(semester, year);
        Map<String, Course> courseMap = courseDAO.getAllCoursesAsMap();
        
        // Get data from *both* DAOs
        Map<Integer, Instructor> instructorMap = instructorDAO.getAllInstructorsAsMap();
        
        // This line will now work correctly!
        Map<Integer, String> usernameMap = authDAO.getAllUsernamesAsMap(); 

        List<CatalogEntry> catalog = new ArrayList<>();

        // 2. Assemble the DTOs
        for (Section s : sections) {
            Course c = courseMap.get(s.getCourseCode());
            if (c == null) {
                log.warn("Section {} has invalid course code {}. Skipping."+ s.getSectionId()+ s.getCourseCode());
                continue;
            }
            
            // COMBINE INSTRUCTOR DATA ---
            String instructorName = "TBA"; // Default
            Instructor inst = instructorMap.get(s.getInstructorId());
            if (inst != null) {
                // Get username (e.g., "jdoe") or title (e.g., "Professor")
                String username = usernameMap.getOrDefault(inst.getUserId(), "");
                instructorName = username; 
            }
            
            // 3. Get enrollment count *per-section*.
            int currentEnrollment = enrollmentDAO.getEnrollmentCount(s.getSectionId());

            // 4. Build the DTO
            catalog.add(new CatalogEntry(s.getSectionId(),s.getCourseCode(),c.getTitle(),c.getCredits(),instructorName,s.getDayTime(),s.getRoom(),s.getCapacity(),currentEnrollment));
        }
        return catalog;
    }

    /**
     * Searches the catalog. (Simple implementation)
     *
     * @param query    The search text.
     * @param semester The semester.
     * @param year     The year.
     * @return A filtered List of CatalogEntry objects.
     */
    public List<CatalogEntry> searchCatalog(String query, String semester, int year) {
        List<CatalogEntry> fullCatalog = getCourseCatalog(semester, year);
        
        if (query == null || query.trim().isEmpty()) {
            return fullCatalog;
        }

        String lowerQuery = query.toLowerCase();
        
        return fullCatalog.stream().filter(entry ->entry.getCourseCode().toLowerCase().contains(lowerQuery) ||entry.getCourseTitle().toLowerCase().contains(lowerQuery) ||entry.getInstructorName().toLowerCase().contains(lowerQuery)).collect(Collectors.toList());
    }
}