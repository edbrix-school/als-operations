package com.asg.operations.projectjob.dto;

import com.asg.common.lib.dto.LovGetListDto;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class FFManifestHdrDto {

    private LocalDate transactionDate;
    private Long companyPoid;

    @Size(max = 20, message = "Doc ID must not exceed 20 characters")
    private String docId;

    @Size(max = 20, message = "FF Job number must not exceed 20 characters")
    private String ffJobNo;

    @Size(max = 20, message = "FF Job type must not exceed 20 characters")
    private String ffJobType;

    private Long linePoid;
    private LovGetListDto lineLov;
    private Long quoatationPoid;
    private Long principalPoid;
    private LovGetListDto principalLov;

    @Size(max = 50, message = "Master BL number must not exceed 50 characters")
    private String masterBlNo;

    @Size(max = 50, message = "House BL number must not exceed 50 characters")
    private String houseBlNo;

    @Size(max = 50, message = "BL status must not exceed 50 characters")
    private String blStatus;

    @Size(max = 20, message = "Booked by must not exceed 20 characters")
    private String bookedBy;

    @Size(max = 20, message = "Freight flag must not exceed 20 characters")
    private String freightFlag;

    @Size(max = 20, message = "Consignment type must not exceed 20 characters")
    private String consignmentType;

    @Size(max = 20, message = "Consignment type must not exceed 20 characters")
    private String workExtensionJobNo;

    private Long salesmanPoid;
    private LovGetListDto salesmanLov;
    private Long agentPoid;

    @Size(max = 20, message = "Agent Account Number must not exceed 20 characters")
    private String agentAcctNo;

    @Size(max = 20, message = "Agent Iata number must not exceed 20 characters")
    private String agentIataNo;

    @Size(max = 50, message = "Shed number must not exceed 50 characters")
    private String shedNo;

    @Size(max = 20, message = "Job Status must not exceed 20 characters")
    private String jobStatus;

    @Size(max = 20, message = "FF Job number must not exceed 20 characters")
    private String jobClosedBy;

    private LocalDateTime jobClosedDate;

    private Long voyagePoid;

    @Size(max = 20, message = "FF Job number must not exceed 20 characters")
    private String motherVslVoyageNo;

    @Size(max = 50, message = "Mother Vsl Name must not exceed 50 characters")
    private String motherVslName;
    private LocalDateTime motherVslSailDate;
    private LocalDateTime motherVslEta;

    private Long motherVslLoadPortPoid;
    private LovGetListDto motherVslLoadPortLov;
    private Long motherVslUnloadPortPoid;
    private LovGetListDto motherVslUnloadPortLov;
    private Long motherVslTranshipPortPoid;
    private LovGetListDto motherVslTranshipPortLov;

    @Size(max = 20, message = "Feeder Voyage Number must not exceed 20 characters")
    private String feederVoyageNo;

    @Size(max = 50, message = "Feeder Vsl Name must not exceed 50 characters")
    private String feederVslName;
    private LocalDateTime feederVslSailDate;
    private LocalDateTime feederVslEta;
    private LocalDateTime feederVslArrivalDate;

    private Long feederLoadportPoid;
    private LovGetListDto FeederLoadPortLov;
    private Long feederUnloadportPoid;
    private LovGetListDto FeederUnloadPortLov;

    @Size(max = 20, message = "Flight Number must not exceed 20 characters")
    private String flightNo;
    private LocalDateTime flightDate;

    @Size(max = 20, message = "Aw Port Of Load must not exceed 20 characters")
    private String awportOfLoad;
    private LovGetListDto awportOfLoadLov;

    @Size(max = 20, message = "Aw Port Of UnLoad must not exceed 20 characters")
    private String awportOfUnload;
    private LovGetListDto awportOfUnloadLov;

    private Long shipperPoid;
    private Long shipperAddressPoid;
    private Long consigneePoid;
    private Long consigneeAddressPoid;

    private Long notifyPoid1;
    private LovGetListDto notifyLov1;
    private Long notifyAddressPoid1;
    private Long notifyPoid2;
    private LovGetListDto notifyLov2;
    private Long notifyAddressPoid2;

    @Size(max = 1, message = "Can require To Sent must be exactly 1 character")
    private String canRequireToSent;
    private List<Long> commodityPoids;
    private List<LovGetListDto> CommodityLovs;

    @Size(max = 500, message = "Cargo Description must not exceed 500 characters")
    private String cargoDescription;

    @Size(max = 500, message = "Mark Number must not exceed 500 characters")
    private String markNumbers;

    @Size(max = 100, message = "Lpa Number must not exceed 100 characters")
    private String lpoNo;
    private LocalDateTime lpoDate;

    private Long totalVolume;
    private Long totalNetVolume;
    private Long totalWeight;
    private Long totalNetWeight;
    private Long weightUnit;

    @Size(max = 20, message = "Unit Pack must not exceed 20 characters")
    private String unitPack;
    private Long totalNoOfPacks;
    private Long chargableWeight;
    private Long noOfPackBooked;
    private Long noOfPackArrived;

    @Size(max = 500, message = "Handling Info must not exceed 500 characters")
    private String handlingInfo;

    @Size(max = 500, message = "Other Details must not exceed 500 characters")
    private String otherDetails;

    @Size(max = 30, message = "Custom Declaration Number must not exceed 30 characters")
    private String customsDeclarationNo;

    @Size(max = 30, message = "Billing To must not exceed 30 characters")
    private String billingTo;
    private LovGetListDto billingToLov;

    private Long masterBlWeight;

    @Size(max = 20, message = "Master Bl Currency must not exceed 20 characters")
    private String masterBlCurrency;
    private Long totalCharges;

    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;

    @Size(max = 100, message = "Air Arrival Port must not exceed 100 characters")
    private String airArrivalport;

    @Size(max = 100, message = "Air Departure Port  must not exceed 100 characters")
    private String airDeparturePort;

    @Size(max = 20, message = "Carrier Code must not exceed 20 characters")
    private String carrierCode;
    private LovGetListDto carrierLov;

    @Size(max = 100, message = "Agent Details must not exceed 100 characters")
    private String agentDetails;

    private Long rateChanges;
    private Long agentCharges;

    @Size(max = 20, message = "Flight No 2 must not exceed 20 characters")
    private String flightNo2;
    private LocalDateTime flightDate2;

    @Size(max = 20, message = "Pri Sup code Old must not exceed 20 characters")
    private String priSupCodeOld;

    @Size(max = 20, message = "ShiprCng code Old must not exceed 20 characters")
    private String shiprCngCodeOld;

    @Size(max = 30, message = "FF Blading Number must not exceed 30 characters")
    private String ffBladingNo;

    @Size(max = 20, message = "Cf Inv Number Old must not exceed 20 characters")
    private String cfInvnoOld;

    @Size(max = 20, message = "Current Do Number must not exceed 20 characters")
    private String currentDoNo;

    @Size(max = 25, message = "Document Reference must not exceed 25 characters")
    private String docRef;

    @Size(max = 1, message = "Deleted Type must be exactly 1 character")
    private String deleted;

    @Size(max = 30, message = "Released type must not exceed 30 characters")
    private String releasedType;
    private LovGetListDto releasedTypeLov;
    private Long relasedSeqNo;

    @Size(max = 20, message = "Released Grant By must not exceed 20 characters")
    private String releasedGrantBy;

    @Size(max = 20, message = "Released Grant Date must not exceed 20 characters")
    private String releasedGrantDate;

    @Size(max = 200, message = "Released Grant Reason must not exceed 200 characters")
        private String releasedGrantReason;

    @Size(max = 30, message = "Cargo Description type must not exceed 30 characters")
    private String firstCarrier;

    @Size(max = 500, message = "Account Info type must not exceed 500 characters")
    private String accountInfo;

    @Size(max = 1, message = "Can Printed To Sent must be exactly 1 character")
    private String canPrinted;

    @Size(max = 1, message = "Do Printed To Sent must be exactly 1 character")
    private String doPrinted;

    @Size(max = 1, message = "Mabl Printed To Sent must be exactly 1 character")
    private String mablPrinted;

    @Size(max = 500, message = "Consigne manual must not exceed 500 characters")
    private String consigneManual;

    @Size(max = 150, message = "Notify Manual must not exceed 150 characters")
    private String notifyManual;

    @Size(max = 500, message = "Shipper Manual must not exceed 500 characters")
    private String shipperManual;

    @Size(max = 30, message = "Ofoq Mnf Reference must not exceed 30 characters")
    private String ofoqMnfRef;
    private Long principalAddrPoid;

    @Size(max = 1, message = "Show Notify Can must be exactly 1 character")
    private String showNotifyCan;

    @Size(max = 20, message = "Can Sent To must be exactly 1 character")
    private String canSentTo;

    @Size(max = 20, message = "Can Printed By must be exactly 1 character")
    private String canPrintedBy;

    private LocalDateTime canPrintedDt;

    @Size(max = 10, message = "FF Sh Job must not exceed 10 characters")
    private String ffShJob;
    private Long billToCustomerPoid;

    private LovGetListDto billToCustomerLov;

    @Size(max = 100, message = "Principal Manual must not exceed 100 characters")
    private String principalManual;

    @Size(max = 100, message = "Mother Vsl Final Delv must not exceed 100 characters")
    private String motherVslFinalDelv;

    @Size(max = 50, message = "Other Reference must not exceed 50 characters")
    private String otherReference;

    @Size(max = 50, message = "Received From must not exceed 50 characters")
    private String recievedFrom;

    @Size(max = 50, message = "Delivery To must not exceed 50 characters")
    private String deliveryTo;
    private Long projectPoid;
    private LovGetListDto projectLov;
    private Long projectCustomerPoid;
    private LovGetListDto projectCustomerPoidLov;

    private LocalDateTime blIssueDate;

    @Size(max = 25, message = "Air Transport must not exceed 25 characters")
    private String airTransPort;

    @Size(max = 30, message = "Air Transport 2 must not exceed 30 characters")
    private String airTransPort2;

    @Size(max = 30, message = "Second Carrier must not exceed 30 characters")
    private String secondCarrier;

    @Size(max = 30, message = "Third Carrier must not exceed 30 characters")
    private String thirdCarrier;

    @Size(max = 500, message = "Container Volume must not exceed 500 characters")
    private String containerVolume;

    @Size(max = 25, message = "Rate Class must not exceed 25 characters")
    private String rateClass;

    @Size(max = 25, message = "Kg Lb must not exceed 25 characters")
    private String kgLb;

    private LocalDateTime fcrDofCargoRcpt;

    @Size(max = 100, message = "Fcr Cargo Remarks must not exceed 100 characters")
    private String fcrCargoRemarks;

    @Size(max = 50, message = "Fcr Suplier Shipper Reference must not exceed 50 characters")
    private String fcrSuplierShipperRef;

    private Long coLoaderAgent;

    @Size(max = 100, message = "Inco Term must not exceed 100 characters")
    private String incoTerm;

    @Size(max = 100, message = "Document Status must not exceed 100 characters")
    private String documentStatus;

    @Size(max = 200, message = "Special Document Remarks must not exceed 200 characters")
    private String specialDocumentRemarks;

    private LocalDateTime deliveryDateFrom;
    private LocalDateTime deliveryDateTo;

    @Size(max = 1, message = "Custom Clearance Invoved must be exactly 1 character")
    private String customsClearanceInvoved;

    @Size(max = 1, message = "Road Transport must be exactly 1 character")
    private String roadTransport;

    private Integer doFreedays;

    @Size(max = 100, message = "Bayan Number must not exceed 100 characters")
    private String bayanNo;

    @Size(max = 25, message = "Bayan type must not exceed 25 characters")
    private String bayanType;
    
    private BigDecimal bayanAmount;
    private LocalDateTime bayanExpiry;
    private String bayanStatus;

    @Size(max = 100, message = "Policy Info must not exceed 100 characters")
    private String policyNo;

    @Size(max = 500, message = "Shipper Manual Edi must not exceed 500 characters")
    private String shipperManualEdi;

    @Size(max = 500, message = "Consign Manual Edi must not exceed 500 characters")
    private String consigneManualEdi;

    @Size(max = 1, message = "Passenger With Cargo must be exactly 1 character")
    private String passengerWithCargo;

    @Size(max = 1, message = "Radio Action must be exactly 1 character")
    private String radioAction;

    @Size(max = 1, message = "Perform Print USD must be exactly 1 character")
    private String performaPrintUSD;

    @Size(max = 1, message = "GLobal Tracking must be exactly 1 character")
    private String globalTracking;

    @Size(max = 100, message = "House Bl No 2 must not exceed 100 characters")
    private String houseBlNo2;

    @Size(max = 1, message = "Is Main Job must be exactly 1 character")
    private String isMainJob;
    private Long mainTransactionPoid;

    @Size(max = 1, message = "Hold Do must be exactly 1 character")
    private String holdDo;

    @Size(max = 100, message = "Can require To Sent must be exactly 100 character")
    private String holdDoUser;

    @Size(max = 100, message = "Shipment Mode must be exactly 100 character")
    private String shipmentMode;

    @Size(max = 100, message = "Transportation Mode must be exactly 100 character")
    private String transportationMode;

    @Size(max = 300, message = "Truck Transport From must be exactly 300 character")
    private String truckTransportFrom;

    @Size(max = 300, message = "Truck Transport To must be exactly 300 character")
    private String truckTransportTo;
}
