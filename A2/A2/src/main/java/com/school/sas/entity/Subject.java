package com.school.sas.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "subjects")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String subjectName;

    private String department;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    @JsonIgnore
    private User teacher;

    @Enumerated(EnumType.STRING)
    private SubjectCategory category;

    private int yearLevel;
}