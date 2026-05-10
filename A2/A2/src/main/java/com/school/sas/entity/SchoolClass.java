package com.school.sas.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "classes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"yearLevel", "streamName"})
})
public class SchoolClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int yearLevel;

    @Column(nullable = false)
    private String streamName;

    @OneToOne
    @JoinColumn(name = "teacher_id", unique = true)
    private User formTeacher;
}