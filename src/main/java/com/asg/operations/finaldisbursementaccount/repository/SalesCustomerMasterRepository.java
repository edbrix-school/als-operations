package com.asg.operations.finaldisbursementaccount.repository;

import com.asg.operations.finaldisbursementaccount.entity.SalesCustomerMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesCustomerMasterRepository extends JpaRepository<SalesCustomerMaster, Long> {
    boolean existsByCustomerPoid(Long customerPoid);
}
