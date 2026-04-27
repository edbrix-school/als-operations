package com.asg.operations.projectjob.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "FF_MANIEST_HDR")
public class FFManifestHdr extends BaseEntity {

    @AuditIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;

    @AuditIgnore
    @Column(name = "TRANSACTION_DATE", nullable = false)
    @NotNull
    private LocalDate transactionDate;

    @AuditIgnore
    @Column(name = "GROUP_POID")
    private Long groupPoid;

    @AuditIgnore
    @Column(name = "COMPANY_POID")
    private Long companyPoid;

    @Column(name = "FF_JOBNO")
    @Size(max = 20)
    private String ffJobNo;

    @Column(name = "FF_JOBTYPE")
    @Size(max = 20)
    private String ffJobType;

    @Column(name = "LINE_POID")
    private Long linePoid;

    @Column(name = "QUOTATION_POID")
    private Long quoatationPoid;

    @Column(name = "PRINCIPAL_POID")
    private Long principalPoid;

    @Column(name = "MASTER_BL_NO")
    @Size(max = 50)
    private String masterBlNo;

    @Column(name = "HOUSE_BL_NO")
    @Size(max = 50)
    private String houseBlNo;

    @Column(name = "BL_STATUS")
    @Size(max = 50)
    private String blStatus;

    @Column(name = "WORK_EXTENSION_JOBNO")
    @Size(max = 20)
    private String workExtensionJobNo;

    @Column(name = "BOOKED_BY")
    @Size(max = 20)
    private String bookedBy;

    @Column(name = "FREIGHT_FLAG")
    @Size(max = 20)
    private String freightFlag;

    @Column(name = "CONSIGNMENT_TYPE")
    @Size(max = 20)
    private String consignmentType;

    @Column(name = "SALESMAN_POID")
    private Long salesmanPoid;

    @Column(name = "AGENT_POID")
    private Long agentPoid;

    @Column(name = "AGENT_ACCT_NO")
    @Size(max = 20)
    private String agentAcctNo;

    @Column(name = "AGENT_IATA_NO")
    @Size(max = 20)
    private String agentIataNo;

    @Column(name = "SHED_NO")
    @Size(max = 50)
    private String shedNo;

    @Column(name = "JOB_STATUS")
    @Size(max = 20)
    private String jobStatus;

    @Column(name = "JOB_CLOSEDBY")
    @Size(max = 20)
    private String jobClosedBy;

    @Column(name = "JOB_CLOSED_DATE")
    private LocalDateTime jobClosedDate;

    @Column(name = "VOYAGE_POID")
    private Long voyagePoid;

    @Column(name = "MOTHER_VSL_VOYAGENO")
    @Size(max = 20)
    private String motherVslVoyageNo;

    @Column(name = "MOTHER_VSL_NAME")
    @Size(max = 50)
    private String motherVslName;

    @Column(name = "MOTHER_VSL_SAIL_DATE")
    private LocalDateTime motherVslSailDate;

    @Column(name = "MOTHER_VSL_ETA")
    private LocalDateTime motherVslEta;

    @Column(name = "MOTHER_VSL_LOADPORT_POID")
    private Long motherVslLoadPortPoid;

    @Column(name = "MOTHER_VSL_UNLOADPORT_POID")
    private Long motherVslUnloadPortPoid;

    @Column(name = "MOTHER_VSL_TRANSHIP_PORT_POID")
    private Long motherVslTranshipPortPoid;

    @Column(name = "FEEDER_VOYAGE_NO")
    @Size(max = 20)
    private String feederVoyageNo;

    @Column(name = "FEEDER_VSL_NAME")
    @Size(max = 50)
    private String feederVslName;

    @Column(name = "FEEDER_VSL_SAIL_DATE")
    private LocalDateTime feederVslSailDate;

    @Column(name = "FEEDER_VSL_ETA")
    private LocalDateTime feederVslEta;

    @Column(name = "FEEDER_VSL_ARRIVAL_DATE")
    private LocalDateTime feederVslArrivalDate;

    @Column(name = "FEEDER_LOADPORT_POID")
    private Long feederLoadportPoid;

    @Column(name = "FEEDER_UNLOADPORT_POID")
    private Long feederUnloadportPoid;

