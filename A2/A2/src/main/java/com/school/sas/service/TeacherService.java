package com.school.sas.service;

import com.school.sas.entity.User;
import com.school.sas.entity.UserType;
import com.school.sas.repository.UserRepository;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeacherService {

    private final UserRepository userRepository;

    public TeacherService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> searchTeachers(Long subjectId, String searchTerm) {
        // This is the updated line - Specification.where() is removed
        Specification<User> spec = (root, query, cb) -> cb.equal(root.get("userType"), UserType.TEACHER);

        if (subjectId != null) {
            spec = spec.and((root, query, cb) -> {
                query.distinct(true); // Prevent duplicate teachers if they teach multiple subjects
                return cb.equal(root.join("subjectsTaught", JoinType.LEFT).get("id"), subjectId);
            });
        }

        if (searchTerm != null && !searchTerm.isEmpty()) {
            String likePattern = "%" + searchTerm.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("firstName")), likePattern),
                    cb.like(cb.lower(root.get("lastName")), likePattern),
                    cb.like(cb.lower(root.get("username")), likePattern)
            ));
        }

        return userRepository.findAll(spec);
    }
}