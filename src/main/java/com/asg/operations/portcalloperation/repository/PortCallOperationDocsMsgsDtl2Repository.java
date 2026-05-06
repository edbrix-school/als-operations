package com.asg.operations.portcalloperation.repository;

import com.asg.operations.portcalloperation.entity.PortCallOperationDocsMsgsDtl2;
import com.asg.operations.portcalloperation.entity.PortCallOperationDocsMsgsDtl2Id;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortCallOperationDocsMsgsDtl2Repository extends JpaRepository<PortCallOperationDocsMsgsDtl2, PortCallOperationDocsMsgsDtl2Id> {
    void deleteByTransactionPoid(Long transactionPoid);

    List<PortCallOperationDocsMsgsDtl2> findByTransactionPoid(Long transactionPoid);

    List<PortCallOperationDocsMsgsDtl2> findByEmailPoid(Long emailPoid);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select coalesce(max(d.detRowId), 0) from PortCallOperationDocsMsgsDtl2 d where d.transactionPoid = :transactionPoid")
    Long findMaxDetRowIdByTransactionPoid(@Param("transactionPoid") Long transactionPoid);
}
