-- =====================================================================
-- Project Freight API - SQL Queries for Manual Verification
-- =====================================================================
-- Replace :projectId with an actual PROJECT_POID value
-- Replace :fromDate / :toDate with date values like '2025-01-01'
-- Or set to NULL to skip date filtering
-- =====================================================================


-- =====================================================================
-- 1. ALL FREIGHTS (Summary tab - all job types)
-- Used by: GET /v1/projects/{projectId}/freights/summary
-- Returns one row per job (header-level only, no detail expansion)
-- =====================================================================

-- 1a. Without date range filter
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
ORDER BY j.TRANSACTION_POID;

-- 1b. With date range filter
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
ORDER BY j.TRANSACTION_POID;


-- =====================================================================
-- 2. AIR FREIGHT JOBS (Air Freight tab - header query)
-- Used by: GET /v1/projects/{projectId}/freights/air
-- The API expands each job into 1 row per air package detail
-- =====================================================================

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
ORDER BY j.TRANSACTION_POID;


-- =====================================================================
-- 3. AIR PACKAGE DETAILS (fetched per batch of air job IDs)
-- One row per air package; joined in Java to air job headers
-- Replace :jobIds with comma-separated TRANSACTION_POIDs from query #2
-- =====================================================================

SELECT
    ap.TRANSACTION_POID,
    ap.DET_ROW_ID,
    ap.NO_OF_PACKS,
    ap.PACK_UNIT,
    ap.TOTAL_WEIGHT,
    ap.TOTAL_VOLUME,
    ap.LENGTH,
    ap.WIDTH,
    ap.HEIGHT,
    ap.CHARGEABLE_WEIGHT,
    ap.DESCRIPTION,
    ap.APPOINTMENT_DATE,
    ap.DELIVERY_DATE,
    ap.DETENTION,
    ap.DOC_STATUS,
    ap.REMARKS
FROM FF_MANIFEST_AIR_PKG_DETAILS ap
WHERE ap.TRANSACTION_POID IN (:jobIds)
ORDER BY ap.TRANSACTION_POID, ap.DET_ROW_ID;


-- =====================================================================
-- 4. SEA FREIGHT JOBS (Sea Freight tab - header query)
-- Used by: GET /v1/projects/{projectId}/freights/sea
-- The API expands each job into 1 row per container detail
-- =====================================================================

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
ORDER BY j.TRANSACTION_POID;


-- =====================================================================
-- 5. CONTAINER DETAILS (fetched per batch of sea job IDs)
-- One row per container; joined in Java to sea job headers
-- Replace :jobIds with comma-separated TRANSACTION_POIDs from query #4
-- =====================================================================

SELECT
    cd.TRANSACTION_POID,
    cd.DET_ROW_ID,
    cd.CONTAINER_NO,
    cd.CONTAINER_SIZE,
    cd.CONTAINER_SEAL_NO as sealNo,
    cd.SEAL_NO,
    cd.NO_OF_PACKS,
    cd.PACK_UNIT,
    cd.GRS_WEIGHT,
    cd.GRS_VOLUME,
    cd.NET_WEIGHT,
    cd.NET_VOLUME,
    cd.CARGO_DESCRIPTION,
    cd.CARGO_COLLECTION_DATE,
    cd.DELIVERY_DATE,
    cd.DETENTION,
    cd.DOC_STATUS,
    cd.REMARKS
FROM FF_MANIEST_CONTAINER_DTL cd
WHERE cd.TRANSACTION_POID IN (:jobIds)
ORDER BY cd.TRANSACTION_POID, cd.DET_ROW_ID;


-- =====================================================================
-- 6. ROAD FREIGHT JOBS (Road Freight tab - header query)
-- Used by: GET /v1/projects/{projectId}/freights/road
-- The API expands each job into 1 row per truck detail
-- =====================================================================

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
ORDER BY j.TRANSACTION_POID;


-- =====================================================================
-- 7. TRUCK DETAILS (fetched per batch of road job IDs)
-- One row per truck; joined in Java to road job headers
-- Truck details contain bayan data inline (no separate bayan table join)
-- Replace :jobIds with comma-separated TRANSACTION_POIDs from query #6
-- =====================================================================

SELECT
    td.TRANSACTION_POID,
    td.DET_ROW_ID,
    td.BL_AWB_NUMBER,
    td.TRUCK_NUMBER,
    td.BAYAN_NUMBER,
    td.BAYAN_MODE as bayanCode,
    td.ETA,
    td.DUTY_AMOUNT,
    td.VAT_AMOUNT,
    td.TOTAL_PAID_AMOUNT,
    td.EXPIRY_DATE,
    td.SUBMITTED_DATE,
    td.PAYMENT_DATE,
    td.DOCUMENT_STATUS
