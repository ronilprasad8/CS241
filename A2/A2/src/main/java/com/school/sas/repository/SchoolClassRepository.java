package com.school.sas.repository;

import com.school.sas.entity.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {
    Optional<SchoolClass> findByYearLevelAndStreamName(int yearLevel, String streamName);

    boolean existsByYearLevelAndStreamName(int yearLevel, String streamName);
}
