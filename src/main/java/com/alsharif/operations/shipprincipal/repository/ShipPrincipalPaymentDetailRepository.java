package com.alsharif.operations.shipprincipal.repository;

import com.alsharif.operations.shipprincipal.entity.ShipPrincipalDetailId;
import com.alsharif.operations.shipprincipal.entity.ShipPrincipalPaymentDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipPrincipalPaymentDetailRepository extends JpaRepository<ShipPrincipalPaymentDetailEntity, ShipPrincipalDetailId> {
    List<ShipPrincipalPaymentDetailEntity> findByPrincipalPoidOrderByDetRowIdAsc(Long principalPoid);
    void deleteByPrincipalPoid(Long principalPoid);
}
