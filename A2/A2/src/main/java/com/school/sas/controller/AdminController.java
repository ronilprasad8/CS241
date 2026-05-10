package com.school.sas.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.school.sas.dto.UserFormDto;
import com.school.sas.entity.Enrollment;
import com.school.sas.entity.SchoolClass;
import com.school.sas.entity.Subject;
import com.school.sas.entity.SubjectCategory;
import com.school.sas.entity.User;
import com.school.sas.entity.UserType;
import com.school.sas.repository.SchoolClassRepository;
import com.school.sas.repository.SubjectRepository;
import com.school.sas.repository.UserRepository;
import com.school.sas.service.AttendanceService;
import com.school.sas.service.EnrollmentService;
import com.school.sas.service.SchoolClassService;
import com.school.sas.service.StudentService;
import com.school.sas.service.SubjectService;
import com.school.sas.service.TeacherService;
import com.school.sas.service.UserService;

@Controller
public class AdminController {

    private final AttendanceService attendanceService;
    private final SubjectService subjectService;
    private final StudentService studentService;
    private final SchoolClassRepository schoolClassRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final EnrollmentService enrollmentService;
    private final TeacherService teacherService;
    private final UserService userService;
    private final SchoolClassService schoolClassService;

    public AdminController(AttendanceService attendanceService, SubjectService subjectService,
                           StudentService studentService, SchoolClassRepository schoolClassRepository,
                           UserRepository userRepository, SubjectRepository subjectRepository,
                           EnrollmentService enrollmentService, TeacherService teacherService,
                           UserService userService, SchoolClassService schoolClassService) {
        this.attendanceService = attendanceService;
        this.subjectService = subjectService;
        this.studentService = studentService;
        this.schoolClassRepository = schoolClassRepository;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.enrollmentService = enrollmentService;
        this.teacherService = teacherService;
        this.userService = userService;
        this.schoolClassService = schoolClassService;
    }

    @GetMapping("/admin/students")
    public String showStudentDataPage(@RequestParam(required = false) Integer year,
                                      @RequestParam(required = false) String stream,
                                      @RequestParam(required = false) String searchTerm,
                                      Model model) {

        List<User> filteredStudents = studentService.searchStudents(year, stream, searchTerm);
        long totalStudents = userRepository.countByUserType(UserType.STUDENT);

        Map<Integer, List<String>> yearStreamMap = schoolClassRepository.findAll().stream()
                .collect(Collectors.groupingBy(SchoolClass::getYearLevel,
                        Collectors.mapping(SchoolClass::getStreamName, Collectors.toList())));

        model.addAttribute("students", filteredStudents);
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("yearStreamMap", yearStreamMap);
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedStream", stream);
        model.addAttribute("searchTerm", searchTerm);

        return "admin/student-data";
    }

    @GetMapping("/admin/teachers")
    public String showTeacherDataPage(@RequestParam(required = false) Long subjectId,
                                      @RequestParam(required = false) String searchTerm,
                                      Model model) {

        List<User> teachers = teacherService.searchTeachers(subjectId, searchTerm);

        Map<Long, List<Subject>> teacherSubjects = new HashMap<>();
        for (User teacher : teachers) {
            teacherSubjects.put(teacher.getId(), subjectService.getSubjectsByTeacher(teacher));
        }

        model.addAttribute("teachers", teachers);
        model.addAttribute("teacherSubjects", teacherSubjects);
        model.addAttribute("subjects", subjectService.getAllSubjects().stream().distinct().collect(Collectors.toList()));
        model.addAttribute("selectedSubjectId", subjectId);
        model.addAttribute("searchTerm", searchTerm);

        return "admin/teacher-data";
    }

    @GetMapping("/admin/users")
    public String showUsersPage(@RequestParam(value = "type", defaultValue = "TEACHER") String type, Model model) {
        List<User> users;
        if ("STUDENT".equalsIgnoreCase(type)) {
            users = userRepository.findAllByUserType(UserType.STUDENT);
            model.addAttribute("activeTab", "STUDENT");
        } else {
            users = userRepository.findAllByUserType(UserType.TEACHER);
            model.addAttribute("activeTab", "TEACHER");
        }
        model.addAttribute("users", users);
        return "admin/users";
    }

    @GetMapping("/admin/attendance")
    public String showAllAttendance(Model model) {
        model.addAttribute("allRecords", attendanceService.findAllAttendanceRecords());
        return "admin/attendance";
    }

