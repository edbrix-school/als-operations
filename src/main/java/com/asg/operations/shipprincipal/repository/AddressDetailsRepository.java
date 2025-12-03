package com.asg.operations.shipprincipal.repository;

import com.asg.operations.shipprincipal.entity.AddressDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressDetailsRepository extends JpaRepository<AddressDetails, Long> {

    List<AddressDetails> findByAddressMasterPoid(Long addressMasterPoid);

    List<AddressDetails> findByAddressMasterPoidOrderByAddressType(Long poid);

}
