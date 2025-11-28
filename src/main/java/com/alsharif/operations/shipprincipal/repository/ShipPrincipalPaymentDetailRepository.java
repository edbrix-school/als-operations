package com.alsharif.operations.shipprincipal.repository;

import com.alsharif.operations.shipprincipal.entity.ShipPrincipalMasterDtlId;
import com.alsharif.operations.shipprincipal.entity.ShipPrincipalMasterPymtDtl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipPrincipalPaymentDetailRepository extends JpaRepository<ShipPrincipalMasterPymtDtl, ShipPrincipalMasterDtlId> {

    List<ShipPrincipalMasterPymtDtl> findByPrincipalPoidOrderByDetRowIdAsc(Long principalPoid);

    void deleteByPrincipalPoid(Long principalPoid);

    @Query("select coalesce(max(d.detRowId), 0) from ShipPrincipalMasterPymtDtl d where d.principalPoid = :principalPoid")
    Long findMaxDetRowIdByPrincipalPoid(@Param("principalPoid") Long principalPoid);
}