FROM FF_MANIFEST_TRUCK_DTL td
WHERE td.TRANSACTION_POID IN (:jobIds)
ORDER BY td.TRANSACTION_POID, td.DET_ROW_ID;


-- =====================================================================
-- 8. BAYAN DETAILS (fetched per batch of AIR/SEA job IDs)
-- Used for Air Freight and Sea Freight tabs (not Road - see truck table)
-- Replace :jobIds with comma-separated TRANSACTION_POIDs from query #2 or #4
-- =====================================================================

SELECT
    bd.TRANSACTION_POID,
    bd.DET_ROW_ID,
    bd.BL_AWB_NUMBER,
    bd.BAYAN_NUMBER,
    bd.BAYAN_MODE,
    bd.DUTY_AMOUNT,
    bd.VAT_AMOUNT,
    bd.TOTAL_PAID_AMOUNT,
    bd.EXPIRY_DATE,
    bd.SUBMITTED_DATE,
    bd.PAYMENT_DATE
FROM FF_MANIFEST_BAYAN_DTL bd
WHERE bd.TRANSACTION_POID IN (:jobIds)
ORDER BY bd.TRANSACTION_POID, bd.DET_ROW_ID;


-- =====================================================================
-- 9. UPCOMING JOBS (filtered by date range, excluding completed/closed)
-- Used by: GET /v1/projects/{projectId}/freights/upcoming
-- =====================================================================

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
ORDER BY j.MOTHER_VSL_ETA ASC;


-- =====================================================================
-- 10. JOB CHARGES (fetched for a single job)
-- Used by: GET /v1/projects/{projectId}/freights/jobs/{jobId}/charges
-- Replace :jobId with a single TRANSACTION_POID
-- =====================================================================

SELECT
    c.TRANSACTION_POID,
    c.DET_ROW_ID,
    c.CHARGE_POID,
    c.CHARGE_CODE_OLD as chargeCode,
    c.CURRENCY_CODE,
    c.CURRENCY_EXCHANGE,
    c.QUANTITY,
    c.BUYING_PERCHARGE,
    c.BILLING_PERCHARGE,
    c.TOTAL_BUYING_CHARGE,
    c.TOTAL_SELLING_CHARGE,
    c.TOTAL_COST_BOOKED,
    c.PAY_MODE,
    c.UNIT_TYPE,
    c.TAX_POID,
    c.TAX_PERCENTAGE,
    c.TAX_AMOUNT,
    c.TAX_INPUT_AMOUNT,
    c.REMARKS,
    c.CHARGE_BASIS,
    c.SUPPLIER_POID
FROM FF_MANIFEST_CHARGES_DTL c
WHERE c.TRANSACTION_POID = :jobId
ORDER BY c.DET_ROW_ID;


-- =====================================================================
-- QUICK VERIFICATION QUERIES
-- =====================================================================

-- Check how many jobs exist for a project, grouped by type
SELECT j.FF_JOBTYPE, COUNT(*) as jobCount
FROM FF_MANIEST_HDR j
WHERE j.PROJECT_POID = :projectId
AND (j.DELETED IS NULL OR j.DELETED = 'N')
GROUP BY j.FF_JOBTYPE;

-- Check detail record counts for a specific job
SELECT 'Air Packages' as detailType, COUNT(*) as cnt FROM FF_MANIFEST_AIR_PKG_DETAILS WHERE TRANSACTION_POID = :jobId
UNION ALL
SELECT 'Containers', COUNT(*) FROM FF_MANIEST_CONTAINER_DTL WHERE TRANSACTION_POID = :jobId
UNION ALL
SELECT 'Trucks', COUNT(*) FROM FF_MANIFEST_TRUCK_DTL WHERE TRANSACTION_POID = :jobId
UNION ALL
SELECT 'Bayans', COUNT(*) FROM FF_MANIFEST_BAYAN_DTL WHERE TRANSACTION_POID = :jobId
UNION ALL
SELECT 'Charges', COUNT(*) FROM FF_MANIFEST_CHARGES_DTL WHERE TRANSACTION_POID = :jobId;

-- Full denormalized view: Air jobs with their package details (mimics API output)
SELECT
    h.TRANSACTION_POID, h.FF_JOBNO, h.MASTER_BL_NO, h.HOUSE_BL_NO,
    h.FLIGHT_NO, h.AWPORT_OF_LOAD as origin, h.AWPORT_OF_UNLOAD as dest,
    h.CARRIER_CODE, h.FLIGHT_DATE, h.JOB_STATUS, h.DOCUMENT_STATUS,
    ap.DET_ROW_ID as pkgRowId, ap.NO_OF_PACKS, ap.TOTAL_WEIGHT as pkgWeight,
    ap.TOTAL_VOLUME as pkgCbm, ap.CHARGEABLE_WEIGHT, ap.DESCRIPTION as pkgDesc,
    ap.DETENTION, ap.REMARKS,
    bd.BAYAN_NUMBER, bd.BAYAN_MODE, bd.DUTY_AMOUNT, bd.VAT_AMOUNT, bd.TOTAL_PAID_AMOUNT