    @Column(name = "FLIGHT_NO")
    @Size(max = 20)
    private String flightNo;

    @Column(name = "FLIGHT_DATE")
    private LocalDateTime flightDate;

    @Column(name = "AWPORT_OF_LOAD")
    @Size(max = 20)
    private String awportOfLoad;

    @Column(name = "AWPORT_OF_UNLOAD")
    @Size(max = 20)
    private String awportOfUnload;

    @Column(name = "SHIPPER_POID")
    private Long shipperPoid;

    @Column(name = "SHIPPER_ADDRESS_POID")
    private Long shipperAddressPoid;

    @Column(name = "CONSIGNEE_POID")
    private Long consigneePoid;

    @Column(name = "CONSIGNEE_ADDRESS_POID")
    private Long consigneeAddressPoid;

    @Column(name = "NOTIFY_POID_1")
    private Long notifyPoid1;

    @Column(name = "NOTIFY_ADDRESS_POID_1")
    private Long notifyAddressPoid1;

    @Column(name = "NOTIFY_POID_2")
    private Long notifyPoid2;

    @Column(name = "NOTIFY_ADDRESS_POID_2")
    private Long notifyAddressPoid2;

    @Column(name = "CAN_REQUIRE_TO_SENT")
    @Size(max = 1)
    private String canRequireToSent;

    @Column(name = "COMODITY_POID")
    @Size(max = 500)
    private String comodityPoid;

    @Column(name = "CARGO_DESCRIPTION")
    @Size(max = 500)
    private String cargoDescription;

    @Column(name = "MARK_NUMBERS")
    @Size(max = 500)
    private String markNumbers;

    @Column(name = "LPO_NO")
    @Size(max = 100)
    private String lpoNo;

    @Column(name = "LPO_DATE")
    private LocalDateTime lpoDate;

    @Column(name = "TOTAL_VOLUME")
    private Long totalVolume;

    @Column(name = "TOTAL_NET_VOLUME")
    private Long totalNetVolume;

    @Column(name = "TOTAL_WEIGHT")
    private Long totalWeight;

    @Column(name = "TOTAL_NET_WEIGHT")
    private Long totalNetWeight;

    @Column(name = "WEIGHT_UNIT")
    private Long weightUnit;

    @Column(name = "UNIT_PACK")
    @Size(max = 20)
    private String unitPack;

    @Column(name = "TOTAL_NO_OF_PACKS")
    private Long totalNoOfPacks;

    @Column(name = "CHARGABLE_WEIGHT")
    private Long chargableWeight;

    @Column(name = "NO_OF_PACK_BOOKED")
    private Long noOfPackBooked;

    @Column(name = "NO_OF_PACK_ARRIVED")
    private Long noOfPackArrived;

    @Column(name = "HANDLING_INFO")
    @Size(max = 500)
    private String handlingInfo;

    @Column(name = "OTHER_DETAILS")
    @Size(max = 500)
    private String otherDetails;

    @Column(name = "CUSTOMS_DECLARATION_NO")
    @Size(max = 30)
    private String customsDeclarationNo;

    @Column(name = "BILLING_TO")
    @Size(max = 30)
    private String billingTo;

    @Column(name = "MASTER_BL_WEIGHT")
    private Long masterBlWeight;

    @Column(name = "MASTER_BL_CURRENCY")
    @Size(max = 20)
    private String masterBlCurrency;

    @Column(name = "TOTAL_CHARGES")
    private Long totalCharges;

    @Column(name = "AIR_ARRIVAL_PORT")
    @Size(max = 100)
    private String airArrivalport;

    @Column(name = "AIR_DEPARTURE_PORT")
    @Size(max = 100)
    private String airDeparturePort;

    @Column(name = "CARRIER_CODE")
    @Size(max = 20)
    private String carrierCode;

    @Column(name = "AGENT_DETAILS")
    @Size(max = 100)
    private String agentDetails;

    @Column(name = "RATE_CHARGES")
    private Long rateChanges;

    @Column(name = "AGENT_CHARGES")
    private Long agentCharges;

    @Column(name = "FLIGHT_NO2")
    @Size(max = 20)
    private String flightNo2;

