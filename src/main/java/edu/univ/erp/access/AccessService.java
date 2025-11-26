package edu.univ.erp.access;

import edu.univ.erp.auth.UserSession;
import edu.univ.erp.data.EnrollmentDAO;
import edu.univ.erp.data.SectionDAO;
import edu.univ.erp.data.SettingsDAO;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;


 // It checks permissions based on the user's role and the application's state (e.g., Maintenance Mode).
public class AccessService {

    private static final Logger log = LoggerFactory.getLogger(AccessService.class);

    // Dependencies needed to make decisions
    private final UserSession session;
    private final SettingsDAO settingsDAO;
    private final SectionDAO sectionDAO;
    private final EnrollmentDAO enrollmentDAO;

    /**
     * Constructs a new AccessService for a specific logged-in user.
     * @param session       The session of the currently logged-in user.
     * @param settingsDAO   DAO to check global settings (like maintenance mode).
     * @param sectionDAO    DAO to check section ownership.
     * @param enrollmentDAO DAO to check enrollment ownership.
     */
    public AccessService(UserSession session, SettingsDAO settingsDAO, SectionDAO sectionDAO, EnrollmentDAO enrollmentDAO) {
        if (session == null) {
            throw new IllegalArgumentException("AccessService cannot be created with a null UserSession.");
        }
        this.session = session;
        this.settingsDAO = settingsDAO;
        this.sectionDAO = sectionDAO;
        this.enrollmentDAO = enrollmentDAO;
    }

    /**
     * Checks if the system is currently in maintenance mode.
     * @return true if maintenance mode is ON, false otherwise.
     */
    public boolean isMaintenanceModeOn() {
        try {
            String value = settingsDAO.getSettingValue("maintenance_mode");
            return "true".equals(value);
        } catch (Exception e) {
            log.error("Failed to read maintenance_mode from settings", e);
            // Failsafe: if we can't read the setting, assume maintenance is ON to prevent writes.
            return true;
        }
    }

    /**
     * Checks if the *current user* is in a "read-only" state.
     * Admins are *never* read-only.
     * Students and Instructors are read-only if Maintenance Mode is ON.
     *
     * @return true if the current user should be blocked from making changes.
     */
    public boolean isReadOnly() {
        // Admins are never read-only
        if (session.getRole().equals("ADMIN")) {
            return false;
        }
        
        // Students and Instructors are read-only during maintenance
        return isMaintenanceModeOn();
    }

    /**
     * Checks if the registration deadline has passed.
     *
     * @return true if the current time is after the deadline, false otherwise.
     */
    public boolean isPastRegistrationDeadline() {
        try {
            String deadlineStr = settingsDAO.getSettingValue("registration_deadline");
            LocalDateTime deadline = LocalDateTime.parse(deadlineStr);
            return LocalDateTime.now().isAfter(deadline);
        } catch (DateTimeParseException e) {
            log.error("Could not parse registration_deadline from settings: {}", e.getMessage());
            // Failsafe: if deadline is misconfigured, block registration.
            return true;
        } catch (Exception e) {
            log.error("Failed to read registration_deadline from settings", e);
            return true;
        }
    }

    /*
      Checks if the current student can register for courses.
      Fails if: read-only mode is on, or deadline has passed.
     */
    public boolean canStudentRegister() {
        if (isReadOnly()) return false;
        if (isPastRegistrationDeadline()) return false;
        return true;
    }

    /*
     Checks if the current student can drop courses.
     Fails if: read-only mode is on, or deadline has passed.
     */
    public boolean canStudentDrop() {
        if (isReadOnly()) return false;
        if (isPastRegistrationDeadline()) return false;
        return true;
    }

    /*
      Checks if the current instructor can modify grades for a specific section.
      Fails if: read-only mode is on, or they don't teach this section.
     */
    public boolean canInstructorManageSection(int sectionId) {
        if (isReadOnly()) return false;
        return isMySection(sectionId);
    }

    /**
     * Checks if the currently logged-in user is the assigned
     * instructor for the given section.
     *
     * @param sectionId The section to check.
     * @return true if they are the instructor, false otherwise.
     */
public boolean isMySection(int sectionId) {
        if (!session.getRole().equals("INSTRUCTOR")) {
            return false;
        }
        try {
            // 1. Get the Optional from the DAO
            Optional<Section> sectionOpt = sectionDAO.getSectionById(sectionId);

            // 2. Use .map() to safely get the ID and compare it.
            //    - If sectionOpt is empty, this chain does nothing.
            //    - If it has a Section, it runs the check and returns an Optional<Boolean>.
            return sectionOpt.map(section -> section.getInstructorId() == session.getUserId())
                             .orElse(false); // 3. If the Optional was empty, return false.

        } catch (Exception e) {
            log.error("Error checking section ownership for sectionId {}", sectionId, e);
            return false;
        }
    }

    /**
     * Checks if the currently logged-in user has permission to view/manage
     * data related to a specific enrollment.
     *
     * - Admins can access anything.
     * - Students can access their *own* enrollments.
     * - Instructors can access enrollments for sections they *teach*.
     *
     * @param enrollmentId The ID of the enrollment record.
     * @return true if access is allowed, false otherwise.
     */
      public boolean canAccessEnrollmentData(int enrollmentId) {
        if (session.getRole().equals("ADMIN")) {
            return true;
        }

        try {
            // 1. Assume getEnrollmentById now returns Optional<Enrollment>
            Optional<Enrollment> enrollmentOpt = enrollmentDAO.getEnrollmentById(enrollmentId);

            // 2. If the enrollment doesn't exist, no access.
            if (enrollmentOpt.isEmpty()) {
                return false;
            }

            // 3. Get the object now that we know it's safe
            Enrollment enrollment = enrollmentOpt.get();

            // Check if the user is the student in this enrollment
            if (session.getRole().equals("STUDENT")) {
                return enrollment.getStudentId() == session.getUserId();
            }

            // Check if the user is the instructor for this enrollment's section
            if (session.getRole().equals("INSTRUCTOR")) {
                return isMySection(enrollment.getSectionId());
            }

        } catch (Exception e) {
            log.error("Error checking enrollment access for enrollmentId {}", enrollmentId, e);
        }

        return false;
    }

    /**
     * Checks if the current user can view a student's profile.
     * (Admin, the student themselves, or an instructor who teaches them).
     * @param studentId The user_id of the student profile to check.
     * @return true if access is allowed.
     */
    public boolean canViewStudentProfile(int studentId) {
        if (session.getRole().equals("ADMIN")) {
            return true;
        }
        if (session.getRole().equals("STUDENT") && session.getUserId() == studentId) {
            return true; // I am viewing my own profile
        }
        if (session.getRole().equals("INSTRUCTOR")) {
            // This is a complex check: "Does this instructor teach this student?"
            // You could implement this by checking all enrollments for the student.
            // For now, we'll keep it simple (or assume instructors use enrollment data only).
            // A simpler rule: Instructors can't just browse all student profiles.
            return false;
        }
        return false;
    }
}