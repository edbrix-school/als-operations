package com.asg.operations.portcalloperation.repository;

import com.asg.operations.portcalloperation.entity.PortCallOperationActCargoFigDtl;
import com.asg.operations.portcalloperation.entity.PortCallOperationActCargoFigDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortCallOperationActCargoFigDtlRepository extends JpaRepository<PortCallOperationActCargoFigDtl, PortCallOperationActCargoFigDtlId> {
    void deleteByTransactionPoid(Long transactionPoid);
    List<PortCallOperationActCargoFigDtl> findByTransactionPoid(Long transactionPoid);
    @Query("select coalesce(max(d.detRowId), 0) from PortCallOperationActCargoFigDtl d where d.transactionPoid = :transactionPoid")
    Long findMaxDetRowIdByTransactionPoid(@Param("transactionPoid") Long transactionPoid);
}
