package com.alsharif.operations.crew.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Crew Visa Type master
 */
@Repository
public interface CrewVisaTypeRepository extends JpaRepository<CrewVisaTypeEntity, String> {

    /**
     * Find active visa types with optional search
     */
    @Query("SELECT vt FROM CrewVisaTypeEntity vt WHERE " +
           "(:activeOnly = false OR vt.active = 'Y') " +
           "AND (:search IS NULL OR UPPER(vt.code) LIKE UPPER(CONCAT('%', :search, '%')) OR UPPER(vt.description) LIKE UPPER(CONCAT('%', :search, '%'))) " +
           "ORDER BY vt.code")
    List<CrewVisaTypeEntity> findActiveVisaTypes(
            @Param("activeOnly") boolean activeOnly,
            @Param("search") String search
    );

    /**
     * Check if visa type code exists
     */
    boolean existsByCode(String code);
}

