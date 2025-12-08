package com.asg.operations.finaldisbursementaccount.repository;

import com.asg.operations.finaldisbursementaccount.entity.SalesSalesmanMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesSalesmanMasterRepository extends JpaRepository<SalesSalesmanMaster, Long> {

    boolean existsBySalesmanPoid(Long salesmanPoid);

}
