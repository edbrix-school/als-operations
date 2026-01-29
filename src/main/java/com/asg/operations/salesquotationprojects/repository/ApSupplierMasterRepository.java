package com.asg.operations.salesquotationprojects.repository;

import com.asg.operations.salesquotationprojects.entity.ApSupplierMaster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApSupplierMasterRepository extends JpaRepository<ApSupplierMaster, Long> {
    boolean existsBySupplierPoid(Long supplierPoid);
}
