package com.school.sas.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.school.sas.entity.Attendance;
import com.school.sas.entity.Enrollment;
import com.school.sas.entity.User;
import com.school.sas.repository.UserRepository;
import com.school.sas.service.AttendanceService;
import com.school.sas.service.EnrollmentService;
import com.school.sas.service.UserService;

@Controller
public class StudentController {

    private final UserRepository userRepository;
    private final AttendanceService attendanceService;
    private final EnrollmentService enrollmentService;
    private final UserService userService;

    public StudentController(UserRepository userRepository, AttendanceService attendanceService, EnrollmentService enrollmentService, UserService userService) {
        this.userRepository = userRepository;
        this.attendanceService = attendanceService;
        this.enrollmentService = enrollmentService;
        this.userService = userService;
    }

    @GetMapping("/student/attendance")
    public String showAttendancePage(@RequestParam(required = false) Integer year, @RequestParam(required = false) Integer month, Model model, Authentication authentication) {
        User student = userRepository.findByUsername(authentication.getName());

        // Default to current month if not specified
        java.time.LocalDate now = java.time.LocalDate.now();
        int currentYear = year != null ? year : now.getYear();
        int currentMonth = month != null ? month : now.getMonthValue();

        List<Attendance> attendanceRecords = attendanceService.findAttendanceForStudentInMonth(student, currentYear, currentMonth);

        // Calculate statistics
        long totalDays = attendanceRecords.size();
        long absentDays = attendanceRecords.stream().filter(a -> a.getStatus() == com.school.sas.entity.AttendanceStatus.ABSENT).count();
        long lateDays = attendanceRecords.stream().filter(a -> a.getStatus() == com.school.sas.entity.AttendanceStatus.LATE).count();
        long presentDays = attendanceRecords.stream().filter(a -> a.getStatus() == com.school.sas.entity.AttendanceStatus.PRESENT).count();
        long excusedDays = attendanceRecords.stream().filter(a -> a.getStatus() == com.school.sas.entity.AttendanceStatus.EXCUSED).count();

        double attendancePercentage = totalDays > 0 ? ((double) (presentDays + excusedDays) / totalDays) * 100 : 0;

        model.addAttribute("attendanceRecords", attendanceRecords);
        model.addAttribute("currentYear", currentYear);
        model.addAttribute("currentMonth", currentMonth);
        model.addAttribute("totalDays", totalDays);
        model.addAttribute("absentDays", absentDays);
        model.addAttribute("lateDays", lateDays);
        model.addAttribute("attendancePercentage", Math.round(attendancePercentage * 100.0) / 100.0);

        return "student/attendance";
    }

    @GetMapping("/student/grades")
    public String showMyGradesPage(Model model, Authentication authentication) {
        // Get the currently logged-in student
        User student = userRepository.findByUsername(authentication.getName());

        // Find all their enrollment records (which contain the grades)
        List<Enrollment> enrollments = enrollmentService.findEnrollmentsByStudent(student);

        model.addAttribute("enrollments", enrollments);
        return "student/my-grades";
    }

    @GetMapping("/student/reset-password")
    public String showResetPasswordPage(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("actionUrl", "/student/reset-password");
        return "reset-password";
    }

    @PostMapping("/student/reset-password")
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
