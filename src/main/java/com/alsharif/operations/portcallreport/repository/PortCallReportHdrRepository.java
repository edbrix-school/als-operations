package com.alsharif.operations.portcallreport.repository;

import com.alsharif.operations.portcallreport.entity.PortCallReportHdr;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PortCallReportHdrRepository extends JpaRepository<PortCallReportHdr, Long> {


    @Query("SELECT p FROM PortCallReportHdr p WHERE " +
            "(p.deleted IS NULL OR p.deleted != 'Y') AND " +
            "(:search IS NULL OR " +
            " UPPER(p.portCallReportName) LIKE UPPER(CONCAT('%', :search, '%')) OR " +
            " UPPER(p.portCallReportId) LIKE UPPER(CONCAT('%', :search, '%')))")
    Page<PortCallReportHdr> findAllNonDeletedWithSearch(@Param("search") String search, Pageable pageable);

    @Query("SELECT COUNT(p) > 0 FROM PortCallReportHdr p WHERE " +
            "UPPER(p.portCallReportName) = UPPER(:name) AND " +
            "(p.deleted IS NULL OR p.deleted != 'Y') AND " +
            "(:id IS NULL OR p.portCallReportPoid != :id)")
    boolean existsByPortCallReportNameIgnoreCaseAndNotDeleted(@Param("name") String name, @Param("id") Long id);

}
