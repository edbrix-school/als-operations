package com.alsharif.operations.pdaporttariffmaster.repository;

import com.alsharif.operations.pdaporttariffmaster.entity.PdaRateTypeMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface PdaRateTypeMasterRepository extends JpaRepository<PdaRateTypeMaster, Long> {
    boolean existsByRateTypePoidAndDeletedIgnoreCase(BigDecimal rateTypePoid, String deleted);
}
