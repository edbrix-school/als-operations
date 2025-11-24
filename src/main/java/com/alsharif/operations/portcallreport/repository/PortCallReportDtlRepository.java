package com.alsharif.operations.portcallreport.repository;

import com.alsharif.operations.portcallreport.entity.PortCallReportDtl;
import com.alsharif.operations.portcallreport.entity.PortCallReportDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortCallReportDtlRepository extends JpaRepository<PortCallReportDtl, PortCallReportDtlId> {
    void deleteByPortCallReportPoid(Long portCallReportPoid);

    List<PortCallReportDtl> findByPortCallReportPoid(Long portCallReportPoid);
}
