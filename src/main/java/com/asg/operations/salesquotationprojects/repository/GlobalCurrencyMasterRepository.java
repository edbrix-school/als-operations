package com.asg.operations.salesquotationprojects.repository;

import com.asg.operations.salesquotationprojects.entity.GlobalCurrencyMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GlobalCurrencyMasterRepository extends JpaRepository<GlobalCurrencyMaster, Long> {
    boolean existsByCurrencyCodeIgnoreCase(String currencyCode);

    boolean existsByCurrencyNameIgnoreCase(String currencyName);
}
