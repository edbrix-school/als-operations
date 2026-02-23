package com.asg.operations.projects.repository;

import com.asg.operations.projects.projection.AirFreightJobProjection;
import com.asg.operations.projects.projection.FreightJobSummaryProjection;
import com.asg.operations.projects.projection.RoadFreightJobProjection;
import com.asg.operations.projects.projection.SeaFreightJobProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FreightJobProjectionRepository extends JpaRepository<com.asg.operations.projects.entity.FFProjectsCtrlSheetDtl, com.asg.operations.projects.entity.FFProjectsCtrlSheetDtl.FFProjectsCtrlSheetDtlId> {

    @Query(value = """
        SELECT 
            cs.DET_ROW_ID as detRowId,
            cs.FF_JOB_POID as jobId,
            cs.AF_ORIGIN as origin,
            cs.AF_DESTINATION as destination,
            cs.ETD as etd,
            cs.ETA as etaAta,
            cs.ARRIVAL_DATE as actualArrivalDate,
            cs.AF_NO_OF_PACKAGES as noOfPackages,
            cs.WEIGHT as weight,
            cs.CBM as cbm,
            cs.AF_CARRIER_POID as carrierCode,
            cs.DESCRIPTION as description,
            cs.JOB_STATUS as jobStatus
        FROM PROJECTS_CTRL_SHEET_DTL cs
        WHERE cs.TRANSACTION_POID = :projectId
        AND cs.FREIGHT_TYPE = 'AIR'
        ORDER BY cs.DET_ROW_ID
        """, nativeQuery = true)
    List<AirFreightJobProjection> findAirFreightJobs(@Param("projectId") Long projectId);

    @Query(value = """
        SELECT 
            cs.DET_ROW_ID as detRowId,
            cs.FF_JOB_POID as jobId,
            cs.AF_ORIGIN as pol,
            cs.AF_DESTINATION as pod,
            cs.ETD as etd,
            cs.ETA as etaAta,
            cs.ARRIVAL_DATE as arrivalDate,
            cs.SAIL_DATE as sailDate,
            cs.WEIGHT as weight,
            cs.CBM as cbm,
            cs.SF_LINE_POID as line,
            cs.DESCRIPTION as description,
            cs.JOB_STATUS as jobStatus
        FROM PROJECTS_CTRL_SHEET_DTL cs
        WHERE cs.TRANSACTION_POID = :projectId
        AND cs.FREIGHT_TYPE = 'SEA'
        ORDER BY cs.DET_ROW_ID
        """, nativeQuery = true)
    List<SeaFreightJobProjection> findSeaFreightJobs(@Param("projectId") Long projectId);

    @Query(value = """
        SELECT 
            cs.DET_ROW_ID as detRowId,
            cs.FF_JOB_POID as jobId,
            cs.RF_TRUCK_NUMBER as truckNumber,
            cs.ETA as eta,
            cs.WEIGHT as weight,
            cs.CBM as cbm,
            cs.JOB_STATUS as jobStatus
        FROM PROJECTS_CTRL_SHEET_DTL cs
        WHERE cs.TRANSACTION_POID = :projectId
        AND cs.FREIGHT_TYPE = 'ROAD'
        ORDER BY cs.DET_ROW_ID
        """, nativeQuery = true)
    List<RoadFreightJobProjection> findRoadFreightJobs(@Param("projectId") Long projectId);

    @Query(value = """
        SELECT 
            cs.DET_ROW_ID as detRowId,
            cs.FF_JOB_POID as jobId,
            cs.FREIGHT_TYPE as mode,
            cs.ETA as etaAta,
            cs.AF_ORIGIN as pol,
            cs.AF_ORIGIN as origin,
            cs.DESCRIPTION as description,
            cs.CBM as cbm,
            cs.WEIGHT as weight,
            cs.JOB_STATUS as jobStatus
        FROM PROJECTS_CTRL_SHEET_DTL cs
        WHERE cs.TRANSACTION_POID = :projectId
        ORDER BY cs.DET_ROW_ID
        """, nativeQuery = true)
    List<FreightJobSummaryProjection> findAllFreightJobs(@Param("projectId") Long projectId);

    @Query(value = """
        SELECT 
            cs.DET_ROW_ID as detRowId,
            cs.FF_JOB_POID as jobId,
            cs.FREIGHT_TYPE as mode,
            cs.ETA as etaAta,
            cs.AF_ORIGIN as pol,
            cs.AF_ORIGIN as origin,
            cs.DESCRIPTION as description,
            cs.CBM as cbm,
            cs.WEIGHT as weight,
            cs.JOB_STATUS as jobStatus
        FROM PROJECTS_CTRL_SHEET_DTL cs
        WHERE cs.TRANSACTION_POID = :projectId
        AND cs.ETA BETWEEN :fromDate AND :toDate
        ORDER BY cs.DET_ROW_ID
        """, nativeQuery = true)
    List<FreightJobSummaryProjection> findAllFreightJobsByDateRange(
        @Param("projectId") Long projectId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );
}
