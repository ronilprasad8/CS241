package com.school.sas.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.school.sas.entity.Subject;
import com.school.sas.entity.User;
import com.school.sas.repository.SubjectRepository;
import com.school.sas.repository.UserRepository;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    public SubjectService(SubjectRepository subjectRepository, UserRepository userRepository) {
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
    }

    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    public void saveSubject(Subject subject) {
        subjectRepository.save(subject);
    }

    public void assignTeacherToSubject(Long subjectId, Long teacherId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        subject.setTeacher(teacher);
        subjectRepository.save(subject);
    }

    public List<Subject> getSubjectsByTeacher(User teacher) {
        return subjectRepository.findByTeacher(teacher);
    }

    public List<Subject> getSubjectsByYearLevel(int yearLevel) {
        return subjectRepository.findByYearLevel(yearLevel);
    }
}