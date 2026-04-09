package com.asg.operations.common.repository;

import com.asg.operations.common.entity.GlobalAddressDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface GlobalAddressDetailsRepository extends JpaRepository<GlobalAddressDetails, BigDecimal> {
}
