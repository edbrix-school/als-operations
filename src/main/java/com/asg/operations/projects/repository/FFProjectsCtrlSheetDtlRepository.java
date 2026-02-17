package com.asg.operations.projects.repository;

import com.asg.operations.projects.entity.FFProjectsCtrlSheetDtl;
import com.asg.operations.projects.key.FFProjectsCtrlSheetDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FFProjectsCtrlSheetDtlRepository extends JpaRepository<FFProjectsCtrlSheetDtl, FFProjectsCtrlSheetDtlId> {

    List<FFProjectsCtrlSheetDtl> findByTransactionPoidAndDeleted(Long transactionPoid, String deleted);

    Optional<FFProjectsCtrlSheetDtl> findByTransactionPoidAndDetRowIdAndDeleted(Long transactionPoid, Long detRowId, String deleted);

    List<FFProjectsCtrlSheetDtl> findByTransactionPoidAndFreightTypeAndDeleted(Long transactionPoid, String freightType, String deleted);

    void deleteByTransactionPoidAndDeleted(Long transactionPoid, String deleted);

    void deleteByTransactionPoidAndDetRowIdInAndDeleted(Long transactionPoid, List<Long> detRowIds, String deleted);
}
