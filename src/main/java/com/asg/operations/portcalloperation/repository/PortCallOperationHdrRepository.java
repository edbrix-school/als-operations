package com.asg.operations.portcalloperation.repository;

import com.asg.operations.portcalloperation.entity.PortCallOperationHdr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PortCallOperationHdrRepository extends JpaRepository<PortCallOperationHdr, Long> {

    Optional<PortCallOperationHdr> findByDocRef(String docRef);

    @Query("SELECT COUNT(p) > 0 FROM PortCallOperationHdr p WHERE " +
            "p.docRef = :docRef AND " +
            "(p.deleted IS NULL OR p.deleted != 'Y') AND " +
            "(:id IS NULL OR p.transactionPoid != :id)")
    boolean existsByDocRefAndNotDeleted(@Param("docRef") String docRef, @Param("id") Long id);

}
