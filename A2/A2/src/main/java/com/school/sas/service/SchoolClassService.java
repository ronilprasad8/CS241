package com.school.sas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school.sas.entity.SchoolClass;
import com.school.sas.entity.User;
import com.school.sas.repository.SchoolClassRepository;
import com.school.sas.repository.UserRepository;

@Service
public class SchoolClassService {

    private final SchoolClassRepository schoolClassRepository;
    private final UserRepository userRepository;

    public SchoolClassService(SchoolClassRepository schoolClassRepository, UserRepository userRepository) {
        this.schoolClassRepository = schoolClassRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void assignTeacherToClass(Long classId, Long teacherId) {
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found with id: " + classId));
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + teacherId));

        // Clear old assignment if teacher was previously assigned to another class
        if (teacher.getSchoolClass() != null) {
            SchoolClass oldClass = teacher.getSchoolClass();
            oldClass.setFormTeacher(null);
            schoolClassRepository.save(oldClass);
        }

        schoolClass.setFormTeacher(teacher);
        teacher.setSchoolClass(schoolClass);

        schoolClassRepository.save(schoolClass);
        userRepository.save(teacher);
    }

    public boolean isTeacherAssignedToClass(Long teacherId) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + teacherId));
        return teacher.getSchoolClass() != null;
    }
}