    @Column(name = "FLIGHT_DATE2")
    private LocalDateTime flightDate2;

    @Column(name = "PRI_SUP_CODE_OLD")
    @Size(max = 20)
    private String priSupCodeOld;

    @Column(name = "SHIPR_CNG_CODE_OLD")
    @Size(max = 20)
    private String shiprCngCodeOld;

    @Column(name = "FF_BLADING_NO")
    @Size(max = 30)
    private String ffBladingNo;

    @Column(name = "CF_INVNO_OLD")
    @Size(max = 20)
    private String cfInvnoOld;

    @Column(name = "CURRENT_DONO")
    @Size(max = 20)
    private String currentDoNo;

    @Column(name = "DOC_REF")
    @Size(max = 25)
    private String docRef;

    @Column(name = "DELETED")
    @Size(max = 1)
    private String deleted;

    @Column(name = "RELEASED_TYPE")
    @Size(max = 30)
    private String releasedType;

    @Column(name = "RELASED_SEQNO")
    private Long relasedSeqNo;

    @Column(name = "RELEASED_GRANT_BY")
    @Size(max = 20)
    private String releasedGrantBy;

    @Column(name = "RELEASED_GRANT_DATE")
    @Size(max = 20)
    private String releasedGrantDate;

    @Column(name = "RELEASED_GRANT_REASON")
    @Size(max = 200)
    private String releasedGrantReason;

    @Column(name = "FIRST_CARRIER")
    @Size(max = 30)
    private String firstCarrier;

    @Column(name = "ACCOUNT_INFO")
    @Size(max = 500)
    private String accountInfo;

    @Column(name = "CAN_PRINTED")
    @Size(max = 1)
    private String canPrinted;

    @Column(name = "DO_PRINTED")
    @Size(max = 1)
    private String doPrinted;

    @Column(name = "MABL_PRINTED")
    @Size(max = 1)
    private String mablPrinted;

    @Column(name = "CONSIGNE_MANUAL")
    @Size(max = 500)
    private String consigneManual;

    @Column(name = "NOTIFY_MANUAL")
    @Size(max = 150)
    private String notifyManual;

    @Column(name = "SHIPPER_MANUAL")
    @Size(max = 500)
    private String shipperManual;

    @Column(name = "OFOQ_MNF_REF")
    @Size(max = 30)
    private String ofoqMnfRef;

    @Column(name = "PRINCIPAL_ADDR_POID")
    private Long principalAddrPoid;

    @Column(name = "SHOW_NOTIFY_CAN")
    @Size(max = 1)
    private String showNotifyCan;

    @Column(name = "CAN_SENT_TO")
    @Size(max = 20)
    private String canSentTo;

    @Column(name = "CAN_PRINTED_BY")
    @Size(max = 20)
    private String canPrintedBy;

    @Column(name = "CAN_PRINT_DT")
    private LocalDateTime canPrintedDt;

    @Column(name = "FF_SH_JOB")
    @Size(max = 10)
    private String ffShJob;

    @Column(name = "BILL_TO_CUSTOMER_POID")
    private Long billToCustomerPoid;

    @Column(name = "PRINCIPAL_MANUAL")
    @Size(max = 100)
    private String principalManual;

    @Column(name = "MOTHER_VSL_FINAL_DELV")
    @Size(max = 100)
    private String motherVslFinalDelv;

    @Column(name = "PROJECT_REF")
    @Size(max = 50)
    private String projectRef;

    @Column(name = "RECEIVED_FROM")
    @Size(max = 50)
    private String recievedFrom;

    @Column(name = "DELIVERY_TO")
    @Size(max = 50)
    private String deliveryTo;

    @Column(name = "PROJECT_POID")
    private Long projectPoid;

    @Column(name = "BL_ISSUE_DATE")
    private LocalDateTime blIssueDate;

    @Column(name = "AIR_TRANS_PORT")
    @Size(max = 25)
    private String airTransPort;

    @Column(name = "AIR_TRANS2_PORT")
    @Size(max = 30)
    private String airTransPort2;

    @Column(name = "SECOND_CARRIER")
    @Size(max = 30)
    private String secondCarrier;

    @Column(name = "THIRD_CARRIER")
    @Size(max = 30)
    private String thirdCarrier;

