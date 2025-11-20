package com.alsharif.operations.shipprincipal.repository;

import com.alsharif.operations.shipprincipal.entity.ShipPrincipalDetailEntity;
import com.alsharif.operations.shipprincipal.entity.ShipPrincipalDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipPrincipalDetailRepository extends JpaRepository<ShipPrincipalDetailEntity, ShipPrincipalDetailId> {
    List<ShipPrincipalDetailEntity> findByPrincipalPoidOrderByDetRowIdAsc(Long principalPoid);
    void deleteByPrincipalPoid(Long principalPoid);
}
