package com.school.sas.service;

import com.school.sas.dto.UserFormDto;
import com.school.sas.entity.User;

public interface UserService {
    User findByUsername(String username);
    void createUserFromAdminForm(UserFormDto userDto);
    void createStudent(UserFormDto userDto);
    void createTeacher(UserFormDto userDto);
    void updateStudent(UserFormDto userDto);
    void updateTeacher(UserFormDto userDto);
    void updateUser(UserFormDto userDto);
    void deleteStudentById(Long id);
    void deleteTeacherById(Long id);
    void resetPassword(String username, String newPassword);
}
