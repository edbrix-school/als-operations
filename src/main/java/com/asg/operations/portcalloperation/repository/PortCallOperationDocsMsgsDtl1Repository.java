package com.asg.operations.portcalloperation.repository;

import com.asg.operations.portcalloperation.entity.PortCallOperationDocsMsgsDtl1;
import com.asg.operations.portcalloperation.entity.PortCallOperationDocsMsgsDtl1Id;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortCallOperationDocsMsgsDtl1Repository extends JpaRepository<PortCallOperationDocsMsgsDtl1, PortCallOperationDocsMsgsDtl1Id> {
    void deleteByTransactionPoid(Long transactionPoid);
    List<PortCallOperationDocsMsgsDtl1> findByTransactionPoid(Long transactionPoid);
    @Query("select coalesce(max(d.detRowId), 0) from PortCallOperationDocsMsgsDtl1 d where d.transactionPoid = :transactionPoid")
    Long findMaxDetRowIdByTransactionPoid(@Param("transactionPoid") Long transactionPoid);
    PortCallOperationDocsMsgsDtl1 findByEmailPoid(Long emailPoid);
}
