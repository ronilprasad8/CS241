package com.school.sas.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserFormDto {

    // Common fields
    private Long id;
    private String userType;
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String email;
    private String contactNumber;
    private String address;
    private String dateOfBirth; // Also used for teachers
    private String gender;      // Also used for teachers

    // Student-specific fields
    private Integer indexNumber;
    private Integer yearLevel;
    private String streamName;
    private String citizenship;
    private String parentGuardian;
    private List<Long> subjectIds;

    // Teacher-specific fields
    private String qualifications;
}