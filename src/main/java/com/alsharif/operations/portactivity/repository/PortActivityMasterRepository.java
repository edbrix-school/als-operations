package com.alsharif.operations.portactivity.repository;

import com.alsharif.operations.portactivity.entity.PortActivityMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PortActivityMasterRepository extends JpaRepository<PortActivityMaster, Long> {
}
