package com.school.sas;

import java.util.List;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.school.sas.controller.StudentController;
import com.school.sas.entity.Enrollment;
import com.school.sas.entity.User;
import com.school.sas.repository.UserRepository;
import com.school.sas.service.AttendanceService;
import com.school.sas.service.EnrollmentService;
import com.school.sas.service.UserService;

@ExtendWith(MockitoExtension.class)
/**
 * Integration tests for StudentController class.
 * Tests cover grade viewing functionality and password reset for students.
 * Uses MockMvc for testing HTTP endpoints and Mockito for mocking dependencies.
 */
public class StudentControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AttendanceService attendanceService;

    @Mock
    private EnrollmentService enrollmentService;

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private StudentController studentController;

    private MockMvc mockMvc;
    private User student;
    private Enrollment enrollment;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(studentController).build();

        student = new User();
        student.setId(1L);
        student.setUsername("student1");
        student.setFirstName("Jane");
        student.setLastName("Doe");

        enrollment = new Enrollment();
        enrollment.setId(1L);
        enrollment.setGrade("A");
        enrollment.setExamType("Midterm");
        enrollment.setMarksGained(90);
        enrollment.setTotalMarks(100);
    }

    @Test
    void testShowMyGradesPage() throws Exception {
        when(authentication.getName()).thenReturn("student1");
        when(userRepository.findByUsername("student1")).thenReturn(student);
        when(enrollmentService.findEnrollmentsByStudent(student)).thenReturn(List.of(enrollment));

        mockMvc.perform(get("/student/grades"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/my-grades"))
                .andExpect(model().attribute("enrollments", List.of(enrollment)));

        verify(enrollmentService).findEnrollmentsByStudent(student);
    }

    @Test
    void testResetPassword_Success() throws Exception {
        when(authentication.getName()).thenReturn("student1");

        mockMvc.perform(post("/student/reset-password")
                .param("password", "newPassword123")
                .param("confirmPassword", "newPassword123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student/dashboard?reset_success=true"));

        verify(userService).resetPassword("student1", "newPassword123");
    }

    @Test
    void testResetPassword_PasswordMismatch() throws Exception {
        when(authentication.getName()).thenReturn("student1");

        mockMvc.perform(post("/student/reset-password")
                .param("password", "newPassword123")
                .param("confirmPassword", "differentPassword"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", "Passwords do not match"));

        verify(userService, never()).resetPassword(anyString(), anyString());
    }

    @Test
    void testResetPassword_Exception() throws Exception {
        when(authentication.getName()).thenReturn("student1");
        doThrow(new RuntimeException("User not found")).when(userService).resetPassword("student1", "newPassword123");

        mockMvc.perform(post("/student/reset-password")
                .param("password", "newPassword123")
                .param("confirmPassword", "newPassword123"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", "Failed to reset password: User not found"));

        verify(userService).resetPassword("student1", "newPassword123");
    }
}
