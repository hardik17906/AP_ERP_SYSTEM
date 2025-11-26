package edu.univ.erp;

import edu.univ.erp.access.AccessService;
import edu.univ.erp.auth.store.AuthDAO;
import edu.univ.erp.data.*;
import edu.univ.erp.domain.AssessmentComponent;
import edu.univ.erp.service.InstructorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InstructorServiceTest {

    @Mock private SectionDAO sectionDAO;
    @Mock private EnrollmentDAO enrollmentDAO;
    @Mock private GradeDAO gradeDAO;
    @Mock private AccessService accessService;
    @Mock private StudentDAO studentDAO;
    @Mock private AuthDAO authDAO;
    @Mock private AssessmentDAO assessmentDAO;

    private InstructorService instructorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        instructorService = new InstructorService(sectionDAO, enrollmentDAO, gradeDAO, accessService, studentDAO, authDAO, assessmentDAO);
    }

    @Test
    void testSchemaValidationInvalidWeight() {
        int sectionId = 101;

        // Setup specific permissions
        when(accessService.canInstructorManageSection(sectionId)).thenReturn(true);
        when(accessService.isMaintenanceModeOn()).thenReturn(false);

        // Create schema that sums to 90% (Invalid)
        List<AssessmentComponent> badSchema = new ArrayList<>();
        badSchema.add(new AssessmentComponent(sectionId, "Quiz", new BigDecimal("50"), new BigDecimal("40"))); // 40%
        badSchema.add(new AssessmentComponent(sectionId, "Final", new BigDecimal("100"), new BigDecimal("50"))); // 50%
        // Total = 90%

        String result = instructorService.saveAssessmentSchema(1, sectionId, badSchema);

        // Assert Error
        assertTrue(result.startsWith("Error: Total weightage must be exactly 100%"));
    }

    @Test
    void testSchemaValidationValid() {
        int sectionId = 101;
        when(accessService.canInstructorManageSection(sectionId)).thenReturn(true);
        when(accessService.isMaintenanceModeOn()).thenReturn(false);
        when(assessmentDAO.createComponent(any())).thenReturn(true); // DB save mock

        // Create schema that sums to 100%
        List<AssessmentComponent> goodSchema = new ArrayList<>();
        goodSchema.add(new AssessmentComponent(sectionId, "Quiz", new BigDecimal("100"), new BigDecimal("50")));
        goodSchema.add(new AssessmentComponent(sectionId, "Final", new BigDecimal("100"), new BigDecimal("50")));

        String result = instructorService.saveAssessmentSchema(1, sectionId, goodSchema);

        assertEquals("Success: Grading schema saved.", result);
    }
}