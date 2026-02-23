package com.asg.operations.projects.repository;

import com.asg.operations.projects.entity.FFProjectsCtrlSheetDtl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FFProjectsCtrlSheetDtlRepository extends JpaRepository<FFProjectsCtrlSheetDtl, FFProjectsCtrlSheetDtl.FFProjectsCtrlSheetDtlId> {

    List<FFProjectsCtrlSheetDtl> findByTransactionPoid(Long transactionPoid);

    Optional<FFProjectsCtrlSheetDtl> findByTransactionPoidAndDetRowId(Long transactionPoid, Long detRowId);

    List<FFProjectsCtrlSheetDtl> findByTransactionPoidAndFreightType(Long transactionPoid, String freightType);
//
//    void deleteByTransactionPoid(Long transactionPoid);
//
//    void deleteByTransactionPoidAndDetRowId(Long transactionPoid, List<Long> detRowIds);
}
