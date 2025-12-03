package com.asg.operations.pdaporttariffmaster.repository;

import com.asg.operations.pdaratetypemaster.entity.PdaRateTypeMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface PdaRateTypeMasterRepository extends JpaRepository<PdaRateTypeMaster, Long> {
    boolean existsByRateTypePoidAndDeletedIgnoreCase(BigDecimal rateTypePoid, String deleted);
}
