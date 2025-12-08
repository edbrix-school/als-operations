package com.asg.operations.shipprincipal.repository;

import com.asg.operations.shipprincipal.entity.ShipPrincipalMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShipPrincipalRepository extends JpaRepository<ShipPrincipalMaster, Long> {

    boolean existsByPrincipalPoid(Long principalPoid);

    boolean existsByPrincipalName(String principalName);
    
    @Query("SELECT p FROM ShipPrincipalMaster p WHERE " +
            "(p.deleted IS NULL OR p.deleted != 'Y') AND " +
            "(:search IS NULL OR " +
            " UPPER(p.principalCode) LIKE UPPER(CONCAT('%', :search, '%')) OR " +
            " UPPER(p.principalName) LIKE UPPER(CONCAT('%', :search, '%')))")
    Page<ShipPrincipalMaster> findAllNonDeletedWithSearch(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT p FROM ShipPrincipalMaster p WHERE p.principalPoid = :principalPoid " +
            "AND (p.deleted IS NULL OR p.deleted != 'Y')")
    Optional<ShipPrincipalMaster> findByIdAndNotDeleted(@Param("principalPoid") Long principalPoid);
}