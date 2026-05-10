package com.school.sas.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.school.sas.entity.Attendance;
import com.school.sas.entity.User;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByStudentOrderByDateDesc(User student);

    List<Attendance> findByDateAndStudentIn(LocalDate date, List<User> students);

    List<Attendance> findByStudentAndDateBetween(User student, LocalDate startDate, LocalDate endDate);
}
