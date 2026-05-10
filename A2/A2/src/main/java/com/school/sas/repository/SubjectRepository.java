package com.school.sas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.school.sas.entity.Subject;
import com.school.sas.entity.User;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    List<Subject> findByTeacher(User teacher);

    Subject findBySubjectNameAndYearLevel(String subjectName, int yearLevel);

    boolean existsBySubjectNameAndYearLevel(String subjectName, int yearLevel);

    List<Subject> findByYearLevel(int yearLevel);
}