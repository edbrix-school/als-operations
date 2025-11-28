package com.alsharif.operations.shipprincipal.repository;

import com.alsharif.operations.shipprincipal.entity.ShipPrincipalMasterDtl;
import com.alsharif.operations.shipprincipal.entity.ShipPrincipalMasterDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipPrincipalDetailRepository extends JpaRepository<ShipPrincipalMasterDtl, ShipPrincipalMasterDtlId> {

    List<ShipPrincipalMasterDtl> findByPrincipalPoidOrderByDetRowIdAsc(Long principalPoid);

    void deleteByPrincipalPoid(Long principalPoid);

    @Query("select coalesce(max(d.detRowId), 0) from ShipPrincipalMasterDtl d where d.principalPoid = :principalPoid")
    Long findMaxDetRowIdByPrincipalPoid(@Param("principalPoid") Long principalPoid);
}
