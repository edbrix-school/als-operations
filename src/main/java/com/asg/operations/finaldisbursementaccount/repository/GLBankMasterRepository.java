package com.asg.operations.finaldisbursementaccount.repository;

import com.asg.operations.finaldisbursementaccount.entity.GLBankMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GLBankMasterRepository extends JpaRepository<GLBankMaster, Long> {

    boolean existsByBankPoid(Long bankPoid);
}
