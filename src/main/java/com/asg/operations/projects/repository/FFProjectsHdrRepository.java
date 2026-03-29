package com.asg.operations.projects.repository;

import com.asg.operations.projects.entity.FFProjectsHdr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FFProjectsHdrRepository extends JpaRepository<FFProjectsHdr, Long>, JpaSpecificationExecutor<FFProjectsHdr> {

    Optional<FFProjectsHdr> findByTransactionPoidAndDeleted(Long transactionPoid, String deleted);

    Optional<FFProjectsHdr> findByDocRefAndDeleted(String docRef, String deleted);

    boolean existsByDocRefAndDeleted(String docRef, String deleted);

    @Query("SELECT MAX(CAST(SUBSTRING(p.docRef, LENGTH(:prefix) + 1, 5) AS int)) FROM FFProjectsHdr p WHERE p.docRef LIKE CONCAT(:prefix, '%')")
    Integer findMaxSequenceByPrefix(@Param("prefix") String prefix);
}
