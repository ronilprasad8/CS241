package com.school.sas.repository;

import com.school.sas.entity.User;
import com.school.sas.entity.UserType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    User findByUsername(String username);

    List<User> findBySchoolClassFormTeacher(User formTeacher);

    List<User> findAllByUserType(UserType userType);

    long countByUserType(UserType userType);
}