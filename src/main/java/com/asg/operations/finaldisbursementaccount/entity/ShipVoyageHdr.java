package com.asg.operations.finaldisbursementaccount.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

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

    @Id
    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;

    @Column(name = "GROUP_POID", nullable = false)
    private Long groupPoid;

    @Column(name = "COMPANY_POID", nullable = false)
    private Long companyPoid;

    @Column(name = "TRANSACTION_DATE", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date transactionDate;

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
    @Temporal(TemporalType.DATE)
    private Date sailDate;

    @Column(name = "START_PORT_POID")
    private Long startPortPoid;

    @Column(name = "LAST_TRANSSHIP_PORT_POID")
    private Long lastTransshipPortPoid;

    @Column(name = "DESTINATION_PORT_POID")
    private Long destinationPortPoid;

    @Column(name = "EXPECTED_DATE")
    @Temporal(TemporalType.DATE)
    private Date expectedDate;

    @Column(name = "BERTH_DATE")
    @Temporal(TemporalType.DATE)
    private Date berthDate;

    @Column(name = "ARRIVAL_DATE")
    @Temporal(TemporalType.DATE)
    private Date arrivalDate;

    @Column(name = "SHIPPED_ONBOARD_DATE")
    @Temporal(TemporalType.DATE)
    private Date shippedOnboardDate;

    @Column(name = "ENTRY_DATE")
    @Temporal(TemporalType.DATE)
    private Date entryDate;

    @Column(name = "CUSTOM_REGNO", length = 50)
    private String customRegNo;

    @Column(name = "CUSTOM_REGDATE")
    @Temporal(TemporalType.DATE)
    private Date customRegDate;

    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;

    @Column(name = "CREATED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedDate;

    @Column(name = "DELETED", length = 1)
    private String deleted;

    @Column(name = "EXPECTED_DEPARTURE_DATE")
    @Temporal(TemporalType.DATE)
    private Date expectedDepartureDate;

    @Column(name = "CURRENCY_CODE", length = 20)
    private String currencyCode;

    @Column(name = "CURRENCY_RATE")
    private Double currencyRate;

    @Column(name = "JOBNO_OLD", length = 20)
    private String jobNoOld;

    @Column(name = "DOC_REF", length = 25)
    private String docRef;

    @Column(name = "OPERATION_START_DATE")
    @Temporal(TemporalType.DATE)
    private Date operationStartDate;

    @Column(name = "OPERATION_END_DATE")
    @Temporal(TemporalType.DATE)
    private Date operationEndDate;

    @Column(name = "MSC_VESSEL_VOYAGE_REFF", length = 100)
    private String mscVesselVoyageReff;

    @Column(name = "ARRIVAL_DATE_CHANGED", length = 1)
    private String arrivalDateChanged;

    @Column(name = "LAST_PORT_POID")
    private Long lastPortPoid;

    @Column(name = "NEXT_PORT_POID")
    private Long nextPortPoid;

    @Column(name = "PRE_ARRIVAL_MSG_VESSEL")
    @Temporal(TemporalType.DATE)
    private Date preArrivalMsgVessel;

    @Column(name = "PRE_ARRIVAL_MSG_PORT")
    @Temporal(TemporalType.DATE)
    private Date preArrivalMsgPort;

    @Column(name = "ENTRY_IN_GCTOS")
    @Temporal(TemporalType.DATE)
    private Date entryInGctos;

    @Column(name = "ENTRY_IN_MARASSI")
    @Temporal(TemporalType.DATE)
    private Date entryInMarassi;

}
