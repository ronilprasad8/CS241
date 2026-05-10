package com.school.sas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.school.sas.controller.TeacherController;
import com.school.sas.entity.Enrollment;
import com.school.sas.entity.Subject;
import com.school.sas.entity.User;
import com.school.sas.repository.EnrollmentRepository;
import com.school.sas.repository.SubjectRepository;
import com.school.sas.repository.UserRepository;
import com.school.sas.service.AttendanceService;
import com.school.sas.service.UserService;

@ExtendWith(MockitoExtension.class)
/**
 * Integration tests for TeacherController class.
 * Tests cover grade entry functionality and password reset for teachers.
 * Uses MockMvc for testing HTTP endpoints and Mockito for mocking dependencies.
 */
public class TeacherControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AttendanceService attendanceService;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TeacherController teacherController;

    private MockMvc mockMvc;
    private User teacher;
    private Subject subject;
    private Enrollment enrollment;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(teacherController).build();

        teacher = new User();
        teacher.setId(1L);
        teacher.setUsername("teacher1");
        teacher.setFirstName("John");
        teacher.setLastName("Doe");

        subject = new Subject();
        subject.setId(1L);
        subject.setSubjectName("Mathematics");

        enrollment = new Enrollment();
        enrollment.setId(1L);
        enrollment.setExamType("Midterm");
        enrollment.setMarksGained(85);
        enrollment.setTotalMarks(100);
    }

    @Test
    void testSaveGrades_Success() throws Exception {
        when(enrollmentRepository.findById(1L)).thenReturn(java.util.Optional.of(enrollment));

        mockMvc.perform(post("/teacher/grades/save")
                .param("examType-1", "Midterm")
                .param("marksGained-1", "85")
                .param("totalMarks-1", "100"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/teacher/grades"));

        verify(enrollmentRepository).save(enrollment);
        assertEquals("Midterm", enrollment.getExamType());
        assertEquals(85, enrollment.getMarksGained());
        assertEquals(100, enrollment.getTotalMarks());
    }

    @Test
    void testSaveGrades_PartialData() throws Exception {
        when(enrollmentRepository.findById(1L)).thenReturn(java.util.Optional.of(enrollment));

        mockMvc.perform(post("/teacher/grades/save")
                .param("examType-1", "Final")
                .param("marksGained-1", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/teacher/grades"));

        verify(enrollmentRepository).save(enrollment);
        assertEquals("Final", enrollment.getExamType());
        assertNull(enrollment.getMarksGained());
    }

    @Test
    void testResetPassword_Success() throws Exception {
        when(authentication.getName()).thenReturn("teacher1");

        mockMvc.perform(post("/teacher/reset-password")
                .param("password", "newPassword123")
                .param("confirmPassword", "newPassword123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/teacher/dashboard?reset_success=true"));

        verify(userService).resetPassword("teacher1", "newPassword123");
    }

    @Test
    void testResetPassword_PasswordMismatch() throws Exception {
        when(authentication.getName()).thenReturn("teacher1");

        mockMvc.perform(post("/teacher/reset-password")
                .param("password", "newPassword123")
                .param("confirmPassword", "differentPassword"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", "Passwords do not match"));

        verify(userService, never()).resetPassword(anyString(), anyString());
    }

    @Test
    void testResetPassword_Exception() throws Exception {
        when(authentication.getName()).thenReturn("teacher1");
        doThrow(new RuntimeException("User not found")).when(userService).resetPassword("teacher1", "newPassword123");

        mockMvc.perform(post("/teacher/reset-password")
                .param("password", "newPassword123")
                .param("confirmPassword", "newPassword123"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", "Failed to reset password: User not found"));

        verify(userService).resetPassword("teacher1", "newPassword123");
    }
}