    @GetMapping("/admin/students/attendance/{studentId}")
    public String showStudentAttendancePage(@PathVariable Long studentId,
                                            @RequestParam(required = false) Integer year,
                                            @RequestParam(required = false) Integer month,
                                            Model model) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid student Id:" + studentId));

        // Default to current month if not specified
        java.time.LocalDate now = java.time.LocalDate.now();
        int currentYear = year != null ? year : now.getYear();
        int currentMonth = month != null ? month : now.getMonthValue();

        List<com.school.sas.entity.Attendance> attendanceRecords = attendanceService.findAttendanceForStudentInMonth(student, currentYear, currentMonth);

        // Calculate statistics
        long totalDays = attendanceRecords.size();
        long absentDays = attendanceRecords.stream().filter(a -> a.getStatus() == com.school.sas.entity.AttendanceStatus.ABSENT).count();
        long lateDays = attendanceRecords.stream().filter(a -> a.getStatus() == com.school.sas.entity.AttendanceStatus.LATE).count();
        long presentDays = attendanceRecords.stream().filter(a -> a.getStatus() == com.school.sas.entity.AttendanceStatus.PRESENT).count();
        long excusedDays = attendanceRecords.stream().filter(a -> a.getStatus() == com.school.sas.entity.AttendanceStatus.EXCUSED).count();

        double attendancePercentage = totalDays > 0 ? ((double) (presentDays + excusedDays) / totalDays) * 100 : 0;

        model.addAttribute("student", student);
        model.addAttribute("attendanceRecords", attendanceRecords);
        model.addAttribute("currentYear", currentYear);
        model.addAttribute("currentMonth", currentMonth);
        model.addAttribute("totalDays", totalDays);
        model.addAttribute("absentDays", absentDays);
        model.addAttribute("lateDays", lateDays);
        model.addAttribute("attendancePercentage", Math.round(attendancePercentage * 100.0) / 100.0);

