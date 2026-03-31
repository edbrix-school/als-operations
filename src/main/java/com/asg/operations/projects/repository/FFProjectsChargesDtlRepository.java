package com.asg.operations.projects.repository;

import com.asg.operations.projects.entity.FFProjectsChargesDtl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FFProjectsChargesDtlRepository extends JpaRepository<FFProjectsChargesDtl, FFProjectsChargesDtl.FFProjectsChargesDtlId> {

    List<FFProjectsChargesDtl> findByTransactionPoid(Long transactionPoid);

    Optional<FFProjectsChargesDtl> findByTransactionPoidAndDetRowId(Long transactionPoid, Long detRowId);

    void deleteByTransactionPoidAndDetRowIdIn(Long transactionPoid, List<Long> detRowIds);
}
