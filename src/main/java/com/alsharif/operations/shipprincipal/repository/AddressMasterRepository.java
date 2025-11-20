package com.alsharif.operations.shipprincipal.repository;

import com.alsharif.operations.shipprincipal.entity.AddressMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressMasterRepository extends JpaRepository<AddressMaster, Long> {
}
