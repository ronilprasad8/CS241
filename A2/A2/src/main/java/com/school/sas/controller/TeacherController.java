package com.school.sas.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.school.sas.entity.Attendance;
import com.school.sas.entity.AttendanceStatus;
import com.school.sas.entity.Enrollment;
import com.school.sas.entity.Subject;
import com.school.sas.entity.User;
import com.school.sas.repository.EnrollmentRepository;
import com.school.sas.repository.SubjectRepository;
import com.school.sas.repository.UserRepository;
import com.school.sas.service.AttendanceService;
import com.school.sas.service.UserService;

@Controller
public class TeacherController {

    private final UserRepository userRepository;
    private final AttendanceService attendanceService;
    private final SubjectRepository subjectRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserService userService;

    public TeacherController(UserRepository userRepository, AttendanceService attendanceService, SubjectRepository subjectRepository, EnrollmentRepository enrollmentRepository, UserService userService) {
        this.userRepository = userRepository;
        this.attendanceService = attendanceService;
        this.subjectRepository = subjectRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userService = userService;
    }

    @GetMapping("/teacher/attendance")
    public String showAttendancePage(Model model, Authentication authentication,
                                     @RequestParam(value = "date", required = false) String dateParam) {
        User teacher = userRepository.findByUsername(authentication.getName());
        List<User> students = userRepository.findBySchoolClassFormTeacher(teacher);

        String className = "No Class Assigned";
        if (!students.isEmpty()) {
            className = "Year " + students.get(0).getSchoolClass().getYearLevel() + students.get(0).getSchoolClass().getStreamName();
        }

        LocalDate attendanceDate = dateParam != null ? LocalDate.parse(dateParam) : LocalDate.now();

        // Load existing attendance records for the date
        List<Attendance> attendanceRecords = attendanceService.findAttendanceByDateAndStudents(attendanceDate, students);

        // Create attendance map
        Map<Long, AttendanceStatus> attendanceMap = new HashMap<>();
        for (Attendance record : attendanceRecords) {
            attendanceMap.put(record.getStudent().getId(), record.getStatus());
        }

        model.addAttribute("students", students);
        model.addAttribute("className", className);
        model.addAttribute("attendanceDate", attendanceDate);
        model.addAttribute("attendanceMap", attendanceMap);

        return "teacher/attendance";
    }



    @PostMapping("/teacher/attendance")
    public String saveAttendance(@RequestParam Map<String, String> formData) {
        String dateStr = formData.get("date");
        LocalDate attendanceDate = dateStr != null ? LocalDate.parse(dateStr) : LocalDate.now();

        Map<Long, AttendanceStatus> attendanceData = formData.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("attendance-"))
                .collect(Collectors.toMap(
                        entry -> Long.parseLong(entry.getKey().replace("attendance-", "")),
                        entry -> AttendanceStatus.valueOf(entry.getValue())
                ));

        attendanceService.saveAttendanceRecordsForDate(attendanceDate, attendanceData);

        return "redirect:/teacher/dashboard?attendance_saved=true";
    }

    @GetMapping("/teacher/grades")
    public String showSubjectsForGrading(Model model, Authentication authentication) {
        User teacher = userRepository.findByUsername(authentication.getName());
        List<Subject> subjects = subjectRepository.findByTeacher(teacher);
        model.addAttribute("subjects", subjects);
        return "teacher/grades-subjects";
    }

    @GetMapping("/teacher/grades/{subjectId}")
    public String showGradeEntryForm(@PathVariable Long subjectId, Model model) {
        Subject subject = subjectRepository.findById(subjectId).orElseThrow(() -> new IllegalArgumentException("Invalid subject Id:" + subjectId));
        List<Enrollment> enrollments = enrollmentRepository.findBySubject(subject);
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("subject", subject);
        return "teacher/grade-entry";
    }

    @PostMapping("/teacher/grades/save")
    public String saveGrades(@RequestParam Map<String, String> formData) {
        for (Map.Entry<String, String> entry : formData.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key.startsWith("examType-")) {
                Long enrollmentId = Long.parseLong(key.replace("examType-", ""));
                Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElse(null);
                if (enrollment != null) {
                    enrollment.setExamType(value);
                    enrollmentRepository.save(enrollment);
                }
            } else if (key.startsWith("marksGained-")) {
                Long enrollmentId = Long.parseLong(key.replace("marksGained-", ""));
                Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElse(null);
                if (enrollment != null && !value.isEmpty()) {
                    enrollment.setMarksGained(Integer.parseInt(value));
                    enrollmentRepository.save(enrollment);
                }
            } else if (key.startsWith("totalMarks-")) {
                Long enrollmentId = Long.parseLong(key.replace("totalMarks-", ""));
                Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElse(null);
                if (enrollment != null && !value.isEmpty()) {
                    enrollment.setTotalMarks(Integer.parseInt(value));
                    enrollmentRepository.save(enrollment);
                }
            }
        }
        return "redirect:/teacher/grades";
    }

    @GetMapping("/teacher/reset-password")
    public String showResetPasswordPage(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("actionUrl", "/teacher/reset-password");
        return "reset-password";
    }

    @PostMapping("/teacher/reset-password")
    public String resetPassword(@RequestParam String password, @RequestParam String confirmPassword, Model model, Authentication authentication) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            model.addAttribute("username", authentication.getName());
            return "reset-password";
        }
        try {
            userService.resetPassword(authentication.getName(), password);
            model.addAttribute("success", "Password Successfully Changed");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to reset password: " + e.getMessage());
        }
        model.addAttribute("username", authentication.getName());
        return "reset-password";
    }
}
