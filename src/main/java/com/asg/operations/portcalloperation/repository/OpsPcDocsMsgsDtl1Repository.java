package com.asg.operations.portcalloperation.repository;

import com.asg.operations.portcalloperation.entity.OpsPcDocsMsgsDtl1;
import com.asg.operations.portcalloperation.entity.OpsPcDocsMsgsDtl1Id;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OpsPcDocsMsgsDtl1Repository extends JpaRepository<OpsPcDocsMsgsDtl1, OpsPcDocsMsgsDtl1Id> {
    boolean existsByIdEmailPoid(Long emailPoid);
}
