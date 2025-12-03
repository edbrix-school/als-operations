package com.asg.operations.shipprincipal.repository;

import com.asg.operations.shipprincipal.entity.AddressMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressMasterRepository extends JpaRepository<AddressMaster, Long> {

    AddressMaster findByAddressMasterPoid(Long addressMasterPoid);

    boolean existsByAddressMasterPoid(Long addressMasterPoid);

    boolean existsByAddressNameIgnoreCase(String addressName);

    boolean existsByAddressNameIgnoreCaseAndAddressMasterPoidNot(String addressName, Long excludePoid);

    boolean existsByAddressNameIgnoreCaseAndGroupPoid(String addressName, Long groupPoid);


}
