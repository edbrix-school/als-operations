package com.asg.operations.finaldisbursementaccount.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


@Getter
@Setter
@Entity
@Table(
        name = "SHIP_VOYAGE_HDR",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_SHIPVOYAGELINEVVO", columnNames = {"LINE_POID", "VESSEL_POID", "VOYAGE_NO"}),
                @UniqueConstraint(name = "SHIP_VOYAGE_HDR_UK_JOB", columnNames = {"JOB_NO", "COMPANY_POID"}),
                @UniqueConstraint(name = "UK_DOCREFFSHIP_VOYAGE_HDR", columnNames = {"DOC_REF"})
        }
)
public class ShipVoyageHdr {

    @AuditIgnore
    @Id
    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;

    @AuditIgnore
    @Column(name = "GROUP_POID", nullable = false)
    private Long groupPoid;

    @AuditIgnore
    @Column(name = "COMPANY_POID", nullable = false)
    private Long companyPoid;

    @Column(name = "TRANSACTION_DATE", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "JOB_NO", nullable = false, length = 20)
    private String jobNo;

    @Column(name = "VOYAGE_NO", nullable = false, length = 20)
    private String voyageNo;

    @Column(name = "LINE_POID", nullable = false)
    private Long linePoid;

    @Column(name = "VESSEL_POID", nullable = false)
    private Long vesselPoid;

    @Column(name = "AGENT_POID")
    private Long agentPoid;

    @Column(name = "SAIL_DATE")
    private LocalDate sailDate;

    @Column(name = "START_PORT_POID")
    private Long startPortPoid;

    @Column(name = "LAST_TRANSSHIP_PORT_POID")
    private Long lastTransshipPortPoid;

    @Column(name = "DESTINATION_PORT_POID")
    private Long destinationPortPoid;

    @Column(name = "EXPECTED_DATE")
    private LocalDate expectedDate;

    @Column(name = "BERTH_DATE")
    private LocalDate berthDate;

    @Column(name = "ARRIVAL_DATE")
    private LocalDate arrivalDate;

    @Column(name = "SHIPPED_ONBOARD_DATE")
    private LocalDate shippedOnboardDate;

    @Column(name = "ENTRY_DATE")
    private LocalDate entryDate;

    @Column(name = "CUSTOM_REGNO", length = 50)
    private String customRegNo;

    @Column(name = "CUSTOM_REGDATE")
    private LocalDate customRegDate;

    @AuditIgnore
    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;

    @AuditIgnore
    @Column(name = "CREATED_DATE")
    private LocalDate createdDate;

    @AuditIgnore
    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastModifiedBy;

    @AuditIgnore
    @Column(name = "LASTMODIFIED_DATE")
    private LocalDate lastModifiedDate;

    @AuditIgnore
    @Column(name = "DELETED", length = 1)
    private String deleted;

    @Column(name = "EXPECTED_DEPARTURE_DATE")
    private LocalDate expectedDepartureDate;

    @Column(name = "CURRENCY_CODE", length = 20)
    private String currencyCode;

    @Column(name = "CURRENCY_RATE")
    private Double currencyRate;

    @Column(name = "JOBNO_OLD", length = 20)
    private String jobNoOld;

    @AuditIgnore
    @Column(name = "DOC_REF", length = 25)
    private String docRef;

    @Column(name = "OPERATION_START_DATE")
    private LocalDate operationStartDate;

    @Column(name = "OPERATION_END_DATE")
    private LocalDate operationEndDate;

    @Column(name = "MSC_VESSEL_VOYAGE_REFF", length = 100)
    private String mscVesselVoyageReff;

    @Column(name = "ARRIVAL_DATE_CHANGED", length = 1)
    private String arrivalDateChanged;

    @Column(name = "LAST_PORT_POID")
    private Long lastPortPoid;

    @Column(name = "NEXT_PORT_POID")
    private Long nextPortPoid;

    @Column(name = "PRE_ARRIVAL_MSG_VESSEL")
    private LocalDate preArrivalMsgVessel;

    @Column(name = "PRE_ARRIVAL_MSG_PORT")
    private LocalDate preArrivalMsgPort;

    @Column(name = "ENTRY_IN_GCTOS")
    private LocalDate entryInGctos;

    @Column(name = "ENTRY_IN_MARASSI")
    private LocalDate entryInMarassi;
}
