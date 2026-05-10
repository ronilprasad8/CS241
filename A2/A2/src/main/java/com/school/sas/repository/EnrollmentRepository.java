package com.school.sas.repository;

import com.school.sas.entity.Enrollment;
import com.school.sas.entity.Subject;
import com.school.sas.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findBySubject(Subject subject);

    Enrollment findBySubjectAndStudent(Subject subject, User student);

    List<Enrollment> findByStudent(User student);
}