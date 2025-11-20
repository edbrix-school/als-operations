package com.alsharif.operations.shipprincipal.repository;

import com.alsharif.operations.shipprincipal.entity.ShipPrincipalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShipPrincipalRepository extends JpaRepository<ShipPrincipalEntity, Long> {
    boolean existsByPrincipalPoid(Long principalPoid);
}