package com.asg.operations.salesquotationprojects.repository;

import com.asg.operations.salesquotationprojects.entity.GlobalTaxMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GlobalTaxMasterRepository extends JpaRepository<GlobalTaxMaster, Long> {

    boolean existsByTaxPoid(Long taxPoid);

    boolean existsByTaxCodeIgnoreCase(String taxCode);

    boolean existsByTaxNameIgnoreCase(String taxName);

    boolean existsByTaxCodeIgnoreCaseAndGroupPoid(String taxCode, Long groupPoid);
}
