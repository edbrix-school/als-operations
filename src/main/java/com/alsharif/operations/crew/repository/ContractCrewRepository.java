package com.alsharif.operations.crew.repository;


import com.alsharif.operations.crew.entity.ContractCrew;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Repository interface for ContractCrew entity
 */
@Repository
public interface ContractCrewRepository extends JpaRepository<ContractCrew, Long> {

    /**
     * Find crew by code
     */
   // Optional<ContractCrew> findByCrewCode(String crewCode);

    /**
     * Check if crew code exists
     */
  //  boolean existsByCrewCode(String crewCode);

    /**
     * Search crews with filters
     */
//    @Query("SELECT cc FROM ContractCrew cc " +
//           "WHERE (:crewCode IS NULL OR UPPER(cc.crewCode) LIKE UPPER(CONCAT('%', :crewCode, '%'))) " +
//           "AND (:crewName IS NULL OR UPPER(cc.crewName) LIKE UPPER(CONCAT('%', :crewName, '%'))) " +
//           "AND (:nationalityPoid IS NULL OR cc.crewNationPoid = :nationalityPoid) " +
//           "AND (:company IS NULL OR UPPER(cc.crewCompany) LIKE UPPER(CONCAT('%', :company, '%'))) " +
//           "AND (:active IS NULL OR cc.active = :active) " +
//           "AND (:companyPoid IS NULL OR cc.companyPoid = :companyPoid)")
//    Page<ContractCrew> searchCrews(
//            @Param("crewCode") String crewCode,
//            @Param("crewName") String crewName,
//            @Param("nationalityPoid") BigDecimal nationalityPoid,
//            @Param("company") String company,
//            @Param("active") String active,
//            @Param("companyPoid") BigDecimal companyPoid,
//            Pageable pageable
//    );

    @Query("SELECT cc FROM ContractCrew cc " +
            "WHERE (:crewName IS NULL OR UPPER(cc.crewName) LIKE UPPER(CONCAT('%', :crewName, '%'))) " +
            "AND (:nationalityPoid IS NULL OR cc.crewNationPoid = :nationalityPoid) " +
            "AND (:company IS NULL OR UPPER(cc.crewCompany) LIKE UPPER(CONCAT('%', :company, '%'))) " +
            "AND (:active IS NULL OR cc.active = :active) " +
            "AND (:companyPoid IS NULL OR cc.companyPoid = :companyPoid)")
    Page<ContractCrew> searchCrews(
            @Param("crewName") String crewName,
            @Param("nationalityPoid") BigDecimal nationalityPoid,
            @Param("company") String company,
            @Param("active") String active,
            @Param("companyPoid") Long companyPoid,
            Pageable pageable
    );

    /**
     * Find by POID with company filter
     */
    @Query("SELECT cc FROM ContractCrew cc WHERE cc.crewPoid = :crewPoid " +
           "AND (:companyPoid IS NULL OR cc.companyPoid = :companyPoid)")
    Optional<ContractCrew> findByCrewPoidAndCompanyPoid(
            @Param("crewPoid") Long crewPoid,
            @Param("companyPoid") Long companyPoid
    );

    Optional<ContractCrew> findByCrewPoid(
            @Param("crewPoid") Long crewPoid
    );
}

