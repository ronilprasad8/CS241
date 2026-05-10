package com.school.sas.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.school.sas.entity.Attendance;
import com.school.sas.entity.AttendanceStatus;
import com.school.sas.entity.User;
import com.school.sas.repository.AttendanceRepository;
import com.school.sas.repository.UserRepository;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;

    public AttendanceService(AttendanceRepository attendanceRepository, UserRepository userRepository) {
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
    }

    public void saveAttendanceRecords(Map<Long, AttendanceStatus> attendanceData) {
        LocalDate today = LocalDate.now();

        attendanceData.forEach((studentId, status) -> {
            User student = userRepository.findById(studentId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid student Id:" + studentId));

            // For simplicity, this creates a new record each time.
            // A more advanced version would check if a record for this student and date already exists.
            Attendance record = new Attendance();
            record.setStudent(student);
            record.setStatus(status);
            record.setDate(today);
            attendanceRepository.save(record);
        });
    }
    public List<Attendance> findAttendanceForStudent(User student) {
        return attendanceRepository.findByStudentOrderByDateDesc(student);
    }
    public List<Attendance> findAllAttendanceRecords() {
        return attendanceRepository.findAll();
    }

    public List<Attendance> findAttendanceByDateAndStudents(LocalDate date, List<User> students) {
        return attendanceRepository.findByDateAndStudentIn(date, students);
    }

    public void saveAttendanceRecordsForDate(LocalDate date, Map<Long, AttendanceStatus> attendanceData) {
        attendanceData.forEach((studentId, status) -> {
            User student = userRepository.findById(studentId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid student Id:" + studentId));

            // Check if record already exists for this student and date
            Attendance existingRecord = attendanceRepository.findByDateAndStudentIn(date, List.of(student))
                    .stream()
                    .filter(a -> a.getStudent().getId().equals(studentId))
                    .findFirst()
                    .orElse(null);

            if (existingRecord != null) {
                existingRecord.setStatus(status);
                attendanceRepository.save(existingRecord);
            } else {
                Attendance record = new Attendance();
                record.setStudent(student);
                record.setStatus(status);
                record.setDate(date);
                attendanceRepository.save(record);
            }
        });
    }

    public List<Attendance> findAttendanceForStudentInMonth(User student, int year, int month) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());
        return attendanceRepository.findByStudentAndDateBetween(student, startOfMonth, endOfMonth);
    }
}