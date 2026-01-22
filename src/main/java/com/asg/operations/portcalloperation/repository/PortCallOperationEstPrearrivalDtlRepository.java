package com.asg.operations.portcalloperation.repository;

import com.asg.operations.portcalloperation.entity.PortCallOperationEstPrearrivalDtl;
import com.asg.operations.portcalloperation.entity.PortCallOperationEstPrearrivalDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortCallOperationEstPrearrivalDtlRepository extends JpaRepository<PortCallOperationEstPrearrivalDtl, PortCallOperationEstPrearrivalDtlId> {
    void deleteByTransactionPoid(Long transactionPoid);

    List<PortCallOperationEstPrearrivalDtl> findByTransactionPoid(Long transactionPoid);

    @Query("select coalesce(max(d.detRowId), 0) from PortCallOperationEstPrearrivalDtl d where d.transactionPoid = :transactionPoid")
    Long findMaxDetRowIdByTransactionPoid(@Param("transactionPoid") Long transactionPoid);
}
