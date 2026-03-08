package com.asg.operations.projects.repository;

import com.asg.operations.projectjob.entity.FFManifestHdr;
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
public interface FreightJobProjectionRepository extends JpaRepository<FFManifestHdr, Long> {

    @Query(value = """
        SELECT
            j.TRANSACTION_POID as jobId,
            j.FF_JOBNO as jobNo,
            j.AWPORT_OF_LOAD as origin,
            j.AWPORT_OF_UNLOAD as destination,
            j.FLIGHT_DATE as etd,
            j.FLIGHT_DATE as etaAta,
            j.TOTAL_NO_OF_PACKS as noOfPackages,
            j.TOTAL_WEIGHT as weight,
            j.TOTAL_VOLUME as cbm,
            j.CARRIER_CODE as carrierCode,
            j.CARGO_DESCRIPTION as description,
            j.JOB_STATUS as jobStatus,
            j.DOCUMENT_STATUS as documentStatus,
            j.FLIGHT_NO as flightNo,
            j.HOUSE_BL_NO as hawbNo,
            j.MASTER_BL_NO as mawbNo
        FROM FF_MANIEST_HDR j
        WHERE j.PROJECT_POID = :projectId
        AND (j.FF_JOBTYPE = 'AIR' OR j.SHIPMENT_MODE = 'AIR FREIGHT')
        AND (j.DELETED IS NULL OR j.DELETED = 'N')
        AND (:fromDate IS NULL OR CAST(j.FLIGHT_DATE AS DATE) >= :fromDate)
        AND (:toDate IS NULL OR CAST(j.FLIGHT_DATE AS DATE) <= :toDate)
        ORDER BY j.TRANSACTION_POID
        """, nativeQuery = true)
    List<AirFreightJobProjection> findAirFreightJobsFiltered(
        @Param("projectId") Long projectId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    @Query(value = """
        SELECT
            j.TRANSACTION_POID as jobId,
            j.FF_JOBNO as jobNo,
            j.MOTHER_VSL_LOADPORT_POID as pol,
            j.MOTHER_VSL_UNLOADPORT_POID as pod,
            j.MOTHER_VSL_SAIL_DATE as etd,
            j.MOTHER_VSL_ETA as etaAta,
            j.FEEDER_VSL_ARRIVAL_DATE as arrivalDate,
            j.MOTHER_VSL_SAIL_DATE as sailDate,
            j.TOTAL_WEIGHT as weight,
            j.TOTAL_VOLUME as cbm,
            j.LINE_POID as line,
            j.MOTHER_VSL_NAME as vesselName,
            j.MASTER_BL_NO as masterBlNo,
            j.HOUSE_BL_NO as houseBlNo,
            j.CARGO_DESCRIPTION as description,
            j.JOB_STATUS as jobStatus,
            j.DOCUMENT_STATUS as documentStatus
        FROM FF_MANIEST_HDR j
        WHERE j.PROJECT_POID = :projectId
        AND (j.FF_JOBTYPE = 'SEA' OR j.SHIPMENT_MODE = 'SEA FREIGHT')
        AND (j.DELETED IS NULL OR j.DELETED = 'N')
        AND (:fromDate IS NULL OR CAST(j.MOTHER_VSL_ETA AS DATE) >= :fromDate)
        AND (:toDate IS NULL OR CAST(j.MOTHER_VSL_ETA AS DATE) <= :toDate)
        ORDER BY j.TRANSACTION_POID
        """, nativeQuery = true)
    List<SeaFreightJobProjection> findSeaFreightJobsFiltered(
        @Param("projectId") Long projectId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    @Query(value = """
        SELECT
            j.TRANSACTION_POID as jobId,
            j.FF_JOBNO as jobNo,
            j.MASTER_BL_NO as blAwbNumber,
            j.TRUCK_TRANSPORT_FROM as transportFrom,
            j.TRUCK_TRANSPORT_TO as transportTo,
            j.MOTHER_VSL_ETA as eta,
            j.TOTAL_WEIGHT as weight,
            j.TOTAL_VOLUME as cbm,
            j.CARGO_DESCRIPTION as description,
            j.JOB_STATUS as jobStatus,
            j.DOCUMENT_STATUS as documentStatus
        FROM FF_MANIEST_HDR j
        WHERE j.PROJECT_POID = :projectId
        AND (j.FF_JOBTYPE = 'ROAD' OR j.SHIPMENT_MODE = 'ROAD')
        AND (j.DELETED IS NULL OR j.DELETED = 'N')
        AND (:fromDate IS NULL OR CAST(j.MOTHER_VSL_ETA AS DATE) >= :fromDate)
        AND (:toDate IS NULL OR CAST(j.MOTHER_VSL_ETA AS DATE) <= :toDate)
        ORDER BY j.TRANSACTION_POID
        """, nativeQuery = true)
    List<RoadFreightJobProjection> findRoadFreightJobsFiltered(
        @Param("projectId") Long projectId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    @Query(value = """
        SELECT
            j.TRANSACTION_POID as jobId,
            j.FF_JOBNO as jobNo,
            j.FF_JOBTYPE as freightMode,
            CAST(j.LINE_POID AS VARCHAR(50)) as line,
            j.MOTHER_VSL_ETA as etaAta,
            CAST(j.MOTHER_VSL_LOADPORT_POID AS VARCHAR(50)) as pol,
            CAST(j.MOTHER_VSL_UNLOADPORT_POID AS VARCHAR(50)) as pod,
            j.AWPORT_OF_LOAD as origin,
            j.AWPORT_OF_UNLOAD as destination,
            j.CARGO_DESCRIPTION as description,
            j.TOTAL_VOLUME as cbm,
            j.TOTAL_NO_OF_PACKS as packages,
            j.TOTAL_WEIGHT as weight,
            j.JOB_STATUS as jobStatus,
            j.MASTER_BL_NO as blAwbNo,
            j.PRINCIPAL_POID as principalPoid
        FROM FF_MANIEST_HDR j
        WHERE j.PROJECT_POID = :projectId
        AND (j.DELETED IS NULL OR j.DELETED = 'N')
        ORDER BY j.TRANSACTION_POID
        """, nativeQuery = true)
    List<FreightJobSummaryProjection> findAllFreightJobs(@Param("projectId") Long projectId);

    @Query(value = """
        SELECT
            j.TRANSACTION_POID as jobId,
            j.FF_JOBNO as jobNo,
            j.FF_JOBTYPE as freightMode,
            CAST(j.LINE_POID AS VARCHAR(50)) as line,
            j.MOTHER_VSL_ETA as etaAta,
            CAST(j.MOTHER_VSL_LOADPORT_POID AS VARCHAR(50)) as pol,
            CAST(j.MOTHER_VSL_UNLOADPORT_POID AS VARCHAR(50)) as pod,
            j.AWPORT_OF_LOAD as origin,
            j.AWPORT_OF_UNLOAD as destination,
            j.CARGO_DESCRIPTION as description,
            j.TOTAL_VOLUME as cbm,
            j.TOTAL_NO_OF_PACKS as packages,
            j.TOTAL_WEIGHT as weight,
            j.JOB_STATUS as jobStatus,
            j.MASTER_BL_NO as blAwbNo,
            j.PRINCIPAL_POID as principalPoid
        FROM FF_MANIEST_HDR j
        WHERE j.PROJECT_POID = :projectId
        AND (j.DELETED IS NULL OR j.DELETED = 'N')
        AND CAST(j.MOTHER_VSL_ETA AS DATE) BETWEEN :fromDate AND :toDate
        ORDER BY j.TRANSACTION_POID
        """, nativeQuery = true)
    List<FreightJobSummaryProjection> findAllFreightJobsByDateRange(
        @Param("projectId") Long projectId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    @Query(value = """
        SELECT
            j.TRANSACTION_POID as jobId,
            j.FF_JOBNO as jobNo,
            j.FF_JOBTYPE as freightMode,
            CAST(j.LINE_POID AS VARCHAR(50)) as line,
            j.MOTHER_VSL_ETA as etaAta,
            CAST(j.MOTHER_VSL_LOADPORT_POID AS VARCHAR(50)) as pol,
            CAST(j.MOTHER_VSL_UNLOADPORT_POID AS VARCHAR(50)) as pod,
            j.AWPORT_OF_LOAD as origin,
            j.AWPORT_OF_UNLOAD as destination,
            j.CARGO_DESCRIPTION as description,
            j.TOTAL_VOLUME as cbm,
            j.TOTAL_NO_OF_PACKS as packages,
            j.TOTAL_WEIGHT as weight,
            j.JOB_STATUS as jobStatus,
            j.MASTER_BL_NO as blAwbNo,
            j.PRINCIPAL_POID as principalPoid
        FROM FF_MANIEST_HDR j
        WHERE j.PROJECT_POID = :projectId
        AND (j.DELETED IS NULL OR j.DELETED = 'N')
        AND CAST(j.MOTHER_VSL_ETA AS DATE) BETWEEN :fromDate AND :toDate
        AND (j.JOB_STATUS IS NULL OR j.JOB_STATUS NOT IN ('COMPLETED', 'CLOSED'))
        ORDER BY j.MOTHER_VSL_ETA ASC
        """, nativeQuery = true)
    List<FreightJobSummaryProjection> findUpcomingJobs(
        @Param("projectId") Long projectId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );
}
