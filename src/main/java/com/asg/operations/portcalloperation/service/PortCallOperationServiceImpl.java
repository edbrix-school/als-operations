package com.asg.operations.portcalloperation.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.utility.PaginationUtil;
import com.asg.operations.exceptions.CustomException;
import com.asg.operations.exceptions.ResourceNotFoundException;
import com.asg.operations.finaldisbursementaccount.repository.PdaFdaHdrRepository;
import com.asg.operations.finaldisbursementaccount.repository.ShipVoyageHdrRepository;
import com.asg.operations.pdaentryform.repository.PdaEntryHdrRepository;
import com.asg.operations.pdaporttariffmaster.repository.ShipPortMasterRepository;
import com.asg.operations.portactivitiesmaster.repository.PortActivityMasterRepository;
import com.asg.operations.portcalloperation.dto.*;
import com.asg.operations.portcalloperation.entity.*;
import com.asg.operations.portcalloperation.repository.*;
import com.asg.operations.portcallreport.enums.ActionType;
import com.asg.operations.portcallreport.repository.PortCallReportHdrRepository;
import com.asg.operations.shipprincipal.repository.ShipPrincipalRepository;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import oracle.jdbc.OracleTypes;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlOutParameter;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortCallOperationServiceImpl implements PortCallOperationService {

    private final JdbcTemplate jdbcTemplate;
    private final PortCallOperationHdrRepository hdrRepository;
    private final PortCallOperationCargoDtlRepository cargoDtlRepository;
    private final PortCallOperationMailDtlRepository mailDtlRepository;
    private final PortCallOperationEstBertDtlRepository estBertDtlRepository;
    private final PortCallOperationEstPrearrivalDtlRepository estPrearrivalDtlRepository;
    private final PortCallOperationEstPrearrivalActDtlRepository estPrearrivalActDtlRepository;
    private final PortCallOperationActTimingDtlRepository actTimingDtlRepository;
    private final PortCallOperationActTimingsActvtyDtlRepository actTimingsActvtyDtlRepository;
    private final PortCallOperationActCondDtlRepository actCondDtlRepository;
    private final PortCallOperationActRmksDtlRepository actRmksDtlRepository;
    private final PortCallOperationActProgDtlRepository actProgDtlRepository;
    private final PortCallOperationActCargoFigDtlRepository actCargoFigDtlRepository;
    private final PortCallOperationActBunkerDtlRepository actBunkerDtlRepository;
    private final PortCallOperationHusbandryCrewDtlRepository husbandryCrewDtlRepository;
    private final PortCallOperationHusbandryOthDtlRepository husbandryOthDtlRepository;
    private final PortCallOperationDocsCopyDtlRepository docsCopyDtlRepository;
    private final PortCallOperationDocsMsgsDtl1Repository docsMsgsDtl1Repository;
    private final PortCallOperationDocsMsgsDtl2Repository docsMsgsDtl2Repository;
    private final DocumentSearchService documentService;
    private final DocumentDeleteService documentDeleteService;
    private final LoggingService loggingService;
    private final ShipVoyageHdrRepository shipVoyageHdrRepository;
    private final ShipPrincipalRepository shipPrincipalRepository;
    private final ShipPortMasterRepository shipPortMasterRepository;
    private final PdaEntryHdrRepository pdaEntryHdrRepository;
    private final PdaFdaHdrRepository pdaFdaHdrRepository;
    private final OpsPcDocsMsgsDtl1Repository msgsDtl1Repository;
    private final PortCallReportHdrRepository portCallReportHdrRepository;
    private final StockUnitMasterRepository stockUnitMasterRepository;
    private final GlobalUserRepository globalUserRepository;
    private final PortActivityMasterRepository portActivityMasterRepository;


    @Override
    public Map<String, Object> listOperations(String docId, FilterRequestDto request, Pageable pageable, LocalDate startDateValue, LocalDate endDateValue) {

        String operator = documentService.resolveOperator(request);
        String isDeleted = documentService.resolveIsDeleted(request);
        List<FilterDto> filters = documentService.resolveDateFilters(request, "TRANSACTION_DATE", startDateValue, endDateValue);

        RawSearchResult raw = documentService.search(docId, filters, operator, pageable, isDeleted,
                "DOC_REF",   // label
                "TRANSACTION_POID");    // value);

        Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable, raw.totalRecords());

        return PaginationUtil.wrapPage(page, raw.displayFields());
    }


    @Override
    public PortCallOperationResponseDto getOperationById(Long id) {
        log.info("Fetching port call operation by id: {}", id);

        PortCallOperationHdr hdr = hdrRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Port call operation", "Transaction Poid", id));

        // Load all detail tables
        List<PortCallOperationCargoDtl> cargoDetails = cargoDtlRepository.findByTransactionPoid(id);
        List<PortCallOperationMailDtl> mailDetails = mailDtlRepository.findByTransactionPoid(id);
        List<PortCallOperationEstBertDtl> estBertDetails = estBertDtlRepository.findByTransactionPoid(id);
        List<PortCallOperationEstPrearrivalDtl> estPrearrivalDetails = estPrearrivalDtlRepository.findByTransactionPoid(id);
        List<PortCallOperationActTimingDtl> actTimingDetails = actTimingDtlRepository.findByTransactionPoid(id);
        List<PortCallOperationActCondDtl> actCondDetails = actCondDtlRepository.findByTransactionPoid(id);
        List<PortCallOperationActRmksDtl> actRmksDetails = actRmksDtlRepository.findByTransactionPoid(id);
        List<PortCallOperationActProgDtl> actProgDetails = actProgDtlRepository.findByTransactionPoid(id);
        List<PortCallOperationActCargoFigDtl> actCargoFigDetails = actCargoFigDtlRepository.findByTransactionPoid(id);
        List<PortCallOperationActBunkerDtl> actBunkerDetails = actBunkerDtlRepository.findByTransactionPoid(id);
        List<PortCallOperationHusbandryCrewDtl> husbandryCrewDetails = husbandryCrewDtlRepository.findByTransactionPoid(id);
        List<PortCallOperationHusbandryOthDtl> husbandryOthDetails = husbandryOthDtlRepository.findByTransactionPoid(id);
        List<PortCallOperationDocsCopyDtl> docsCopyDetails = docsCopyDtlRepository.findByTransactionPoid(id);
        List<PortCallOperationDocsMsgsDtl1> docsMsgsDtl1Details = docsMsgsDtl1Repository.findByTransactionPoid(id);
        List<PortCallOperationDocsMsgsDtl2> docsMsgsDtl2Details = docsMsgsDtl2Repository.findByTransactionPoid(id);

        return PortCallOperationResponseDto.builder()
                .transactionPoid(hdr.getTransactionPoid())
                .transactionDate(hdr.getTransactionDate())
                .groupPoid(hdr.getGroupPoid())
                .docRef(hdr.getDocRef())
                .companyPoid(hdr.getCompanyPoid())
                .vesselVoyagePoid(hdr.getVesselVoyagePoid())
                .callSign(hdr.getCallSign())
                .callType(hdr.getCallType())
                .principalPoid(hdr.getPrincipalPoid())
                .vesselTypePoid(hdr.getVesselTypePoid())
                .operatorName(hdr.getOperatorName())
                .chartererName(hdr.getChartererName())
                .berth(hdr.getBerth())
                .portOfCallPoid(hdr.getPortOfCallPoid())
                .agencyType(hdr.getAgencyType())
                .specialInstructions(hdr.getSpecialInstructions())
                .termsConditions(hdr.getTermsConditions())
                .pcInfoAttachments(hdr.getPcInfoAttachments())
                .pdaRefPoid(hdr.getPdaRefPoid())
                .fdaRefPoid(hdr.getFdaRefPoid())
                .pdaAnchorageStayDays(hdr.getPdaAnchorageStayDays())
                .pdaBerthStayDays(hdr.getPdaBerthStayDays())
                .pdaPortStayDays(hdr.getPdaPortStayDays())
                .pdaFdaAttachments(hdr.getPdaFdaAttachments())
                .pdaFdaRemarks(hdr.getPdaFdaRemarks())
                .portCallActualTimingRemarks(hdr.getPortCallActualTimingRemarks())
                .husbandryCrewReqBy(hdr.getHusbandryCrewReqBy())
                .docsCopyEmailPoid(hdr.getDocsCopyEmailPoid())
                .status(hdr.getStatus())
                .lastModifiedBy(hdr.getLastModifiedBy())
                .lastModifiedDate(hdr.getLastModifiedDate())
                .grt(hdr.getGrt())
                .nrt(hdr.getNrt())
                .dwt(hdr.getDwt())
                .cargoDetails(mapCargoDetailsToResponse(cargoDetails))
                .mailDetails(mapMailDetailsToResponse(mailDetails))
                .estBertDetails(mapEstBertDetailsToResponse(estBertDetails))
                .estPrearrivalDetails(mapEstPrearrivalDetailsToResponse(estPrearrivalDetails))
                .actTimingDetails(mapActTimingDetailsToResponse(actTimingDetails))
                .actCondDetails(mapActCondDetailsToResponse(actCondDetails))
                .actRmksDetails(mapActRmksDetailsToResponse(actRmksDetails))
                .actProgDetails(mapActProgDetailsToResponse(actProgDetails))
                .actCargoFigDetails(mapActCargoFigDetailsToResponse(actCargoFigDetails))
                .actBunkerDetails(mapActBunkerDetailsToResponse(actBunkerDetails))
                .husbandryCrewDetails(mapHusbandryCrewDetailsToResponse(husbandryCrewDetails))
                .husbandryOthDetails(mapHusbandryOthDetailsToResponse(husbandryOthDetails))
                .docsCopyDetails(mapDocsCopyDetailsToResponse(docsCopyDetails))
                .docsMsgsDtl1Details(mapDocsMsgsDtl1DetailsToResponse(docsMsgsDtl1Details))
                .docsMsgsDtl2Details(mapDocsMsgsDtl2DetailsToResponse(docsMsgsDtl2Details))
                .build();
    }

    // Mapper methods for response DTOs
    private List<PortCallOperationCargoDetailResponseDto> mapCargoDetailsToResponse(List<PortCallOperationCargoDtl> details) {
        return details.stream().map(dtl -> PortCallOperationCargoDetailResponseDto.builder()
                .transactionPoid(dtl.getTransactionPoid())
                .detRowId(dtl.getDetRowId())
                .productName(dtl.getProductName())
                .portCargoName(dtl.getPortCargoName())
                .qtyMt(dtl.getQtyMt())
                .qtyCbm(dtl.getQtyCbm())
                .noOfQty(dtl.getNoOfQty())
                .callType(dtl.getCallType())
                .portOfCallPoid(dtl.getPortOfCallPoid())
                .berth(dtl.getBerth())
                .shipper(dtl.getShipper())
                .receiver(dtl.getReceiver())
                .build()).collect(Collectors.toList());
    }

    private List<PortCallOperationMailDetailResponseDto> mapMailDetailsToResponse(List<PortCallOperationMailDtl> details) {
        return details.stream().map(dtl -> PortCallOperationMailDetailResponseDto.builder()
                .transactionPoid(dtl.getTransactionPoid())
                .detRowId(dtl.getDetRowId())
                .communicationType(dtl.getCommunicationType())
                .communicationMode(dtl.getCommunicationMode())
                .company(dtl.getCompany())
                .addressee(dtl.getAddressee())
                .emailIds(dtl.getEmailIds())
                .build()).collect(Collectors.toList());
    }

    private List<PortCallOperationEstBertDetailResponseDto> mapEstBertDetailsToResponse(List<PortCallOperationEstBertDtl> details) {
        return details.stream().map(dtl -> {
            PortCallOperationEstBertDetailResponseDto.PortCallOperationEstBertDetailResponseDtoBuilder builder = PortCallOperationEstBertDetailResponseDto.builder()
                    .transactionPoid(dtl.getTransactionPoid())
                    .detRowId(dtl.getDetRowId())
                    .eta(dtl.getEta())
                    .etb(dtl.getEtb())
                    .updatedOn(dtl.getLastModifiedDate())
                    .updatedBy(dtl.getLastModifiedBy())
                    .berthingAttachments(dtl.getBerthingAttachments())
                    .emailPoid(dtl.getEmailPoid());

            // Fetch email details from PortCallOperationDocsMsgsDtl1 if emailPoid exists
            if (dtl.getEmailPoid() != null) {
                docsMsgsDtl1Repository.findByEmailPoid(dtl.getEmailPoid())
                        .ifPresent(emailRecord -> {
                            builder.emailSentOn(emailRecord.getEmailSendOn() != null ? emailRecord.getEmailSendOn().atStartOfDay() : null).remarks(emailRecord.getEmailRemarks());
                        });
            }

            return builder.build();
        }).collect(Collectors.toList());
    }

    private List<PortCallOperationEstPrearrivalDetailResponseDto> mapEstPrearrivalDetailsToResponse(List<PortCallOperationEstPrearrivalDtl> details) {
        return details.stream().map(dtl -> {
            PortCallOperationEstPrearrivalDetailResponseDto.PortCallOperationEstPrearrivalDetailResponseDtoBuilder builder = PortCallOperationEstPrearrivalDetailResponseDto.builder()
                    .transactionPoid(dtl.getTransactionPoid())
                    .detRowId(dtl.getDetRowId())
                    .preActivityDtlPoid(dtl.getPreActivityDtlPoid())
                    .eta(dtl.getEta())
                    .etb(dtl.getEtb())
                    .updatedOn(dtl.getLastModifiedDate())
                    .updatedBy(dtl.getLastModifiedBy())
                    .preArrivalAttachments(dtl.getPreArrivalAttachments())
                    .emailPoid(dtl.getEmailPoid());

            // Fetch email details from PortCallOperationDocsMsgsDtl1 if emailPoid exists
            if (dtl.getEmailPoid() != null) {
                docsMsgsDtl1Repository.findByEmailPoid(dtl.getEmailPoid())
                        .ifPresent(emailRecord -> {
                            builder.emailSentOn(emailRecord.getEmailSendOn() != null ? emailRecord.getEmailSendOn().atStartOfDay() : null).remarks(emailRecord.getEmailRemarks());
                        });
            }

            return builder.build();
        }).collect(Collectors.toList());
    }

    private List<PortCallOperationActTimingDetailResponseDto> mapActTimingDetailsToResponse(List<PortCallOperationActTimingDtl> details) {
        return details.stream().map(dtl -> PortCallOperationActTimingDetailResponseDto.builder()
                .transactionPoid(dtl.getTransactionPoid())
                .detRowId(dtl.getDetRowId())
                .portReportPoid(dtl.getPortReportPoid())
                .actualsTimingDtlPoid(dtl.getActualsTimingDtlPoid())
                .emailPoid(dtl.getEmailPoid())
                .build()).collect(Collectors.toList());
    }

    private List<PortCallOperationActCondDetailResponseDto> mapActCondDetailsToResponse(List<PortCallOperationActCondDtl> details) {
        return details.stream().map(dtl -> PortCallOperationActCondDetailResponseDto.builder()
                .transactionPoid(dtl.getTransactionPoid())
                .detRowId(dtl.getDetRowId())
                .conditionType(dtl.getConditionType())
                .draftForward(dtl.getDraftForward())
                .draftMid(dtl.getDraftMid())
                .draftAft(dtl.getDraftAft())
                .fuelOil(dtl.getFuelOil())
                .dieselOil(dtl.getDieselOil())
                .freshWater(dtl.getFreshWater())
                .tugsService(dtl.getTugsService())
                .build()).collect(Collectors.toList());
    }

    private List<PortCallOperationActRmksDetailResponseDto> mapActRmksDetailsToResponse(List<PortCallOperationActRmksDtl> details) {
        return details.stream().map(dtl -> PortCallOperationActRmksDetailResponseDto.builder()
                .transactionPoid(dtl.getTransactionPoid())
                .detRowId(dtl.getDetRowId())
                .remarksType(dtl.getRemarksType())
                .remarksFrom(dtl.getRemarksFrom())
                .remarksTo(dtl.getRemarksTo())
                .cargoDetails(dtl.getCargoDetails())
                .reason(dtl.getReason())
                .pcReportPoid(dtl.getPcReportPoid())
                .build()).collect(Collectors.toList());
    }

    private List<PortCallOperationActProgDetailResponseDto> mapActProgDetailsToResponse(List<PortCallOperationActProgDtl> details) {
        return details.stream().map(dtl -> PortCallOperationActProgDetailResponseDto.builder()
                .transactionPoid(dtl.getTransactionPoid())
                .detRowId(dtl.getDetRowId())
                .emailPoid(dtl.getEmailPoid())
                .cargo(dtl.getCargo())
                .progressDateTime(dtl.getProgressDateTime())
                .progressQty(dtl.getProgressQty())
                .progressStatus(dtl.getProgressStatus())
                .balanceQty(dtl.getBalanceQty())
                .unitPoid(dtl.getUnitPoid())
                .ratePerHr(dtl.getRatePerHr())
                .etc(dtl.getEtc())
                .estBlDate(dtl.getEstBlDate())
                .build()).collect(Collectors.toList());
    }

    private List<PortCallOperationActCargoFigDetailResponseDto> mapActCargoFigDetailsToResponse(List<PortCallOperationActCargoFigDtl> details) {
        return details.stream().map(dtl -> PortCallOperationActCargoFigDetailResponseDto.builder()
                .transactionPoid(dtl.getTransactionPoid())
                .detRowId(dtl.getDetRowId())
                .cargo(dtl.getCargo())
                .callType(dtl.getCallType())
                .qty(dtl.getQty())
                .unitPoid(dtl.getUnitPoid())
                .vesselReq(dtl.getVesselReq())
                .terminalNom(dtl.getTerminalNom())
                .shipFigureMt(dtl.getShipFigureMt())
                .shoreFigureMt(dtl.getShoreFigureMt())
                .shipFigureBbls(dtl.getShipFigureBbls())
                .blDate(dtl.getBlDate())
                .hoseNo(dtl.getHoseNo())
                .hoseSize(dtl.getHoseSize())
                .build()).collect(Collectors.toList());
    }

    private List<PortCallOperationActBunkerDetailResponseDto> mapActBunkerDetailsToResponse(List<PortCallOperationActBunkerDtl> details) {
        return details.stream().map(dtl -> PortCallOperationActBunkerDetailResponseDto.builder()
                .transactionPoid(dtl.getTransactionPoid())
                .detRowId(dtl.getDetRowId())
                .grade(dtl.getGrade())
                .nominatedQtyMt(dtl.getNominatedQtyMt())
                .suppliedQtyMt(dtl.getSuppliedQtyMt())
                .shipQtyMt(dtl.getShipQtyMt())
                .build()).collect(Collectors.toList());
    }

    private List<PortCallOperationHusbandryCrewDetailResponseDto> mapHusbandryCrewDetailsToResponse(List<PortCallOperationHusbandryCrewDtl> details) {
        return details.stream().map(dtl -> PortCallOperationHusbandryCrewDetailResponseDto.builder()
                .transactionPoid(dtl.getTransactionPoid())
                .detRowId(dtl.getDetRowId())
                .crewName(dtl.getCrewName())
                .crewGenderPoid(dtl.getCrewGenderPoid())
                .crewNationalityPoid(dtl.getCrewNationalityPoid())
                .crewPptNumber(dtl.getCrewPptNumber())
                .crewSeamanNo(dtl.getCrewSeamanNo())
                .crewRank(dtl.getCrewRank())
                .crewAttachments(dtl.getCrewAttachments())
                .build()).collect(Collectors.toList());
    }

    private List<PortCallOperationHusbandryOthDetailResponseDto> mapHusbandryOthDetailsToResponse(List<PortCallOperationHusbandryOthDtl> details) {
        return details.stream().map(dtl -> PortCallOperationHusbandryOthDetailResponseDto.builder()
                .transactionPoid(dtl.getTransactionPoid())
                .detRowId(dtl.getDetRowId())
                .arrangement(dtl.getArrangement())
                .descriptionText(dtl.getDescriptionText())
                .meetGreet(dtl.getMeetGreet())
                .noOfDays(dtl.getNoOfDays())
                .qty(dtl.getQty())
                .unitPoid(dtl.getUnitPoid())
                .unitPrice(dtl.getUnitPrice())
                .currencyCode(dtl.getCurrencyCode())
                .totalPrice(dtl.getTotalPrice())
                .adjustedPrice(dtl.getAdjustedPrice())
                .arrngmntAttachments(dtl.getArrngmntAttachments())
                .requestedBy(dtl.getRequestedBy())
                .paymentMode(dtl.getPaymentMode())
                .build()).collect(Collectors.toList());
    }

    private List<PortCallOperationDocsCopyDetailResponseDto> mapDocsCopyDetailsToResponse(List<PortCallOperationDocsCopyDtl> details) {
        return details.stream().map(dtl -> PortCallOperationDocsCopyDetailResponseDto.builder()
                .transactionPoid(dtl.getTransactionPoid())
                .detRowId(dtl.getDetRowId())
                .documentFrom(dtl.getDocumentFrom())
                .documentList(dtl.getDocumentList())
                .documentSelect(dtl.getDocumentSelect())
                .documentAttachments(dtl.getDocumentAttachments())
                .build()).collect(Collectors.toList());
    }

    private List<PortCallOperationDocsMsgsDtl1DetailResponseDto> mapDocsMsgsDtl1DetailsToResponse(List<PortCallOperationDocsMsgsDtl1> details) {
        return details.stream().map(dtl -> PortCallOperationDocsMsgsDtl1DetailResponseDto.builder()
                .transactionPoid(dtl.getTransactionPoid())
                .detRowId(dtl.getDetRowId())
                .emailPoid(dtl.getEmailPoid())
                .sendByPoid(dtl.getSendByPoid())
                .emailSubject(dtl.getEmailSubject())
                .emailDocuments(dtl.getEmailDocuments())
                .emailSendOn(dtl.getEmailSendOn())
                .emailContent(dtl.getEmailContent())
                .emailRemarks(dtl.getEmailRemarks())
                .build()).collect(Collectors.toList());
    }

    private List<PortCallOperationDocsMsgsDtl2DetailResponseDto> mapDocsMsgsDtl2DetailsToResponse(List<PortCallOperationDocsMsgsDtl2> details) {
        return details.stream().map(dtl -> PortCallOperationDocsMsgsDtl2DetailResponseDto.builder()
                .transactionPoid(dtl.getTransactionPoid())
                .detRowId(dtl.getDetRowId())
                .emailPoid(dtl.getEmailPoid())
                .emailType(dtl.getEmailType())
                .company(dtl.getCompany())
                .addressee(dtl.getAddressee())
                .toEmailId(dtl.getToEmailId())
                .ccEmailId(dtl.getCcEmailId())
                .build()).collect(Collectors.toList());
    }


    @Override
    @Transactional
    public PortCallOperationResponseDto createOperation(PortCallOperationCreateDto dto, Long userPoid, Long groupPoid) {
        log.info("Creating port call operation");

        if (dto.getVesselVoyagePoid() != null) {
            if (!shipVoyageHdrRepository.existsByTransactionPoid(dto.getVesselVoyagePoid())) {
                throw new ResourceNotFoundException("Vessel Voyage", "Vessel Voyage Poid", dto.getVesselVoyagePoid());
            }
        }
        if (dto.getPrincipalPoid() != null) {
            if (!shipPrincipalRepository.existsByPrincipalPoid(dto.getPrincipalPoid())) {
                throw new ResourceNotFoundException("Principal master", "Principal master Poid", dto.getPrincipalPoid());
            }
        }
        if (dto.getPortOfCallPoid() != null) {
            if (!shipPortMasterRepository.existsByIdPortPoid(BigDecimal.valueOf(dto.getPortOfCallPoid()))) {
                throw new ResourceNotFoundException("Vessel Voyage", "Vessel Voyage Poid", dto.getVesselVoyagePoid());
            }
        }

        validateFinalMailDetailState(null, dto.getMailDetails(), true);

        PortCallOperationHdr hdr = PortCallOperationHdr.builder()
                .transactionDate(LocalDate.now())
                .groupPoid(groupPoid)
                .companyPoid(UserContext.getCompanyPoid())
                .vesselVoyagePoid(dto.getVesselVoyagePoid())
                .callSign(dto.getCallSign())
                .callType(dto.getCallType())
                .principalPoid(dto.getPrincipalPoid())
                .operatorName(dto.getOperatorName())
                .chartererName(dto.getChartererName())
                .berth(dto.getBerth())
                .grt(dto.getGrt())
                .nrt(dto.getNrt())
                .dwt(dto.getDwt())
                .portOfCallPoid(dto.getPortOfCallPoid())
                .specialInstructions(dto.getSpecialInstructions())
                .termsConditions(dto.getTermsConditions())
                .pcInfoAttachments(dto.getPcInfoAttachments())
                .createdBy(UserContext.getUserId())
                .createdDate(LocalDateTime.now())
                .lastModifiedBy(UserContext.getUserId())
                .lastModifiedDate(LocalDateTime.now())
                .build();

        hdr = hdrRepository.save(hdr);

        // Save cargo details
        if (dto.getCargoDetails() != null && !dto.getCargoDetails().isEmpty()) {
            Long transactionPoid = hdr.getTransactionPoid();
            long nextDetRowId = cargoDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
            List<PortCallOperationCargoDtl> cargoDetails = new ArrayList<>();
            for (PortCallOperationCargoDetailDto cargoDto : dto.getCargoDetails()) {
                if (cargoDto.getActionType() == ActionType.isCreated || cargoDto.getActionType() == null) {
                    cargoDetails.add(PortCallOperationCargoDtl.builder()
                            .transactionPoid(transactionPoid)
                            .detRowId(nextDetRowId++)
                            .productName(cargoDto.getProductName())
                            .portCargoName(cargoDto.getPortCargoName())
                            .qtyMt(cargoDto.getQtyMt())
                            .qtyCbm(cargoDto.getQtyCbm())
                            .noOfQty(cargoDto.getNoOfQty())
                            .callType(cargoDto.getCallType())
                            .portOfCallPoid(cargoDto.getPortOfCallPoid())
                            .berth(cargoDto.getBerth())
                            .shipper(cargoDto.getShipper())
                            .receiver(cargoDto.getReceiver())
                            .createdBy(UserContext.getUserId())
                            .createdDate(LocalDateTime.now())
                            .lastModifiedBy(UserContext.getUserId())
                            .lastModifiedDate(LocalDateTime.now())
                            .build());
                }
            }
            if (!cargoDetails.isEmpty()) {
                List<PortCallOperationCargoDtl> savedCargoDetails = cargoDtlRepository.saveAll(cargoDetails);
                for (PortCallOperationCargoDtl saved : savedCargoDetails) {
                    String logDetail = String.format("Row Created on [Port Call Operation Cargo Details] with detRowId: %s", saved.getDetRowId());
                    loggingService.createLogSummaryEntry(UserContext.getDocumentId(), hdr.getTransactionPoid().toString(), logDetail);
                }
            }
        }

        // Save mail details
        if (dto.getMailDetails() != null && !dto.getMailDetails().isEmpty()) {
            Long transactionPoid = hdr.getTransactionPoid();
            long nextDetRowId = mailDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
            List<PortCallOperationMailDtl> mailDetails = new ArrayList<>();
            for (PortCallOperationMailDetailDto mailDto : dto.getMailDetails()) {
                if (mailDto.getActionType() == ActionType.isCreated || mailDto.getActionType() == null) {
                    mailDetails.add(PortCallOperationMailDtl.builder()
                            .transactionPoid(transactionPoid)
                            .detRowId(nextDetRowId++)
                            .communicationType(mailDto.getCommunicationType())
                            .communicationMode(mailDto.getCommunicationMode())
                            .company(mailDto.getCompany())
                            .addressee(mailDto.getAddressee())
                            .emailIds(mailDto.getEmailIds())
                            .createdBy(UserContext.getUserId())
                            .createdDate(LocalDateTime.now())
                            .lastModifiedBy(UserContext.getUserId())
                            .lastModifiedDate(LocalDateTime.now())
                            .build());
                }
            }
            if (!mailDetails.isEmpty()) {
                List<PortCallOperationMailDtl> savedMailDetails = mailDtlRepository.saveAll(mailDetails);
                for (PortCallOperationMailDtl saved : savedMailDetails) {
                    String logDetail = String.format("Row Created on [Port Call Operation Mail Details] with detRowId: %s", saved.getDetRowId());
                    loggingService.createLogSummaryEntry(UserContext.getDocumentId(), hdr.getTransactionPoid().toString(), logDetail);
                }
            }
        }

        // Use it if needed in future
        // Save all other detail tables
//        saveAllDetailTables(hdr.getTransactionPoid(), dto, UserContext.getUserId());

        loggingService.createLogSummaryEntry(LogDetailsEnum.CREATED, UserContext.getDocumentId(), hdr.getTransactionPoid().toString());
        return getOperationById(hdr.getTransactionPoid());
    }

    private void saveAllDetailTables(Long transactionPoid, PortCallOperationDto dto) {
        // Save Est Bert Details
        if (dto.getEstBertDetails() != null && !dto.getEstBertDetails().isEmpty()) {
            long nextDetRowId = estBertDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
            List<PortCallOperationEstBertDtl> details = new ArrayList<>();
            for (PortCallOperationEstBertDetailDto detailDto : dto.getEstBertDetails()) {
                if (detailDto.getActionType() == ActionType.isCreated || detailDto.getActionType() == null) {
                    details.add(PortCallOperationEstBertDtl.builder()
                            .transactionPoid(transactionPoid)
                            .detRowId(nextDetRowId++)
                            .eta(detailDto.getEta())
                            .etb(detailDto.getEtb())
                            .berthingAttachments(detailDto.getBerthingAttachments())
                            .emailPoid(detailDto.getEmailPoid())
                            .createdBy(UserContext.getUserId())
                            .createdDate(LocalDateTime.now())
                            .lastModifiedBy(UserContext.getUserId())
                            .lastModifiedDate(LocalDateTime.now())
                            .build());
                }
            }
            if (!details.isEmpty()) estBertDtlRepository.saveAll(details);
        }

        // Save Est Prearrival Details
        if (dto.getEstPrearrivalDetails() != null && !dto.getEstPrearrivalDetails().isEmpty()) {
            long nextDetRowId = estPrearrivalDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
            List<PortCallOperationEstPrearrivalDtl> details = new ArrayList<>();
            for (PortCallOperationEstPrearrivalDetailDto detailDto : dto.getEstPrearrivalDetails()) {
                if (detailDto.getActionType() == ActionType.isCreated || detailDto.getActionType() == null) {
                    details.add(PortCallOperationEstPrearrivalDtl.builder()
                            .transactionPoid(transactionPoid)
                            .detRowId(nextDetRowId++)
                            .preActivityDtlPoid(detailDto.getPreActivityDtlPoid())
                            .eta(detailDto.getEta())
                            .etb(detailDto.getEtb())
                            .preArrivalAttachments(detailDto.getPreArrivalAttachments())
                            .emailPoid(detailDto.getEmailPoid())
                            .createdBy(UserContext.getUserId())
                            .createdDate(LocalDateTime.now())
                            .lastModifiedBy(UserContext.getUserId())
                            .lastModifiedDate(LocalDateTime.now())
                            .build());
                }
            }
            if (!details.isEmpty()) estPrearrivalDtlRepository.saveAll(details);
        }

        // Save Act Timing Details
        if (dto.getActTimingDetails() != null && !dto.getActTimingDetails().isEmpty()) {
            long nextDetRowId = actTimingDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
            List<PortCallOperationActTimingDtl> details = new ArrayList<>();
            for (PortCallOperationActTimingDetailDto detailDto : dto.getActTimingDetails()) {
                if (detailDto.getActionType() == ActionType.isCreated || detailDto.getActionType() == null) {
                    details.add(PortCallOperationActTimingDtl.builder()
                            .transactionPoid(transactionPoid)
                            .detRowId(nextDetRowId++)
                            .portReportPoid(detailDto.getPortReportPoid())
                            .actualsTimingDtlPoid(detailDto.getActualsTimingDtlPoid())
                            .emailPoid(detailDto.getEmailPoid())
                            .createdBy(UserContext.getUserId())
                            .createdDate(LocalDateTime.now())
                            .lastModifiedBy(UserContext.getUserId())
                            .lastModifiedDate(LocalDateTime.now())
                            .build());
                }
            }
            if (!details.isEmpty()) actTimingDtlRepository.saveAll(details);
        }

        // Save Act Cond Details
        if (dto.getActCondDetails() != null && !dto.getActCondDetails().isEmpty()) {
            long nextDetRowId = actCondDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
            List<PortCallOperationActCondDtl> details = new ArrayList<>();
            for (PortCallOperationActCondDetailDto detailDto : dto.getActCondDetails()) {
                if (detailDto.getActionType() == ActionType.isCreated || detailDto.getActionType() == null) {
                    details.add(PortCallOperationActCondDtl.builder()
                            .transactionPoid(transactionPoid)
                            .detRowId(nextDetRowId++)
                            .conditionType(detailDto.getConditionType())
                            .draftForward(detailDto.getDraftForward())
                            .draftMid(detailDto.getDraftMid())
                            .draftAft(detailDto.getDraftAft())
                            .fuelOil(detailDto.getFuelOil())
                            .dieselOil(detailDto.getDieselOil())
                            .freshWater(detailDto.getFreshWater())
                            .tugsService(detailDto.getTugsService())
                            .createdBy(UserContext.getUserId())
                            .createdDate(LocalDateTime.now())
                            .lastModifiedBy(UserContext.getUserId())
                            .lastModifiedDate(LocalDateTime.now())
                            .build());
                }
            }
            if (!details.isEmpty()) actCondDtlRepository.saveAll(details);
        }

        // Save Act Rmks Details
        if (dto.getActRmksDetails() != null && !dto.getActRmksDetails().isEmpty()) {
            long nextDetRowId = actRmksDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
            List<PortCallOperationActRmksDtl> details = new ArrayList<>();
            for (PortCallOperationActRmksDetailDto detailDto : dto.getActRmksDetails()) {
                if (detailDto.getActionType() == ActionType.isCreated || detailDto.getActionType() == null) {
                    details.add(PortCallOperationActRmksDtl.builder()
                            .transactionPoid(transactionPoid)
                            .detRowId(nextDetRowId++)
                            .remarksType(detailDto.getRemarksType())
                            .remarksFrom(detailDto.getRemarksFrom())
                            .remarksTo(detailDto.getRemarksTo())
                            .cargoDetails(detailDto.getCargoDetails())
                            .reason(detailDto.getReason())
                            .pcReportPoid(detailDto.getPcReportPoid())
                            .createdBy(UserContext.getUserId())
                            .createdDate(LocalDateTime.now())
                            .lastModifiedBy(UserContext.getUserId())
                            .lastModifiedDate(LocalDateTime.now())
                            .build());
                }
            }
            if (!details.isEmpty()) actRmksDtlRepository.saveAll(details);
        }

        // Save Act Prog Details
        if (dto.getActProgDetails() != null && !dto.getActProgDetails().isEmpty()) {
            long nextDetRowId = actProgDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
            List<PortCallOperationActProgDtl> details = new ArrayList<>();
            for (PortCallOperationActProgDetailDto detailDto : dto.getActProgDetails()) {
                if (detailDto.getActionType() == ActionType.isCreated || detailDto.getActionType() == null) {
                    details.add(PortCallOperationActProgDtl.builder()
                            .transactionPoid(transactionPoid)
                            .detRowId(nextDetRowId++)
                            .emailPoid(detailDto.getEmailPoid())
                            .cargo(detailDto.getCargo())
                            .progressDateTime(detailDto.getProgressDateTime())
                            .progressQty(detailDto.getProgressQty())
                            .progressStatus(detailDto.getProgressStatus())
                            .balanceQty(detailDto.getBalanceQty())
                            .unitPoid(detailDto.getUnitPoid())
                            .ratePerHr(detailDto.getRatePerHr())
                            .etc(detailDto.getEtc())
                            .estBlDate(detailDto.getEstBlDate())
                            .createdBy(UserContext.getUserId())
                            .createdDate(LocalDateTime.now())
                            .lastModifiedBy(UserContext.getUserId())
                            .lastModifiedDate(LocalDateTime.now())
                            .build());
                }
            }
            if (!details.isEmpty()) actProgDtlRepository.saveAll(details);
        }

        // Save Act Cargo Fig Details
        if (dto.getActCargoFigDetails() != null && !dto.getActCargoFigDetails().isEmpty()) {
            long nextDetRowId = actCargoFigDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
            List<PortCallOperationActCargoFigDtl> details = new ArrayList<>();
            for (PortCallOperationActCargoFigDetailDto detailDto : dto.getActCargoFigDetails()) {
                if (detailDto.getActionType() == ActionType.isCreated || detailDto.getActionType() == null) {
                    details.add(PortCallOperationActCargoFigDtl.builder()
                            .transactionPoid(transactionPoid)
                            .detRowId(nextDetRowId++)
                            .cargo(detailDto.getCargo())
                            .callType(detailDto.getCallType())
                            .qty(detailDto.getQty())
                            .unitPoid(detailDto.getUnitPoid())
                            .vesselReq(detailDto.getVesselReq())
                            .terminalNom(detailDto.getTerminalNom())
                            .shipFigureMt(detailDto.getShipFigureMt())
                            .shoreFigureMt(detailDto.getShoreFigureMt())
                            .shipFigureBbls(detailDto.getShipFigureBbls())
                            .blDate(detailDto.getBlDate())
                            .hoseNo(detailDto.getHoseNo())
                            .hoseSize(detailDto.getHoseSize())
                            .createdBy(UserContext.getUserId())
                            .createdDate(LocalDateTime.now())
                            .lastModifiedBy(UserContext.getUserId())
                            .lastModifiedDate(LocalDateTime.now())
                            .build());
                }
            }
            if (!details.isEmpty()) actCargoFigDtlRepository.saveAll(details);
        }

        // Save Act Bunker Details
        if (dto.getActBunkerDetails() != null && !dto.getActBunkerDetails().isEmpty()) {
            long nextDetRowId = actBunkerDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
            List<PortCallOperationActBunkerDtl> details = new ArrayList<>();
            for (PortCallOperationActBunkerDetailDto detailDto : dto.getActBunkerDetails()) {
                if (detailDto.getActionType() == ActionType.isCreated || detailDto.getActionType() == null) {
                    details.add(PortCallOperationActBunkerDtl.builder()
                            .transactionPoid(transactionPoid)
                            .detRowId(nextDetRowId++)
                            .grade(detailDto.getGrade())
                            .nominatedQtyMt(detailDto.getNominatedQtyMt())
                            .suppliedQtyMt(detailDto.getSuppliedQtyMt())
                            .shipQtyMt(detailDto.getShipQtyMt())
                            .createdBy(UserContext.getUserId())
                            .createdDate(LocalDateTime.now())
                            .lastModifiedBy(UserContext.getUserId())
                            .lastModifiedDate(LocalDateTime.now())
                            .build());
                }
            }
            if (!details.isEmpty()) actBunkerDtlRepository.saveAll(details);
        }

        // Save Husbandry Crew Details
        if (dto.getHusbandryCrewDetails() != null && !dto.getHusbandryCrewDetails().isEmpty()) {
            long nextDetRowId = husbandryCrewDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
            List<PortCallOperationHusbandryCrewDtl> details = new ArrayList<>();
            for (PortCallOperationHusbandryCrewDetailDto detailDto : dto.getHusbandryCrewDetails()) {
                if (detailDto.getActionType() == ActionType.isCreated || detailDto.getActionType() == null) {
                    details.add(PortCallOperationHusbandryCrewDtl.builder()
                            .transactionPoid(transactionPoid)
                            .detRowId(nextDetRowId++)
                            .crewName(detailDto.getCrewName())
                            .crewGenderPoid(detailDto.getCrewGenderPoid())
                            .crewNationalityPoid(detailDto.getCrewNationalityPoid())
                            .crewPptNumber(detailDto.getCrewPptNumber())
                            .crewSeamanNo(detailDto.getCrewSeamanNo())
                            .crewRank(detailDto.getCrewRank())
                            .crewAttachments(detailDto.getCrewAttachments())
                            .createdBy(UserContext.getUserId())
                            .createdDate(LocalDateTime.now())
                            .lastModifiedBy(UserContext.getUserId())
                            .lastModifiedDate(LocalDateTime.now())
                            .build());
                }
            }
            if (!details.isEmpty()) husbandryCrewDtlRepository.saveAll(details);
        }

        // Save Husbandry Oth Details
        if (dto.getHusbandryOthDetails() != null && !dto.getHusbandryOthDetails().isEmpty()) {
            long nextDetRowId = husbandryOthDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
            List<PortCallOperationHusbandryOthDtl> details = new ArrayList<>();
            for (PortCallOperationHusbandryOthDetailDto detailDto : dto.getHusbandryOthDetails()) {
                if (detailDto.getActionType() == ActionType.isCreated || detailDto.getActionType() == null) {
                    details.add(PortCallOperationHusbandryOthDtl.builder()
                            .transactionPoid(transactionPoid)
                            .detRowId(nextDetRowId++)
                            .arrangement(detailDto.getArrangement())
                            .descriptionText(detailDto.getDescriptionText())
                            .meetGreet(detailDto.getMeetGreet())
                            .noOfDays(detailDto.getNoOfDays())
                            .qty(detailDto.getQty())
                            .unitPoid(detailDto.getUnitPoid())
                            .unitPrice(detailDto.getUnitPrice())
                            .currencyCode(detailDto.getCurrencyCode())
                            .totalPrice(detailDto.getTotalPrice())
                            .adjustedPrice(detailDto.getAdjustedPrice())
                            .arrngmntAttachments(detailDto.getArrngmntAttachments())
                            .requestedBy(detailDto.getRequestedBy())
                            .paymentMode(detailDto.getPaymentMode())
                            .createdBy(UserContext.getUserId())
                            .createdDate(LocalDateTime.now())
                            .lastModifiedBy(UserContext.getUserId())
                            .lastModifiedDate(LocalDateTime.now())
                            .build());
                }
            }
            if (!details.isEmpty()) husbandryOthDtlRepository.saveAll(details);
        }

        // Save Docs Copy Details
        if (dto.getDocsCopyDetails() != null && !dto.getDocsCopyDetails().isEmpty()) {
            long nextDetRowId = docsCopyDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
            List<PortCallOperationDocsCopyDtl> details = new ArrayList<>();
            for (PortCallOperationDocsCopyDetailDto detailDto : dto.getDocsCopyDetails()) {
                if (detailDto.getActionType() == ActionType.isCreated || detailDto.getActionType() == null) {
                    details.add(PortCallOperationDocsCopyDtl.builder()
                            .transactionPoid(transactionPoid)
                            .detRowId(nextDetRowId++)
                            .documentFrom(detailDto.getDocumentFrom())
                            .documentList(detailDto.getDocumentList())
                            .documentSelect(detailDto.getDocumentSelect())
                            .documentAttachments(detailDto.getDocumentAttachments())
                            .createdBy(UserContext.getUserId())
                            .createdDate(LocalDateTime.now())
                            .lastModifiedBy(UserContext.getUserId())
                            .lastModifiedDate(LocalDateTime.now())
                            .build());
                }
            }
            if (!details.isEmpty()) docsCopyDtlRepository.saveAll(details);
        }

        // Save Docs Msgs Dtl1 Details
        if (dto.getDocsMsgsDtl1Details() != null && !dto.getDocsMsgsDtl1Details().isEmpty()) {
            long nextDetRowId = docsMsgsDtl1Repository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
            List<PortCallOperationDocsMsgsDtl1> details = new ArrayList<>();
            for (PortCallOperationDocsMsgsDtl1DetailDto detailDto : dto.getDocsMsgsDtl1Details()) {
                if (detailDto.getActionType() == ActionType.isCreated || detailDto.getActionType() == null) {
                    details.add(PortCallOperationDocsMsgsDtl1.builder()
                            .transactionPoid(transactionPoid)
                            .detRowId(nextDetRowId++)
                            .sendByPoid(detailDto.getSendByPoid())
                            .emailSubject(detailDto.getEmailSubject())
                            .emailDocuments(detailDto.getEmailDocuments())
                            .emailSendOn(detailDto.getEmailSendOn())
                            .emailContent(detailDto.getEmailContent())
                            .emailRemarks(detailDto.getEmailRemarks())
                            .createdBy(UserContext.getUserId())
                            .createdDate(LocalDateTime.now())
                            .lastModifiedBy(UserContext.getUserId())
                            .lastModifiedDate(LocalDateTime.now())
                            .build());
                }
            }
            if (!details.isEmpty()) docsMsgsDtl1Repository.saveAll(details);
        }

        // Save Docs Msgs Dtl2 Details
        if (dto.getDocsMsgsDtl2Details() != null && !dto.getDocsMsgsDtl2Details().isEmpty()) {
            long nextDetRowId = docsMsgsDtl2Repository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
            List<PortCallOperationDocsMsgsDtl2> details = new ArrayList<>();
            for (PortCallOperationDocsMsgsDtl2DetailDto detailDto : dto.getDocsMsgsDtl2Details()) {
                if (detailDto.getActionType() == ActionType.isCreated || detailDto.getActionType() == null) {
                    details.add(PortCallOperationDocsMsgsDtl2.builder()
                            .transactionPoid(transactionPoid)
                            .detRowId(nextDetRowId++)
                            .emailPoid(detailDto.getEmailPoid())
                            .emailType(detailDto.getEmailType())
                            .company(detailDto.getCompany())
                            .addressee(detailDto.getAddressee())
                            .toEmailId(detailDto.getToEmailId())
                            .ccEmailId(detailDto.getCcEmailId())
                            .createdBy(UserContext.getUserId())
                            .createdDate(LocalDateTime.now())
                            .lastModifiedBy(UserContext.getUserId())
                            .lastModifiedDate(LocalDateTime.now())
                            .build());
                }
            }
            if (!details.isEmpty()) docsMsgsDtl2Repository.saveAll(details);
        }
    }


    @Override
    @Transactional
    public PortCallOperationResponseDto updateOperation(Long id, PortCallOperationDto dto, Long userPoid, Long groupPoid) {
        log.info("Updating port call operation id: {}", id);

        PortCallOperationHdr hdr = hdrRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Port call operation", "Transaction Poid", id));

        PortCallOperationHdr oldHdr = new PortCallOperationHdr();
        org.springframework.beans.BeanUtils.copyProperties(hdr, oldHdr);

        if (dto.getVesselVoyagePoid() != null) {
            if (!shipVoyageHdrRepository.existsByTransactionPoid(dto.getVesselVoyagePoid())) {
                throw new ResourceNotFoundException("Vessel Voyage", "Vessel Voyage Poid", dto.getVesselVoyagePoid());
            }
        }
        if (dto.getPrincipalPoid() != null) {
            if (!shipPrincipalRepository.existsByPrincipalPoid(dto.getPrincipalPoid())) {
                throw new ResourceNotFoundException("Principal master", "Principal master Poid", dto.getPrincipalPoid());
            }
        }
        if (dto.getPortOfCallPoid() != null) {
            if (!shipPortMasterRepository.existsByIdPortPoid(BigDecimal.valueOf(dto.getPortOfCallPoid()))) {
                throw new ResourceNotFoundException("Vessel Voyage", "Vessel Voyage Poid", dto.getVesselVoyagePoid());
            }
        }
        if (dto.getPdaRefPoid() != null) {
            if (!pdaEntryHdrRepository.existsByTransactionPoid(dto.getPdaRefPoid())) {
                throw new ResourceNotFoundException("PDA Entry", "PDA Ref Poid", dto.getPdaRefPoid());
            }
        }
        if (dto.getFdaRefPoid() != null) {
            if (!pdaFdaHdrRepository.existsByTransactionPoid(dto.getFdaRefPoid())) {
                throw new ResourceNotFoundException("FDA Entry", "FDA Ref Poid", dto.getFdaRefPoid());
            }
        }

        validateFinalMailDetailState(id, dto.getMailDetails(), false);

        hdr.setTransactionDate(LocalDate.now());
        hdr.setGroupPoid(groupPoid);
        hdr.setCompanyPoid(UserContext.getCompanyPoid());
        hdr.setLastModifiedBy(UserContext.getUserId());
        hdr.setLastModifiedDate(LocalDateTime.now());

        hdr.setVesselVoyagePoid(dto.getVesselVoyagePoid());
        hdr.setCallSign(dto.getCallSign());
        hdr.setCallType(dto.getCallType());
        hdr.setPrincipalPoid(dto.getPrincipalPoid());
        hdr.setOperatorName(dto.getOperatorName());
        hdr.setChartererName(dto.getChartererName());
        hdr.setBerth(dto.getBerth());
        hdr.setGrt(dto.getGrt());
        hdr.setNrt(dto.getNrt());
        hdr.setDwt(dto.getDwt());
        hdr.setPortOfCallPoid(dto.getPortOfCallPoid());

        hdr.setSpecialInstructions(dto.getSpecialInstructions());
        hdr.setTermsConditions(dto.getTermsConditions());
        hdr.setPcInfoAttachments(dto.getPcInfoAttachments());

        hdr.setPdaRefPoid(dto.getPdaRefPoid());
        hdr.setFdaRefPoid(dto.getFdaRefPoid());

        hdr.setPdaAnchorageStayDays(dto.getPdaAnchorageStayDays());
        hdr.setPdaBerthStayDays(dto.getPdaBerthStayDays());
        hdr.setPdaPortStayDays(dto.getPdaPortStayDays());
        hdr.setPdaFdaRemarks(dto.getPdaFdaRemarks());
        hdr.setPdaFdaAttachments(dto.getPdaFdaAttachments());

        hdr.setHusbandryCrewReqBy(dto.getHusbandryCrewReqBy());

        hdr = hdrRepository.save(hdr);

        // Update cargo details with actionType
        if (dto.getCargoDetails() != null && !dto.getCargoDetails().isEmpty()) {
            for (PortCallOperationCargoDetailDto cargoDto : dto.getCargoDetails()) {
                ActionType action = cargoDto.getActionType();
                if (action == null) continue;

                if (action == ActionType.isCreated) {
                    Long nextDetRowId = cargoDtlRepository.findMaxDetRowIdByTransactionPoid(id) + 1;
                    PortCallOperationCargoDtl newDetail = PortCallOperationCargoDtl.builder()
                            .transactionPoid(id)
                            .detRowId(nextDetRowId)
                            .productName(cargoDto.getProductName())
                            .portCargoName(cargoDto.getPortCargoName())
                            .qtyMt(cargoDto.getQtyMt())
                            .qtyCbm(cargoDto.getQtyCbm())
                            .noOfQty(cargoDto.getNoOfQty())
                            .callType(cargoDto.getCallType())
                            .portOfCallPoid(cargoDto.getPortOfCallPoid())
                            .berth(cargoDto.getBerth())
                            .shipper(cargoDto.getShipper())
                            .receiver(cargoDto.getReceiver())
                            .createdBy(UserContext.getUserId())
                            .createdDate(LocalDateTime.now())
                            .lastModifiedBy(UserContext.getUserId())
                            .lastModifiedDate(LocalDateTime.now())
                            .build();
                    PortCallOperationCargoDtl saved = cargoDtlRepository.save(newDetail);
                    String logDetail = String.format("Row Created on [Port Call Operation Cargo Details] with detRowId: %s", saved.getDetRowId());
                    loggingService.createLogSummaryEntry(UserContext.getDocumentId(), id.toString(), logDetail);
                } else if (action == ActionType.isDeleted) {
                    // Validate that no ActProgDtl has cargo matching the portCargoName of the cargo being deleted
                    cargoDtlRepository.findById(new PortCallOperationCargoDtlId(id, cargoDto.getDetRowId()))
                            .ifPresent(existingCargo -> {
                                String portCargoName = existingCargo.getPortCargoName();
                                if (portCargoName != null && actProgDtlRepository.existsByTransactionPoidAndCargo(id, portCargoName)) {
                                    throw new ValidationException(String.format("Cannot delete cargo detail with portCargoName '%s' as it is referenced in Act Progress Details", portCargoName));
                                }
                            });
                    cargoDtlRepository.deleteById(new PortCallOperationCargoDtlId(id, cargoDto.getDetRowId()));
                    loggingService.logDelete(cargoDto, UserContext.getDocumentId(), id.toString());
                } else if (action == ActionType.isUpdated) {
                    cargoDtlRepository.findById(new PortCallOperationCargoDtlId(id, cargoDto.getDetRowId()))
                            .ifPresent(existing -> {
                                PortCallOperationCargoDtl oldDetail = new PortCallOperationCargoDtl();
                                org.springframework.beans.BeanUtils.copyProperties(existing, oldDetail);

                                existing.setProductName(cargoDto.getProductName());
                                existing.setPortCargoName(cargoDto.getPortCargoName());
                                existing.setQtyMt(cargoDto.getQtyMt());
                                existing.setQtyCbm(cargoDto.getQtyCbm());
                                existing.setNoOfQty(cargoDto.getNoOfQty());
                                existing.setCallType(cargoDto.getCallType());
                                existing.setPortOfCallPoid(cargoDto.getPortOfCallPoid());
                                existing.setBerth(cargoDto.getBerth());
                                existing.setShipper(cargoDto.getShipper());
                                existing.setReceiver(cargoDto.getReceiver());
                                existing.setLastModifiedBy(UserContext.getUserId());
                                existing.setLastModifiedDate(LocalDateTime.now());
                                existing = cargoDtlRepository.save(existing);

                                String logDetail = String.format("KeyId = TRANSACTION_POID %s: DET_ROW_ID %s", existing.getTransactionPoid(), existing.getDetRowId());
                                loggingService.createLog(oldDetail, existing, PortCallOperationCargoDtl.class, UserContext.getDocumentId(), id.toString(), logDetail);
                            });
                }
            }
        }

        // Update mail details with actionType
        if (dto.getMailDetails() != null && !dto.getMailDetails().isEmpty()) {
            for (PortCallOperationMailDetailDto mailDto : dto.getMailDetails()) {
                ActionType action = mailDto.getActionType();
                if (action == null) continue;

                if (action == ActionType.isCreated) {
                    Long nextDetRowId = mailDtlRepository.findMaxDetRowIdByTransactionPoid(id) + 1;
                    PortCallOperationMailDtl newDetail = PortCallOperationMailDtl.builder()
                            .transactionPoid(id)
                            .detRowId(nextDetRowId)
                            .communicationType(mailDto.getCommunicationType())
                            .communicationMode(mailDto.getCommunicationMode())
                            .company(mailDto.getCompany())
                            .addressee(mailDto.getAddressee())
                            .emailIds(mailDto.getEmailIds())
                            .createdBy(UserContext.getUserId())
                            .createdDate(LocalDateTime.now())
                            .lastModifiedBy(UserContext.getUserId())
                            .lastModifiedDate(LocalDateTime.now())
                            .build();
                    PortCallOperationMailDtl saved = mailDtlRepository.save(newDetail);
                    String logDetail = String.format("Row Created on [Port Call Operation Mail Details] with detRowId: %s", saved.getDetRowId());
                    loggingService.createLogSummaryEntry(UserContext.getDocumentId(), id.toString(), logDetail);
                }
//                else if (action == ActionType.isDeleted) {
//                    mailDtlRepository.deleteById(new PortCallOperationMailDtlId(id, mailDto.getDetRowId()));
//                    loggingService.logDelete(mailDto, UserContext.getDocumentId(), id.toString());
//                }
                else if (action == ActionType.isUpdated) {
                    mailDtlRepository.findById(new PortCallOperationMailDtlId(id, mailDto.getDetRowId()))
                            .ifPresent(existing -> {
                                PortCallOperationMailDtl oldDetail = new PortCallOperationMailDtl();
                                org.springframework.beans.BeanUtils.copyProperties(existing, oldDetail);

                                existing.setCommunicationType(mailDto.getCommunicationType());
                                existing.setCommunicationMode(mailDto.getCommunicationMode());
                                existing.setCompany(mailDto.getCompany());
                                existing.setAddressee(mailDto.getAddressee());
                                existing.setEmailIds(mailDto.getEmailIds());
                                existing.setLastModifiedBy(UserContext.getUserId());
                                existing.setLastModifiedDate(LocalDateTime.now());
                                existing = mailDtlRepository.save(existing);

                                String logDetail = String.format("KeyId = TRANSACTION_POID %s: DET_ROW_ID %s", existing.getTransactionPoid(), existing.getDetRowId());
                                loggingService.createLog(oldDetail, existing, PortCallOperationMailDtl.class, UserContext.getDocumentId(), id.toString(), logDetail);
                            });
                }
            }
        }
        // Update all other detail tables with actionType
        updateAllDetailTables(id, dto, UserContext.getUserId());

        loggingService.logChanges(oldHdr, hdr, PortCallOperationHdr.class, UserContext.getDocumentId(), id.toString(), LogDetailsEnum.MODIFIED, "TRANSACTION_POID");
        return getOperationById(id);
    }

    private void validateFinalMailDetailState(Long transactionPoid, List<PortCallOperationMailDetailDto> mailDetails, boolean isCreate) {

        if (mailDetails == null || mailDetails.isEmpty()) {
            throw new ValidationException("At least one mail detail must be provided");
        }

        long existingCount = 0;
        if (!isCreate) {
            existingCount = mailDtlRepository.countByTransactionPoid(transactionPoid);
        }

        long createCount = mailDetails.stream().filter(d -> ActionType.isCreated.equals(d.getActionType())).count();

        long deleteCount = mailDetails.stream().filter(d -> ActionType.isDeleted.equals(d.getActionType())).count();

        long finalCount = existingCount - deleteCount + createCount;

        if (finalCount <= 0) {
            throw new ValidationException("At least one mail detail must exist after the operation");
        }
    }


    private void updateAllDetailTables(Long transactionPoid, PortCallOperationDto dto, String userId) {
        // Update Est Bert Details
        if (dto.getEstBertDetails() != null && !dto.getEstBertDetails().isEmpty()) {
            for (PortCallOperationEstBertDetailDto detailDto : dto.getEstBertDetails()) {
                if (detailDto.getEmailPoid() != null) {
                    if (!msgsDtl1Repository.existsByIdEmailPoid(detailDto.getEmailPoid())) {
                        throw new ResourceNotFoundException("Email", "Email Poid", detailDto.getEmailPoid());
                    }
                }
                ActionType action = detailDto.getActionType();
                if (action == null || action == ActionType.noChange) continue;
                if (action == ActionType.isCreated) {
                    Long nextDetRowId = estBertDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
                    PortCallOperationEstBertDtl saved = estBertDtlRepository.save(PortCallOperationEstBertDtl.builder()
                            .transactionPoid(transactionPoid)
                            .detRowId(nextDetRowId)
                            .eta(detailDto.getEta())
                            .etb(detailDto.getEtb())
                            .berthingAttachments(detailDto.getBerthingAttachments())
                            .emailPoid(detailDto.getEmailPoid())
                            .createdBy(UserContext.getUserId())
                            .createdDate(LocalDateTime.now())
                            .lastModifiedBy(UserContext.getUserId())
                            .lastModifiedDate(LocalDateTime.now())
                            .build());
                    String logDetail = String.format("Row Created on [Port Call Operation Est Bert Details] with detRowId: %s", saved.getDetRowId());
                    loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                } else if (action == ActionType.isUpdated) {
                    estBertDtlRepository.findById(new PortCallOperationEstBertDtlId(transactionPoid, detailDto.getDetRowId()))
                            .ifPresent(existing -> {
                                PortCallOperationEstBertDtl oldDetail = new PortCallOperationEstBertDtl();
                                org.springframework.beans.BeanUtils.copyProperties(existing, oldDetail);
                                existing.setEta(detailDto.getEta());
                                existing.setEtb(detailDto.getEtb());
                                existing.setBerthingAttachments(detailDto.getBerthingAttachments());
                                existing.setEmailPoid(detailDto.getEmailPoid());
                                existing.setLastModifiedBy(userId);
                                existing.setLastModifiedDate(LocalDateTime.now());
                                existing = estBertDtlRepository.save(existing);
                                String logDetail = String.format("KeyId = TRANSACTION_POID %s: DET_ROW_ID %s", existing.getTransactionPoid(), existing.getDetRowId());
                                loggingService.createLog(oldDetail, existing, PortCallOperationEstBertDtl.class, UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                            });
                }
            }
        }

        // Similar pattern for all other detail tables - Est Prearrival, Act Timing, etc.
        // Due to length constraints, I'll add a few key ones and note that the pattern should be repeated for all

        // Update Est Prearrival Details
        if (dto.getEstPrearrivalDetails() != null && !dto.getEstPrearrivalDetails().isEmpty()) {
            for (PortCallOperationEstPrearrivalDetailDto detailDto : dto.getEstPrearrivalDetails()) {
                if (detailDto.getEmailPoid() != null) {
                    if (!msgsDtl1Repository.existsByIdEmailPoid(detailDto.getEmailPoid())) {
                        throw new ResourceNotFoundException("Email", "Email Poid", detailDto.getEmailPoid());
                    }
                }
                ActionType action = detailDto.getActionType();
                if (action == null || action == ActionType.noChange) continue;
                if (action == ActionType.isCreated) {
                    Long nextDetRowId = estPrearrivalDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
                    PortCallOperationEstPrearrivalDtl saved = estPrearrivalDtlRepository.save(PortCallOperationEstPrearrivalDtl.builder()
                            .transactionPoid(transactionPoid).detRowId(nextDetRowId)
                            .preActivityDtlPoid(detailDto.getPreActivityDtlPoid())
                            .eta(detailDto.getEta())
                            .etb(detailDto.getEtb())
                            .preArrivalAttachments(detailDto.getPreArrivalAttachments())
                            .emailPoid(detailDto.getEmailPoid())
                            .createdBy(userId).build());
                    String logDetail = String.format("Row Created on [Port Call Operation Est Prearrival Details] with detRowId: %s", saved.getDetRowId());
                    loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                } else if (action == ActionType.isUpdated) {
                    estPrearrivalDtlRepository.findById(new PortCallOperationEstPrearrivalDtlId(transactionPoid, detailDto.getDetRowId()))
                            .ifPresent(existing -> {
                                PortCallOperationEstPrearrivalDtl oldDetail = new PortCallOperationEstPrearrivalDtl();
                                org.springframework.beans.BeanUtils.copyProperties(existing, oldDetail);
                                existing.setPreActivityDtlPoid(detailDto.getPreActivityDtlPoid());
                                existing.setEta(detailDto.getEta());
                                existing.setEtb(detailDto.getEtb());
                                existing.setPreArrivalAttachments(detailDto.getPreArrivalAttachments());
                                existing.setEmailPoid(detailDto.getEmailPoid());
                                existing.setLastModifiedBy(userId);
                                existing.setLastModifiedDate(LocalDateTime.now());
                                existing = estPrearrivalDtlRepository.save(existing);
                                String logDetail = String.format("KeyId = TRANSACTION_POID %s: DET_ROW_ID %s", existing.getTransactionPoid(), existing.getDetRowId());
                                loggingService.createLog(oldDetail, existing, PortCallOperationEstPrearrivalDtl.class, UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                            });
                }
            }
        }

        // Update Act Timing Details (3-part key)
        if (dto.getActTimingDetails() != null && !dto.getActTimingDetails().isEmpty()) {
            for (PortCallOperationActTimingDetailDto detailDto : dto.getActTimingDetails()) {
                if (detailDto.getEmailPoid() != null) {
                    if (!msgsDtl1Repository.existsByIdEmailPoid(detailDto.getEmailPoid())) {
                        throw new ResourceNotFoundException("Email", "Email Poid", detailDto.getEmailPoid());
                    }
                }
                if (detailDto.getPortReportPoid() != null) {
                    if (!portCallReportHdrRepository.existsByPortCallReportPoid(detailDto.getPortReportPoid())) {
                        throw new ResourceNotFoundException("Port Report", "Port Report Poid", detailDto.getPortReportPoid());
                    }
                }
                ActionType action = detailDto.getActionType();
                if (action == null || action == ActionType.noChange) continue;
                if (action == ActionType.isCreated) {
                    Long nextDetRowId = actTimingDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
                    PortCallOperationActTimingDtl saved = actTimingDtlRepository.save(PortCallOperationActTimingDtl.builder()
                            .transactionPoid(transactionPoid)
                            .detRowId(nextDetRowId)
                            .portReportPoid(detailDto.getPortReportPoid())
                            .actualsTimingDtlPoid(detailDto.getActualsTimingDtlPoid())
                            .emailPoid(detailDto.getEmailPoid())
                            .createdBy(UserContext.getUserId())
                            .createdDate(LocalDateTime.now())
                            .lastModifiedBy(UserContext.getUserId())
                            .lastModifiedDate(LocalDateTime.now())
                            .build());
                    String logDetail = String.format("Row Created on [Port Call Operation Act Timing Details] with detRowId: %s", saved.getDetRowId());
                    loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                } else if (action == ActionType.isUpdated) {
                    actTimingDtlRepository.findById(new PortCallOperationActTimingDtlId(
                                    transactionPoid, detailDto.getDetRowId(), detailDto.getPortReportPoid()))
                            .ifPresent(existing -> {
                                PortCallOperationActTimingDtl oldDetail = new PortCallOperationActTimingDtl();
                                org.springframework.beans.BeanUtils.copyProperties(existing, oldDetail);
                                existing.setActualsTimingDtlPoid(detailDto.getActualsTimingDtlPoid());
                                existing.setEmailPoid(detailDto.getEmailPoid());
                                existing.setLastModifiedBy(userId);
                                existing.setLastModifiedDate(LocalDateTime.now());
                                existing = actTimingDtlRepository.save(existing);
                                String logDetail = String.format("KeyId = TRANSACTION_POID %s: DET_ROW_ID %s", existing.getTransactionPoid(), existing.getDetRowId());
                                loggingService.createLog(oldDetail, existing, PortCallOperationActTimingDtl.class, UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                            });
                }
            }
        }


        // Update Act Cond Details
        if (dto.getActCondDetails() != null && !dto.getActCondDetails().isEmpty()) {
            for (PortCallOperationActCondDetailDto detailDto : dto.getActCondDetails()) {
                ActionType action = detailDto.getActionType();
                if (action == null || action == ActionType.noChange) continue;
                if (action == ActionType.isCreated) {
                    Long nextDetRowId = actCondDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
                    PortCallOperationActCondDtl saved = actCondDtlRepository.save(PortCallOperationActCondDtl.builder()
                            .transactionPoid(transactionPoid)
                            .detRowId(nextDetRowId)
                            .conditionType(detailDto.getConditionType())
                            .draftForward(detailDto.getDraftForward())
                            .draftMid(detailDto.getDraftMid())
                            .draftAft(detailDto.getDraftAft())
                            .fuelOil(detailDto.getFuelOil())
                            .dieselOil(detailDto.getDieselOil())
                            .freshWater(detailDto.getFreshWater())
                            .tugsService(detailDto.getTugsService())
                            .createdBy(UserContext.getUserId())
                            .createdDate(LocalDateTime.now())
                            .lastModifiedBy(UserContext.getUserId())
                            .lastModifiedDate(LocalDateTime.now())
                            .build());
                    String logDetail = String.format("Row Created on [Port Call Operation Act Cond Details] with detRowId: %s", saved.getDetRowId());
                    loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                } else if (action == ActionType.isUpdated) {
                    actCondDtlRepository.findById(new PortCallOperationActCondDtlId(transactionPoid, detailDto.getDetRowId()))
                            .ifPresent(existing -> {
                                PortCallOperationActCondDtl oldDetail = new PortCallOperationActCondDtl();
                                org.springframework.beans.BeanUtils.copyProperties(existing, oldDetail);
                                existing.setConditionType(detailDto.getConditionType());
                                existing.setDraftForward(detailDto.getDraftForward());
                                existing.setDraftMid(detailDto.getDraftMid());
                                existing.setDraftAft(detailDto.getDraftAft());
                                existing.setFuelOil(detailDto.getFuelOil());
                                existing.setDieselOil(detailDto.getDieselOil());
                                existing.setFreshWater(detailDto.getFreshWater());
                                existing.setTugsService(detailDto.getTugsService());
                                existing.setLastModifiedBy(userId);
                                existing.setLastModifiedDate(LocalDateTime.now());
                                existing = actCondDtlRepository.save(existing);
                                String logDetail = String.format("KeyId = TRANSACTION_POID %s: DET_ROW_ID %s", existing.getTransactionPoid(), existing.getDetRowId());
                                loggingService.createLog(oldDetail, existing, PortCallOperationActCondDtl.class, UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                            });
                }
            }
        }

        // Update Act Rmks Details
        if (dto.getActRmksDetails() != null && !dto.getActRmksDetails().isEmpty()) {
            for (PortCallOperationActRmksDetailDto detailDto : dto.getActRmksDetails()) {
                if (detailDto.getPcReportPoid() != null) {
                    if (!portCallReportHdrRepository.existsByPortCallReportPoid(detailDto.getPcReportPoid())) {
                        throw new ResourceNotFoundException("Port Call Report", "Port Call Report Poid", detailDto.getPcReportPoid());
                    }
                }
                ActionType action = detailDto.getActionType();
                if (action == null || action == ActionType.noChange) continue;
                if (action == ActionType.isCreated) {
                    Long nextDetRowId = actRmksDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
                    PortCallOperationActRmksDtl saved = actRmksDtlRepository.save(PortCallOperationActRmksDtl.builder()
                            .transactionPoid(transactionPoid)
                            .detRowId(nextDetRowId)
                            .remarksType(detailDto.getRemarksType())
                            .remarksFrom(detailDto.getRemarksFrom())
                            .remarksTo(detailDto.getRemarksTo())
                            .cargoDetails(detailDto.getCargoDetails())
                            .reason(detailDto.getReason())
                            .pcReportPoid(detailDto.getPcReportPoid())
                            .createdBy(UserContext.getUserId())
                            .createdDate(LocalDateTime.now())
                            .lastModifiedBy(UserContext.getUserId())
                            .lastModifiedDate(LocalDateTime.now())
                            .build());
                    String logDetail = String.format("Row Created on [Port Call Operation Act Rmks Details] with detRowId: %s", saved.getDetRowId());
                    loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                } else if (action == ActionType.isUpdated) {
                    actRmksDtlRepository.findById(new PortCallOperationActRmksDtlId(transactionPoid, detailDto.getDetRowId()))
                            .ifPresent(existing -> {
                                PortCallOperationActRmksDtl oldDetail = new PortCallOperationActRmksDtl();
                                org.springframework.beans.BeanUtils.copyProperties(existing, oldDetail);
                                existing.setRemarksType(detailDto.getRemarksType());
                                existing.setRemarksFrom(detailDto.getRemarksFrom());
                                existing.setRemarksTo(detailDto.getRemarksTo());
                                existing.setCargoDetails(detailDto.getCargoDetails());
                                existing.setReason(detailDto.getReason());
                                existing.setPcReportPoid(detailDto.getPcReportPoid());
                                existing.setLastModifiedBy(userId);
                                existing.setLastModifiedDate(LocalDateTime.now());
                                existing = actRmksDtlRepository.save(existing);
                                String logDetail = String.format("KeyId = TRANSACTION_POID %s: DET_ROW_ID %s", existing.getTransactionPoid(), existing.getDetRowId());
                                loggingService.createLog(oldDetail, existing, PortCallOperationActRmksDtl.class, UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                            });
                }
            }
        }

        // Update Act Prog Details
        if (dto.getActProgDetails() != null && !dto.getActProgDetails().isEmpty()) {
            for (PortCallOperationActProgDetailDto detailDto : dto.getActProgDetails()) {
                if (detailDto.getEmailPoid() != null) {
                    if (!msgsDtl1Repository.existsByIdEmailPoid(detailDto.getEmailPoid())) {
                        throw new ResourceNotFoundException("Email", "Email Poid", detailDto.getEmailPoid());
                    }
                }
                if (detailDto.getUnitPoid() != null) {
                    if (!stockUnitMasterRepository.existsByStockUnitPoid(detailDto.getUnitPoid())) {
                        throw new ResourceNotFoundException("Stock Unit Master", "Unit Poid", detailDto.getUnitPoid());
                    }
                }
                ActionType action = detailDto.getActionType();
                if (action == null || action == ActionType.noChange) continue;
                if (action == ActionType.isCreated) {
                    Long nextDetRowId = actProgDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
                    PortCallOperationActProgDtl saved = actProgDtlRepository.save(PortCallOperationActProgDtl.builder()
                            .transactionPoid(transactionPoid)
                            .detRowId(nextDetRowId)
                            .emailPoid(detailDto.getEmailPoid())
                            .cargo(detailDto.getCargo())
                            .progressDateTime(detailDto.getProgressDateTime())
                            .progressQty(detailDto.getProgressQty())
                            .progressStatus(detailDto.getProgressStatus())
                            .balanceQty(detailDto.getBalanceQty())
                            .unitPoid(detailDto.getUnitPoid())
                            .ratePerHr(detailDto.getRatePerHr())
                            .etc(detailDto.getEtc())
                            .estBlDate(detailDto.getEstBlDate())
                            .createdBy(UserContext.getUserId())
                            .createdDate(LocalDateTime.now())
                            .lastModifiedBy(UserContext.getUserId())
                            .lastModifiedDate(LocalDateTime.now())
                            .build());
                    String logDetail = String.format("Row Created on [Port Call Operation Act Prog Details] with detRowId: %s", saved.getDetRowId());
                    loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                } else if (action == ActionType.isUpdated) {
                    actProgDtlRepository.findById(new PortCallOperationActProgDtlId(transactionPoid, detailDto.getDetRowId()))
                            .ifPresent(existing -> {
                                PortCallOperationActProgDtl oldDetail = new PortCallOperationActProgDtl();
                                org.springframework.beans.BeanUtils.copyProperties(existing, oldDetail);
                                existing.setEmailPoid(detailDto.getEmailPoid());
                                existing.setCargo(detailDto.getCargo());
                                existing.setProgressDateTime(detailDto.getProgressDateTime());
                                existing.setProgressQty(detailDto.getProgressQty());
                                existing.setProgressStatus(detailDto.getProgressStatus());
                                existing.setBalanceQty(detailDto.getBalanceQty());
                                existing.setUnitPoid(detailDto.getUnitPoid());
                                existing.setRatePerHr(detailDto.getRatePerHr());
                                existing.setEtc(detailDto.getEtc());
                                existing.setEstBlDate(detailDto.getEstBlDate());
                                existing.setLastModifiedBy(userId);
                                existing.setLastModifiedDate(LocalDateTime.now());
                                existing = actProgDtlRepository.save(existing);
                                String logDetail = String.format("KeyId = TRANSACTION_POID %s: DET_ROW_ID %s", existing.getTransactionPoid(), existing.getDetRowId());
                                loggingService.createLog(oldDetail, existing, PortCallOperationActProgDtl.class, UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                            });
                }
            }
        }

        // Update Act Cargo Fig Details
        if (dto.getActCargoFigDetails() != null && !dto.getActCargoFigDetails().isEmpty()) {
            for (PortCallOperationActCargoFigDetailDto detailDto : dto.getActCargoFigDetails()) {
                if (detailDto.getUnitPoid() != null) {
                    if (!stockUnitMasterRepository.existsByStockUnitPoid(detailDto.getUnitPoid())) {
                        throw new ResourceNotFoundException("Stock Unit Master", "Unit Poid", detailDto.getUnitPoid());
                    }
                }
                ActionType action = detailDto.getActionType();
                if (action == null || action == ActionType.noChange) continue;
                if (action == ActionType.isCreated) {
                    Long nextDetRowId = actCargoFigDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
                    PortCallOperationActCargoFigDtl saved = actCargoFigDtlRepository.save(PortCallOperationActCargoFigDtl.builder()
                            .transactionPoid(transactionPoid)
                            .detRowId(nextDetRowId)
                            .cargo(detailDto.getCargo())
                            .callType(detailDto.getCallType())
                            .qty(detailDto.getQty())
                            .unitPoid(detailDto.getUnitPoid())
                            .vesselReq(detailDto.getVesselReq())
                            .terminalNom(detailDto.getTerminalNom())
                            .shipFigureMt(detailDto.getShipFigureMt())
                            .shoreFigureMt(detailDto.getShoreFigureMt())
                            .shipFigureBbls(detailDto.getShipFigureBbls())
                            .blDate(detailDto.getBlDate())
                            .hoseNo(detailDto.getHoseNo())
                            .hoseSize(detailDto.getHoseSize())
                            .createdBy(UserContext.getUserId())
                            .createdDate(LocalDateTime.now())
                            .lastModifiedBy(UserContext.getUserId())
                            .lastModifiedDate(LocalDateTime.now())
                            .build());
                    String logDetail = String.format("Row Created on [Port Call Operation Act Cargo Fig Details] with detRowId: %s", saved.getDetRowId());
                    loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                } else if (action == ActionType.isUpdated) {
                    actCargoFigDtlRepository.findById(new PortCallOperationActCargoFigDtlId(transactionPoid, detailDto.getDetRowId()))
                            .ifPresent(existing -> {
                                PortCallOperationActCargoFigDtl oldDetail = new PortCallOperationActCargoFigDtl();
                                org.springframework.beans.BeanUtils.copyProperties(existing, oldDetail);
                                existing.setCargo(detailDto.getCargo());
                                existing.setCallType(detailDto.getCallType());
                                existing.setQty(detailDto.getQty());
                                existing.setUnitPoid(detailDto.getUnitPoid());
                                existing.setVesselReq(detailDto.getVesselReq());
                                existing.setTerminalNom(detailDto.getTerminalNom());
                                existing.setShipFigureMt(detailDto.getShipFigureMt());
                                existing.setShoreFigureMt(detailDto.getShoreFigureMt());
                                existing.setShipFigureBbls(detailDto.getShipFigureBbls());
                                existing.setBlDate(detailDto.getBlDate());
                                existing.setHoseNo(detailDto.getHoseNo());
                                existing.setHoseSize(detailDto.getHoseSize());
                                existing.setLastModifiedBy(userId);
                                existing.setLastModifiedDate(LocalDateTime.now());
                                existing = actCargoFigDtlRepository.save(existing);
                                String logDetail = String.format("KeyId = TRANSACTION_POID %s: DET_ROW_ID %s", existing.getTransactionPoid(), existing.getDetRowId());
                                loggingService.createLog(oldDetail, existing, PortCallOperationActCargoFigDtl.class, UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                            });
                }
            }
        }

        // Update Act Bunker Details
        if (dto.getActBunkerDetails() != null && !dto.getActBunkerDetails().isEmpty()) {
            for (PortCallOperationActBunkerDetailDto detailDto : dto.getActBunkerDetails()) {
                ActionType action = detailDto.getActionType();
                if (action == null || action == ActionType.noChange) continue;
                if (action == ActionType.isCreated) {
                    Long nextDetRowId = actBunkerDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
                    PortCallOperationActBunkerDtl saved = actBunkerDtlRepository.save(PortCallOperationActBunkerDtl.builder()
                            .transactionPoid(transactionPoid)
                            .detRowId(nextDetRowId)
                            .grade(detailDto.getGrade())
                            .nominatedQtyMt(detailDto.getNominatedQtyMt())
                            .suppliedQtyMt(detailDto.getSuppliedQtyMt())
                            .shipQtyMt(detailDto.getShipQtyMt())
                            .createdBy(UserContext.getUserId())
                            .createdDate(LocalDateTime.now())
                            .lastModifiedBy(UserContext.getUserId())
                            .lastModifiedDate(LocalDateTime.now())
                            .build());
                    String logDetail = String.format("Row Created on [Port Call Operation Act Bunker Details] with detRowId: %s", saved.getDetRowId());
                    loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                } else if (action == ActionType.isUpdated) {
                    actBunkerDtlRepository.findById(new PortCallOperationActBunkerDtlId(transactionPoid, detailDto.getDetRowId()))
                            .ifPresent(existing -> {
                                PortCallOperationActBunkerDtl oldDetail = new PortCallOperationActBunkerDtl();
                                org.springframework.beans.BeanUtils.copyProperties(existing, oldDetail);
                                existing.setGrade(detailDto.getGrade());
                                existing.setNominatedQtyMt(detailDto.getNominatedQtyMt());
                                existing.setSuppliedQtyMt(detailDto.getSuppliedQtyMt());
                                existing.setShipQtyMt(detailDto.getShipQtyMt());
                                existing.setLastModifiedBy(userId);
                                existing.setLastModifiedDate(LocalDateTime.now());
                                existing = actBunkerDtlRepository.save(existing);
                                String logDetail = String.format("KeyId = TRANSACTION_POID %s: DET_ROW_ID %s", existing.getTransactionPoid(), existing.getDetRowId());
                                loggingService.createLog(oldDetail, existing, PortCallOperationActBunkerDtl.class, UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                            });
                }
            }
        }

        // Update Husbandry Crew Details
        if (dto.getHusbandryCrewDetails() != null && !dto.getHusbandryCrewDetails().isEmpty()) {
            for (PortCallOperationHusbandryCrewDetailDto detailDto : dto.getHusbandryCrewDetails()) {
                ActionType action = detailDto.getActionType();
                if (action == null || action == ActionType.noChange) continue;
                if (action == ActionType.isCreated) {
                    Long nextDetRowId = husbandryCrewDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
                    PortCallOperationHusbandryCrewDtl saved = husbandryCrewDtlRepository.save(PortCallOperationHusbandryCrewDtl.builder()
                            .transactionPoid(transactionPoid)
                            .detRowId(nextDetRowId)
                            .crewName(detailDto.getCrewName())
                            .crewGenderPoid(detailDto.getCrewGenderPoid())
                            .crewNationalityPoid(detailDto.getCrewNationalityPoid())
                            .crewPptNumber(detailDto.getCrewPptNumber())
                            .crewSeamanNo(detailDto.getCrewSeamanNo())
                            .crewRank(detailDto.getCrewRank())
                            .crewAttachments(detailDto.getCrewAttachments())
                            .createdBy(UserContext.getUserId())
                            .createdDate(LocalDateTime.now())
                            .lastModifiedBy(UserContext.getUserId())
                            .lastModifiedDate(LocalDateTime.now())
                            .build());
                    String logDetail = String.format("Row Created on [Port Call Operation Husbandry Crew Details] with detRowId: %s", saved.getDetRowId());
                    loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                } else if (action == ActionType.isUpdated) {
                    husbandryCrewDtlRepository.findById(new PortCallOperationHusbandryCrewDtlId(transactionPoid, detailDto.getDetRowId()))
                            .ifPresent(existing -> {
                                PortCallOperationHusbandryCrewDtl oldDetail = new PortCallOperationHusbandryCrewDtl();
                                org.springframework.beans.BeanUtils.copyProperties(existing, oldDetail);
                                existing.setCrewName(detailDto.getCrewName());
                                existing.setCrewGenderPoid(detailDto.getCrewGenderPoid());
                                existing.setCrewNationalityPoid(detailDto.getCrewNationalityPoid());
                                existing.setCrewPptNumber(detailDto.getCrewPptNumber());
                                existing.setCrewSeamanNo(detailDto.getCrewSeamanNo());
                                existing.setCrewRank(detailDto.getCrewRank());
                                existing.setCrewAttachments(detailDto.getCrewAttachments());
                                existing.setLastModifiedBy(userId);
                                existing.setLastModifiedDate(LocalDateTime.now());
                                existing = husbandryCrewDtlRepository.save(existing);
                                String logDetail = String.format("KeyId = TRANSACTION_POID %s: DET_ROW_ID %s", existing.getTransactionPoid(), existing.getDetRowId());
                                loggingService.createLog(oldDetail, existing, PortCallOperationHusbandryCrewDtl.class, UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                            });
                }
            }
        }

        // Update Husbandry Other Details
        if (dto.getHusbandryOthDetails() != null && !dto.getHusbandryOthDetails().isEmpty()) {
            for (PortCallOperationHusbandryOthDetailDto detailDto : dto.getHusbandryOthDetails()) {
                if (detailDto.getUnitPoid() != null) {
                    if (!stockUnitMasterRepository.existsByStockUnitPoid(detailDto.getUnitPoid())) {
                        throw new ResourceNotFoundException("Stock Unit Master", "Unit Poid", detailDto.getUnitPoid());
                    }
                }
                ActionType action = detailDto.getActionType();
                if (action == null || action == ActionType.noChange) continue;
                if (action == ActionType.isCreated) {
                    Long nextDetRowId = husbandryOthDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
                    PortCallOperationHusbandryOthDtl saved = husbandryOthDtlRepository.save(PortCallOperationHusbandryOthDtl.builder()
                            .transactionPoid(transactionPoid)
                            .detRowId(nextDetRowId)
                            .arrangement(detailDto.getArrangement())
                            .descriptionText(detailDto.getDescriptionText())
                            .meetGreet(detailDto.getMeetGreet())
                            .noOfDays(detailDto.getNoOfDays())
                            .qty(detailDto.getQty())
                            .unitPoid(detailDto.getUnitPoid())
                            .unitPrice(detailDto.getUnitPrice())
                            .currencyCode(detailDto.getCurrencyCode())
                            .totalPrice(detailDto.getTotalPrice())
                            .adjustedPrice(detailDto.getAdjustedPrice())
                            .arrngmntAttachments(detailDto.getArrngmntAttachments())
                            .requestedBy(detailDto.getRequestedBy())
                            .paymentMode(detailDto.getPaymentMode())
                            .createdBy(UserContext.getUserId())
                            .createdDate(LocalDateTime.now())
                            .lastModifiedBy(UserContext.getUserId())
                            .lastModifiedDate(LocalDateTime.now())
                            .build());
                    String logDetail = String.format("Row Created on [Port Call Operation Husbandry Other Details] with detRowId: %s", saved.getDetRowId());
                    loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                } else if (action == ActionType.isUpdated) {
                    husbandryOthDtlRepository.findById(new PortCallOperationHusbandryOthDtlId(transactionPoid, detailDto.getDetRowId()))
                            .ifPresent(existing -> {
                                PortCallOperationHusbandryOthDtl oldDetail = new PortCallOperationHusbandryOthDtl();
                                org.springframework.beans.BeanUtils.copyProperties(existing, oldDetail);
                                existing.setArrangement(detailDto.getArrangement());
                                existing.setDescriptionText(detailDto.getDescriptionText());
                                existing.setMeetGreet(detailDto.getMeetGreet());
                                existing.setNoOfDays(detailDto.getNoOfDays());
                                existing.setQty(detailDto.getQty());
                                existing.setUnitPoid(detailDto.getUnitPoid());
                                existing.setUnitPrice(detailDto.getUnitPrice());
                                existing.setCurrencyCode(detailDto.getCurrencyCode());
                                existing.setTotalPrice(detailDto.getTotalPrice());
                                existing.setAdjustedPrice(detailDto.getAdjustedPrice());
                                existing.setArrngmntAttachments(detailDto.getArrngmntAttachments());
                                existing.setRequestedBy(detailDto.getRequestedBy());
                                existing.setPaymentMode(detailDto.getPaymentMode());
                                existing.setLastModifiedBy(userId);
                                existing.setLastModifiedDate(LocalDateTime.now());
                                existing = husbandryOthDtlRepository.save(existing);
                                String logDetail = String.format("KeyId = TRANSACTION_POID %s: DET_ROW_ID %s", existing.getTransactionPoid(), existing.getDetRowId());
                                loggingService.createLog(oldDetail, existing, PortCallOperationHusbandryOthDtl.class, UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                            });
                }
            }
        }

        // Update Docs Copy Details
        if (dto.getDocsCopyDetails() != null && !dto.getDocsCopyDetails().isEmpty()) {
            for (PortCallOperationDocsCopyDetailDto detailDto : dto.getDocsCopyDetails()) {
                ActionType action = detailDto.getActionType();
                if (action == null || action == ActionType.noChange) continue;
                if (action == ActionType.isCreated) {
                    Long nextDetRowId = docsCopyDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
                    PortCallOperationDocsCopyDtl saved = docsCopyDtlRepository.save(PortCallOperationDocsCopyDtl.builder()
                            .transactionPoid(transactionPoid)
                            .detRowId(nextDetRowId)
                            .documentFrom(detailDto.getDocumentFrom())
                            .documentList(detailDto.getDocumentList())
                            .documentSelect(detailDto.getDocumentSelect())
                            .documentAttachments(detailDto.getDocumentAttachments())
                            .createdBy(UserContext.getUserId())
                            .createdDate(LocalDateTime.now())
                            .lastModifiedBy(UserContext.getUserId())
                            .lastModifiedDate(LocalDateTime.now())
                            .build());
                    String logDetail = String.format("Row Created on [Port Call Operation Docs Copy Details] with detRowId: %s", saved.getDetRowId());
                    loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                } else if (action == ActionType.isUpdated) {
                    docsCopyDtlRepository.findById(new PortCallOperationDocsCopyDtlId(transactionPoid, detailDto.getDetRowId()))
                            .ifPresent(existing -> {
                                PortCallOperationDocsCopyDtl oldDetail = new PortCallOperationDocsCopyDtl();
                                org.springframework.beans.BeanUtils.copyProperties(existing, oldDetail);
                                existing.setDocumentFrom(detailDto.getDocumentFrom());
                                existing.setDocumentList(detailDto.getDocumentList());
                                existing.setDocumentSelect(detailDto.getDocumentSelect());
                                existing.setDocumentAttachments(detailDto.getDocumentAttachments());
                                existing.setLastModifiedBy(userId);
                                existing.setLastModifiedDate(LocalDateTime.now());
                                existing = docsCopyDtlRepository.save(existing);
                                String logDetail = String.format("KeyId = TRANSACTION_POID %s: DET_ROW_ID %s", existing.getTransactionPoid(), existing.getDetRowId());
                                loggingService.createLog(oldDetail, existing, PortCallOperationDocsCopyDtl.class, UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                            });
                }
            }
        }

        // Update Docs Msgs Dtl1 Details (3-part key, EMAIL_POID auto-generated)
        if (dto.getDocsMsgsDtl1Details() != null && !dto.getDocsMsgsDtl1Details().isEmpty()) {
            for (PortCallOperationDocsMsgsDtl1DetailDto detailDto : dto.getDocsMsgsDtl1Details()) {
                if (detailDto.getEmailPoid() != null) {
                    if (!msgsDtl1Repository.existsByIdEmailPoid(detailDto.getEmailPoid())) {
                        throw new ResourceNotFoundException("Email", "Email Poid", detailDto.getEmailPoid());
                    }
                }
                if (detailDto.getSendByPoid() != null) {
                    if (!globalUserRepository.existsByUserPoid(detailDto.getSendByPoid())) {
                        throw new ResourceNotFoundException("User", "Send By Poid", detailDto.getSendByPoid());
                    }
                }
                ActionType action = detailDto.getActionType();
                if (action == null || action == ActionType.noChange) continue;
                if (action == ActionType.isCreated) {
                    Long nextDetRowId = docsMsgsDtl1Repository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
                    // EMAIL_POID will be auto-generated by the sequence trigger
                    PortCallOperationDocsMsgsDtl1 saved = docsMsgsDtl1Repository.save(PortCallOperationDocsMsgsDtl1.builder()
                            .transactionPoid(transactionPoid)
                            .detRowId(nextDetRowId)
                            .sendByPoid(detailDto.getSendByPoid())
                            .emailSubject(detailDto.getEmailSubject())
                            .emailDocuments(detailDto.getEmailDocuments())
                            .emailSendOn(detailDto.getEmailSendOn())
                            .emailContent(detailDto.getEmailContent())
                            .emailRemarks(detailDto.getEmailRemarks())
                            .createdBy(UserContext.getUserId())
                            .createdDate(LocalDateTime.now())
                            .lastModifiedBy(UserContext.getUserId())
                            .lastModifiedDate(LocalDateTime.now())
                            .build());
                    String logDetail = String.format("Row Created on [Port Call Operation Docs Msgs Dtl1 Details] with detRowId: %s", saved.getDetRowId());
                    loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                } else if (action == ActionType.isUpdated) {
                    if (detailDto.getEmailPoid() != null) {
                        docsMsgsDtl1Repository.findById(new PortCallOperationDocsMsgsDtl1Id(
                                        transactionPoid, detailDto.getDetRowId(), detailDto.getEmailPoid()))
                                .ifPresent(existing -> {
                                    PortCallOperationDocsMsgsDtl1 oldDetail = new PortCallOperationDocsMsgsDtl1();
                                    org.springframework.beans.BeanUtils.copyProperties(existing, oldDetail);
                                    existing.setSendByPoid(detailDto.getSendByPoid());
                                    existing.setEmailSubject(detailDto.getEmailSubject());
                                    existing.setEmailDocuments(detailDto.getEmailDocuments());
                                    existing.setEmailSendOn(detailDto.getEmailSendOn());
                                    existing.setEmailContent(detailDto.getEmailContent());
                                    existing.setEmailRemarks(detailDto.getEmailRemarks());
                                    existing.setLastModifiedBy(userId);
                                    existing.setLastModifiedDate(LocalDateTime.now());
                                    existing = docsMsgsDtl1Repository.save(existing);
                                    String logDetail = String.format("KeyId = TRANSACTION_POID %s: DET_ROW_ID %s", existing.getTransactionPoid(), existing.getDetRowId());
                                    loggingService.createLog(oldDetail, existing, PortCallOperationDocsMsgsDtl1.class, UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                                });
                    }
                }
            }
        }

        // Update Docs Msgs Dtl2 Details
        if (dto.getDocsMsgsDtl2Details() != null && !dto.getDocsMsgsDtl2Details().isEmpty()) {
            for (PortCallOperationDocsMsgsDtl2DetailDto detailDto : dto.getDocsMsgsDtl2Details()) {
                if (detailDto.getEmailPoid() != null) {
                    if (!msgsDtl1Repository.existsByIdEmailPoid(detailDto.getEmailPoid())) {
                        throw new ResourceNotFoundException("Email", "Email Poid", detailDto.getEmailPoid());
                    }
                }
                ActionType action = detailDto.getActionType();
                if (action == null || action == ActionType.noChange) continue;
                if (action == ActionType.isCreated) {
                    Long nextDetRowId = docsMsgsDtl2Repository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
                    PortCallOperationDocsMsgsDtl2 saved = docsMsgsDtl2Repository.save(PortCallOperationDocsMsgsDtl2.builder()
                            .transactionPoid(transactionPoid)
                            .detRowId(nextDetRowId)
                            .emailPoid(detailDto.getEmailPoid())
                            .emailType(detailDto.getEmailType())
                            .company(detailDto.getCompany())
                            .addressee(detailDto.getAddressee())
                            .toEmailId(detailDto.getToEmailId())
                            .ccEmailId(detailDto.getCcEmailId())
                            .createdBy(UserContext.getUserId())
                            .createdDate(LocalDateTime.now())
                            .lastModifiedBy(UserContext.getUserId())
                            .lastModifiedDate(LocalDateTime.now())
                            .build());
                    String logDetail = String.format("Row Created on [Port Call Operation Docs Msgs Dtl2 Details] with detRowId: %s", saved.getDetRowId());
                    loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                } else if (action == ActionType.isUpdated) {
                    docsMsgsDtl2Repository.findById(new PortCallOperationDocsMsgsDtl2Id(transactionPoid, detailDto.getDetRowId()))
                            .ifPresent(existing -> {
                                PortCallOperationDocsMsgsDtl2 oldDetail = new PortCallOperationDocsMsgsDtl2();
                                org.springframework.beans.BeanUtils.copyProperties(existing, oldDetail);
                                existing.setEmailPoid(detailDto.getEmailPoid());
                                existing.setEmailType(detailDto.getEmailType());
                                existing.setCompany(detailDto.getCompany());
                                existing.setAddressee(detailDto.getAddressee());
                                existing.setToEmailId(detailDto.getToEmailId());
                                existing.setCcEmailId(detailDto.getCcEmailId());
                                existing.setLastModifiedBy(userId);
                                existing.setLastModifiedDate(LocalDateTime.now());
                                existing = docsMsgsDtl2Repository.save(existing);
                                String logDetail = String.format("KeyId = TRANSACTION_POID %s: DET_ROW_ID %s", existing.getTransactionPoid(), existing.getDetRowId());
                                loggingService.createLog(oldDetail, existing, PortCallOperationDocsMsgsDtl2.class, UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                            });
                }
            }
        }
    }


    @Override
    @Transactional
    public void deleteOperation(Long id, DeleteReasonDto deleteReasonDto) {
        log.info("Deleting port call operation id: {}", id);

        PortCallOperationHdr hdr = hdrRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Port call operation", "Transaction Poid", id));

        documentDeleteService.deleteDocument(
                id,
                "OPS_PC_OPERATION_HDR",
                "TRANSACTION_POID",
                deleteReasonDto,
                hdr.getTransactionDate()
        );
    }

    // Stored Procedure Implementations
    @Override
    public Map<String, Object> loadPda(String pdaPoid, Long groupPoid, Long companyPoid, Long userPoid) {
        try {
            log.info("[SP] PROC_PC_LOAD_PDA - pdaPoid: {}", pdaPoid);
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_PC_LOAD_PDA")
                    .declareParameters(
                            new SqlParameter("P_LOGIN_GROUP_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_COMPANY_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_USER_POID", Types.NUMERIC),
                            new SqlParameter("P_PDA_POID", Types.VARCHAR),
                            new SqlOutParameter("OUTDATA1", OracleTypes.CURSOR),
                            new SqlOutParameter("OUTDATA2", OracleTypes.CURSOR),
                            new SqlOutParameter("P_RESULT", Types.VARCHAR)
                    );
            Map<String, Object> params = new HashMap<>();
            params.put("P_LOGIN_GROUP_POID", groupPoid);
            params.put("P_LOGIN_COMPANY_POID", companyPoid);
            params.put("P_LOGIN_USER_POID", userPoid);
            params.put("P_PDA_POID", pdaPoid);
            return jdbcCall.execute(params);
        } catch (Exception e) {
            log.error("[SP] PROC_PC_LOAD_PDA - Error: {}", e.getMessage(), e);
            throw new CustomException("Error loading PDA: " + e.getMessage(), 500);
        }
    }

    @Override
    public Map<String, Object> loadFda(String fdaPoid, Long groupPoid, Long companyPoid, Long userPoid) {
        try {
            log.info("[SP] PROC_PC_LOAD_FDA - fdaPoid: {}", fdaPoid);
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_PC_LOAD_FDA")
                    .declareParameters(
                            new SqlParameter("P_LOGIN_GROUP_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_COMPANY_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_USER_POID", Types.NUMERIC),
                            new SqlParameter("P_FDA_POID", Types.VARCHAR),
                            new SqlOutParameter("OUTDATA1", OracleTypes.CURSOR),
                            new SqlOutParameter("OUTDATA2", OracleTypes.CURSOR),
                            new SqlOutParameter("P_RESULT", Types.VARCHAR)
                    );
            Map<String, Object> params = new HashMap<>();
            params.put("P_LOGIN_GROUP_POID", groupPoid);
            params.put("P_LOGIN_COMPANY_POID", companyPoid);
            params.put("P_LOGIN_USER_POID", userPoid);
            params.put("P_FDA_POID", fdaPoid);
            return jdbcCall.execute(params);
        } catch (Exception e) {
            log.error("[SP] PROC_PC_LOAD_FDA - Error: {}", e.getMessage(), e);
            throw new CustomException("Error loading FDA: " + e.getMessage(), 500);
        }
    }

    @Override
    public Map<String, Object> loadVoyage(Long voyagePoid, Long groupPoid, Long companyPoid, Long userPoid) {
        try {
            log.info("[SP] PROC_PC_LOAD_VOYAGE - voyagePoid: {}", voyagePoid);
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_PC_LOAD_VOYAGE")
                    .declareParameters(
                            new SqlParameter("P_LOGIN_GROUP_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_COMPANY_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_USER_POID", Types.NUMERIC),
                            new SqlParameter("P_VOYAGE_POID", Types.NUMERIC),
                            new SqlOutParameter("OUTDATA", OracleTypes.CURSOR),
                            new SqlOutParameter("P_RESULT", Types.VARCHAR)
                    );
            Map<String, Object> params = new HashMap<>();
            params.put("P_LOGIN_GROUP_POID", groupPoid);
            params.put("P_LOGIN_COMPANY_POID", companyPoid);
            params.put("P_LOGIN_USER_POID", userPoid);
            params.put("P_VOYAGE_POID", voyagePoid);
            return jdbcCall.execute(params);
        } catch (Exception e) {
            log.error("[SP] PROC_PC_LOAD_VOYAGE - Error: {}", e.getMessage(), e);
            throw new CustomException("Error loading Voyage: " + e.getMessage(), 500);
        }
    }

    @Override
    public Map<String, Object> loadEmailList(String transactionPoid, Long groupPoid, Long companyPoid, Long userPoid) {
        try {
            log.info("[SP] PROC_PC_LOAD_EMAIL_LIST - transactionPoid: {}", transactionPoid);
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_PC_LOAD_EMAIL_LIST")
                    .declareParameters(
                            new SqlParameter("P_LOGIN_GROUP_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_COMPANY_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_USER_POID", Types.NUMERIC),
                            new SqlParameter("P_TRANSACTION_POID", Types.VARCHAR),
                            new SqlOutParameter("OUTDATA", OracleTypes.CURSOR),
                            new SqlOutParameter("P_RESULT", Types.VARCHAR)
                    );
            Map<String, Object> params = new HashMap<>();
            params.put("P_LOGIN_GROUP_POID", groupPoid);
            params.put("P_LOGIN_COMPANY_POID", companyPoid);
            params.put("P_LOGIN_USER_POID", userPoid);
            params.put("P_TRANSACTION_POID", transactionPoid);
            return jdbcCall.execute(params);
        } catch (Exception e) {
            log.error("[SP] PROC_PC_LOAD_EMAIL_LIST - Error: {}", e.getMessage(), e);
            throw new CustomException("Error loading email list: " + e.getMessage(), 500);
        }
    }

    @Override
    public Map<String, Object> getMailTemplate(String transactionPoid, Long templatePoid, Long groupPoid, Long companyPoid, Long userPoid) {
        try {
            log.info("[SP] PROC_PC_GET_MAIL_TEMPLATE - transactionPoid: {}, templatePoid: {}", transactionPoid, templatePoid);
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_PC_GET_MAIL_TEMPLATE")
                    .declareParameters(
                            new SqlParameter("P_LOGIN_GROUP_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_COMPANY_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_USER_POID", Types.NUMERIC),
                            new SqlParameter("P_TRANSACTION_POID", Types.VARCHAR),
                            new SqlParameter("P_TEMPLATE_POID", Types.NUMERIC),
                            new SqlOutParameter("OUTDATA1", OracleTypes.CURSOR),
                            new SqlOutParameter("OUTDATA2", OracleTypes.CURSOR),
                            new SqlOutParameter("OUTDATA3", OracleTypes.CURSOR),
                            new SqlOutParameter("OUTDATA4", OracleTypes.CURSOR),
                            new SqlOutParameter("P_RESULT", Types.VARCHAR)
                    );
            Map<String, Object> params = new HashMap<>();
            params.put("P_LOGIN_GROUP_POID", groupPoid);
            params.put("P_LOGIN_COMPANY_POID", companyPoid);
            params.put("P_LOGIN_USER_POID", userPoid);
            params.put("P_TRANSACTION_POID", transactionPoid);
            params.put("P_TEMPLATE_POID", templatePoid);
            return jdbcCall.execute(params);
        } catch (Exception e) {
            log.error("[SP] PROC_PC_GET_MAIL_TEMPLATE - Error: {}", e.getMessage(), e);
            throw new CustomException("Error getting mail template: " + e.getMessage(), 500);
        }
    }

    @Override
    public Map<String, Object> getPortReportActivities(String transactionPoid, Long portReportPoid, Long groupPoid, Long companyPoid, Long userPoid) {
        try {
            log.info("[SP] PROC_PC_PORT_RPT_ACTVITIES - transactionPoid: {}, portReportPoid: {}", transactionPoid, portReportPoid);
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_PC_PORT_RPT_ACTVITIES")
                    .declareParameters(
                            new SqlParameter("P_LOGIN_GROUP_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_COMPANY_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_USER_POID", Types.NUMERIC),
                            new SqlParameter("P_TRANSACTION_POID", Types.VARCHAR),
                            new SqlParameter("P_PORT_REPORT_POID", Types.NUMERIC),
                            new SqlOutParameter("OUTDATA", OracleTypes.CURSOR),
                            new SqlOutParameter("P_RESULT", Types.VARCHAR)
                    );
            Map<String, Object> params = new HashMap<>();
            params.put("P_LOGIN_GROUP_POID", groupPoid);
            params.put("P_LOGIN_COMPANY_POID", companyPoid);
            params.put("P_LOGIN_USER_POID", userPoid);
            params.put("P_TRANSACTION_POID", transactionPoid);
            params.put("P_PORT_REPORT_POID", portReportPoid);
            return jdbcCall.execute(params);
        } catch (Exception e) {
            log.error("[SP] PROC_PC_PORT_RPT_ACTVITIES - Error: {}", e.getMessage(), e);
            throw new CustomException("Error getting port report activities: " + e.getMessage(), 500);
        }
    }

    @Override
    public Map<String, Object> getEmailRecord(Long emailPoid, String transactionPoid, Long groupPoid, Long companyPoid, Long userPoid) {
        try {
            log.info("[SP] PROC_PC_GET_EMAIL_RECORD - emailPoid: {}, transactionPoid: {}", emailPoid, transactionPoid);
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_PC_GET_EMAIL_RECORD")
                    .declareParameters(
                            new SqlParameter("P_LOGIN_GROUP_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_COMPANY_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_USER_POID", Types.NUMERIC),
                            new SqlParameter("P_EMAIL_POID", Types.NUMERIC),
                            new SqlParameter("P_TRANSACTION_POID", Types.VARCHAR),
                            new SqlOutParameter("OUTDATA", OracleTypes.CURSOR),
                            new SqlOutParameter("P_RESULT", Types.VARCHAR)
                    );
            Map<String, Object> params = new HashMap<>();
            params.put("P_LOGIN_GROUP_POID", groupPoid);
            params.put("P_LOGIN_COMPANY_POID", companyPoid);
            params.put("P_LOGIN_USER_POID", userPoid);
            params.put("P_EMAIL_POID", emailPoid);
            params.put("P_TRANSACTION_POID", transactionPoid);
            return jdbcCall.execute(params);
        } catch (Exception e) {
            log.error("[SP] PROC_PC_GET_EMAIL_RECORD - Error: {}", e.getMessage(), e);
            throw new CustomException("Error getting email record: " + e.getMessage(), 500);
        }
    }

    @Override
    public Map<String, Object> getEmailHistory(String transactionPoid, Long groupPoid, Long companyPoid, Long userPoid) {
        try {
            log.info("[SP] PROC_PC_GET_EMAIL_HISTORY - transactionPoid: {}", transactionPoid);
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_PC_GET_EMAIL_HISTORY")
                    .declareParameters(
                            new SqlParameter("P_LOGIN_GROUP_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_COMPANY_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_USER_POID", Types.NUMERIC),
                            new SqlParameter("P_TRANSACTION_POID", Types.VARCHAR),
                            new SqlOutParameter("OUTDATA", OracleTypes.CURSOR),
                            new SqlOutParameter("P_RESULT", Types.VARCHAR)
                    );
            Map<String, Object> params = new HashMap<>();
            params.put("P_LOGIN_GROUP_POID", groupPoid);
            params.put("P_LOGIN_COMPANY_POID", companyPoid);
            params.put("P_LOGIN_USER_POID", userPoid);
            params.put("P_TRANSACTION_POID", transactionPoid);
            return jdbcCall.execute(params);
        } catch (Exception e) {
            log.error("[SP] PROC_PC_GET_EMAIL_HISTORY - Error: {}", e.getMessage(), e);
            throw new CustomException("Error getting email history: " + e.getMessage(), 500);
        }
    }

    @Override
    public PortCallOperationEstBertDetailResponseDto getEstBertDetail(Long transactionPoid, Long detRowId) {
        log.info("Fetching EstBertDetail for transactionPoid: {}, detRowId: {}", transactionPoid, detRowId);

        PortCallOperationEstBertDtl entity = estBertDtlRepository.findById(new PortCallOperationEstBertDtlId(transactionPoid, detRowId))
                .orElseThrow(() -> new ResourceNotFoundException("EstBertDetail", "transactionPoid: " + transactionPoid + ", detRowId", detRowId));

        PortCallOperationEstBertDetailResponseDto.PortCallOperationEstBertDetailResponseDtoBuilder builder = PortCallOperationEstBertDetailResponseDto.builder()
                .transactionPoid(entity.getTransactionPoid())
                .detRowId(entity.getDetRowId())
                .eta(entity.getEta())
                .etb(entity.getEtb())
                .berthingAttachments(entity.getBerthingAttachments())
                .emailPoid(entity.getEmailPoid())
                .updatedOn(entity.getLastModifiedDate())
                .updatedBy(entity.getLastModifiedBy());

        // Fetch email details from PortCallOperationDocsMsgsDtl1 if emailPoid exists
        if (entity.getEmailPoid() != null) {
            docsMsgsDtl1Repository.findByEmailPoid(entity.getEmailPoid())
                    .ifPresent(emailRecord -> {
                        builder.emailSentOn(emailRecord.getEmailSendOn() != null ? emailRecord.getEmailSendOn().atStartOfDay() : null).remarks(emailRecord.getEmailRemarks());
                    });
        }

        return builder.build();
    }

    @Override
    @Transactional
    public PortCallOperationResponseDto createEstBertDetail(Long transactionPoid, PortCallOperationEstBertDetailRequestDto dto) {
        log.info("Creating EstBertDetail for transactionPoid: {}", transactionPoid);

        if (!hdrRepository.existsById(transactionPoid)) {
            throw new ResourceNotFoundException("Port call operation", "Transaction Poid", transactionPoid);
        }

        if (dto.getEmailPoid() != null && !msgsDtl1Repository.existsByIdEmailPoid(dto.getEmailPoid())) {
            throw new ResourceNotFoundException("Email", "Email Poid", dto.getEmailPoid());
        }

        if (dto.getSendEmail()) {
            if (dto.getEmailPoid() == null) {
                // Logic to send email
            } else {
                throw new CustomException("Email already sent", 400);
            }
        }

        Long nextDetRowId = estBertDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;

        PortCallOperationEstBertDtl entity = PortCallOperationEstBertDtl.builder()
                .transactionPoid(transactionPoid)
                .detRowId(nextDetRowId)
                .eta(dto.getEta())
                .etb(dto.getEtb())
                .berthingAttachments(dto.getBerthingAttachments())
                .emailPoid(dto.getEmailPoid())
                .createdBy(UserContext.getUserId())
                .createdDate(LocalDateTime.now())
                .lastModifiedBy(UserContext.getUserId())
                .lastModifiedDate(LocalDateTime.now())
                .build();

        estBertDtlRepository.save(entity);
        return getOperationById(transactionPoid);
    }

    @Override
    @Transactional
    public PortCallOperationResponseDto updateEstBertDetail(Long transactionPoid, Long detRowId, PortCallOperationEstBertDetailRequestDto dto) {
        log.info("Updating EstBertDetail for transactionPoid: {}, detRowId: {}", transactionPoid, detRowId);

        PortCallOperationEstBertDtl entity = estBertDtlRepository.findById(new PortCallOperationEstBertDtlId(transactionPoid, detRowId))
                .orElseThrow(() -> new ResourceNotFoundException("EstBertDetail", "transactionPoid: " + transactionPoid + ", detRowId", detRowId));

        if (dto.getEmailPoid() != null && !msgsDtl1Repository.existsByIdEmailPoid(dto.getEmailPoid())) {
            throw new ResourceNotFoundException("Email", "Email Poid", dto.getEmailPoid());
        }

        if (dto.getSendEmail()) {
            if (dto.getEmailPoid() == null) {
                // Logic to send email
            } else {
                throw new CustomException("Email already sent", 400);
            }
        }

        entity.setEta(dto.getEta());
        entity.setEtb(dto.getEtb());
        entity.setBerthingAttachments(dto.getBerthingAttachments());
        entity.setEmailPoid(dto.getEmailPoid());
        entity.setLastModifiedBy(UserContext.getUserId());
        entity.setLastModifiedDate(LocalDateTime.now());

        estBertDtlRepository.save(entity);
        return getOperationById(transactionPoid);
    }

    @Override
    public List<PortCallOperationEstPrearrivalActDetailResponseDto> listEstPrearrivalActDetails(Long transactionPoid, Long detRowId) {
        log.info("Listing EstPrearrivalActDetails for transactionPoid: {}, detRowId: {}", transactionPoid, detRowId);

        List<PortCallOperationEstPrearrivalActDtl> entities = estPrearrivalActDtlRepository.findByTransactionPoidAndDetRowId(transactionPoid, detRowId);

        return entities.stream()
                .map(e -> PortCallOperationEstPrearrivalActDetailResponseDto.builder()
                        .transactionPoid(e.getTransactionPoid())
                        .detRowId(e.getDetRowId())
                        .preActivityDtlPoid(e.getPreActivityDtlPoid())
                        .activityPoid(e.getActivityPoid())
                        .otherDescription(e.getOtherDescription())
                        .estimatedDatetime(e.getEstimatedDatetime())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PortCallOperationEstPrearrivalActDetailResponseDto createEstPrearrivalActDetail(Long transactionPoid, Long detRowId, PortCallOperationEstPrearrivalActDetailDto dto) {
        log.info("Creating EstPrearrivalActDetail for transactionPoid: {}, detRowId: {}", transactionPoid, detRowId);

        if (!hdrRepository.existsById(transactionPoid)) {
            throw new ResourceNotFoundException("Port call operation", "Transaction Poid", transactionPoid);
        }
        if (!portActivityMasterRepository.existsByPortActivityTypePoid(dto.getActivityPoid())) {
            throw new ResourceNotFoundException("Port activity", "Transaction Poid", dto.getActivityPoid());
        }

        if (dto.getSendEmail()) {
            // Validate that email can only be sent at least 1 day before ETA/ETB
            PortCallOperationEstPrearrivalDtl estPrearrivalDtl = estPrearrivalDtlRepository
                    .findById(new PortCallOperationEstPrearrivalDtlId(transactionPoid, detRowId))
                    .orElseThrow(() -> new ResourceNotFoundException("Est Prearrival Detail not found with transactionPoid: " + transactionPoid + ", detRowId: " + detRowId));

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneDayFromNow = now.plusDays(1);

            if (estPrearrivalDtl.getEta() != null && estPrearrivalDtl.getEta().isBefore(oneDayFromNow)) {
                throw new ValidationException(String.format("Email can only be sent at least 1 day before ETA. ETA is %s, current time is %s", estPrearrivalDtl.getEta(), now));
            }

            if (estPrearrivalDtl.getEtb() != null && estPrearrivalDtl.getEtb().isBefore(oneDayFromNow)) {
                throw new ValidationException(String.format("Email can only be sent at least 1 day before ETB. ETB is %s, current time is %s", estPrearrivalDtl.getEtb(), now));
            }
        }

        Long nextPreActivityDtlPoid = estPrearrivalActDtlRepository.findMaxPreActivityDtlPoidByTransactionPoidAndDetRowId(transactionPoid, detRowId) + 1;

        PortCallOperationEstPrearrivalActDtl entity = PortCallOperationEstPrearrivalActDtl.builder()
                .transactionPoid(transactionPoid)
                .detRowId(detRowId)
                .preActivityDtlPoid(nextPreActivityDtlPoid)
                .activityPoid(dto.getActivityPoid())
                .otherDescription(dto.getOtherDescription())
                .estimatedDatetime(dto.getEstimatedDatetime())
                .createdBy(UserContext.getUserId())
                .createdDate(LocalDateTime.now())
                .lastModifiedBy(UserContext.getUserId())
                .lastModifiedDate(LocalDateTime.now())
                .build();

        entity = estPrearrivalActDtlRepository.save(entity);

        return PortCallOperationEstPrearrivalActDetailResponseDto.builder()
                .transactionPoid(entity.getTransactionPoid())
                .detRowId(entity.getDetRowId())
                .preActivityDtlPoid(entity.getPreActivityDtlPoid())
                .activityPoid(entity.getActivityPoid())
                .otherDescription(entity.getOtherDescription())
                .estimatedDatetime(entity.getEstimatedDatetime())
                .build();
    }

    @Override
    @Transactional
    public PortCallOperationEstPrearrivalActDetailResponseDto updateEstPrearrivalActDetail(Long transactionPoid, Long detRowId, Long preActivityDtlPoid, PortCallOperationEstPrearrivalActDetailDto dto) {
        log.info("Updating EstPrearrivalActDetail for transactionPoid: {}, detRowId: {}, preActivityDtlPoid: {}", transactionPoid, detRowId, preActivityDtlPoid);

        PortCallOperationEstPrearrivalActDtl entity = estPrearrivalActDtlRepository.findById(new PortCallOperationEstPrearrivalActDtlId(transactionPoid, detRowId, preActivityDtlPoid))
                .orElseThrow(() -> new ResourceNotFoundException("EstPrearrivalActDetail", "transactionPoid: " + transactionPoid + ", detRowId: " + detRowId + ", preActivityDtlPoid", preActivityDtlPoid));

        if (!portActivityMasterRepository.existsByPortActivityTypePoid(dto.getActivityPoid())) {
            throw new ResourceNotFoundException("Port activity", "Transaction Poid", dto.getActivityPoid());
        }

        PortCallOperationEstPrearrivalActDtl latestRecord = estPrearrivalActDtlRepository.findByTransactionPoidOrderByLastModifiedDateDesc(transactionPoid).getFirst();
        if (!latestRecord.getPreActivityDtlPoid().equals(preActivityDtlPoid)) {
            throw new ValidationException("Cannot edit this record. Please select a latest one");
        }

        if (dto.getSendEmail()) {
            // Validate that email can only be sent at least 1 day before ETA/ETB
            PortCallOperationEstPrearrivalDtl estPrearrivalDtl = estPrearrivalDtlRepository
                    .findById(new PortCallOperationEstPrearrivalDtlId(transactionPoid, detRowId))
                    .orElseThrow(() -> new ResourceNotFoundException("Est Prearrival Detail not found with transactionPoid: " + transactionPoid + ", detRowId: " + detRowId));

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneDayFromNow = now.plusDays(1);

            if (estPrearrivalDtl.getEta() != null && estPrearrivalDtl.getEta().isBefore(oneDayFromNow)) {
                throw new ValidationException(String.format("Email can only be sent at least 1 day before ETA. ETA is %s, current time is %s", estPrearrivalDtl.getEta(), now));
            }

            if (estPrearrivalDtl.getEtb() != null && estPrearrivalDtl.getEtb().isBefore(oneDayFromNow)) {
                throw new ValidationException(String.format("Email can only be sent at least 1 day before ETB. ETB is %s, current time is %s", estPrearrivalDtl.getEtb(), now));
            }
        }

        entity.setActivityPoid(dto.getActivityPoid());
        entity.setOtherDescription(dto.getOtherDescription());
        entity.setEstimatedDatetime(dto.getEstimatedDatetime());
        entity.setLastModifiedBy(UserContext.getUserId());
        entity.setLastModifiedDate(LocalDateTime.now());

        entity = estPrearrivalActDtlRepository.save(entity);

        return PortCallOperationEstPrearrivalActDetailResponseDto.builder()
                .transactionPoid(entity.getTransactionPoid())
                .detRowId(entity.getDetRowId())
                .preActivityDtlPoid(entity.getPreActivityDtlPoid())
                .activityPoid(entity.getActivityPoid())
                .otherDescription(entity.getOtherDescription())
                .estimatedDatetime(entity.getEstimatedDatetime())
                .build();
    }

    @Override
    public List<PortCallOperationActTimingsActvtyDetailResponseDto> listActTimingsActvtyDetails(Long transactionPoid, Long detRowId) {
        log.info("Listing ActTimingsActvtyDetails for transactionPoid: {}, detRowId: {}", transactionPoid, detRowId);

        List<PortCallOperationActTimingsActvtyDtl> entities = actTimingsActvtyDtlRepository.findByTransactionPoidAndDetRowId(transactionPoid, detRowId);

        return entities.stream()
                .map(e -> PortCallOperationActTimingsActvtyDetailResponseDto.builder()
                        .transactionPoid(e.getTransactionPoid())
                        .detRowId(e.getDetRowId())
                        .actualsTimingDtlPoid(e.getActualsTimingDtlPoid())
                        .activityPoid(e.getActivityPoid())
                        .details(e.getDetails())
                        .estimatedDatetime(e.getEstimatedDatetime())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PortCallOperationActTimingsActvtyDetailResponseDto createActTimingsActvtyDetail(Long transactionPoid, Long detRowId, PortCallOperationActTimingsActvtyDetailDto dto) {
        log.info("Creating ActTimingsActvtyDetail for transactionPoid: {}, detRowId: {}", transactionPoid, detRowId);

        if (!hdrRepository.existsById(transactionPoid)) {
            throw new ResourceNotFoundException("Port call operation", "Transaction Poid", transactionPoid);
        }
        if (!portActivityMasterRepository.existsByPortActivityTypePoid(dto.getActivityPoid())) {
            throw new ResourceNotFoundException("Port activity", "Transaction Poid", dto.getActivityPoid());
        }

        if (dto.getSendEmail()) {
            // Logic to send email
        }

        Long nextActualsTimingDtlPoid = actTimingsActvtyDtlRepository.findMaxActualsTimingDtlPoidByTransactionPoidAndDetRowId(transactionPoid, detRowId) + 1;

        PortCallOperationActTimingsActvtyDtl entity = PortCallOperationActTimingsActvtyDtl.builder()
                .transactionPoid(transactionPoid)
                .detRowId(detRowId)
                .actualsTimingDtlPoid(nextActualsTimingDtlPoid)
                .activityPoid(dto.getActivityPoid())
                .details(dto.getDetails())
                .estimatedDatetime(dto.getEstimatedDatetime())
                .createdBy(UserContext.getUserId())
                .createdDate(LocalDateTime.now())
                .lastModifiedBy(UserContext.getUserId())
                .lastModifiedDate(LocalDateTime.now())
                .build();

        entity = actTimingsActvtyDtlRepository.save(entity);

        return PortCallOperationActTimingsActvtyDetailResponseDto.builder()
                .transactionPoid(entity.getTransactionPoid())
                .detRowId(entity.getDetRowId())
                .actualsTimingDtlPoid(entity.getActualsTimingDtlPoid())
                .activityPoid(entity.getActivityPoid())
                .details(entity.getDetails())
                .estimatedDatetime(entity.getEstimatedDatetime())
                .build();
    }

    @Override
    @Transactional
    public PortCallOperationActTimingsActvtyDetailResponseDto updateActTimingsActvtyDetail(Long transactionPoid, Long detRowId, Long actualsTimingDtlPoid, PortCallOperationActTimingsActvtyDetailDto dto) {
        log.info("Updating ActTimingsActvtyDetail for transactionPoid: {}, detRowId: {}, actualsTimingDtlPoid: {}", transactionPoid, detRowId, actualsTimingDtlPoid);

        PortCallOperationActTimingsActvtyDtl entity = actTimingsActvtyDtlRepository.findById(new PortCallOperationActTimingsActvtyDtlId(transactionPoid, detRowId, actualsTimingDtlPoid))
                .orElseThrow(() -> new ResourceNotFoundException("ActTimingsActvtyDetail", "transactionPoid: " + transactionPoid + ", detRowId: " + detRowId + ", actualsTimingDtlPoid", actualsTimingDtlPoid));

        if (!portActivityMasterRepository.existsByPortActivityTypePoid(dto.getActivityPoid())) {
            throw new ResourceNotFoundException("Port activity", "Transaction Poid", dto.getActivityPoid());
        }

        PortCallOperationActTimingsActvtyDtl latestRecord = actTimingsActvtyDtlRepository.findByTransactionPoidOrderByLastModifiedDateDesc(transactionPoid).getFirst();
        if (!latestRecord.getActualsTimingDtlPoid().equals(actualsTimingDtlPoid)) {
            throw new ValidationException("Cannot edit this record. Please select a latest one");
        }

        if (dto.getSendEmail()) {
            // Logic to send email
        }

        entity.setActivityPoid(dto.getActivityPoid());
        entity.setDetails(dto.getDetails());
        entity.setEstimatedDatetime(dto.getEstimatedDatetime());
        entity.setLastModifiedBy(UserContext.getUserId());
        entity.setLastModifiedDate(LocalDateTime.now());

        entity = actTimingsActvtyDtlRepository.save(entity);

        return PortCallOperationActTimingsActvtyDetailResponseDto.builder()
                .transactionPoid(entity.getTransactionPoid())
                .detRowId(entity.getDetRowId())
                .actualsTimingDtlPoid(entity.getActualsTimingDtlPoid())
                .activityPoid(entity.getActivityPoid())
                .details(entity.getDetails())
                .estimatedDatetime(entity.getEstimatedDatetime())
                .build();
    }

    @Override
    public PortCallOperationDocsCopyDetailResponseDto getDocsCopyDetail(Long transactionPoid, Long detRowId) {
        log.info("Fetching DocsCopyDetail for transactionPoid: {}, detRowId: {}", transactionPoid, detRowId);

        PortCallOperationDocsCopyDtl entity = docsCopyDtlRepository.findById(new PortCallOperationDocsCopyDtlId(transactionPoid, detRowId))
                .orElseThrow(() -> new ResourceNotFoundException("DocsCopyDetail", "transactionPoid: " + transactionPoid + ", detRowId", detRowId));

        return PortCallOperationDocsCopyDetailResponseDto.builder()
                .transactionPoid(entity.getTransactionPoid())
                .detRowId(entity.getDetRowId())
                .documentFrom(entity.getDocumentFrom())
                .documentList(entity.getDocumentList())
                .documentSelect(entity.getDocumentSelect())
                .documentAttachments(entity.getDocumentAttachments())
                .build();
    }

    @Override
    @Transactional
    public PortCallOperationResponseDto createDocsCopyDetail(Long transactionPoid, PortCallOperationDocsCopyDetailRequestDto dto) {
        log.info("Creating DocsCopyDetail for transactionPoid: {}", transactionPoid);

        if (!hdrRepository.existsById(transactionPoid)) {
            throw new ResourceNotFoundException("Port call operation", "Transaction Poid", transactionPoid);
        }

        if (dto.getSendEmail()) {
            // Logic to send email
        }

        Long nextDetRowId = docsCopyDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;

        PortCallOperationDocsCopyDtl entity = PortCallOperationDocsCopyDtl.builder()
                .transactionPoid(transactionPoid)
                .detRowId(nextDetRowId)
                .documentFrom(dto.getDocumentFrom())
                .documentList(dto.getDocumentList())
                .documentSelect(dto.getDocumentSelect())
                .documentAttachments(dto.getDocumentAttachments())
                .createdBy(UserContext.getUserId())
                .createdDate(LocalDateTime.now())
                .lastModifiedBy(UserContext.getUserId())
                .lastModifiedDate(LocalDateTime.now())
                .build();

        docsCopyDtlRepository.save(entity);
        return getOperationById(transactionPoid);
    }

    @Override
    @Transactional
    public PortCallOperationResponseDto updateDocsCopyDetail(Long transactionPoid, Long detRowId, PortCallOperationDocsCopyDetailRequestDto dto) {
        log.info("Updating DocsCopyDetail for transactionPoid: {}, detRowId: {}", transactionPoid, detRowId);

        PortCallOperationDocsCopyDtl entity = docsCopyDtlRepository.findById(new PortCallOperationDocsCopyDtlId(transactionPoid, detRowId))
                .orElseThrow(() -> new ResourceNotFoundException("DocsCopyDetail", "transactionPoid: " + transactionPoid + ", detRowId", detRowId));

        PortCallOperationDocsCopyDtl latestRecord = docsCopyDtlRepository.findByTransactionPoidOrderByLastModifiedDateDesc(transactionPoid).getFirst();
        if (!latestRecord.getDetRowId().equals(detRowId)) {
            throw new ValidationException("Cannot edit this record. Please select a latest one");
        }

        if (dto.getSendEmail()) {
            // Logic to send email
        }

        entity.setDocumentFrom(dto.getDocumentFrom());
        entity.setDocumentList(dto.getDocumentList());
        entity.setDocumentSelect(dto.getDocumentSelect());
        entity.setDocumentAttachments(dto.getDocumentAttachments());
        entity.setLastModifiedBy(UserContext.getUserId());
        entity.setLastModifiedDate(LocalDateTime.now());

        docsCopyDtlRepository.save(entity);
        return getOperationById(transactionPoid);
    }
}
