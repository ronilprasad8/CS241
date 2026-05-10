package com.school.sas.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school.sas.dto.UserFormDto;
import com.school.sas.entity.Enrollment;
import com.school.sas.entity.Role;
import com.school.sas.entity.SchoolClass;
import com.school.sas.entity.Subject;
import com.school.sas.entity.User;
import com.school.sas.entity.UserType;
import com.school.sas.repository.AttendanceRepository;
import com.school.sas.repository.EnrollmentRepository;
import com.school.sas.repository.RoleRepository;
import com.school.sas.repository.SchoolClassRepository;
import com.school.sas.repository.SubjectRepository;
import com.school.sas.repository.UserRepository;
import com.school.sas.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final SubjectRepository subjectRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
                           SchoolClassRepository schoolClassRepository, SubjectRepository subjectRepository,
                           EnrollmentRepository enrollmentRepository, AttendanceRepository attendanceRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.schoolClassRepository = schoolClassRepository;
        this.subjectRepository = subjectRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.attendanceRepository = attendanceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional
    public void createUserFromAdminForm(UserFormDto dto) {
        User existingUser = userRepository.findByUsername(dto.getUsername());
        if (existingUser != null) {
            throw new RuntimeException("Username '" + dto.getUsername() + "' already exists.");
        }

        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setAddress(dto.getAddress());
        user.setContactNumber(dto.getContactNumber());
        if (dto.getDateOfBirth() != null && !dto.getDateOfBirth().isEmpty()) {
            user.setDateOfBirth(LocalDate.parse(dto.getDateOfBirth()));
        }
        user.setGender(dto.getGender());

        if ("STUDENT".equals(dto.getUserType())) {
            Role studentRole = roleRepository.findByName("STUDENT");
            if (studentRole == null) {
                throw new RuntimeException("Student role not found. Please ensure the database is initialized.");
            }
            user.setUserType(UserType.STUDENT);
            user.setRoles(List.of(studentRole));
            user.setIndexNumber(dto.getIndexNumber());
            user.setCitizenship(dto.getCitizenship());
            user.setParentGuardian(dto.getParentGuardian());

            if (dto.getYearLevel() == null) {
                throw new RuntimeException("Year level must be selected for student.");
            }
            if (dto.getStreamName() == null || dto.getStreamName().isEmpty()) {
                throw new RuntimeException("Stream must be selected for student.");
            }

            SchoolClass sc = schoolClassRepository.findByYearLevelAndStreamName(dto.getYearLevel(), dto.getStreamName())
                    .orElseThrow(() -> new RuntimeException("Class not found for Year " + dto.getYearLevel() + " Stream " + dto.getStreamName()));
            user.setSchoolClass(sc);

            User savedStudent = userRepository.save(user);

            if (dto.getSubjectIds() != null) {
                for (Long subjectId : dto.getSubjectIds()) {
                    Subject subject = subjectRepository.findById(subjectId).orElseThrow();
                    Enrollment enrollment = new Enrollment();
                    enrollment.setStudent(savedStudent);
                    enrollment.setSubject(subject);
                    enrollment.setGrade("N/A");
                    enrollmentRepository.save(enrollment);
                }
            }
        } else if ("TEACHER".equals(dto.getUserType())) {
            Role teacherRole = roleRepository.findByName("TEACHER");
            if (teacherRole == null) {
                throw new RuntimeException("Teacher role not found. Please ensure the database is initialized.");
            }
            user.setUserType(UserType.TEACHER);
            user.setRoles(List.of(teacherRole));
            user.setQualifications(dto.getQualifications());
            userRepository.save(user);
        }
    }

    @Override
    @Transactional
    public void updateStudent(UserFormDto dto) {
        User user = userRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check username uniqueness if changed
        if (!user.getUsername().equals(dto.getUsername())) {
            User existing = userRepository.findByUsername(dto.getUsername());
            if (existing != null) {
                throw new RuntimeException("Username '" + dto.getUsername() + "' already exists.");
            }
            user.setUsername(dto.getUsername());
        }

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        user.setEmail(dto.getEmail());
        user.setAddress(dto.getAddress());
        user.setContactNumber(dto.getContactNumber());
        if (dto.getDateOfBirth() != null && !dto.getDateOfBirth().isEmpty()) {
            user.setDateOfBirth(LocalDate.parse(dto.getDateOfBirth()));
        }
        user.setGender(dto.getGender());

        user.setIndexNumber(dto.getIndexNumber());
        user.setCitizenship(dto.getCitizenship());
        user.setParentGuardian(dto.getParentGuardian());

        if (dto.getYearLevel() != null && dto.getStreamName() != null && !dto.getStreamName().isEmpty()) {
            SchoolClass sc = schoolClassRepository.findByYearLevelAndStreamName(dto.getYearLevel(), dto.getStreamName())
                    .orElseThrow(() -> new RuntimeException("Class not found for Year " + dto.getYearLevel() + " Stream " + dto.getStreamName()));
            user.setSchoolClass(sc);
        }

        // Update enrollments if subjectIds provided
        if (dto.getSubjectIds() != null) {
            // Remove old enrollments
            enrollmentRepository.deleteAll(enrollmentRepository.findByStudent(user));
            // Add new ones
            for (Long subjectId : dto.getSubjectIds()) {
                Subject subject = subjectRepository.findById(subjectId).orElseThrow();
                Enrollment enrollment = new Enrollment();
                enrollment.setStudent(user);
                enrollment.setSubject(subject);
                enrollment.setGrade("N/A");
                enrollmentRepository.save(enrollment);
            }
        }

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateTeacher(UserFormDto dto) {
        User user = userRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check username uniqueness if changed
        if (!user.getUsername().equals(dto.getUsername())) {
            User existing = userRepository.findByUsername(dto.getUsername());
            if (existing != null) {
                throw new RuntimeException("Username '" + dto.getUsername() + "' already exists.");
            }
            user.setUsername(dto.getUsername());
        }

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        user.setEmail(dto.getEmail());
        user.setAddress(dto.getAddress());
        user.setContactNumber(dto.getContactNumber());
        if (dto.getDateOfBirth() != null && !dto.getDateOfBirth().isEmpty()) {
            user.setDateOfBirth(LocalDate.parse(dto.getDateOfBirth()));
        }
        user.setGender(dto.getGender());
        user.setQualifications(dto.getQualifications());

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateUser(UserFormDto dto) {
        if ("STUDENT".equals(dto.getUserType())) {
            updateStudent(dto);
        } else {
            updateTeacher(dto);
        }
    }

    @Override
    @Transactional
    public void createStudent(UserFormDto dto) {
        User existingUser = userRepository.findByUsername(dto.getUsername());
        if (existingUser != null) {
            throw new RuntimeException("Username '" + dto.getUsername() + "' already exists.");
        }

        Role studentRole = roleRepository.findByName("STUDENT");
        if (studentRole == null) {
            studentRole = new Role();
            studentRole.setName("STUDENT");
            roleRepository.save(studentRole);
        }

        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setAddress(dto.getAddress());
        user.setContactNumber(dto.getContactNumber());
        if (dto.getDateOfBirth() != null && !dto.getDateOfBirth().isEmpty()) {
            user.setDateOfBirth(LocalDate.parse(dto.getDateOfBirth()));
        }
        user.setGender(dto.getGender());
        user.setUserType(UserType.STUDENT);
        user.setRoles(List.of(studentRole));
        user.setIndexNumber(dto.getIndexNumber());
        user.setCitizenship(dto.getCitizenship());
        user.setParentGuardian(dto.getParentGuardian());

        if (dto.getYearLevel() == null) {
            throw new RuntimeException("Year level must be selected for student.");
        }
        if (dto.getStreamName() == null || dto.getStreamName().isEmpty()) {
            throw new RuntimeException("Stream must be selected for student.");
        }

        SchoolClass sc = schoolClassRepository.findByYearLevelAndStreamName(dto.getYearLevel(), dto.getStreamName())
                .orElseThrow(() -> new RuntimeException("Class not found for Year " + dto.getYearLevel() + " Stream " + dto.getStreamName()));
        user.setSchoolClass(sc);

        User savedStudent = userRepository.save(user);

        if (dto.getSubjectIds() != null) {
            for (Long subjectId : dto.getSubjectIds()) {
                Subject subject = subjectRepository.findById(subjectId).orElseThrow();
                Enrollment enrollment = new Enrollment();
                enrollment.setStudent(savedStudent);
                enrollment.setSubject(subject);
                enrollment.setGrade("N/A");
                enrollmentRepository.save(enrollment);
            }
        }
    }

    @Override
    @Transactional
    public void createTeacher(UserFormDto dto) {
        User existingUser = userRepository.findByUsername(dto.getUsername());
        if (existingUser != null) {
            throw new RuntimeException("Username '" + dto.getUsername() + "' already exists.");
        }

        Role teacherRole = roleRepository.findByName("TEACHER");
        if (teacherRole == null) {
            teacherRole = new Role();
            teacherRole.setName("TEACHER");
            roleRepository.save(teacherRole);
        }

        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setAddress(dto.getAddress());
        user.setContactNumber(dto.getContactNumber());
        if (dto.getDateOfBirth() != null && !dto.getDateOfBirth().isEmpty()) {
            user.setDateOfBirth(LocalDate.parse(dto.getDateOfBirth()));
        }
        user.setGender(dto.getGender());
        user.setUserType(UserType.TEACHER);
        user.setRoles(List.of(teacherRole));
        user.setQualifications(dto.getQualifications());
        userRepository.save(user);
    }

    @Override
    public void deleteStudentById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

        // Remove all attendance records for the student before deleting
        attendanceRepository.deleteAll(attendanceRepository.findByStudentOrderByDateDesc(user));

        // Remove all enrollments for the student before deleting
        enrollmentRepository.deleteAll(enrollmentRepository.findByStudent(user));

        userRepository.delete(user);
    }

    @Override
    public void deleteTeacherById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

        // Remove teacher assignment from all subjects before deleting the teacher
        List<Subject> subjects = subjectRepository.findByTeacher(user);
        for (Subject subject : subjects) {
            subject.setTeacher(null);
            subjectRepository.save(subject);
        }

        // Remove teacher assignment from any class where they are the form teacher
        if (user.getSchoolClass() != null) {
            SchoolClass schoolClass = user.getSchoolClass();
            schoolClass.setFormTeacher(null);
            schoolClassRepository.save(schoolClass);
        }

        userRepository.delete(user);
    }

    @Override
    public void resetPassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
