package edu.univ.erp;

import edu.univ.erp.access.AccessService;
import edu.univ.erp.auth.UserSession;
import edu.univ.erp.data.*;
import edu.univ.erp.domain.Section;
import edu.univ.erp.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudentServiceTest {

    @Mock private EnrollmentDAO enrollmentDAO;
    @Mock private SectionDAO sectionDAO;
    @Mock private GradeDAO gradeDAO;
    @Mock private SettingsDAO settingsDAO;
    @Mock private AccessService accessService;
    @Mock private CourseDAO courseDAO;
    @Mock private StudentDAO studentDAO;
    @Mock private UserSession session;

    private StudentService studentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Inject all mocks into the service
        studentService = new StudentService(enrollmentDAO, sectionDAO, gradeDAO, settingsDAO, accessService, courseDAO, session, studentDAO);
    }

    @Test
    void testRegisterSectionFull() {
        int studentId = 1;
        int sectionId = 101;

        // 1. Mock Section Data (Capacity is 50)
        Section mockSection = new Section();
        mockSection.setSectionId(sectionId);
        mockSection.setCapacity(50);
        when(sectionDAO.getSectionById(sectionId)).thenReturn(Optional.of(mockSection));

        // 2. Mock Enrollment Count (Currently 50 students -> FULL)
        when(enrollmentDAO.getEnrollmentCount(sectionId)).thenReturn(50);

        // 3. Mock Maintenance (Not in maintenance mode)
        when(accessService.isMaintenanceModeOn()).thenReturn(false);

        // Execute
        String result = studentService.registerForSection(studentId, sectionId);

        // Assert
        assertEquals("Error: Section is full.", result);

        // Verify we NEVER called the database save method
        verify(enrollmentDAO, never()).createEnrollment(anyInt(), anyInt());
    }

    @Test
    void testRegisterSuccess() {
        int studentId = 1;
        int sectionId = 101;

        Section mockSection = new Section();
        mockSection.setCapacity(50);
        mockSection.setCourseCode("CS101");

        when(sectionDAO.getSectionById(sectionId)).thenReturn(Optional.of(mockSection));
        when(enrollmentDAO.getEnrollmentCount(sectionId)).thenReturn(10); // Not full
        when(enrollmentDAO.hasEnrollment(studentId, sectionId)).thenReturn(false); // Not duplicate
        when(enrollmentDAO.createEnrollment(studentId, sectionId)).thenReturn(true); // DB save works

        String result = studentService.registerForSection(studentId, sectionId);

        assertTrue(result.startsWith("Success"));
    }
}