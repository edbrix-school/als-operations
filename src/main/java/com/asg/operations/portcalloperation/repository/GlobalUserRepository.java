package com.asg.operations.portcalloperation.repository;

import com.asg.operations.portcalloperation.entity.GlobalUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GlobalUserRepository extends JpaRepository<GlobalUser, Long> {
    boolean existsByUserPoid(Long userPoid);
}
