package com.alsharif.operations.shipprincipal.repository;

import com.alsharif.operations.shipprincipal.entity.AddressDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressDetailsRepository extends JpaRepository<AddressDetails, Long> {
}