    @Column(name = "CONTAINER_VOLUME")
    @Size(max = 500)
    private String containerVolume;

    @Column(name = "RATE_CLASS")
    @Size(max = 25)
    private String rateClass;

    @Column(name = "KG_LB")
    @Size(max = 25)
    private String kgLb;

    @Column(name = "FCR_DOF_CARGO_RCPT")
    private LocalDateTime fcrDofCargoRcpt;

    @Column(name = "FCR_CARGO_REMARKS")
    @Size(max = 100)
    private String fcrCargoRemarks;

    @Column(name = "FCR_SUPLIER_SHIPPER_REF")
    @Size(max = 50)
    private String fcrSuplierShipperRef;

    @Column(name = "CO_LOADER_AGENT")
    private Long coLoaderAgent;

    @Column(name = "INCO_TERM")
    @Size(max = 100)
    private String incoTerm;

    @Column(name = "DOCUMENT_STATUS")
    @Size(max = 100)
    private String documentStatus;

    @Column(name = "SPECIAL_DOCUMENT_REMARKS")
    @Size(max = 200)
    private String specialDocumentRemarks;

    @Column(name = "DELIVERY_DATE_FROM")
    private LocalDateTime deliveryDateFrom;

    @Column(name = "DELIVERY_DATE_TO")
    private LocalDateTime deliveryDateTo;

    @Column(name = "CUSTOMS_CLEARANCE_INVOLVED")
    @Size(max = 1)
    private String customsClearanceInvoved;

    @Column(name = "ROAD_TRANSPORT")
    @Size(max = 1)
    private String roadTransport;

    @Column(name = "DO_FREE_DAYS")
    @Digits(integer = 4, fraction = 0)
    private Integer doFreedays;

    @Column(name = "BAYAN_NO")
    @Size(max = 100)
    private String bayanNo;

    @Column(name = "BAYAN_TYPE")
    @Size(max = 25)
    private String bayanType;

    @Digits(integer = 12, fraction = 3)
    @Column(precision = 15, scale = 3, name = "BAYAN_AMOUNT")
    private BigDecimal bayanAmount;

    @Column(name = "BAYAN_EXPIRY")
    private LocalDateTime bayanExpiry;

    @Column(name = "BAYAN_STATUS")
    private String bayanStatus;

    @Column(name = "POLICY_NO")
    @Size(max = 100)
    private String policyNo;

    @Column(name = "SHIPPER_MANUAL_EDI")
    @Size(max = 500)
    private String shipperManualEdi;

    @Column(name = "CONSIGNE_MANUAL_EDI")
    @Size(max = 500)
    private String consigneManualEdi;

    @Column(name = "PASSENGER_WITH_CARGO")
    @Size(max = 1)
    private String passengerWithCargo;

    @Column(name = "RADIOACTION")
    @Size(max = 1)
    private String radioAction;

    @Column(name = "PERFORMA_PRINT_USD")
    @Size(max = 1)
    private String performaPrintUSD;

    @Column(name = "GLOBAL_TRACKING")
    @Size(max = 1)
    private String globalTracking;

    @Column(name = "HOUSE_BL_NO_2")
    @Size(max = 100)
    private String houseBlNo2;

    @Column(name = "IS_MAIN_JOB")
    @Size(max = 1)
    private String isMainJob;

    @Column(name = "MAIN_TRANSACTION_POID")
    private Long mainTransactionPoid;

    @Column(name = "HOLD_DO")
    @Size(max = 1)
    private String holdDo;

    @Column(name = "HOLD_DO_USER")
    @Size(max = 100)
    private String holdDoUser;

    @Column(name = "SHIPMENT_MODE")
    @Size(max = 100)
    private String shipmentMode;

    @Column(name = "TRANSPORTATION_MODE")
    @Size(max = 100)
    private String transportationMode;

    @Column(name = "TRUCK_TRANSPORT_FROM")
    @Size(max = 300)
    private String truckTransportFrom;

    @Column(name = "TRUCK_TRANSPORT_TO")
    @Size(max = 300)
    private String truckTransportTo;

    @Column(name = "DOC_ID")
    @Size(max = 20)
    private String docId;

}
