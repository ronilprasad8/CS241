package com.school.sas;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.school.sas.dto.UserFormDto;
import com.school.sas.entity.Enrollment;
import com.school.sas.entity.Role;
import com.school.sas.entity.SchoolClass;
import com.school.sas.entity.Subject;
import com.school.sas.entity.User;
import com.school.sas.repository.EnrollmentRepository;
import com.school.sas.repository.RoleRepository;
import com.school.sas.repository.SchoolClassRepository;
import com.school.sas.repository.SubjectRepository;
import com.school.sas.repository.UserRepository;
import com.school.sas.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
/**
 * Unit tests for UserServiceImpl class.
 * Tests cover student and teacher creation, password reset, and username lookup.
 * Uses Mockito for mocking dependencies.
 */
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private SchoolClassRepository schoolClassRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserFormDto studentDto;
    private UserFormDto teacherDto;
    private SchoolClass schoolClass;
    private Role studentRole;
    private Role teacherRole;
    private Subject subject;

    @BeforeEach
    /**
     * Sets up test data before each test method.
     * Initializes DTOs, entities, and mock behaviors for consistent testing.
     */
    void setUp() {
        studentDto = new UserFormDto();
        studentDto.setFirstName("John");
        studentDto.setLastName("Doe");
        studentDto.setUsername("john.doe");
        studentDto.setPassword("password123");
        studentDto.setEmail("john.doe@example.com");
        studentDto.setUserType("STUDENT");
        studentDto.setYearLevel(10);
        studentDto.setStreamName("A");
        studentDto.setSubjectIds(List.of(1L));

        teacherDto = new UserFormDto();
        teacherDto.setFirstName("Jane");
        teacherDto.setLastName("Smith");
        teacherDto.setUsername("jane.smith");
        teacherDto.setPassword("password123");
        teacherDto.setEmail("jane.smith@example.com");
        teacherDto.setUserType("TEACHER");
        teacherDto.setQualifications("MSc Computer Science");

        schoolClass = new SchoolClass();
        schoolClass.setId(1L);
        schoolClass.setYearLevel(10);
        schoolClass.setStreamName("A");

        studentRole = new Role();
        studentRole.setId(1L);
        studentRole.setName("STUDENT");

        teacherRole = new Role();
        teacherRole.setId(2L);
        teacherRole.setName("TEACHER");

        subject = new Subject();
        subject.setId(1L);
        subject.setSubjectName("Mathematics");
    }

    @Test
    /**
     * Tests successful student creation when username is unique.
     * Verifies that user and enrollment are saved correctly.
     */
    void testCreateStudent_Success() {
        // Mock dependencies to simulate successful creation
        when(userRepository.findByUsername("john.doe")).thenReturn(null);
        when(roleRepository.findByName("STUDENT")).thenReturn(studentRole);
        when(schoolClassRepository.findByYearLevelAndStreamName(10, "A")).thenReturn(Optional.of(schoolClass));
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        assertDoesNotThrow(() -> userService.createStudent(studentDto));

        verify(userRepository).save(any(User.class));
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    /**
     * Tests student creation failure when username already exists.
     * Verifies that RuntimeException is thrown with appropriate message.
     */
    void testCreateStudent_UsernameExists() {
        // Mock existing user to trigger username conflict
        when(userRepository.findByUsername("john.doe")).thenReturn(new User());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.createStudent(studentDto));
        assertEquals("Username 'john.doe' already exists.", exception.getMessage());
    }

    @Test
    /**
     * Tests successful teacher creation when username is unique.
     * Verifies that user is saved correctly.
     */
    void testCreateTeacher_Success() {
        // Mock dependencies to simulate successful creation
        when(userRepository.findByUsername("jane.smith")).thenReturn(null);
        when(roleRepository.findByName("TEACHER")).thenReturn(teacherRole);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        assertDoesNotThrow(() -> userService.createTeacher(teacherDto));

        verify(userRepository).save(any(User.class));
    }

    @Test
    /**
     * Tests teacher creation failure when username already exists.
     * Verifies that RuntimeException is thrown with appropriate message.
     */
    void testCreateTeacher_UsernameExists() {
        // Mock existing user to trigger username conflict
        when(userRepository.findByUsername("jane.smith")).thenReturn(new User());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.createTeacher(teacherDto));
        assertEquals("Username 'jane.smith' already exists.", exception.getMessage());
    }

    @Test
    /**
     * Tests successful password reset for existing user.
     * Verifies that password is updated and user is saved.
     */
    void testResetPassword_Success() {
        // Create user object for testing
        User user = new User();
        user.setUsername("john.doe");
        user.setPassword("oldPassword");

        when(userRepository.findByUsername("john.doe")).thenReturn(user);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        assertDoesNotThrow(() -> userService.resetPassword("john.doe", "newPassword"));

        verify(userRepository).save(user);
        assertEquals("encodedNewPassword", user.getPassword());
    }

    @Test
    /**
     * Tests password reset failure when user does not exist.
     * Verifies that RuntimeException is thrown with appropriate message.
     */
    void testResetPassword_UserNotFound() {
        // Mock non-existent user
        when(userRepository.findByUsername("nonexistent")).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.resetPassword("nonexistent", "newPassword"));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    /**
     * Tests finding user by username.
     * Verifies that the correct user object is returned.
     */
    void testFindByUsername() {
        // Create user object for testing
        User user = new User();
        user.setUsername("john.doe");

        when(userRepository.findByUsername("john.doe")).thenReturn(user);

        User result = userService.findByUsername("john.doe");

        assertNotNull(result);
        assertEquals("john.doe", result.getUsername());
    }
}