FROM FF_MANIEST_HDR h
LEFT JOIN FF_MANIFEST_AIR_PKG_DETAILS ap ON ap.TRANSACTION_POID = h.TRANSACTION_POID
LEFT JOIN (
    SELECT *, ROW_NUMBER() OVER (PARTITION BY TRANSACTION_POID ORDER BY DET_ROW_ID) as rn
    FROM FF_MANIFEST_BAYAN_DTL
) bd ON bd.TRANSACTION_POID = h.TRANSACTION_POID AND bd.rn = 1
WHERE h.PROJECT_POID = :projectId
AND (h.FF_JOBTYPE = 'AIR' OR h.SHIPMENT_MODE = 'AIR FREIGHT')
AND (h.DELETED IS NULL OR h.DELETED = 'N')
ORDER BY h.TRANSACTION_POID, ap.DET_ROW_ID;

-- Full denormalized view: Sea jobs with their container details (mimics API output)
SELECT
    h.TRANSACTION_POID, h.FF_JOBNO, h.MASTER_BL_NO, h.HOUSE_BL_NO,
    h.MOTHER_VSL_NAME, h.MOTHER_VSL_LOADPORT_POID as pol,
    h.MOTHER_VSL_UNLOADPORT_POID as pod, h.LINE_POID,
    h.MOTHER_VSL_SAIL_DATE as etd, h.MOTHER_VSL_ETA as eta,
    h.JOB_STATUS, h.DOCUMENT_STATUS,
    cd.DET_ROW_ID as cntRowId, cd.CONTAINER_NO, cd.CONTAINER_SIZE,
    cd.CONTAINER_SEAL_NO as sealNo, cd.NO_OF_PACKS, cd.GRS_WEIGHT, cd.GRS_VOLUME,
    cd.CARGO_COLLECTION_DATE as appointmentDate, cd.DELIVERY_DATE,
    cd.DETENTION, cd.DOC_STATUS, cd.REMARKS,
    bd.BAYAN_NUMBER, bd.BAYAN_MODE, bd.DUTY_AMOUNT, bd.VAT_AMOUNT, bd.TOTAL_PAID_AMOUNT
FROM FF_MANIEST_HDR h
LEFT JOIN FF_MANIEST_CONTAINER_DTL cd ON cd.TRANSACTION_POID = h.TRANSACTION_POID
LEFT JOIN (
    SELECT *, ROW_NUMBER() OVER (PARTITION BY TRANSACTION_POID ORDER BY DET_ROW_ID) as rn
    FROM FF_MANIFEST_BAYAN_DTL
) bd ON bd.TRANSACTION_POID = h.TRANSACTION_POID AND bd.rn = 1
WHERE h.PROJECT_POID = :projectId
AND (h.FF_JOBTYPE = 'SEA' OR h.SHIPMENT_MODE = 'SEA FREIGHT')
AND (h.DELETED IS NULL OR h.DELETED = 'N')
ORDER BY h.TRANSACTION_POID, cd.DET_ROW_ID;

-- Full denormalized view: Road jobs with their truck details (mimics API output)
SELECT
    h.TRANSACTION_POID, h.FF_JOBNO, h.MASTER_BL_NO,
    h.TRUCK_TRANSPORT_FROM, h.TRUCK_TRANSPORT_TO,
    h.TOTAL_WEIGHT, h.TOTAL_VOLUME as cbm,
    h.CARGO_DESCRIPTION, h.JOB_STATUS, h.DOCUMENT_STATUS,
    td.DET_ROW_ID as truckRowId, td.BL_AWB_NUMBER, td.TRUCK_NUMBER,
    td.BAYAN_NUMBER, td.BAYAN_MODE, td.ETA as truckEta,
    td.DUTY_AMOUNT, td.VAT_AMOUNT, td.TOTAL_PAID_AMOUNT,
    td.EXPIRY_DATE, td.SUBMITTED_DATE, td.PAYMENT_DATE
FROM FF_MANIEST_HDR h
LEFT JOIN FF_MANIFEST_TRUCK_DTL td ON td.TRANSACTION_POID = h.TRANSACTION_POID
WHERE h.PROJECT_POID = :projectId
AND (h.FF_JOBTYPE = 'ROAD' OR h.SHIPMENT_MODE = 'ROAD')
AND (h.DELETED IS NULL OR h.DELETED = 'N')
ORDER BY h.TRANSACTION_POID, td.DET_ROW_ID;