        return "admin/student-attendance";
    }

    @GetMapping("/admin/subjects")
    public String showSubjectsPage(Model model) {
        List<Subject> allSubjects = subjectService.getAllSubjects();
        Map<Integer, Map<SubjectCategory, List<Subject>>> subjectsByYearAndCategory = allSubjects.stream()
                .collect(Collectors.groupingBy(Subject::getYearLevel,
                        Collectors.groupingBy(Subject::getCategory)));

        model.addAttribute("subjectsByYear", subjectsByYearAndCategory);
        return "admin/subjects";
    }

    @GetMapping("/admin/users/add")
    public String showAddUserForm() {
        return "admin/user-type-selection"; // Points to our new selection page
    }

    @GetMapping("/admin/users/add-teacher")
    public String showAddTeacherForm(Model model) {
        UserFormDto userForm = new UserFormDto();
        userForm.setUserType("TEACHER");
        model.addAttribute("userForm", userForm);
        return "admin/teacher-form";
    }

    @GetMapping("/admin/users/add-student")
    public String showAddStudentForm(Model model) {
        if (!model.containsAttribute("userForm")) {
            model.addAttribute("userForm", new UserFormDto());
        }

        // Data for the dynamic 'Year' -> 'Stream' dropdowns
        Map<Integer, List<String>> yearStreamMap = schoolClassRepository.findAll().stream()
                .collect(Collectors.groupingBy(SchoolClass::getYearLevel,
                        Collectors.mapping(SchoolClass::getStreamName, Collectors.toList())));
        model.addAttribute("yearStreamMap", yearStreamMap);

        // Data for the dynamic 'Year' -> 'Subject' selection
        List<Subject> allSubjects = subjectService.getAllSubjects();
        Map<Integer, Map<SubjectCategory, List<Subject>>> subjectsByYear = allSubjects.stream()
                .collect(Collectors.groupingBy(Subject::getYearLevel,
                        Collectors.groupingBy(Subject::getCategory)));
        model.addAttribute("subjectsByYear", subjectsByYear);

        // This should point to your single, dynamic form file
        return "admin/student-form";
    }

    @PostMapping("/users/save-student")
    public String saveStudent(@ModelAttribute("userForm") UserFormDto userDto, RedirectAttributes redirectAttributes) {
        try {
            userService.createStudent(userDto);
            redirectAttributes.addFlashAttribute("successMessage", "Student '" + userDto.getUsername() + "' created successfully!");
            return "redirect:/admin/students";
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Username '" + userDto.getUsername() + "' already exists.");
            redirectAttributes.addFlashAttribute("userForm", userDto);
            return "redirect:/admin/users/add-student";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("userForm", userDto);
            return "redirect:/admin/users/add-student";
        }
    }

    @PostMapping("/users/save-teacher")
    public String saveTeacher(@ModelAttribute("userForm") UserFormDto userDto, RedirectAttributes redirectAttributes) {
        try {
            userService.createTeacher(userDto);
            redirectAttributes.addFlashAttribute("successMessage", "Teacher '" + userDto.getUsername() + "' created successfully!");
            return "redirect:/admin/teachers";
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Username '" + userDto.getUsername() + "' already exists.");
            redirectAttributes.addFlashAttribute("userForm", userDto);
            return "redirect:/admin/users/add-teacher";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("userForm", userDto);
            return "redirect:/admin/users/add-teacher";
        }
    }

    @GetMapping("/admin/students/view/{id}")
    public String showStudentProfile(@PathVariable Long id, Model model) {
        User student = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid student Id:" + id));
        List<Enrollment> enrollments = enrollmentService.findEnrollmentsByStudent(student);
        model.addAttribute("student", student);
        model.addAttribute("enrollments", enrollments);
        return "admin/student-profile";
    }

    @GetMapping("/admin/subjects/assign")
    public String showAssignSubjectForm(Model model) {
        model.addAttribute("teachers", userRepository.findAllByUserType(UserType.TEACHER));

        Map<Integer, List<String>> yearStreamMap = Map.of(
                9, List.of("A", "B", "C", "D", "E", "F", "G", "H"),
                10, List.of("A", "B", "C", "D", "E", "F", "G", "H"),
                11, List.of("A", "B", "C", "D", "E", "F", "G"),
                12, List.of("A", "B", "C", "D", "E", "F", "G"),
                13, List.of("A", "B", "C", "D")
        );
        model.addAttribute("yearStreamMap", yearStreamMap);

        List<Subject> allSubjects = subjectService.getAllSubjects();
        Map<Integer, List<Subject>> subjectsByYear = allSubjects.stream()
                .collect(Collectors.groupingBy(Subject::getYearLevel));
        model.addAttribute("subjectsByYear", subjectsByYear);

        return "admin/assign-subject-form";
    }

    @PostMapping("/admin/subjects/assign")
    public String assignSubjectToTeacher(@RequestParam Long subjectId, @RequestParam Long teacherId) {
        subjectService.assignTeacherToSubject(subjectId, teacherId);
        return "redirect:/admin/subjects";
    }

    @GetMapping("/admin/students/edit/{id}")
    public String showEditStudentForm(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid student Id:" + id));

        UserFormDto userDto = new UserFormDto();
        userDto.setId(user.getId());
        userDto.setUserType(user.getUserType().name());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setContactNumber(user.getContactNumber());
        userDto.setAddress(user.getAddress());
        userDto.setDateOfBirth(user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : null);
        userDto.setGender(user.getGender());

        userDto.setCitizenship(user.getCitizenship());
        userDto.setParentGuardian(user.getParentGuardian());
        if (user.getSchoolClass() != null) {
            userDto.setYearLevel(user.getSchoolClass().getYearLevel());
            userDto.setStreamName(user.getSchoolClass().getStreamName());
        }
        List<Enrollment> enrollments = enrollmentService.findEnrollmentsByStudent(user);
        List<Long> subjectIds = enrollments.stream().map(e -> e.getSubject().getId()).collect(Collectors.toList());
        userDto.setSubjectIds(subjectIds);

        // Add data for dropdowns
        Map<Integer, List<String>> yearStreamMap = schoolClassRepository.findAll().stream()
                .collect(Collectors.groupingBy(SchoolClass::getYearLevel,
                        Collectors.mapping(SchoolClass::getStreamName, Collectors.toList())));
        model.addAttribute("yearStreamMap", yearStreamMap);

        List<Subject> allSubjects = subjectService.getAllSubjects();
        Map<Integer, Map<SubjectCategory, List<Subject>>> subjectsByYear = allSubjects.stream()
                .collect(Collectors.groupingBy(Subject::getYearLevel,
                        Collectors.groupingBy(Subject::getCategory)));
        model.addAttribute("subjectsByYear", subjectsByYear);

        model.addAttribute("userForm", userDto);
        return "admin/student-edit-form";
    }

    @GetMapping("/admin/teachers/edit/{id}")
    public String showEditTeacherForm(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid teacher Id:" + id));

        UserFormDto userDto = new UserFormDto();
        userDto.setId(user.getId());
        userDto.setUserType(user.getUserType().name());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setContactNumber(user.getContactNumber());
        userDto.setAddress(user.getAddress());
        userDto.setDateOfBirth(user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : null);
        userDto.setGender(user.getGender());
        userDto.setQualifications(user.getQualifications());

        model.addAttribute("userForm", userDto);
        return "admin/teacher-edit-form";
    }

    @PostMapping("/admin/students/update")
    public String updateStudent(@ModelAttribute("userForm") UserFormDto userDto, RedirectAttributes redirectAttributes) {
        try {
            userService.updateStudent(userDto);
            redirectAttributes.addFlashAttribute("successMessage", "Student '" + userDto.getUsername() + "' updated successfully!");
            return "redirect:/admin/students";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("userForm", userDto);
            return "redirect:/admin/students/edit/" + userDto.getId();
        }
    }

    @PostMapping("/admin/teachers/update")
    public String updateTeacher(@ModelAttribute("userForm") UserFormDto userDto, RedirectAttributes redirectAttributes) {
        try {
            userService.updateTeacher(userDto);
            redirectAttributes.addFlashAttribute("successMessage", "Teacher '" + userDto.getUsername() + "' updated successfully!");
            return "redirect:/admin/teachers";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("userForm", userDto);
            return "redirect:/admin/teachers/edit/" + userDto.getId();
        }
    }

    @PostMapping("/admin/students/delete/{id}")
    public String deleteStudent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteStudentById(id);
            redirectAttributes.addFlashAttribute("userType", "STUDENT");
            return "redirect:/admin/user-deleted";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete student: " + e.getMessage());
            return "redirect:/admin/students";
        }
    }

    @PostMapping("/admin/teachers/delete/{id}")
    public String deleteTeacher(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteTeacherById(id);
            redirectAttributes.addFlashAttribute("userType", "TEACHER");
            return "redirect:/admin/user-deleted";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete teacher: " + e.getMessage());
            return "redirect:/admin/teachers";
        }
    }

    @GetMapping("/admin/classes/assign-teacher")
    public String showAssignClassForm(Model model) {
        model.addAttribute("teachers", userRepository.findAllByUserType(UserType.TEACHER));
        model.addAttribute("classes", schoolClassRepository.findAll());
        return "admin/assign-class-form";
    }

    @PostMapping("/admin/classes/assign-teacher")
    public String assignTeacherToClass(@RequestParam Long classId, @RequestParam Long teacherId, RedirectAttributes redirectAttributes) {
        if (schoolClassService.isTeacherAssignedToClass(teacherId)) {
            User teacher = userRepository.findById(teacherId).orElseThrow();
            SchoolClass currentClass = teacher.getSchoolClass();
            redirectAttributes.addFlashAttribute("confirmMessage",
                "Teacher " + teacher.getFirstName() + " " + teacher.getLastName() +
                " is currently assigned as Year Manager for Year " + currentClass.getYearLevel() + " " + currentClass.getStreamName() +
                ". Do you want to reassign them to a new class?");
            redirectAttributes.addFlashAttribute("classId", classId);
            redirectAttributes.addFlashAttribute("teacherId", teacherId);
            return "redirect:/admin/classes/assign-confirm";
        }

        schoolClassService.assignTeacherToClass(classId, teacherId);
        redirectAttributes.addFlashAttribute("successMessage", "Teacher successfully assigned as Year Manager.");
        return "redirect:/admin/classes/assign-success";
    }

    @GetMapping("/admin/user-deleted")
    public String showUserDeletedPage(Model model) {
        return "admin/user-deleted";
    }

    @GetMapping("/admin/classes/assign-success")
    public String showAssignClassSuccessPage(Model model) {
        return "admin/assign-class-success";
    }

    @GetMapping("/admin/classes/assign-confirm")
    public String showAssignClassConfirmPage(Model model) {
        return "admin/assign-class-confirm";
    }

    @PostMapping("/admin/classes/assign-teacher/confirm")
    public String confirmAssignTeacherToClass(@RequestParam Long classId, @RequestParam Long teacherId, RedirectAttributes redirectAttributes) {
        schoolClassService.assignTeacherToClass(classId, teacherId);
        redirectAttributes.addFlashAttribute("successMessage", "Teacher successfully reassigned as Year Manager.");
        return "redirect:/admin/classes/assign-success";
    }


}
