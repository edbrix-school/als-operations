package com.asg.operations.projectjob.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class FFManifestHdrDto {

    private LocalDateTime transactionDate;
    private Long companyPoid;

    private String ffJobNo;
    private String ffJobType;

    private BigDecimal linePoid;
    private BigDecimal quoatationPoid;
    private BigDecimal principalPoid;

    private String masterBlNo;
    private String houseBlNo;
    private String blStatus;
    private String workExtensionJobNo;

    private String bookedBy;
    private String freightFlag;
    private String consignmentType;

    private BigDecimal salesmanPoid;
    private BigDecimal agentPoid;
    private String agentAcctNo;
    private String agentIataNo;

    private String shedNo;
    private String jobStatus;
    private String jobClosedBy;
    private LocalDateTime jobClosedDate;

    private BigDecimal voyagePoid;
    private String motherVslVoyageNo;
    private String motherVslName;
    private LocalDateTime motherVslSailDate;
    private LocalDateTime motherVslEta;

    private BigDecimal motherVslLoadPortPoid;
    private BigDecimal motherVslUnloadPortPoid;
    private BigDecimal motherVslTranshipPortPoid;

    private String feederVoyageNo;
    private String feederVslName;
    private LocalDateTime feederVslSailDate;
    private LocalDateTime feederVslEta;
    private LocalDateTime feederVslArrivalDate;

    private BigDecimal feederLoadportPoid;
    private BigDecimal feederUnloadportPoid;

    private String flightNo;
    private LocalDateTime flightDate;

    private String awportOfLoad;
    private String awportOfUnload;

    private BigDecimal shipperPoid;
    private BigDecimal shipperAddressPoid;
    private BigDecimal consigneePoid;
    private BigDecimal consigneeAddressPoid;

    private BigDecimal notifyPoid1;
    private BigDecimal notifyAddressPoid1;
    private BigDecimal notifyPoid2;
    private BigDecimal notifyAddressPoid2;

    private String canRequireToSent;
    private BigDecimal comodityPoid;
    private String cargoDescription;
    private String markNumbers;

    private String lpoNo;
    private LocalDateTime lpoDate;

    private BigDecimal totalVolume;
    private BigDecimal totalNetVolume;
    private BigDecimal totalWeight;
    private BigDecimal totalNetWeight;
    private BigDecimal weightUnit;

    private String unitPack;
    private BigDecimal totalNoOfPacks;
    private BigDecimal chargableWeight;
    private BigDecimal noOfPackBooked;
    private BigDecimal noOfPackArrived;

    private String handlingInfo;
    private String otherDetails;
    private String customsDeclarationNo;
    private String billingTo;

    private BigDecimal masterBlWeight;
    private String masterBlCurrency;
    private BigDecimal totalCharges;

    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime latModifiedDate;

    private String airArrivalport;
    private String airDeparturePort;
    private String carrierCode;
    private String agentDetails;

    private BigDecimal rateChanges;
    private BigDecimal agentCharges;

    private String flightNo2;
    private LocalDateTime flightDate2;

    private String priSupCodeOld;
    private String shiprCngCodeOld;
    private String ffBladingNo;

    private String cfInvnoOld;
    private String currentDoNo;
    private String docRef;
    private String deleted;

    private String releasedType;
    private BigDecimal relasedSeqNo;
    private String releasedGrantBy;
    private String releasedGrantDate;
    private String releasedGrantReason;

    private String firstCarrier;
    private String accountInfo;

    private String canPrinted;
    private String doPrinted;
    private String mablPrinted;

    private String consigneManual;
    private String notifyManual;
    private String shipperManual;

    private String ofoqMnfRef;
    private BigDecimal principalAddrPoid;

    private String showNotifyCan;
    private String canSentTo;
    private String canPrintedBy;
    private LocalDateTime canPrintedDt;

    private String ffShJob;
    private BigDecimal billToCustomerPoid;

    private String principalManual;
    private String motherVslFinalDelv;

    private String projectRef;
    private String recievedFrom;
    private String deliveryTo;
    private BigDecimal projectPoid;

    private LocalDateTime blIssueDate;
    private String airTransPort;
    private String airTransPort2;

    private String secondCarrier;
    private String thirdCarrier;

    private String containerVolume;
    private String rateClass;
    private String kgLb;

    private LocalDateTime fcrDofCargoRcpt;
    private String fcrCargoRemarks;
    private String fcrSuplierShipperRef;

    private BigDecimal coLoaderAgent;
    private String incoTerm;
    private String documentStatus;
    private String specialDocumentRemarks;

    private LocalDateTime deliveryDateFrom;
    private LocalDateTime deliveryDateTo;

    private String customsClearanceInvoved;
    private String roadTransport;

    private Integer doFreedays;

    private String bayanNo;
    private String bayanType;
    private BigDecimal bayanAmount;
    private LocalDateTime bayanExpiry;
    private String bayanStatus;

    private String policyNo;
    private String shipperManualEdi;
    private String consigneManualEdi;

    private String passengerWithCargo;
    private String radioAction;
    private String performaPrintUSD;
    private String globalTracking;

    private String houseBlNo2;
    private String isMainJob;
    private BigDecimal mainTransactionPoid;

    private String holdDo;
    private String holdDoUser;

    private String shipmentMode;
    private String transportationMode;

    private String truckTransportFrom;
    private String truckTransportTo;
}
