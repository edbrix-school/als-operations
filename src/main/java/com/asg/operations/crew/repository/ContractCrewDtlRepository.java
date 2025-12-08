package com.asg.operations.crew.repository;


import com.asg.operations.crew.entity.ContractCrewDtl;
import com.asg.operations.crew.entity.ContractCrewDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository interface for ContractCrewDtl entity
 */
@Repository
public interface ContractCrewDtlRepository extends JpaRepository<ContractCrewDtl, ContractCrewDtlId> {

    /**
     * Find all detail records for a crew
     */
//    List<ContractCrewDtl> findByCrewPoidOrderByDetRowId(Long crewPoid);
    List<ContractCrewDtl> findByIdCrewPoidOrderByIdDetRowId(Long crewPoid);

    ContractCrewDtl findByIdCrewPoidAndIdDetRowId(Long crewPoid, Long detRowId);

    /**
     * Find the maximum detRowId for a given crewPoid
     */
    @Query("select coalesce(max(d.id.detRowId), 0) from ContractCrewDtl d where d.id.crewPoid = :crewPoid")
    Long findMaxDetRowIdByCrewPoid(@Param("crewPoid") Long crewPoid);

    /**
     * Delete all detail records for a crew
     */
    @Modifying
    @Transactional
    void deleteByIdCrewPoid(Long crewPoid);



    /**
     * Delete specific detail records by row IDs
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ContractCrewDtl ccd WHERE ccd.id.crewPoid = :crewPoid AND ccd.id.detRowId IN :detRowIds")
    void deleteByCrewPoidAndDetRowIds(@Param("crewPoid") Long crewPoid, @Param("detRowIds") List<Long> detRowIds);

    /**
     * Check if detail record exists
     */
//   boolean existsByCrewPoidAndDetRowId(Long crewPoid, Long detRowId);
    boolean existsByIdCrewPoidAndIdDetRowId(Long crewPoid, Long detRowId);

}

