package com.school.sas.service;

import com.school.sas.entity.Enrollment;
import com.school.sas.entity.User;
import com.school.sas.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    public List<Enrollment> findEnrollmentsByStudent(User student) {
        return enrollmentRepository.findByStudent(student);
    }
}