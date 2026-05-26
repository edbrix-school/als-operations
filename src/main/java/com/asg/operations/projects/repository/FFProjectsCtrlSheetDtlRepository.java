package com.asg.operations.projects.repository;

import com.asg.operations.projects.entity.FFProjectsCtrlSheetDtl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FFProjectsCtrlSheetDtlRepository extends JpaRepository<FFProjectsCtrlSheetDtl, FFProjectsCtrlSheetDtl.FFProjectsCtrlSheetDtlId> {

    List<FFProjectsCtrlSheetDtl> findByTransactionPoid(Long transactionPoid);

    List<FFProjectsCtrlSheetDtl> findByTransactionPoidAndActive(Long transactionPoid, String active);

    Optional<FFProjectsCtrlSheetDtl> findByTransactionPoidAndDetRowId(Long transactionPoid, Long detRowId);

    List<FFProjectsCtrlSheetDtl> findByTransactionPoidAndFreightType(Long transactionPoid, String freightType);

    List<FFProjectsCtrlSheetDtl> findByTransactionPoidAndFreightTypeAndActive(Long transactionPoid, String freightType, String active);

    void deleteByTransactionPoidAndDetRowIdIn(Long transactionPoid, List<Long> detRowIds);

    void deleteByTransactionPoid(Long transactionPoid);
}
