package com.school.sas;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.school.sas.entity.Role;
import com.school.sas.entity.SchoolClass;
import com.school.sas.entity.Subject;
import com.school.sas.entity.SubjectCategory;
import com.school.sas.entity.User;
import com.school.sas.entity.UserType;
import com.school.sas.repository.RoleRepository;
import com.school.sas.repository.SchoolClassRepository;
import com.school.sas.repository.SubjectRepository;
import com.school.sas.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SubjectRepository subjectRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, RoleRepository roleRepository,
                           SubjectRepository subjectRepository, SchoolClassRepository schoolClassRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.subjectRepository = subjectRepository;
        this.schoolClassRepository = schoolClassRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Create Roles and Admin User
        Role adminRole = findOrCreateRole("ADMIN");
        if (userRepository.findByUsername("admin") == null) {
            User admin = new User();
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRoles(List.of(adminRole));
            admin.setUserType(UserType.ADMIN);
            userRepository.save(admin);
        }
        findOrCreateRole("TEACHER");
        findOrCreateRole("STUDENT");

        // Create all subjects
        createSubjects();

        // Create all classes
        createClasses();
    }

    private void createSubjects() {
        // --- Subjects for Year 9 & 10 ---
        List<String> juniorCompulsory = List.of("Mathematics", "English", "Basic Science", "Social Science");
        List<String> juniorElectives = List.of("Agriculture", "Commercial Studies", "Home Economics", "Basic Technology", "Office Technology");
        List<String> juniorLanguages = List.of("i-Taukei", "Rotuman", "Hindi", "Urdu");

        createSubjectsForYear(9, juniorCompulsory, juniorElectives, juniorLanguages);
        createSubjectsForYear(10, juniorCompulsory, juniorElectives, juniorLanguages);

        // --- Subjects for Year 11, 12 & 13 ---
        List<String> seniorCompulsory = List.of("Mathematics", "English");
        List<String> seniorElectives = List.of(
                "Biology", "Chemistry", "Physics", "Accounting", "Economics", "Geography",
                "History", "Computer Studies", "Home Economics", "Office Technology",
                "Applied Technology", "Technical Drawing", "Agricultural Science"
        );
        List<String> seniorLanguages = List.of("i-Taukei", "Rotuman", "Hindi", "Urdu");

        createSubjectsForYear(11, seniorCompulsory, seniorElectives, seniorLanguages);
        createSubjectsForYear(12, seniorCompulsory, seniorElectives, seniorLanguages);
        createSubjectsForYear(13, seniorCompulsory, seniorElectives, seniorLanguages);
    }

    private void createSubjectsForYear(int year, List<String> compulsory, List<String> electives, List<String> languages) {
        compulsory.forEach(name -> findOrCreateSubject(name, year, SubjectCategory.COMPULSORY));
        electives.forEach(name -> findOrCreateSubject(name, year, SubjectCategory.ELECTIVE));
        languages.forEach(name -> findOrCreateSubject(name, year, SubjectCategory.LANGUAGE));
    }

    private void findOrCreateSubject(String name, int year, SubjectCategory category) {
        if (!subjectRepository.existsBySubjectNameAndYearLevel(name, year)) {
            Subject subject = new Subject();
            subject.setSubjectName(name);
            subject.setYearLevel(year);
            subject.setCategory(category);
            subjectRepository.save(subject);
            System.out.println(">>> Created Subject: " + name + " for Year " + year);
        }
    }

    private void createClasses() {
        // Create classes for each year with streams
        createClassesForYear(9, List.of("A", "B", "C", "D", "E", "F", "G", "H"));
        createClassesForYear(10, List.of("A", "B", "C", "D", "E", "F", "G", "H"));
        createClassesForYear(11, List.of("A", "B", "C", "D", "E", "F", "G"));
        createClassesForYear(12, List.of("A", "B", "C", "D", "E", "F", "G"));
        createClassesForYear(13, List.of("A", "B", "C", "D"));
    }

    private void createClassesForYear(int year, List<String> streams) {
        streams.forEach(stream -> findOrCreateClass(year, stream));
    }

    private void findOrCreateClass(int year, String stream) {
        if (!schoolClassRepository.existsByYearLevelAndStreamName(year, stream)) {
            SchoolClass schoolClass = new SchoolClass();
            schoolClass.setYearLevel(year);
            schoolClass.setStreamName(stream);
            schoolClassRepository.save(schoolClass);
            System.out.println(">>> Created Class: Year " + year + " " + stream);
        }
    }

    private Role findOrCreateRole(String roleName) {
        Role role = roleRepository.findByName(roleName);
        if (role == null) {
            role = new Role();
            role.setName(roleName);
            role = roleRepository.save(role);
        }
        return role;
    }
}
