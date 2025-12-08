package com.asg.operations.portcallreport.repository;

import com.asg.operations.portcallreport.entity.PortCallReportDtl;
import com.asg.operations.portcallreport.entity.PortCallReportDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortCallReportDtlRepository extends JpaRepository<PortCallReportDtl, PortCallReportDtlId> {
    void deleteByPortCallReportPoid(Long portCallReportPoid);

    List<PortCallReportDtl> findByPortCallReportPoid(Long portCallReportPoid);
    
    @Query("select coalesce(max(d.detRowId), 0) from PortCallReportDtl d where d.portCallReportPoid = :portCallReportPoid")
    Long findMaxDetRowIdByPortCallReportPoid(@Param("portCallReportPoid") Long portCallReportPoid);
}
