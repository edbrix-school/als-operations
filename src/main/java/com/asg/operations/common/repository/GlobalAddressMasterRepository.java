package com.asg.operations.common.repository;

import com.asg.operations.common.entity.GlobalAddressMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GlobalAddressMasterRepository extends JpaRepository<GlobalAddressMaster, Long> {
}
