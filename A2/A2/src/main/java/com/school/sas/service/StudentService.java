package com.school.sas.service;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.school.sas.entity.Role;
import com.school.sas.entity.SchoolClass;
import com.school.sas.entity.User;
import com.school.sas.entity.UserType;
import com.school.sas.repository.RoleRepository;
import com.school.sas.repository.SchoolClassRepository;
import com.school.sas.repository.UserRepository;

@Service
public class StudentService {

    private final UserRepository userRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentService(UserRepository userRepository, SchoolClassRepository schoolClassRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.schoolClassRepository = schoolClassRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> searchStudents(Integer year, String stream, String searchTerm) {
        Specification<User> spec = (root, query, cb) -> cb.equal(root.get("userType"), UserType.STUDENT);

        if (year != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("schoolClass").get("yearLevel"), year));
        }
        if (stream != null && !stream.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("schoolClass").get("streamName"), stream));
        }
        if (searchTerm != null && !searchTerm.isEmpty()) {
            String likePattern = "%" + searchTerm.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("firstName")), likePattern),
                    cb.like(cb.lower(root.get("lastName")), likePattern),
                    cb.like(cb.lower(root.get("studentIdNumber")), likePattern)
            ));
        }

        return userRepository.findAll(spec);
    }

    public void saveStudent(User student, Integer year, String stream) {
        SchoolClass schoolClass = schoolClassRepository.findByYearLevelAndStreamName(year, stream)
                .orElseThrow(() -> new RuntimeException("Invalid Class specified"));
        student.setSchoolClass(schoolClass);

        String username = (student.getFirstName() + "." + student.getLastName()).toLowerCase();
        student.setUsername(username);
        student.setPassword(passwordEncoder.encode("default123"));
        student.setUserType(UserType.STUDENT);

        // This line sets the student ID to be the same as the username.
        student.setStudentIdNumber(student.getUsername());

        Role studentRole = roleRepository.findByName("STUDENT");
        student.setRoles(List.of(studentRole));

        userRepository.save(student);
    }
}