package com.asg.operations.projects.repository;

import com.asg.operations.projects.entity.FFProjectsChargesDtl;
import com.asg.operations.projects.key.FFProjectsChargesDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FFProjectsChargesDtlRepository extends JpaRepository<FFProjectsChargesDtl, FFProjectsChargesDtlId> {

    List<FFProjectsChargesDtl> findByTransactionPoidAndDeleted(Long transactionPoid, String deleted);

    Optional<FFProjectsChargesDtl> findByTransactionPoidAndDetRowIdAndDeleted(Long transactionPoid, Long detRowId, String deleted);

    void deleteByTransactionPoidAndDeleted(Long transactionPoid, String deleted);

    void deleteByTransactionPoidAndDetRowIdInAndDeleted(Long transactionPoid, List<Long> detRowIds, String deleted);
}
