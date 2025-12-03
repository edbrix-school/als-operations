package com.asg.operations.finaldisbursementaccount.repository;

import com.asg.operations.finaldisbursementaccount.entity.CostCenterMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CostCenterRepository extends JpaRepository<CostCenterMaster, Long> {
    boolean existsByCostCenterPoid(Long costCenterPoid);
}
