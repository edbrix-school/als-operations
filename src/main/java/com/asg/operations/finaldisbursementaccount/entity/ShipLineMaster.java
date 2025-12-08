package com.asg.operations.finaldisbursementaccount.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(
        name = "SHIP_LINE_MASTER",
        uniqueConstraints = {
                @UniqueConstraint(name = "SHIP_LINE_MASTER_UK_CODE", columnNames = {"LINE_CODE"}),
                @UniqueConstraint(name = "SHIP_LINE_MASTER_UK_NAME", columnNames = {"LINE_NAME"})
        }
)
public class ShipLineMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ship_line_seq")
    @SequenceGenerator(name = "ship_line_seq", sequenceName = "SHIP_LINE_MASTER_SEQ", allocationSize = 1)
    @Column(name = "LINE_POID")
    private Long linePoid;

    @Column(name = "GROUP_POID")
    private Long groupPoid;

    @Column(name = "LINE_CODE", nullable = false, length = 20)
    private String lineCode;

    @Column(name = "LINE_NAME", nullable = false, length = 100)
    private String lineName;

    @Column(name = "LINE_NAME2", length = 100)
    private String lineName2;

    @Column(name = "LINE_ADDRESS", length = 500)
    private String lineAddress;

    @Column(name = "COUNTRY_POID")
    private Long countryPoid;

    @Column(name = "CURRENCY_POID")
    private Long currencyPoid;

    @Column(name = "BL_PREFIX", length = 10)
    private String blPrefix;

    @Column(name = "BL_REMARKS_COUNT")
    private Integer blRemarksCount;

    @Column(name = "AGENCY_STARTED_DATE")
    @Temporal(TemporalType.DATE)
    private Date agencyStartedDate;

    @Column(name = "NEXT_RENEWAL_DATE")
    @Temporal(TemporalType.DATE)
    private Date nextRenewalDate;

    @Column(name = "ACTIVE", length = 1)
    private String active;

    @Column(name = "SEQNO")
    private Integer seqNo;

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

    @Column(name = "THC_PAY_AND_COLLECT", length = 1)
    private String thcPayAndCollect;

    @Column(name = "BANK_GUARANTEE_AMT")
    private Double bankGuaranteeAmt;

    @Column(name = "BANK_GUARANTEE_PERIOD_FROM")
    @Temporal(TemporalType.DATE)
    private Date bankGuaranteePeriodFrom;

    @Column(name = "BANK_GUARANTEE_PERIOD_TO")
    @Temporal(TemporalType.DATE)
    private Date bankGuaranteePeriodTo;

    @Column(name = "BANK_GUARANTEE_EXPIRY")
    @Temporal(TemporalType.DATE)
    private Date bankGuaranteeExpiry;

    @Column(name = "LINE_TYPE", length = 20)
    private String lineType;

    @Column(name = "CHAMBER_OF_COMMERCE")
    private Long chamberOfCommerce;

    @Column(name = "CHAMBER_OF_COMMERCE_EXPIRY")
    @Temporal(TemporalType.DATE)
    private Date chamberOfCommerceExpiry;

    @Column(name = "COMPANY_POID")
    private Long companyPoid;

    @Column(name = "LINE_PORT_REFNO", length = 25)
    private String linePortRefNo;

    @Column(name = "LINE_PORT_REGISTER_NAME", length = 25)
    private String linePortRegisterName;

    @Column(name = "TERMINAL_LINE_CODE", length = 25)
    private String terminalLineCode;

    @Column(name = "AGENCY_CONTRACT_START")
    @Temporal(TemporalType.DATE)
    private Date agencyContractStart;

    @Column(name = "AGENCY_CONTRACT_END")
    @Temporal(TemporalType.DATE)
    private Date agencyContractEnd;

    @Column(name = "BANK_GUARANTEE_NO", length = 25)
    private String bankGuaranteeNo;

    @Column(name = "BANK_GUARANTEE_BANK_POID")
    private Long bankGuaranteeBankPoid;

    @Column(name = "BL_PRINT_LINER", length = 1)
    private String blPrintLiner;

    @Column(name = "BANK_GUARANTEE_CURRENCY", length = 10)
    private String bankGuaranteeCurrency;

    @Column(name = "BL_PRINT_FORMAT", length = 2000)
    private String blPrintFormat;

    @Column(name = "BL_PRINT_RIDER", length = 2000)
    private String blPrintRider;

    @Column(name = "CONTAINER_FORM_VHENT", length = 1)
    private String containerFormVhent = "Y";

    @Column(name = "CONTAINER_FORM_RTN", length = 1)
    private String containerFormRtn = "Y";

    @Column(name = "LINE_SHORT_NAME", length = 25)
    private String lineShortName;

    @Column(name = "DO_PRINT_LINE", length = 1)
    private String doPrintLine = "Y";

    @Column(name = "LINE_VESSEL_TYPE_POID")
    private Long lineVesselTypePoid;

    @Column(name = "LINE_RANK")
    private Integer lineRank;

    @Column(name = "OTHER_INVOICE_ALLOWED", length = 1)
    private String otherInvoiceAllowed = "N";

    @Column(name = "BILL_TO", length = 50)
    private String billTo;

    @Column(name = "REPORTING_TYPE", length = 50)
    private String reportingType = "WEEKLY";

    @Column(name = "REPORTING_DAY", length = 50)
    private String reportingDay = "THRUSDAY";

    @Column(name = "REPORT_DESCRIPTION", length = 200)
    private String reportDescription;

    @Column(name = "PRINCIPAL_POID")
    private Long principalPoid;

    @Column(name = "RCPT_PRINT_LINE", length = 25)
    private String rcptPrintLine = "Y";

    @Column(name = "LINE_NOTE", length = 1000)
    private String lineNote;

    @Column(name = "LINE_CATEGORY", length = 20)
    private String lineCategory = "NVO";

    @Column(name = "MIS_LINE_CATEGORY", length = 100)
    private String misLineCategory;

    @Column(name = "LINE_COST_POID", length = 100)
    private String lineCostPoid;

    @Column(name = "PRINCIPAL_DO_REQUIRED", length = 3)
    private String principalDoRequired = "N";

    @Column(name = "ADDRESS_POID")
    private Long addressPoid;

}
