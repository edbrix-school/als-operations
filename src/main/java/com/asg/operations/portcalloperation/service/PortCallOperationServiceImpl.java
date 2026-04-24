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
import com.asg.common.lib.utility.DateUtil;
import com.asg.common.lib.utility.PaginationUtil;
import com.asg.operations.common.repository.GlobalParameterRepository;
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
import com.asg.operations.portcallreport.entity.PortCallReportDtl;
import com.asg.operations.portcallreport.enums.ActionType;
import com.asg.operations.portcallreport.repository.PortCallReportDtlRepository;
import com.asg.operations.portcallreport.repository.PortCallReportHdrRepository;
import com.asg.operations.shipprincipal.repository.ShipPrincipalRepository;
import jakarta.persistence.EntityManager;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import oracle.jdbc.OracleTypes;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlOutParameter;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
    private final PortCallReportHdrRepository portCallReportHdrRepository;
    private final StockUnitMasterRepository stockUnitMasterRepository;
    private final PortActivityMasterRepository portActivityMasterRepository;
    private final GlobalParameterRepository globalParameterRepository;
    private final PortCallReportDtlRepository dtlRepository;
    private final PortCallOperationScreenAttachmentService screenAttachmentService;
    private final EntityManager entityManager;


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
                .createdBy(hdr.getCreatedBy())
                .createdDate(hdr.getCreatedDate())
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
                    .updatedOn(dtl.getLastModifiedDate() != null ? dtl.getLastModifiedDate().truncatedTo(ChronoUnit.MINUTES) : null)
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
        return details.stream().map(dtl -> {
            PortCallOperationActTimingDetailResponseDto.PortCallOperationActTimingDetailResponseDtoBuilder builder = PortCallOperationActTimingDetailResponseDto.builder()
                    .transactionPoid(dtl.getTransactionPoid())
                    .detRowId(dtl.getDetRowId())
                    .portReportPoid(dtl.getPortReportPoid())
                    .actualsTimingDtlPoid(dtl.getActualsTimingDtlPoid())
                    .emailPoid(dtl.getEmailPoid());

            // Fetch email details from PortCallOperationDocsMsgsDtl1 if emailPoid exists
            if (dtl.getEmailPoid() != null) {
                docsMsgsDtl1Repository.findByEmailPoid(dtl.getEmailPoid())
                        .ifPresent(emailRecord -> {
                            builder.sentStatus(emailRecord.getEmailSendOn() != null ? emailRecord.getEmailSendOn().atStartOfDay() : null).details(emailRecord.getEmailRemarks());
                        });
            }

            return builder.build();
        }).collect(Collectors.toList());
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
        return details.stream().map(dtl -> {
            PortCallOperationActProgDetailResponseDto.PortCallOperationActProgDetailResponseDtoBuilder builder = PortCallOperationActProgDetailResponseDto.builder()
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
                    .estBlDate(dtl.getEstBlDate());

            // Fetch email details from PortCallOperationDocsMsgsDtl1 if emailPoid exists
            if (dtl.getEmailPoid() != null) {
                docsMsgsDtl1Repository.findByEmailPoid(dtl.getEmailPoid())
                        .ifPresent(emailRecord -> {
                            builder.emailSendOn(emailRecord.getEmailSendOn() != null ? emailRecord.getEmailSendOn().atStartOfDay() : null);
                        });
            }

            return builder.build();
        }).collect(Collectors.toList());
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
        return details.stream().map(dtl -> {
            PortCallOperationDocsCopyDetailResponseDto.PortCallOperationDocsCopyDetailResponseDtoBuilder builder = PortCallOperationDocsCopyDetailResponseDto.builder()
                    .transactionPoid(dtl.getTransactionPoid())
                    .detRowId(dtl.getDetRowId())
                    .documentFrom(dtl.getDocumentFrom())
                    .documentList(dtl.getDocumentList())
                    .documentSelect(dtl.getDocumentSelect())
                    .emailPoid(dtl.getEmailPoid())
                    .documentAttachments(dtl.getDocumentAttachments());

            // Fetch email details from PortCallOperationDocsMsgsDtl1 if emailPoid exists
            if (dtl.getEmailPoid() != null) {
                docsMsgsDtl1Repository.findByEmailPoid(dtl.getEmailPoid())
                        .ifPresent(emailRecord -> {
                            builder.emailSentOn(emailRecord.getEmailSendOn() != null ? emailRecord.getEmailSendOn() : null);
                        });
            }

            return builder.build();
        }).collect(Collectors.toList());
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

        LocalDate txnDate = dto.getTransactionDate() != null
                ? dto.getTransactionDate()
                : DateUtil.getCurrentDateInUserTimeZone();
        PortCallOperationHdr hdr = PortCallOperationHdr.builder()
                .transactionDate(txnDate)
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
                .agencyType(dto.getAgencyType())
                .portOfCallPoid(dto.getPortOfCallPoid())
                .specialInstructions(dto.getSpecialInstructions())
                .termsConditions(dto.getTermsConditions())
                .build();

        hdr = hdrRepository.save(hdr);
        entityManager.refresh(hdr);

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

        String key = hdr.getTransactionPoid().toString();
        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), key, String.format("%s %s", LogDetailsEnum.CREATED, hdr.getDocRef()));
        return getOperationById(hdr.getTransactionPoid());
    }

    @Override
    @Transactional
    public PortCallOperationResponseDto updateOperation(Long id, PortCallOperationDto dto, Long userPoid, Long groupPoid,
                                                        Long[] husbandryCrewDetRowIdByDetailIndexOut,
                                                        Long[] husbandryOthDetRowIdByDetailIndexOut) {
        log.info("Updating port call operation id: {}", id);

        PortCallOperationHdr hdr = hdrRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Port call operation", "Transaction Poid", id));

        PortCallOperationHdr oldHdr = new PortCallOperationHdr();
        BeanUtils.copyProperties(hdr, oldHdr);

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

        LocalDate txnDate = dto.getTransactionDate() != null
                ? dto.getTransactionDate()
                : DateUtil.getCurrentDateInUserTimeZone();
        hdr.setTransactionDate(txnDate);
        hdr.setGroupPoid(groupPoid);
        hdr.setCompanyPoid(UserContext.getCompanyPoid());
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
        hdr.setAgencyType(dto.getAgencyType());
        hdr.setSpecialInstructions(dto.getSpecialInstructions());
        hdr.setTermsConditions(dto.getTermsConditions());

        hdr.setPdaRefPoid(dto.getPdaRefPoid());
        hdr.setFdaRefPoid(dto.getFdaRefPoid());

        hdr.setPdaAnchorageStayDays(dto.getPdaAnchorageStayDays());
        hdr.setPdaBerthStayDays(dto.getPdaBerthStayDays());
        hdr.setPdaPortStayDays(dto.getPdaPortStayDays());
        hdr.setPdaFdaRemarks(dto.getPdaFdaRemarks());

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
                                BeanUtils.copyProperties(existing, oldDetail);

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
                                BeanUtils.copyProperties(existing, oldDetail);

                                existing.setCommunicationType(mailDto.getCommunicationType());
                                existing.setCommunicationMode(mailDto.getCommunicationMode());
                                existing.setCompany(mailDto.getCompany());
                                existing.setAddressee(mailDto.getAddressee());
                                existing.setEmailIds(mailDto.getEmailIds());
                                existing = mailDtlRepository.save(existing);

                                String logDetail = String.format("KeyId = TRANSACTION_POID %s: DET_ROW_ID %s", existing.getTransactionPoid(), existing.getDetRowId());
                                loggingService.createLog(oldDetail, existing, PortCallOperationMailDtl.class, UserContext.getDocumentId(), id.toString(), logDetail);
                            });
                }
            }
        }
        // Update all other detail tables with actionType
        updateAllDetailTables(id, dto, UserContext.getUserId(),
                husbandryCrewDetRowIdByDetailIndexOut, husbandryOthDetRowIdByDetailIndexOut);

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


    private void updateAllDetailTables(Long transactionPoid, PortCallOperationDto dto, String userId,
                                       Long[] husbandryCrewDetRowIdByDetailIndexOut,
                                       Long[] husbandryOthDetRowIdByDetailIndexOut) {

        if (husbandryCrewDetRowIdByDetailIndexOut != null) {
            if (dto.getHusbandryCrewDetails() == null || dto.getHusbandryCrewDetails().isEmpty()) {
                throw new ValidationException("When resolving husbandry crew row ids, dto.husbandryCrewDetails must be non-empty.");
            }
            if (husbandryCrewDetRowIdByDetailIndexOut.length != dto.getHusbandryCrewDetails().size()) {
                throw new IllegalArgumentException("husbandryCrewDetRowIdByDetailIndexOut length must match husbandryCrewDetails size.");
            }
        }
        if (husbandryOthDetRowIdByDetailIndexOut != null) {
            if (dto.getHusbandryOthDetails() == null || dto.getHusbandryOthDetails().isEmpty()) {
                throw new ValidationException("When resolving husbandry other row ids, dto.husbandryOthDetails must be non-empty.");
            }
            if (husbandryOthDetRowIdByDetailIndexOut.length != dto.getHusbandryOthDetails().size()) {
                throw new IllegalArgumentException("husbandryOthDetRowIdByDetailIndexOut length must match husbandryOthDetails size.");
            }
        }
//
//        // Update Est Bert Details
//        if (dto.getEstBertDetails() != null && !dto.getEstBertDetails().isEmpty()) {
//            for (PortCallOperationEstBertDetailDto detailDto : dto.getEstBertDetails()) {
//                if (detailDto.getEmailPoid() != null) {
//                    if (!docsMsgsDtl1Repository.existsByEmailPoid(detailDto.getEmailPoid())) {
//                        throw new ResourceNotFoundException("Email", "Email Poid", detailDto.getEmailPoid());
//                    }
//                }
//                ActionType action = detailDto.getActionType();
//                if (action == null || action == ActionType.noChange) continue;
//                if (action == ActionType.isCreated) {
//                    Long nextDetRowId = estBertDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
//                    PortCallOperationEstBertDtl saved = estBertDtlRepository.save(PortCallOperationEstBertDtl.builder()
//                            .transactionPoid(transactionPoid)
//                            .detRowId(nextDetRowId)
//                            .eta(detailDto.getEta())
//                            .etb(detailDto.getEtb())
//                            .emailPoid(detailDto.getEmailPoid())
//                            .createdBy(UserContext.getUserId())
//                            .createdDate(LocalDateTime.now())
//                            .lastModifiedBy(UserContext.getUserId())
//                            .lastModifiedDate(LocalDateTime.now())
//                            .build());
//                    String logDetail = String.format("Row Created on [Port Call Operation Est Bert Details] with detRowId: %s", saved.getDetRowId());
//                    loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
//                } else if (action == ActionType.isUpdated) {
//                    estBertDtlRepository.findById(new PortCallOperationEstBertDtlId(transactionPoid, detailDto.getDetRowId()))
//                            .ifPresent(existing -> {
//                                PortCallOperationEstBertDtl oldDetail = new PortCallOperationEstBertDtl();
//                                org.springframework.beans.BeanUtils.copyProperties(existing, oldDetail);
//                                existing.setEta(detailDto.getEta());
//                                existing.setEtb(detailDto.getEtb());
//                                existing.setEmailPoid(detailDto.getEmailPoid());
//                                existing.setLastModifiedBy(userId);
//                                existing.setLastModifiedDate(LocalDateTime.now());
//                                existing = estBertDtlRepository.save(existing);
//                                String logDetail = String.format("KeyId = TRANSACTION_POID %s: DET_ROW_ID %s", existing.getTransactionPoid(), existing.getDetRowId());
//                                loggingService.createLog(oldDetail, existing, PortCallOperationEstBertDtl.class, UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
//                            });
//                }
//            }
//        }
//
        // Similar pattern for all other detail tables - Est Prearrival, Act Timing, etc.
        // Due to length constraints, I'll add a few key ones and note that the pattern should be repeated for all

        // Update Est Prearrival Details
        if (dto.getEstPrearrivalDetails() != null && !dto.getEstPrearrivalDetails().isEmpty()) {
            for (PortCallOperationEstPrearrivalDetailDto detailDto : dto.getEstPrearrivalDetails()) {
                if (detailDto.getEmailPoid() != null) {
                    if (!docsMsgsDtl1Repository.existsByEmailPoid(detailDto.getEmailPoid())) {
                        throw new ResourceNotFoundException("Email", "Email Poid", detailDto.getEmailPoid());
                    }
                }
                ActionType action = detailDto.getActionType();
                if (action == null || action == ActionType.noChange) continue;
//                if (action == ActionType.isCreated) {
//                    Long nextDetRowId = estPrearrivalDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
//                    PortCallOperationEstPrearrivalDtl saved = estPrearrivalDtlRepository.save(PortCallOperationEstPrearrivalDtl.builder()
//                            .transactionPoid(transactionPoid).detRowId(nextDetRowId)
//                            .preActivityDtlPoid(detailDto.getPreActivityDtlPoid())
//                            .eta(detailDto.getEta())
//                            .etb(detailDto.getEtb())
//                            .emailPoid(detailDto.getEmailPoid())
//                            .createdBy(userId).build());
//                    String logDetail = String.format("Row Created on [Port Call Operation Est Prearrival Details] with detRowId: %s", saved.getDetRowId());
//                    loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
//                }
                else if (action == ActionType.isUpdated) {
                    estPrearrivalDtlRepository.findById(new PortCallOperationEstPrearrivalDtlId(transactionPoid, detailDto.getDetRowId()))
                            .ifPresent(existing -> {
                                PortCallOperationEstPrearrivalDtl oldDetail = new PortCallOperationEstPrearrivalDtl();
                                org.springframework.beans.BeanUtils.copyProperties(existing, oldDetail);
                                existing.setEta(detailDto.getEta());
                                existing.setEtb(detailDto.getEtb());
                                existing = estPrearrivalDtlRepository.save(existing);
                                String logDetail = String.format("KeyId = TRANSACTION_POID %s: DET_ROW_ID %s", existing.getTransactionPoid(), existing.getDetRowId());
                                loggingService.createLog(oldDetail, existing, PortCallOperationEstPrearrivalDtl.class, UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                            });
                }
            }
        }

//        // Update Act Timing Details (3-part key)
//        if (dto.getActTimingDetails() != null && !dto.getActTimingDetails().isEmpty()) {
//            for (PortCallOperationActTimingDetailDto detailDto : dto.getActTimingDetails()) {
//                if (detailDto.getEmailPoid() != null) {
//                    if (!docsMsgsDtl1Repository.existsByEmailPoid(detailDto.getEmailPoid())) {
//                        throw new ResourceNotFoundException("Email", "Email Poid", detailDto.getEmailPoid());
//                    }
//                }
//                if (detailDto.getPortReportPoid() != null) {
//                    if (!portCallReportHdrRepository.existsByPortCallReportPoid(detailDto.getPortReportPoid())) {
//                        throw new ResourceNotFoundException("Port Report", "Port Report Poid", detailDto.getPortReportPoid());
//                    }
//                }
//                ActionType action = detailDto.getActionType();
//                if (action == null || action == ActionType.noChange) continue;
//                if (action == ActionType.isCreated) {
//                    Long nextDetRowId = actTimingDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
//                    PortCallOperationActTimingDtl saved = actTimingDtlRepository.save(PortCallOperationActTimingDtl.builder()
//                            .transactionPoid(transactionPoid)
//                            .detRowId(nextDetRowId)
//                            .portReportPoid(detailDto.getPortReportPoid())
//                            .actualsTimingDtlPoid(detailDto.getActualsTimingDtlPoid())
//                            .emailPoid(detailDto.getEmailPoid())
//                            .createdBy(UserContext.getUserId())
//                            .createdDate(LocalDateTime.now())
//                            .lastModifiedBy(UserContext.getUserId())
//                            .lastModifiedDate(LocalDateTime.now())
//                            .build());
//                    String logDetail = String.format("Row Created on [Port Call Operation Act Timing Details] with detRowId: %s", saved.getDetRowId());
//                    loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
//                } else if (action == ActionType.isUpdated) {
//                    actTimingDtlRepository.findById(new PortCallOperationActTimingDtlId(
//                                    transactionPoid, detailDto.getDetRowId(), detailDto.getPortReportPoid()))
//                            .ifPresent(existing -> {
//                                PortCallOperationActTimingDtl oldDetail = new PortCallOperationActTimingDtl();
//                                org.springframework.beans.BeanUtils.copyProperties(existing, oldDetail);
//                                existing.setActualsTimingDtlPoid(detailDto.getActualsTimingDtlPoid());
//                                existing.setEmailPoid(detailDto.getEmailPoid());
//                                existing.setLastModifiedBy(userId);
//                                existing.setLastModifiedDate(LocalDateTime.now());
//                                existing = actTimingDtlRepository.save(existing);
//                                String logDetail = String.format("KeyId = TRANSACTION_POID %s: DET_ROW_ID %s", existing.getTransactionPoid(), existing.getDetRowId());
//                                loggingService.createLog(oldDetail, existing, PortCallOperationActTimingDtl.class, UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
//                            });
//                }
//            }
//        }
//

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
                            .build());
                    String logDetail = String.format("Row Created on [Port Call Operation Act Cond Details] with detRowId: %s", saved.getDetRowId());
                    loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                } else if (action == ActionType.isUpdated) {
                    actCondDtlRepository.findById(new PortCallOperationActCondDtlId(transactionPoid, detailDto.getDetRowId()))
                            .ifPresent(existing -> {
                                PortCallOperationActCondDtl oldDetail = new PortCallOperationActCondDtl();
                                BeanUtils.copyProperties(existing, oldDetail);
                                existing.setConditionType(detailDto.getConditionType());
                                existing.setDraftForward(detailDto.getDraftForward());
                                existing.setDraftMid(detailDto.getDraftMid());
                                existing.setDraftAft(detailDto.getDraftAft());
                                existing.setFuelOil(detailDto.getFuelOil());
                                existing.setDieselOil(detailDto.getDieselOil());
                                existing.setFreshWater(detailDto.getFreshWater());
                                existing.setTugsService(detailDto.getTugsService());
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
                            .build());
                    String logDetail = String.format("Row Created on [Port Call Operation Act Rmks Details] with detRowId: %s", saved.getDetRowId());
                    loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                } else if (action == ActionType.isUpdated) {
                    actRmksDtlRepository.findById(new PortCallOperationActRmksDtlId(transactionPoid, detailDto.getDetRowId()))
                            .ifPresent(existing -> {
                                PortCallOperationActRmksDtl oldDetail = new PortCallOperationActRmksDtl();
                                BeanUtils.copyProperties(existing, oldDetail);
                                existing.setRemarksType(detailDto.getRemarksType());
                                existing.setRemarksFrom(detailDto.getRemarksFrom());
                                existing.setRemarksTo(detailDto.getRemarksTo());
                                existing.setCargoDetails(detailDto.getCargoDetails());
                                existing.setReason(detailDto.getReason());
                                existing.setPcReportPoid(detailDto.getPcReportPoid());
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
                    if (!docsMsgsDtl1Repository.existsByEmailPoid(detailDto.getEmailPoid())) {
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
                            .build());
                    String logDetail = String.format("Row Created on [Port Call Operation Act Prog Details] with detRowId: %s", saved.getDetRowId());
                    loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                } else if (action == ActionType.isUpdated) {
                    actProgDtlRepository.findById(new PortCallOperationActProgDtlId(transactionPoid, detailDto.getDetRowId()))
                            .ifPresent(existing -> {
                                PortCallOperationActProgDtl oldDetail = new PortCallOperationActProgDtl();
                                BeanUtils.copyProperties(existing, oldDetail);
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
                            .build());
                    String logDetail = String.format("Row Created on [Port Call Operation Act Cargo Fig Details] with detRowId: %s", saved.getDetRowId());
                    loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                } else if (action == ActionType.isUpdated) {
                    actCargoFigDtlRepository.findById(new PortCallOperationActCargoFigDtlId(transactionPoid, detailDto.getDetRowId()))
                            .ifPresent(existing -> {
                                PortCallOperationActCargoFigDtl oldDetail = new PortCallOperationActCargoFigDtl();
                                BeanUtils.copyProperties(existing, oldDetail);
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
                            .build());
                    String logDetail = String.format("Row Created on [Port Call Operation Act Bunker Details] with detRowId: %s", saved.getDetRowId());
                    loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                } else if (action == ActionType.isUpdated) {
                    actBunkerDtlRepository.findById(new PortCallOperationActBunkerDtlId(transactionPoid, detailDto.getDetRowId()))
                            .ifPresent(existing -> {
                                PortCallOperationActBunkerDtl oldDetail = new PortCallOperationActBunkerDtl();
                                BeanUtils.copyProperties(existing, oldDetail);
                                existing.setGrade(detailDto.getGrade());
                                existing.setNominatedQtyMt(detailDto.getNominatedQtyMt());
                                existing.setSuppliedQtyMt(detailDto.getSuppliedQtyMt());
                                existing.setShipQtyMt(detailDto.getShipQtyMt());
                                existing = actBunkerDtlRepository.save(existing);
                                String logDetail = String.format("KeyId = TRANSACTION_POID %s: DET_ROW_ID %s", existing.getTransactionPoid(), existing.getDetRowId());
                                loggingService.createLog(oldDetail, existing, PortCallOperationActBunkerDtl.class, UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                            });
                }
            }
        }

        // Update Husbandry Crew Details (indexed so callers can map file uploads to detRowId, including new rows)
        if (dto.getHusbandryCrewDetails() != null && !dto.getHusbandryCrewDetails().isEmpty()) {
            List<PortCallOperationHusbandryCrewDetailDto> crewList = dto.getHusbandryCrewDetails();
            for (int i = 0; i < crewList.size(); i++) {
                PortCallOperationHusbandryCrewDetailDto detailDto = crewList.get(i);
                ActionType action = detailDto.getActionType();
                if (action == null || action == ActionType.noChange) {
                    if (husbandryCrewDetRowIdByDetailIndexOut != null) {
                        husbandryCrewDetRowIdByDetailIndexOut[i] = detailDto.getDetRowId();
                    }
                    continue;
                }
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
                            .build());
                    if (husbandryCrewDetRowIdByDetailIndexOut != null) {
                        husbandryCrewDetRowIdByDetailIndexOut[i] = saved.getDetRowId();
                    }
                    String logDetail = String.format("Row Created on [Port Call Operation Husbandry Crew Details] with detRowId: %s", saved.getDetRowId());
                    loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                } else if (action == ActionType.isUpdated) {
                    if (husbandryCrewDetRowIdByDetailIndexOut != null) {
                        husbandryCrewDetRowIdByDetailIndexOut[i] = detailDto.getDetRowId();
                    }
                    husbandryCrewDtlRepository.findById(new PortCallOperationHusbandryCrewDtlId(transactionPoid, detailDto.getDetRowId()))
                            .ifPresent(existing -> {
                                PortCallOperationHusbandryCrewDtl oldDetail = new PortCallOperationHusbandryCrewDtl();
                                BeanUtils.copyProperties(existing, oldDetail);
                                existing.setCrewName(detailDto.getCrewName());
                                existing.setCrewGenderPoid(detailDto.getCrewGenderPoid());
                                existing.setCrewNationalityPoid(detailDto.getCrewNationalityPoid());
                                existing.setCrewPptNumber(detailDto.getCrewPptNumber());
                                existing.setCrewSeamanNo(detailDto.getCrewSeamanNo());
                                existing.setCrewRank(detailDto.getCrewRank());
                                existing = husbandryCrewDtlRepository.save(existing);
                                String logDetail = String.format("KeyId = TRANSACTION_POID %s: DET_ROW_ID %s", existing.getTransactionPoid(), existing.getDetRowId());
                                loggingService.createLog(oldDetail, existing, PortCallOperationHusbandryCrewDtl.class, UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                            });
                }
            }
        }

        // Update Husbandry Other Details
        if (dto.getHusbandryOthDetails() != null && !dto.getHusbandryOthDetails().isEmpty()) {
            List<PortCallOperationHusbandryOthDetailDto> othList = dto.getHusbandryOthDetails();
            for (int i = 0; i < othList.size(); i++) {
                PortCallOperationHusbandryOthDetailDto detailDto = othList.get(i);
                if (detailDto.getUnitPoid() != null) {
                    if (!stockUnitMasterRepository.existsByStockUnitPoid(detailDto.getUnitPoid())) {
                        throw new ResourceNotFoundException("Stock Unit Master", "Unit Poid", detailDto.getUnitPoid());
                    }
                }
                ActionType action = detailDto.getActionType();
                if (action == null || action == ActionType.noChange) {
                    if (husbandryOthDetRowIdByDetailIndexOut != null) {
                        husbandryOthDetRowIdByDetailIndexOut[i] = detailDto.getDetRowId();
                    }
                    continue;
                }
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
                            .requestedBy(detailDto.getRequestedBy())
                            .paymentMode(detailDto.getPaymentMode())
                            .build());
                    if (husbandryOthDetRowIdByDetailIndexOut != null) {
                        husbandryOthDetRowIdByDetailIndexOut[i] = saved.getDetRowId();
                    }
                    String logDetail = String.format("Row Created on [Port Call Operation Husbandry Other Details] with detRowId: %s", saved.getDetRowId());
                    loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                } else if (action == ActionType.isUpdated) {
                    if (husbandryOthDetRowIdByDetailIndexOut != null) {
                        husbandryOthDetRowIdByDetailIndexOut[i] = detailDto.getDetRowId();
                    }
                    husbandryOthDtlRepository.findById(new PortCallOperationHusbandryOthDtlId(transactionPoid, detailDto.getDetRowId()))
                            .ifPresent(existing -> {
                                PortCallOperationHusbandryOthDtl oldDetail = new PortCallOperationHusbandryOthDtl();
                                BeanUtils.copyProperties(existing, oldDetail);
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
                                existing.setRequestedBy(detailDto.getRequestedBy());
                                existing.setPaymentMode(detailDto.getPaymentMode());
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
                            .build());
                    String logDetail = String.format("Row Created on [Port Call Operation Docs Copy Details] with detRowId: %s", saved.getDetRowId());
                    loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                } else if (action == ActionType.isUpdated) {
                    docsCopyDtlRepository.findById(new PortCallOperationDocsCopyDtlId(transactionPoid, detailDto.getDetRowId()))
                            .ifPresent(existing -> {
                                PortCallOperationDocsCopyDtl oldDetail = new PortCallOperationDocsCopyDtl();
                                BeanUtils.copyProperties(existing, oldDetail);
                                existing.setDocumentFrom(detailDto.getDocumentFrom());
                                existing.setDocumentList(detailDto.getDocumentList());
                                existing.setDocumentSelect(detailDto.getDocumentSelect());
                                existing = docsCopyDtlRepository.save(existing);
                                String logDetail = String.format("KeyId = TRANSACTION_POID %s: DET_ROW_ID %s", existing.getTransactionPoid(), existing.getDetRowId());
                                loggingService.createLog(oldDetail, existing, PortCallOperationDocsCopyDtl.class, UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
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
                .orElseThrow(() -> new ResourceNotFoundException("EstBertDetail", "Transaction Poid and Det Row Id", String.format("%s, %s", transactionPoid, detRowId)));

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
    public PortCallOperationEstBertDetailResponseDto createEstBertDetail(Long transactionPoid, PortCallOperationEstBertDetailRequestDto dto, MultipartFile[] files, String[] remarks, String[] checklistNames) {
        log.info("Creating EstBertDetail for transactionPoid: {}", transactionPoid);

        if (!hdrRepository.existsById(transactionPoid)) {
            throw new ResourceNotFoundException("Port call operation", "Transaction Poid", transactionPoid);
        }
        if (dto.getEmailPoid() != null && !docsMsgsDtl1Repository.existsByEmailPoid(dto.getEmailPoid())) {
            throw new ResourceNotFoundException("Email", "Email Poid", dto.getEmailPoid());
        }

        Long nextDetRowId = estBertDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
        Long emailPoidToUse;

        if (dto.getEmailPoid() == null) {
            PortCallOperationDocsMsgsDtl1 newMsgsDtl1 = PortCallOperationDocsMsgsDtl1.builder()
                    .transactionPoid(transactionPoid)
                    .detRowId(nextDetRowId)
                    .emailRemarks(dto.getRemarks())
                    .build();
            newMsgsDtl1 = docsMsgsDtl1Repository.save(newMsgsDtl1);
            docsMsgsDtl1Repository.flush();
            // DB trigger OPS_PC_DOCS_MSGS_DTL1_TRG sets EMAIL_POID on INSERT (sequence NEXTVAL), which can
            // differ from Hibernate's @GeneratedValue. Read the actual value from the database (latest row).
            Long actualEmailPoid = jdbcTemplate.queryForObject("SELECT MAX(EMAIL_POID) FROM OPS_PC_DOCS_MSGS_DTL1 WHERE TRANSACTION_POID = ? AND DET_ROW_ID = ?", Long.class, transactionPoid, nextDetRowId);
            if (actualEmailPoid == null) {
                throw new IllegalStateException("Email Poid was not set by trigger after insert");
            }
            emailPoidToUse = actualEmailPoid;
        } else {
            emailPoidToUse = dto.getEmailPoid();
            PortCallOperationDocsMsgsDtl1 msgsDtl1 = docsMsgsDtl1Repository.findByEmailPoid(dto.getEmailPoid())
                    .orElseThrow(() -> new ResourceNotFoundException("Email", "Email Poid", dto.getEmailPoid()));
            msgsDtl1.setEmailRemarks(dto.getRemarks());
            docsMsgsDtl1Repository.save(msgsDtl1);
        }

        if (dto.getSendEmail()) {
            PortCallOperationDocsMsgsDtl1 msgsForSend = docsMsgsDtl1Repository.findByEmailPoid(emailPoidToUse)
                    .orElseThrow(() -> new ResourceNotFoundException("Email", "Email Poid", emailPoidToUse));
            if (msgsForSend.getEmailSendOn() == null) {
                // Logic to send email
            } else {
                throw new CustomException("Email already sent", 400);
            }
        }

        // Insert EstBertDtl with emailPoid=null first to avoid FK constraint violation (OPS_PC_EST_BERT_HDR_FK2
        // may be checked at commit; parent row in same transaction can still cause "parent key not found").
        PortCallOperationEstBertDtl entity = PortCallOperationEstBertDtl.builder()
                .transactionPoid(transactionPoid)
                .detRowId(nextDetRowId)
                .eta(dto.getEta())
                .etb(dto.getEtb())
                .emailPoid(null)
                .build();

        PortCallOperationEstBertDtl saved = estBertDtlRepository.save(entity);
        estBertDtlRepository.flush();

        // Now set emailPoid using native SQL update to ensure Oracle sees the flushed parent row.
        // Hibernate's entity update might check constraints before Oracle sees the parent row.
        jdbcTemplate.update("UPDATE OPS_PC_EST_BERT_DTL SET EMAIL_POID = ? WHERE TRANSACTION_POID = ? AND DET_ROW_ID = ?", emailPoidToUse, transactionPoid, nextDetRowId);
        // Update the entity object to reflect the change
        saved.setEmailPoid(emailPoidToUse);

        // Upload attachments if provided (optional)
        if (files != null && files.length > 0) {
            screenAttachmentService.uploadBerthingAttachments(transactionPoid, nextDetRowId, files, remarks, checklistNames);
            // Reload entity to get updated berthingAttachments
            entity = estBertDtlRepository.findById(new PortCallOperationEstBertDtlId(transactionPoid, nextDetRowId))
                    .orElseThrow(() -> new ResourceNotFoundException("EstBertDetail", "Transaction Poid and Det Row Id", String.format("%s, %s", transactionPoid, nextDetRowId)));
        }

        String logDetail = String.format("Row Created on [Port Call Operation Est Bert Details] with detRowId: %s", saved.getDetRowId());
        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
        PortCallOperationEstBertDetailResponseDto.PortCallOperationEstBertDetailResponseDtoBuilder builder = PortCallOperationEstBertDetailResponseDto.builder()
                .transactionPoid(entity.getTransactionPoid())
                .detRowId(entity.getDetRowId())
                .eta(entity.getEta())
                .etb(entity.getEtb())
                .updatedOn(entity.getLastModifiedDate())
                .updatedBy(entity.getLastModifiedBy())
                .emailPoid(entity.getEmailPoid())
                .berthingAttachments(entity.getBerthingAttachments());

        if (entity.getEmailPoid() != null) {
            docsMsgsDtl1Repository.findByEmailPoid(entity.getEmailPoid())
                    .ifPresent(emailRecord -> {
                        builder.emailSentOn(emailRecord.getEmailSendOn() != null ? emailRecord.getEmailSendOn().atStartOfDay() : null).remarks(emailRecord.getEmailRemarks() != null ? emailRecord.getEmailRemarks() : null);
                    });
        }

        return builder.build();
    }

    @Override
    @Transactional
    public PortCallOperationEstBertDetailResponseDto updateEstBertDetail(Long transactionPoid, Long detRowId, PortCallOperationEstBertDetailRequestDto dto, MultipartFile[] files, String[] remarks, String[] checklistNames) {
        log.info("Updating EstBertDetail for transactionPoid: {}, detRowId: {}", transactionPoid, detRowId);

        PortCallOperationEstBertDtl entity = estBertDtlRepository.findById(new PortCallOperationEstBertDtlId(transactionPoid, detRowId))
                .orElseThrow(() -> new ResourceNotFoundException("EstBertDetail", "Transaction Poid and Det Row Id", String.format("%s, %s", transactionPoid, detRowId)));

        PortCallOperationEstBertDtl oldEntity = new PortCallOperationEstBertDtl();
        BeanUtils.copyProperties(entity, oldEntity);

        if (dto.getEmailPoid() != null && !docsMsgsDtl1Repository.existsByEmailPoid(dto.getEmailPoid())) {
            throw new ResourceNotFoundException("Email", "Email Poid", dto.getEmailPoid());
        }

        Long emailPoidToUse;
        if (dto.getEmailPoid() == null) {
            PortCallOperationDocsMsgsDtl1 newMsgsDtl1 = PortCallOperationDocsMsgsDtl1.builder()
                    .transactionPoid(transactionPoid)
                    .detRowId(detRowId)
                    .emailRemarks(dto.getRemarks())
                    .build();
            newMsgsDtl1 = docsMsgsDtl1Repository.save(newMsgsDtl1);
            docsMsgsDtl1Repository.flush();
            // DB trigger OPS_PC_DOCS_MSGS_DTL1_TRG sets EMAIL_POID on INSERT (sequence NEXTVAL).
            // Read the actual value from the database (latest row).
            Long actualEmailPoid = jdbcTemplate.queryForObject("SELECT MAX(EMAIL_POID) FROM OPS_PC_DOCS_MSGS_DTL1 WHERE TRANSACTION_POID = ? AND DET_ROW_ID = ?", Long.class, transactionPoid, detRowId);
            if (actualEmailPoid == null) {
                throw new IllegalStateException("Email Poid was not set by trigger after insert");
            }
            emailPoidToUse = actualEmailPoid;
        } else {
            emailPoidToUse = dto.getEmailPoid();
            PortCallOperationDocsMsgsDtl1 msgsDtl1 = docsMsgsDtl1Repository.findByEmailPoid(dto.getEmailPoid())
                    .orElseThrow(() -> new ResourceNotFoundException("Email", "Email Poid", dto.getEmailPoid()));
            msgsDtl1.setEmailRemarks(dto.getRemarks());
            docsMsgsDtl1Repository.save(msgsDtl1);
        }

        if (dto.getSendEmail()) {
            PortCallOperationDocsMsgsDtl1 msgsForSend = docsMsgsDtl1Repository.findByEmailPoid(emailPoidToUse)
                    .orElseThrow(() -> new ResourceNotFoundException("Email", "Email Poid", emailPoidToUse));
            if (msgsForSend.getEmailSendOn() == null) {
                // Logic to send email
            } else {
                throw new CustomException("Email already sent", 400);
            }
        }

        entity.setEta(dto.getEta());
        entity.setEtb(dto.getEtb());
        entity.setEmailPoid(emailPoidToUse);

        PortCallOperationEstBertDtl saved = estBertDtlRepository.save(entity);

        // Upload attachments if provided (optional)
        if (files != null && files.length > 0) {
            screenAttachmentService.uploadBerthingAttachments(transactionPoid, detRowId, files, remarks, checklistNames);
            // Reload entity to get updated berthingAttachments
            entity = estBertDtlRepository.findById(new PortCallOperationEstBertDtlId(transactionPoid, detRowId))
                    .orElseThrow(() -> new ResourceNotFoundException("EstBertDetail", "Transaction Poid and Det Row Id", String.format("%s, %s", transactionPoid, detRowId)));
        }

        PortCallOperationEstBertDetailResponseDto.PortCallOperationEstBertDetailResponseDtoBuilder builder = PortCallOperationEstBertDetailResponseDto.builder()
                .transactionPoid(entity.getTransactionPoid())
                .detRowId(entity.getDetRowId())
                .eta(entity.getEta())
                .etb(entity.getEtb())
                .updatedOn(entity.getLastModifiedDate())
                .updatedBy(entity.getLastModifiedBy())
                .emailPoid(entity.getEmailPoid())
                .berthingAttachments(entity.getBerthingAttachments());

        if (entity.getEmailPoid() != null) {
            docsMsgsDtl1Repository.findByEmailPoid(entity.getEmailPoid())
                    .ifPresent(emailRecord -> {
                        builder.emailSentOn(emailRecord.getEmailSendOn() != null ? emailRecord.getEmailSendOn().atStartOfDay() : null).remarks(emailRecord.getEmailRemarks() != null ? emailRecord.getEmailRemarks() : null);
                    });
        }
        String logDetail = String.format("KeyId = TRANSACTION_POID %s: DET_ROW_ID %s", saved.getTransactionPoid(), saved.getDetRowId());
        loggingService.createLog(oldEntity, entity, PortCallOperationEstBertDtl.class, UserContext.getDocumentId(), transactionPoid.toString(), logDetail);

        return builder.build();
    }

    @Override
    public Map<String, Object> listDefaultEstPrearrivalActDetails(Long transactionPoid) {
        log.info("Listing EstPrearrivalActDetails for transactionPoid: {}", transactionPoid);
        Optional<String> portReportPoidOpt = globalParameterRepository.findParameterValueByName("PC_PRE_ARRIVAL_DTL_ACTIVITY_RPT_POID");
        if (portReportPoidOpt.isEmpty()) {
            throw new CustomException("PC_PRE_ARRIVAL_DTL_ACTIVITY_RPT_POID must be configured in global parameters", 400);
        }
        long portReportPoid;
        try {
            portReportPoid = Long.parseLong(portReportPoidOpt.get());
        } catch (Exception e) {
            throw new CustomException("Not a valid Port Call Report Poid: " + portReportPoidOpt.get(), 400);
        }
        return getPortReportActivities(String.valueOf(transactionPoid), portReportPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid());
    }

    @Override
    public List<PortCallOperationEstPrearrivalActDetailResponseDto> listEstPrearrivalActDetails(Long transactionPoid, Long detRowId) {
        log.info("Listing EstPrearrivalActDetails for transactionPoid: {}, detRowId: {}", transactionPoid, detRowId);

        List<PortCallOperationEstPrearrivalActDtl> entities = estPrearrivalActDtlRepository.findByTransactionPoidAndDetRowId(transactionPoid, detRowId);
        Optional<PortCallOperationEstPrearrivalDtl> prearrivalDtlOptional = estPrearrivalDtlRepository.findByTransactionPoidAndDetRowId(transactionPoid, detRowId);

        if (prearrivalDtlOptional.isEmpty()) {
            throw new ResourceNotFoundException("EstPrearrivalDtl", "Transaction Poid and Det Row Id", String.format("%s, %s", transactionPoid, detRowId));
        }

        Optional<String> portReportPoidOpt = globalParameterRepository.findParameterValueByName("PC_PRE_ARRIVAL_DTL_ACTIVITY_RPT_POID");
        if (portReportPoidOpt.isEmpty()) {
            throw new CustomException("PC_PRE_ARRIVAL_DTL_ACTIVITY_RPT_POID must be configured in global parameters", 400);
        }
        long portReportPoid;
        try {
            portReportPoid = Long.parseLong(portReportPoidOpt.get());
        } catch (Exception e) {
            throw new CustomException("Not a valid Port Call Report Poid: " + portReportPoidOpt.get(), 400);
        }

        Map<String, Object> spResult = getPortReportActivities(transactionPoid.toString(), portReportPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid());

        List<Map<String, Object>> outData = (List<Map<String, Object>>) spResult.get("OUTDATA");

        Map<Long, String> activityMandatoryMap = new HashMap<>();
        if (outData != null) {
            for (Map<String, Object> row : outData) {
                Long activityTypePoid = row.get("PORT_ACTIVITY_TYPE_POID") != null ? Long.valueOf(row.get("PORT_ACTIVITY_TYPE_POID").toString()) : null;
                String mandatory = row.get("ACTIVITY_MANDATORY") != null ? row.get("ACTIVITY_MANDATORY").toString() : null;
                if (activityTypePoid != null && mandatory != null) {
                    activityMandatoryMap.put(activityTypePoid, mandatory);
                }
            }
        }

        return entities.stream()
                .map(e -> {
                    String activityMandatory = null;
                    if (e.getActivityPoid() != null) {
                        String value = activityMandatoryMap.get(e.getActivityPoid());
                        if (value != null) {
                            activityMandatory = value.equalsIgnoreCase("Y") ? "Y" : "N";
                        }
                    }
                    return PortCallOperationEstPrearrivalActDetailResponseDto.builder()
                            .transactionPoid(e.getTransactionPoid())
                            .detRowId(e.getDetRowId())
                            .preActivityDtlPoid(e.getPreActivityDtlPoid())
                            .activityPoid(e.getActivityPoid())
                            .activityMandatory(activityMandatory)
                            .activityName(e.getActivityName())
                            .otherDescription(e.getOtherDescription())
                            .estimatedDatetime(e.getEstimatedDatetime())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PortCallOperationEstPrearrivalActDetailResponseDto createEstPrearrivalActDetail(Long transactionPoid, PortCallOperationEstPrearrivalActDetailDto dto, MultipartFile[] files, String[] remarks, String[] checklistNames) {
        // Generate detRowId automatically
        Long detRowId = estPrearrivalDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
        log.info("Creating EstPrearrivalActDetail for transactionPoid: {}, generated detRowId: {}", transactionPoid, detRowId);

        if (dto.getEmailPoid() != null && !docsMsgsDtl1Repository.existsByEmailPoid(dto.getEmailPoid())) {
            throw new ResourceNotFoundException("Email", "Email Poid", dto.getEmailPoid());
        }

        Long emailPoidToUse;
        if (dto.getEmailPoid() == null) {
            PortCallOperationDocsMsgsDtl1 newMsgsDtl1 = PortCallOperationDocsMsgsDtl1.builder()
                    .transactionPoid(transactionPoid)
                    .detRowId(detRowId)
                    .emailRemarks(dto.getRemarks())
                    .build();
            newMsgsDtl1 = docsMsgsDtl1Repository.save(newMsgsDtl1);
            docsMsgsDtl1Repository.flush();
            // DB trigger OPS_PC_DOCS_MSGS_DTL1_TRG sets EMAIL_POID on INSERT (sequence NEXTVAL).
            // Read the actual value from the database (latest row).
            Long actualEmailPoid = jdbcTemplate.queryForObject("SELECT MAX(EMAIL_POID) FROM OPS_PC_DOCS_MSGS_DTL1 WHERE TRANSACTION_POID = ? AND DET_ROW_ID = ?", Long.class, transactionPoid, detRowId);
            if (actualEmailPoid == null) {
                throw new IllegalStateException("Email Poid was not set by trigger after insert");
            }
            emailPoidToUse = actualEmailPoid;
        } else {
            emailPoidToUse = dto.getEmailPoid();
            PortCallOperationDocsMsgsDtl1 msgsDtl1 = docsMsgsDtl1Repository.findByEmailPoid(dto.getEmailPoid())
                    .orElseThrow(() -> new ResourceNotFoundException("Email", "Email Poid", dto.getEmailPoid()));
            msgsDtl1.setEmailRemarks(dto.getRemarks());
            docsMsgsDtl1Repository.save(msgsDtl1);
        }

        if (dto.getSendEmail()) {
            PortCallOperationDocsMsgsDtl1 msgsForSend = docsMsgsDtl1Repository.findByEmailPoid(emailPoidToUse)
                    .orElseThrow(() -> new ResourceNotFoundException("Email", "Email Poid", emailPoidToUse));
            if (msgsForSend.getEmailSendOn() == null) {
                // Logic to send email
            } else {
                throw new CustomException("Email already sent", 400);
            }
        }

        // Validate pre-arrival is enabled: allow create/update only from X days before ETA (configurable via PC_PREARRIVAL_EDIT_ALLOW_DAYS)
        List<PortCallOperationEstBertDtl> estBertRecords = estBertDtlRepository.findByTransactionPoidOrderByLastModifiedDateDesc(transactionPoid);
        if (!estBertRecords.isEmpty()) {
            Optional<String> daysToBeEnabledForEdit = globalParameterRepository.findParameterValueByName("PC_PREARRIVAL_EDIT_ALLOW_DAYS");
            if (daysToBeEnabledForEdit.isEmpty()) {
                throw new CustomException("PC_PREARRIVAL_EDIT_ALLOW_DAYS must be configured in global parameters", 400);
            }
            long daysToBeEnabledForEditLong;
            try {
                daysToBeEnabledForEditLong = Long.parseLong(daysToBeEnabledForEdit.get());
            } catch (Exception e) {
                throw new CustomException("PC_PREARRIVAL_EDIT_ALLOW_DAYS must be a valid number", 400);
            }
            PortCallOperationEstBertDtl latestEstBert = estBertRecords.getFirst();
            LocalDateTime now = LocalDateTime.now();
            if (latestEstBert.getEta() != null) {
                LocalDateTime dayBeforeEta = latestEstBert.getEta().minusDays(daysToBeEnabledForEditLong);
                if (now.isBefore(dayBeforeEta)) {
                    throw new ValidationException(String.format("Pre-arrival operations are allowed only from %s day(s) before ETA. ETA is %s, current time is %s", daysToBeEnabledForEditLong, latestEstBert.getEta(), now));
                }
                if (now.isAfter(latestEstBert.getEta())) {
                    throw new ValidationException(String.format("Pre-arrival operations are not allowed after ETA has passed. ETA is %s, current time is %s", latestEstBert.getEta(), now));
                }
            }
        }

        if (!hdrRepository.existsById(transactionPoid)) {
            throw new ResourceNotFoundException("Port call operation", "Transaction Poid", transactionPoid);
        }
        List<PortCallReportActivityDto> activities = dto.getActivities() != null ? dto.getActivities() : List.of();
        for (PortCallReportActivityDto activity : activities) {
            if (activity.getActivityPoid() != null && !portActivityMasterRepository.existsByPortActivityTypePoid(activity.getActivityPoid())) {
                throw new ResourceNotFoundException("Port activity", "Transaction Poid", activity.getActivityPoid());
            }
        }

        Optional<String> portCallReportPoid = globalParameterRepository.findParameterValueByName("PC_PRE_ARRIVAL_DTL_ACTIVITY_RPT_POID");
        if (portCallReportPoid.isEmpty()) {
            throw new CustomException("Port Call Report Poid is required, Please update PC_PRE_ARRIVAL_DTL_ACTIVITY_RPT_POID in global parameter");
        }
        long portCallReportPoidLong;
        try {
            portCallReportPoidLong = Long.parseLong(portCallReportPoid.get());
        } catch (Exception e) {
            throw new CustomException("Not a valid Port Call Report Poid: " + portCallReportPoid.get(), 400);
        }
        List<PortCallReportDtl> portCallReportActivities = dtlRepository.findByPortCallReportPoid(portCallReportPoidLong);

        // Validate mandatory activities: map by portActivityTypePoid, require otherDescription and estimatedDatetime when activityMandatory=Y
        Map<Long, PortCallReportActivityDto> requestActivityMap = (dto.getActivities() != null ? dto.getActivities() : List.<PortCallReportActivityDto>of()).stream()
                .filter(a -> a.getActivityPoid() != null)
                .collect(Collectors.toMap(PortCallReportActivityDto::getActivityPoid, a -> a, (a, b) -> a));
        for (PortCallReportDtl reportDtl : portCallReportActivities) {
            if ("Y".equalsIgnoreCase(reportDtl.getActivityMandatory())) {
                Long activityPoid = reportDtl.getPortActivityTypePoid();
                PortCallReportActivityDto activityDto = requestActivityMap.get(activityPoid);
                if (activityDto == null) {
                    throw new ValidationException("Mandatory activity (activityPoid: " + activityPoid + ") is required");
                }
                if (activityDto.getOtherDescription() == null || activityDto.getOtherDescription().isBlank()) {
                    throw new ValidationException("otherDescription is required for mandatory activity (activityPoid: " + activityPoid + ")");
                }
                if (activityDto.getEstimatedDatetime() == null) {
                    throw new ValidationException("estimatedDatetime is required for mandatory activity (activityPoid: " + activityPoid + ")");
                }
            }
        }

        long nextPreActivityDtlPoid = estPrearrivalActDtlRepository.findMaxPreActivityDtlPoidByTransactionPoidAndDetRowId(transactionPoid, detRowId) + 1;

        List<PortCallOperationEstPrearrivalActDtl> entitiesToSave = new ArrayList<>();
        for (PortCallReportActivityDto activity : activities) {
            if (activity.getActivityPoid() == null && StringUtils.isBlank(activity.getActivityName())) {
                throw new ValidationException("activityName is required when activityPoid is not provided");
            }
            entitiesToSave.add(PortCallOperationEstPrearrivalActDtl.builder()
                    .transactionPoid(transactionPoid)
                    .detRowId(detRowId)
                    .preActivityDtlPoid(nextPreActivityDtlPoid++)
                    .activityPoid(activity.getActivityPoid())
                    .activityName(activity.getActivityName())
                    .otherDescription(activity.getOtherDescription())
                    .estimatedDatetime(activity.getEstimatedDatetime())
                    .build());
        }

        PortCallOperationEstPrearrivalDtl newPrearrivalDtl = new PortCallOperationEstPrearrivalDtl();
        newPrearrivalDtl.setEmailPoid(emailPoidToUse);
        newPrearrivalDtl.setTransactionPoid(transactionPoid);
        newPrearrivalDtl.setDetRowId(detRowId);

        PortCallOperationEstPrearrivalActDetailResponseDto response;
        if (entitiesToSave.isEmpty()) {
            // No activities: do not save PortCallOperationEstPrearrivalActDtl; only create header record
            newPrearrivalDtl.setPreActivityDtlPoid(null);
            estPrearrivalDtlRepository.save(newPrearrivalDtl);
            String prearrivalDtlLogDetail = String.format("Row Created on [Port Call Operation Est Prearrival Details] with detRowId: %s", detRowId);
            loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), prearrivalDtlLogDetail);
            response = PortCallOperationEstPrearrivalActDetailResponseDto.builder()
                    .transactionPoid(transactionPoid)
                    .detRowId(detRowId)
                    .build();
        } else {
            List<PortCallOperationEstPrearrivalActDtl> saved = estPrearrivalActDtlRepository.saveAll(entitiesToSave);
            PortCallOperationEstPrearrivalActDtl entity = saved.getLast();

            String prearrivalActDtlLogDetail = String.format("Row Created on [Port Call Operation Est Prearrival Act Details] with preActivityDtlPoid: %s", entity.getPreActivityDtlPoid());
            loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), prearrivalActDtlLogDetail);

            newPrearrivalDtl.setPreActivityDtlPoid(entity.getPreActivityDtlPoid());

            estPrearrivalDtlRepository.save(newPrearrivalDtl);

            String prearrivalDtlLogDetail = String.format("Row Created on [Port Call Operation Est Prearrival Details] with preActivityDtlPoid: %s", entity.getPreActivityDtlPoid());
            loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), prearrivalDtlLogDetail);

            response = PortCallOperationEstPrearrivalActDetailResponseDto.builder()
                    .transactionPoid(entity.getTransactionPoid())
                    .detRowId(entity.getDetRowId())
                    .preActivityDtlPoid(entity.getPreActivityDtlPoid())
                    .activityPoid(entity.getActivityPoid())
                    .activityName(entity.getActivityName())
                    .otherDescription(entity.getOtherDescription())
                    .estimatedDatetime(entity.getEstimatedDatetime())
                    .build();
        }

        // Upload attachments if provided (optional) - same for both empty and non-empty
        if (files != null && files.length > 0) {
            screenAttachmentService.uploadPreArrivalAttachments(transactionPoid, detRowId, files, remarks, checklistNames);
            estPrearrivalDtlRepository.findById(new PortCallOperationEstPrearrivalDtlId(transactionPoid, detRowId))
                    .orElseThrow(() -> new ResourceNotFoundException("EstPrearrivalDetail", "Transaction Poid and Det Row Id", String.format("%s, %s", transactionPoid, detRowId)));
        }

        return response;
    }

    @Override
    @Transactional
    public PortCallOperationEstPrearrivalActDetailResponseDto updateEstPrearrivalActDetail(Long transactionPoid, Long detRowId, PortCallOperationEstPrearrivalActDetailDto dto, MultipartFile[] files, String[] remarks, String[] checklistNames) {
        log.info("Updating EstPrearrivalActDetail for transactionPoid: {}, detRowId: {}", transactionPoid, detRowId);

        // Parent row is PortCallOperationEstPrearrivalDtl (edit opens from its table); ActDtl children may not exist yet.
        if (!estPrearrivalDtlRepository.existsById(new PortCallOperationEstPrearrivalDtlId(transactionPoid, detRowId))) {
            throw new ResourceNotFoundException("EstPrearrivalDtl", "Transaction Poid and Det Row Id", String.format("%s, %s", transactionPoid, detRowId));
        }

        if (!hdrRepository.existsById(transactionPoid)) {
            throw new ResourceNotFoundException("Port call operation", "Transaction Poid", transactionPoid);
        }

        // Existing activity rows for this prearrival detail (may be empty when first saving activities from the popup)
        List<PortCallOperationEstPrearrivalActDtl> existingEntities = estPrearrivalActDtlRepository.findByTransactionPoidAndDetRowId(transactionPoid, detRowId);

        PortCallOperationEstPrearrivalActDtl oldEntity = new PortCallOperationEstPrearrivalActDtl();
        if (!existingEntities.isEmpty()) {
            BeanUtils.copyProperties(existingEntities.getFirst(), oldEntity);
        }

        if (dto.getEmailPoid() != null && !docsMsgsDtl1Repository.existsByEmailPoid(dto.getEmailPoid())) {
            throw new ResourceNotFoundException("Email", "Email Poid", dto.getEmailPoid());
        }

        Long emailPoidToUse;
        if (dto.getEmailPoid() == null) {
            PortCallOperationDocsMsgsDtl1 newMsgsDtl1 = PortCallOperationDocsMsgsDtl1.builder()
                    .transactionPoid(transactionPoid)
                    .detRowId(detRowId)
                    .emailRemarks(dto.getRemarks())
                    .build();
            newMsgsDtl1 = docsMsgsDtl1Repository.save(newMsgsDtl1);
            docsMsgsDtl1Repository.flush();
            // DB trigger OPS_PC_DOCS_MSGS_DTL1_TRG sets EMAIL_POID on INSERT (sequence NEXTVAL).
            // Read the actual value from the database (latest row).
            Long actualEmailPoid = jdbcTemplate.queryForObject("SELECT MAX(EMAIL_POID) FROM OPS_PC_DOCS_MSGS_DTL1 WHERE TRANSACTION_POID = ? AND DET_ROW_ID = ?", Long.class, transactionPoid, detRowId);
            if (actualEmailPoid == null) {
                throw new IllegalStateException("Email Poid was not set by trigger after insert");
            }
            emailPoidToUse = actualEmailPoid;
        } else {
            emailPoidToUse = dto.getEmailPoid();
            PortCallOperationDocsMsgsDtl1 msgsDtl1 = docsMsgsDtl1Repository.findByEmailPoid(dto.getEmailPoid())
                    .orElseThrow(() -> new ResourceNotFoundException("Email", "Email Poid", dto.getEmailPoid()));
            msgsDtl1.setEmailRemarks(dto.getRemarks());
            docsMsgsDtl1Repository.save(msgsDtl1);
        }

        if (dto.getSendEmail()) {
            PortCallOperationDocsMsgsDtl1 msgsForSend = docsMsgsDtl1Repository.findByEmailPoid(emailPoidToUse)
                    .orElseThrow(() -> new ResourceNotFoundException("Email", "Email Poid", emailPoidToUse));
            if (msgsForSend.getEmailSendOn() == null) {
                // Logic to send email
            } else {
                throw new CustomException("Email already sent", 400);
            }
        }

        // Validate pre-arrival is enabled: allow create/update only from X days before ETA (configurable via PC_PREARRIVAL_EDIT_ALLOW_DAYS)
        List<PortCallOperationEstBertDtl> estBertRecords = estBertDtlRepository.findByTransactionPoidOrderByLastModifiedDateDesc(transactionPoid);
        if (!estBertRecords.isEmpty()) {
            Optional<String> daysToBeEnabledForEdit = globalParameterRepository.findParameterValueByName("PC_PREARRIVAL_EDIT_ALLOW_DAYS");
            if (daysToBeEnabledForEdit.isEmpty()) {
                throw new CustomException("PC_PREARRIVAL_EDIT_ALLOW_DAYS must be configured in global parameters", 400);
            }
            long daysToBeEnabledForEditLong;
            try {
                daysToBeEnabledForEditLong = Long.parseLong(daysToBeEnabledForEdit.get());
            } catch (Exception e) {
                throw new CustomException("PC_PREARRIVAL_EDIT_ALLOW_DAYS must be a valid number", 400);
            }
            PortCallOperationEstBertDtl latestEstBert = estBertRecords.getFirst();
            LocalDateTime now = LocalDateTime.now();
            if (latestEstBert.getEta() != null) {
                LocalDateTime dayBeforeEta = latestEstBert.getEta().minusDays(daysToBeEnabledForEditLong);
                if (now.isBefore(dayBeforeEta)) {
                    throw new ValidationException(String.format("Pre-arrival operations are allowed only from %s day(s) before ETA. ETA is %s, current time is %s", daysToBeEnabledForEditLong, latestEstBert.getEta(), now));
                }
                if (now.isAfter(latestEstBert.getEta())) {
                    throw new ValidationException(String.format("Pre-arrival operations are not allowed after ETA has passed. ETA is %s, current time is %s", latestEstBert.getEta(), now));
                }
            }
        }

        // Validate mandatory activities (same as create)
        Optional<String> portCallReportPoid = globalParameterRepository.findParameterValueByName("PC_PRE_ARRIVAL_DTL_ACTIVITY_RPT_POID");
        if (portCallReportPoid.isPresent()) {
            long portCallReportPoidLong;
            try {
                portCallReportPoidLong = Long.parseLong(portCallReportPoid.get());
            } catch (Exception e) {
                throw new CustomException("Not a valid Port Call Report Poid: " + portCallReportPoid.get(), 400);
            }
            List<PortCallReportDtl> portCallReportActivitiesUpdate = dtlRepository.findByPortCallReportPoid(portCallReportPoidLong);

            Map<Long, PortCallReportActivityDto> requestActivityMapUpdate = (dto.getActivities() != null ? dto.getActivities() : List.<PortCallReportActivityDto>of()).stream()
                    .filter(a -> a.getActivityPoid() != null)
                    .collect(Collectors.toMap(PortCallReportActivityDto::getActivityPoid, a -> a, (a, b) -> a));
            for (PortCallReportDtl reportDtl : portCallReportActivitiesUpdate) {
                if ("Y".equalsIgnoreCase(reportDtl.getActivityMandatory())) {
                    Long activityPoid = reportDtl.getPortActivityTypePoid();
                    PortCallReportActivityDto activityDto = requestActivityMapUpdate.get(activityPoid);
                    if (activityDto == null) {
                        throw new ValidationException("Mandatory activity (activityPoid: " + activityPoid + ") is required");
                    }
                    if (activityDto.getOtherDescription() == null || activityDto.getOtherDescription().isBlank()) {
                        throw new ValidationException("otherDescription is required for mandatory activity (activityPoid: " + activityPoid + ")");
                    }
                    if (activityDto.getEstimatedDatetime() == null) {
                        throw new ValidationException("estimatedDatetime is required for mandatory activity (activityPoid: " + activityPoid + ")");
                    }
                }
            }
        }

        List<PortCallReportActivityDto> activities = dto.getActivities() != null ? dto.getActivities() : List.of();
        for (PortCallReportActivityDto activity : activities) {
            if (activity.getActivityPoid() != null && !portActivityMasterRepository.existsByPortActivityTypePoid(activity.getActivityPoid())) {
                throw new ResourceNotFoundException("Port activity", "Transaction Poid", activity.getActivityPoid());
            }
        }

        // Map existing activities by preActivityDtlPoid (PK) for matching
        Map<Long, PortCallOperationEstPrearrivalActDtl> existingActivitiesMap = existingEntities.stream()
                .collect(Collectors.toMap(PortCallOperationEstPrearrivalActDtl::getPreActivityDtlPoid, e -> e));

        // Track which preActivityDtlPoids from DTO we've processed
        Set<Long> processedPreActivityDtlPoids = new HashSet<>();
        List<PortCallOperationEstPrearrivalActDtl> entitiesToUpdate = new ArrayList<>();
        List<PortCallOperationEstPrearrivalActDtl> entitiesToCreate = new ArrayList<>();
        List<PortCallOperationEstPrearrivalActDtl> entitiesToDelete = new ArrayList<>();

        // Calculate next preActivityDtlPoid for new activities
        long nextPreActivityDtlPoid = estPrearrivalActDtlRepository.findMaxPreActivityDtlPoidByTransactionPoidAndDetRowId(transactionPoid, detRowId) + 1;

        // Process activities from DTO: update existing or mark for creation
        for (PortCallReportActivityDto activity : activities) {
            if (activity.getActivityPoid() == null && StringUtils.isBlank(activity.getActivityName())) {
                throw new ValidationException("activityName is required when activityPoid is not provided");
            }

            // If preActivityDtlPoid is present it's an existing row, otherwise create
            PortCallOperationEstPrearrivalActDtl existingActivity = activity.getPreActivityDtlPoid() != null
                    ? existingActivitiesMap.get(activity.getPreActivityDtlPoid())
                    : null;

            if (existingActivity != null) {
                processedPreActivityDtlPoids.add(existingActivity.getPreActivityDtlPoid());

                PortCallOperationEstPrearrivalActDtl oldActivity = new PortCallOperationEstPrearrivalActDtl();
                BeanUtils.copyProperties(existingActivity, oldActivity);

                existingActivity.setActivityPoid(activity.getActivityPoid());
                existingActivity.setActivityName(activity.getActivityName());
                existingActivity.setOtherDescription(activity.getOtherDescription());
                existingActivity.setEstimatedDatetime(activity.getEstimatedDatetime());

                entitiesToUpdate.add(existingActivity);
                loggingService.createLog(oldActivity, existingActivity, PortCallOperationEstPrearrivalActDtl.class, UserContext.getDocumentId(), transactionPoid.toString(),
                        String.format("Activity updated: preActivityDtlPoid=%s", existingActivity.getPreActivityDtlPoid()));
            } else {
                entitiesToCreate.add(PortCallOperationEstPrearrivalActDtl.builder()
                        .transactionPoid(transactionPoid)
                        .detRowId(detRowId)
                        .preActivityDtlPoid(nextPreActivityDtlPoid++)
                        .activityPoid(activity.getActivityPoid())
                        .activityName(activity.getActivityName())
                        .otherDescription(activity.getOtherDescription())
                        .estimatedDatetime(activity.getEstimatedDatetime())
                        .build());
            }
        }

        // Mark activities for deletion that are no longer in the DTO
        for (PortCallOperationEstPrearrivalActDtl existingActivity : existingEntities) {
            if (!processedPreActivityDtlPoids.contains(existingActivity.getPreActivityDtlPoid())) {
                entitiesToDelete.add(existingActivity);
            }
        }

        if (entitiesToUpdate.isEmpty() && entitiesToCreate.isEmpty()) {
            throw new ValidationException("At least one activity must be provided");
        }

        // Save updates and creates
        if (!entitiesToUpdate.isEmpty()) {
            estPrearrivalActDtlRepository.saveAll(entitiesToUpdate);
        }
        if (!entitiesToCreate.isEmpty()) {
            estPrearrivalActDtlRepository.saveAll(entitiesToCreate);
        }
        // Delete activities that are no longer in the DTO
        if (!entitiesToDelete.isEmpty()) {
            estPrearrivalActDtlRepository.deleteAll(entitiesToDelete);
        }

        // Get the last saved entity for response (prefer updated, then created)
        PortCallOperationEstPrearrivalActDtl entity = !entitiesToUpdate.isEmpty()
                ? entitiesToUpdate.getLast()
                : entitiesToCreate.getLast();

        // Calculate the maximum preActivityDtlPoid from all saved activities (updated + created)
        long maxPreActivityDtlPoid = estPrearrivalActDtlRepository.findMaxPreActivityDtlPoidByTransactionPoidAndDetRowId(transactionPoid, detRowId);

        // Update PortCallOperationEstPrearrivalDtl record
        PortCallOperationEstPrearrivalDtl existingPrearrivalDtl = estPrearrivalDtlRepository.findById(new PortCallOperationEstPrearrivalDtlId(transactionPoid, detRowId))
                .orElseThrow(() -> new ResourceNotFoundException("EstPrearrivalDtl", "Transaction Poid and Det Row Id", String.format("%s, %s", transactionPoid, detRowId)));

        PortCallOperationEstPrearrivalDtl oldPrearrivalDtl = new PortCallOperationEstPrearrivalDtl();
        BeanUtils.copyProperties(existingPrearrivalDtl, oldPrearrivalDtl);

        existingPrearrivalDtl.setEmailPoid(emailPoidToUse);
        existingPrearrivalDtl.setPreActivityDtlPoid(maxPreActivityDtlPoid);

        PortCallOperationEstPrearrivalDtl savedPrearrivalDtl = estPrearrivalDtlRepository.save(existingPrearrivalDtl);

        // Upload attachments if provided (optional)
        if (files != null && files.length > 0) {
            screenAttachmentService.uploadPreArrivalAttachments(transactionPoid, detRowId, files, remarks, checklistNames);
            // Reload entity to get updated preArrivalAttachments
            savedPrearrivalDtl = estPrearrivalDtlRepository.findById(new PortCallOperationEstPrearrivalDtlId(transactionPoid, detRowId))
                    .orElseThrow(() -> new ResourceNotFoundException("EstPrearrivalDetail", "Transaction Poid and Det Row Id", String.format("%s, %s", transactionPoid, detRowId)));
        }

        String prearrivalDtlLogDetail = String.format("KeyId = TRANSACTION_POID %s: DET_ROW_ID %s", savedPrearrivalDtl.getTransactionPoid(), savedPrearrivalDtl.getDetRowId());
        loggingService.createLog(oldPrearrivalDtl, savedPrearrivalDtl, PortCallOperationEstPrearrivalDtl.class, UserContext.getDocumentId(), transactionPoid.toString(), prearrivalDtlLogDetail);

        return PortCallOperationEstPrearrivalActDetailResponseDto.builder()
                .transactionPoid(entity.getTransactionPoid())
                .detRowId(entity.getDetRowId())
                .preActivityDtlPoid(entity.getPreActivityDtlPoid())
                .activityPoid(entity.getActivityPoid())
                .activityName(entity.getActivityName())
                .otherDescription(entity.getOtherDescription())
                .estimatedDatetime(entity.getEstimatedDatetime())
                .build();
    }

    @Override
    public List<PortCallOperationActTimingsActvtyDetailResponseDto> listActTimingsActvtyDetails(Long transactionPoid, Long detRowId) {
        log.info("Listing ActTimingsActvtyDetails for transactionPoid: {}, detRowId: {}", transactionPoid, detRowId);

        List<PortCallOperationActTimingsActvtyDtl> entities = actTimingsActvtyDtlRepository.findByTransactionPoidAndDetRowId(transactionPoid, detRowId);
        Optional<PortCallOperationActTimingDtl> actualTimingOptional = actTimingDtlRepository.findByTransactionPoidAndDetRowId(transactionPoid, detRowId);

        if (actualTimingOptional.isEmpty()) {
            throw new ResourceNotFoundException("ActTimingDtl", "Transaction Poid and Det Row Id", String.format("%s, %s", transactionPoid, detRowId));
        }
        PortCallOperationActTimingDtl actualTiming = actualTimingOptional.get();

        Map<String, Object> spResult = getPortReportActivities(transactionPoid.toString(), actualTiming.getPortReportPoid(), UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid());

        List<Map<String, Object>> outData = (List<Map<String, Object>>) spResult.get("OUTDATA");

        Map<Long, String> activityMandatoryMap = new HashMap<>();

        if (outData != null) {
            for (Map<String, Object> row : outData) {
                Long activityTypePoid = row.get("PORT_ACTIVITY_TYPE_POID") != null ? Long.valueOf(row.get("PORT_ACTIVITY_TYPE_POID").toString()) : null;

                String mandatory = row.get("ACTIVITY_MANDATORY") != null ? row.get("ACTIVITY_MANDATORY").toString() : null;

                if (activityTypePoid != null && mandatory != null) {
                    activityMandatoryMap.put(activityTypePoid, mandatory);
                }
            }
        }

        return entities.stream()
                .map(e -> {
                    String activityMandatory = null;
                    if (e.getActivityPoid() != null) {
                        String value = activityMandatoryMap.get(e.getActivityPoid());

                        if (value != null) {
                            activityMandatory = value.equalsIgnoreCase("Y") ? "Y" : "N";
                        }
                    }
                    return PortCallOperationActTimingsActvtyDetailResponseDto.builder()
                            .transactionPoid(e.getTransactionPoid())
                            .detRowId(e.getDetRowId())
                            .actualsTimingDtlPoid(e.getActualsTimingDtlPoid())
                            .activityPoid(e.getActivityPoid())
                            .activityMandatory(activityMandatory)
                            .activityName(e.getActivityName())
                            .details(e.getDetails())
                            .estimatedDatetime(e.getEstimatedDatetime())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PortCallOperationActTimingsActvtyDetailResponseDto createActTimingsActvtyDetail(Long transactionPoid, PortCallOperationActTimingsActivityDetailDto dto, MultipartFile[] files, String[] remarks, String[] checklistNames) {
        // Generate detRowId automatically
        Long detRowId = actTimingDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
        log.info("Creating ActTimingsActvtyDetail for transactionPoid: {}, generated detRowId: {}", transactionPoid, detRowId);

        if (dto.getEmailPoid() != null && !docsMsgsDtl1Repository.existsByEmailPoid(dto.getEmailPoid())) {
            throw new ResourceNotFoundException("Email", "Email Poid", dto.getEmailPoid());
        }

        if (!hdrRepository.existsById(transactionPoid)) {
            throw new ResourceNotFoundException("Port call operation", "Transaction Poid", transactionPoid);
        }

        List<PortCallReportActivityDto> activities = dto.getActivities() != null ? dto.getActivities() : List.of();
        for (PortCallReportActivityDto activity : activities) {
            if (activity.getActivityPoid() != null && !portActivityMasterRepository.existsByPortActivityTypePoid(activity.getActivityPoid())) {
                throw new ResourceNotFoundException("Port activity", "Transaction Poid", activity.getActivityPoid());
            }
        }

        List<PortCallReportDtl> portCallReportActivities = dtlRepository.findByPortCallReportPoid(dto.getPortCallReportPoid());

        // Validate mandatory activities: map by portActivityTypePoid, require otherDescription and estimatedDatetime when activityMandatory=Y
        Map<Long, PortCallReportActivityDto> requestActivityMap = activities.stream()
                .filter(a -> a.getActivityPoid() != null)
                .collect(Collectors.toMap(PortCallReportActivityDto::getActivityPoid, a -> a, (a, b) -> a));
        for (PortCallReportDtl reportDtl : portCallReportActivities) {
            if ("Y".equalsIgnoreCase(reportDtl.getActivityMandatory())) {
                Long activityPoid = reportDtl.getPortActivityTypePoid();
                PortCallReportActivityDto activityDto = requestActivityMap.get(activityPoid);
                if (activityDto == null) {
                    throw new ValidationException("Mandatory activity (activityPoid: " + activityPoid + ") is required");
                }
                if (activityDto.getOtherDescription() == null || activityDto.getOtherDescription().isBlank()) {
                    throw new ValidationException("otherDescription is required for mandatory activity (activityPoid: " + activityPoid + ")");
                }
                if (activityDto.getEstimatedDatetime() == null) {
                    throw new ValidationException("estimatedDatetime is required for mandatory activity (activityPoid: " + activityPoid + ")");
                }
            }
        }

        Long emailPoidToUse;
        if (dto.getEmailPoid() == null) {
            PortCallOperationDocsMsgsDtl1 newMsgsDtl1 = PortCallOperationDocsMsgsDtl1.builder()
                    .transactionPoid(transactionPoid)
                    .detRowId(detRowId)
                    .emailRemarks(dto.getRemarks())
                    .build();
            newMsgsDtl1 = docsMsgsDtl1Repository.save(newMsgsDtl1);
            docsMsgsDtl1Repository.flush();
            // DB trigger OPS_PC_DOCS_MSGS_DTL1_TRG sets EMAIL_POID on INSERT (sequence NEXTVAL).
            // Read the actual value from the database (latest row).
            Long actualEmailPoid = jdbcTemplate.queryForObject("SELECT MAX(EMAIL_POID) FROM OPS_PC_DOCS_MSGS_DTL1 WHERE TRANSACTION_POID = ? AND DET_ROW_ID = ?", Long.class, transactionPoid, detRowId);
            if (actualEmailPoid == null) {
                throw new IllegalStateException("Email Poid was not set by trigger after insert");
            }
            emailPoidToUse = actualEmailPoid;
        } else {
            emailPoidToUse = dto.getEmailPoid();
            PortCallOperationDocsMsgsDtl1 msgsDtl1 = docsMsgsDtl1Repository.findByEmailPoid(dto.getEmailPoid())
                    .orElseThrow(() -> new ResourceNotFoundException("Email", "Email Poid", dto.getEmailPoid()));
            msgsDtl1.setEmailRemarks(dto.getRemarks());
            docsMsgsDtl1Repository.save(msgsDtl1);
        }

        if (dto.getSendEmail()) {
            PortCallOperationDocsMsgsDtl1 msgsForSend = docsMsgsDtl1Repository.findByEmailPoid(emailPoidToUse)
                    .orElseThrow(() -> new ResourceNotFoundException("Email", "Email Poid", emailPoidToUse));
            if (msgsForSend.getEmailSendOn() == null) {
                // Logic to send email
            } else {
                throw new CustomException("Email already sent", 400);
            }
        }

        long nextActualsTimingDtlPoid = actTimingsActvtyDtlRepository.findMaxActualsTimingDtlPoidByTransactionPoidAndDetRowId(transactionPoid, detRowId) + 1;

        List<PortCallOperationActTimingsActvtyDtl> entitiesToSave = new ArrayList<>();
        for (PortCallReportActivityDto activity : activities) {
            if (activity.getActivityPoid() == null && StringUtils.isBlank(activity.getActivityName())) {
                throw new ValidationException("activityName is required when activityPoid is not provided");
            }
            entitiesToSave.add(PortCallOperationActTimingsActvtyDtl.builder()
                    .transactionPoid(transactionPoid)
                    .detRowId(detRowId)
                    .actualsTimingDtlPoid(nextActualsTimingDtlPoid++)
                    .activityPoid(activity.getActivityPoid())
                    .activityName(activity.getActivityName())
                    .details(activity.getOtherDescription())
                    .estimatedDatetime(activity.getEstimatedDatetime())
                    .build());
        }

        PortCallOperationActTimingDtl newActTimingDtl = new PortCallOperationActTimingDtl();
        newActTimingDtl.setEmailPoid(emailPoidToUse);
        newActTimingDtl.setTransactionPoid(transactionPoid);
        newActTimingDtl.setDetRowId(detRowId);
        newActTimingDtl.setPortReportPoid(dto.getPortCallReportPoid());

        PortCallOperationActTimingsActvtyDetailResponseDto response;
        if (entitiesToSave.isEmpty()) {
            // No activities: do not save PortCallOperationActTimingsActvtyDtl; only create header record
            newActTimingDtl.setActualsTimingDtlPoid(nextActualsTimingDtlPoid);
            actTimingDtlRepository.save(newActTimingDtl);
            String actTimingsDtlLogDetail = String.format("Row Created on [Port Call Operation Act Timings Details] with detRowId: %s", detRowId);
            loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), actTimingsDtlLogDetail);
            response = PortCallOperationActTimingsActvtyDetailResponseDto.builder()
                    .transactionPoid(transactionPoid)
                    .detRowId(detRowId)
                    .build();
        } else {
            List<PortCallOperationActTimingsActvtyDtl> saved = actTimingsActvtyDtlRepository.saveAll(entitiesToSave);
            PortCallOperationActTimingsActvtyDtl entity = saved.getLast();

            String actTimingsActvtyDtlLogDetail = String.format("Row Created on [Port Call Operation Act Timings Actvty Details] with actualsTimingDtlPoid: %s", entity.getActualsTimingDtlPoid());
            loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), actTimingsActvtyDtlLogDetail);

            newActTimingDtl.setActualsTimingDtlPoid(entity.getActualsTimingDtlPoid());

            actTimingDtlRepository.save(newActTimingDtl);

            String actTimingsDtlLogDetail = String.format("Row Created on [Port Call Operation Act Timings Details] with actualsTimingDtlPoid: %s", entity.getActualsTimingDtlPoid());
            loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), actTimingsDtlLogDetail);

            response = PortCallOperationActTimingsActvtyDetailResponseDto.builder()
                    .transactionPoid(entity.getTransactionPoid())
                    .detRowId(entity.getDetRowId())
                    .actualsTimingDtlPoid(entity.getActualsTimingDtlPoid())
                    .activityPoid(entity.getActivityPoid())
                    .activityName(entity.getActivityName())
                    .details(entity.getDetails())
                    .estimatedDatetime(entity.getEstimatedDatetime())
                    .build();
        }

        // Upload attachments if provided (optional) - same for both empty and non-empty
        if (files != null && files.length > 0) {
            screenAttachmentService.uploadTimingAttachments(transactionPoid, detRowId, files, remarks, checklistNames);
        }

        return response;
    }

    @Override
    @Transactional
    public PortCallOperationActTimingsActvtyDetailResponseDto updateActTimingsActvtyDetail(Long transactionPoid, Long detRowId, PortCallOperationActTimingsActivityDetailDto dto, MultipartFile[] files, String[] remarks, String[] checklistNames) {
        log.info("Updating ActTimingsActvtyDetail for transactionPoid: {}, detRowId: {}", transactionPoid, detRowId);

        if (!hdrRepository.existsById(transactionPoid)) {
            throw new ResourceNotFoundException("Port call operation", "Transaction Poid", transactionPoid);
        }

        // Parent row is PortCallOperationActTimingDtl (edit opens from its table); ActvtyDtl children may not exist yet.
        if (dto.getPortCallReportPoid() == null) {
            throw new ValidationException("portCallReportPoid is required to update act timing activities");
        }
        if (!actTimingDtlRepository.existsById(new PortCallOperationActTimingDtlId(transactionPoid, detRowId))) {
            throw new ResourceNotFoundException("ActTimingDtl", "Transaction Poid and Det Row Id", String.format("%s, %s", transactionPoid, detRowId));
        }

        // Existing activity rows for this act-timing detail (may be empty when first saving activities from the popup)
        List<PortCallOperationActTimingsActvtyDtl> existingEntities = actTimingsActvtyDtlRepository.findByTransactionPoidAndDetRowId(transactionPoid, detRowId);

        if (dto.getEmailPoid() != null && !docsMsgsDtl1Repository.existsByEmailPoid(dto.getEmailPoid())) {
            throw new ResourceNotFoundException("Email", "Email Poid", dto.getEmailPoid());
        }

        // Validate mandatory activities (same as create)
        List<PortCallReportDtl> portCallReportActivitiesUpdate = dtlRepository.findByPortCallReportPoid(dto.getPortCallReportPoid());
        Map<Long, PortCallReportActivityDto> requestActivityMapUpdate = (dto.getActivities() != null ? dto.getActivities() : List.<PortCallReportActivityDto>of()).stream()
                .filter(a -> a.getActivityPoid() != null)
                .collect(Collectors.toMap(PortCallReportActivityDto::getActivityPoid, a -> a, (a, b) -> a));
        for (PortCallReportDtl reportDtl : portCallReportActivitiesUpdate) {
            if ("Y".equalsIgnoreCase(reportDtl.getActivityMandatory())) {
                Long activityPoid = reportDtl.getPortActivityTypePoid();
                PortCallReportActivityDto activityDto = requestActivityMapUpdate.get(activityPoid);
                if (activityDto == null) {
                    throw new ValidationException("Mandatory activity (activityPoid: " + activityPoid + ") is required");
                }
                if (activityDto.getOtherDescription() == null || activityDto.getOtherDescription().isBlank()) {
                    throw new ValidationException("otherDescription is required for mandatory activity (activityPoid: " + activityPoid + ")");
                }
                if (activityDto.getEstimatedDatetime() == null) {
                    throw new ValidationException("estimatedDatetime is required for mandatory activity (activityPoid: " + activityPoid + ")");
                }
            }
        }

        List<PortCallReportActivityDto> activities = dto.getActivities() != null ? dto.getActivities() : List.of();
        for (PortCallReportActivityDto activity : activities) {
            if (activity.getActivityPoid() != null && !portActivityMasterRepository.existsByPortActivityTypePoid(activity.getActivityPoid())) {
                throw new ResourceNotFoundException("Port activity", "Transaction Poid", activity.getActivityPoid());
            }
        }

        Long emailPoidToUse;
        if (dto.getEmailPoid() == null) {
            PortCallOperationDocsMsgsDtl1 newMsgsDtl1 = PortCallOperationDocsMsgsDtl1.builder()
                    .transactionPoid(transactionPoid)
                    .detRowId(detRowId)
                    .emailRemarks(dto.getRemarks())
                    .build();
            newMsgsDtl1 = docsMsgsDtl1Repository.save(newMsgsDtl1);
            docsMsgsDtl1Repository.flush();
            // DB trigger OPS_PC_DOCS_MSGS_DTL1_TRG sets EMAIL_POID on INSERT (sequence NEXTVAL).
            // Read the actual value from the database (latest row).
            Long actualEmailPoid = jdbcTemplate.queryForObject("SELECT MAX(EMAIL_POID) FROM OPS_PC_DOCS_MSGS_DTL1 WHERE TRANSACTION_POID = ? AND DET_ROW_ID = ?", Long.class, transactionPoid, detRowId);
            if (actualEmailPoid == null) {
                throw new IllegalStateException("Email Poid was not set by trigger after insert");
            }
            emailPoidToUse = actualEmailPoid;
        } else {
            emailPoidToUse = dto.getEmailPoid();
            PortCallOperationDocsMsgsDtl1 msgsDtl1 = docsMsgsDtl1Repository.findByEmailPoid(dto.getEmailPoid())
                    .orElseThrow(() -> new ResourceNotFoundException("Email", "Email Poid", dto.getEmailPoid()));
            msgsDtl1.setEmailRemarks(dto.getRemarks());
            docsMsgsDtl1Repository.save(msgsDtl1);
        }

        if (dto.getSendEmail()) {
            PortCallOperationDocsMsgsDtl1 msgsForSend = docsMsgsDtl1Repository.findByEmailPoid(emailPoidToUse)
                    .orElseThrow(() -> new ResourceNotFoundException("Email", "Email Poid", emailPoidToUse));
            if (msgsForSend.getEmailSendOn() == null) {
                // Logic to send email
            } else {
                throw new CustomException("Email already sent", 400);
            }
        }

        // Map existing activities by actualsTimingDtlPoid (PK) for matching
        Map<Long, PortCallOperationActTimingsActvtyDtl> existingActivitiesMap = existingEntities.stream()
                .collect(Collectors.toMap(PortCallOperationActTimingsActvtyDtl::getActualsTimingDtlPoid, e -> e));

        Set<Long> processedActualsTimingDtlPoids = new HashSet<>();
        List<PortCallOperationActTimingsActvtyDtl> entitiesToUpdate = new ArrayList<>();
        List<PortCallOperationActTimingsActvtyDtl> entitiesToCreate = new ArrayList<>();
        List<PortCallOperationActTimingsActvtyDtl> entitiesToDelete = new ArrayList<>();

        long nextActualsTimingDtlPoid = actTimingsActvtyDtlRepository.findMaxActualsTimingDtlPoidByTransactionPoidAndDetRowId(transactionPoid, detRowId) + 1;

        for (PortCallReportActivityDto activity : activities) {
            if (activity.getActivityPoid() == null && StringUtils.isBlank(activity.getActivityName())) {
                throw new ValidationException("activityName is required when activityPoid is not provided");
            }

            // If actualsTimingDtlPoid is present in the activity it's an existing row, otherwise create
            PortCallOperationActTimingsActvtyDtl existingActivity = activity.getActualsTimingDtlPoid() != null
                    ? existingActivitiesMap.get(activity.getActualsTimingDtlPoid())
                    : null;

            if (existingActivity != null) {
                processedActualsTimingDtlPoids.add(existingActivity.getActualsTimingDtlPoid());

                PortCallOperationActTimingsActvtyDtl oldActivity = new PortCallOperationActTimingsActvtyDtl();
                BeanUtils.copyProperties(existingActivity, oldActivity);

                existingActivity.setActivityPoid(activity.getActivityPoid());
                existingActivity.setActivityName(activity.getActivityName());
                existingActivity.setDetails(activity.getOtherDescription());
                existingActivity.setEstimatedDatetime(activity.getEstimatedDatetime());

                entitiesToUpdate.add(existingActivity);
                loggingService.createLog(oldActivity, existingActivity, PortCallOperationActTimingsActvtyDtl.class, UserContext.getDocumentId(), transactionPoid.toString(),
                        String.format("Activity updated: actualsTimingDtlPoid=%s", existingActivity.getActualsTimingDtlPoid()));
            } else {
                entitiesToCreate.add(PortCallOperationActTimingsActvtyDtl.builder()
                        .transactionPoid(transactionPoid)
                        .detRowId(detRowId)
                        .actualsTimingDtlPoid(nextActualsTimingDtlPoid++)
                        .activityPoid(activity.getActivityPoid())
                        .activityName(activity.getActivityName())
                        .details(activity.getOtherDescription())
                        .estimatedDatetime(activity.getEstimatedDatetime())
                        .build());
            }
        }

        // Mark activities for deletion that are no longer in the DTO
        for (PortCallOperationActTimingsActvtyDtl existingActivity : existingEntities) {
            if (!processedActualsTimingDtlPoids.contains(existingActivity.getActualsTimingDtlPoid())) {
                entitiesToDelete.add(existingActivity);
            }
        }

        if (entitiesToUpdate.isEmpty() && entitiesToCreate.isEmpty()) {
            throw new ValidationException("At least one activity must be provided");
        }

        if (!entitiesToUpdate.isEmpty()) {
            actTimingsActvtyDtlRepository.saveAll(entitiesToUpdate);
        }
        if (!entitiesToCreate.isEmpty()) {
            actTimingsActvtyDtlRepository.saveAll(entitiesToCreate);
        }
        if (!entitiesToDelete.isEmpty()) {
            actTimingsActvtyDtlRepository.deleteAll(entitiesToDelete);
        }

        // Get the last saved entity for response (prefer updated, then created)
        PortCallOperationActTimingsActvtyDtl entity = !entitiesToUpdate.isEmpty()
                ? entitiesToUpdate.getLast()
                : entitiesToCreate.getLast();

        // Calculate the maximum actualsTimingDtlPoid from all saved activities (updated + created)
        long maxActualsTimingDtlPoid = actTimingsActvtyDtlRepository.findMaxActualsTimingDtlPoidByTransactionPoidAndDetRowId(transactionPoid, detRowId);

        // Update PortCallOperationActTimingDtl record
        PortCallOperationActTimingDtl existingActTimingDtl = actTimingDtlRepository.findById(new PortCallOperationActTimingDtlId(transactionPoid, detRowId))
                .orElseThrow(() -> new ResourceNotFoundException("ActTimingDtl", "Transaction Poid and Det Row Id", String.format("%s, %s", transactionPoid, detRowId)));

        PortCallOperationActTimingDtl oldActTimingDtl = new PortCallOperationActTimingDtl();
        BeanUtils.copyProperties(existingActTimingDtl, oldActTimingDtl);

        existingActTimingDtl.setEmailPoid(emailPoidToUse);
        existingActTimingDtl.setPortReportPoid(dto.getPortCallReportPoid());
        existingActTimingDtl.setActualsTimingDtlPoid(maxActualsTimingDtlPoid);

        actTimingDtlRepository.save(existingActTimingDtl);

        // Upload attachments if provided (optional)
        if (files != null && files.length > 0) {
            screenAttachmentService.uploadTimingAttachments(transactionPoid, detRowId, files, remarks, checklistNames);
        }

        String actTimingDtlLogDetail = String.format("KeyId = TRANSACTION_POID %s: DET_ROW_ID %s: PORT_REPORT_POID %s", existingActTimingDtl.getTransactionPoid(), existingActTimingDtl.getDetRowId(), existingActTimingDtl.getPortReportPoid());
        loggingService.createLog(oldActTimingDtl, existingActTimingDtl, PortCallOperationActTimingDtl.class, UserContext.getDocumentId(), transactionPoid.toString(), actTimingDtlLogDetail);

        return PortCallOperationActTimingsActvtyDetailResponseDto.builder()
                .transactionPoid(entity.getTransactionPoid())
                .detRowId(entity.getDetRowId())
                .actualsTimingDtlPoid(entity.getActualsTimingDtlPoid())
                .activityPoid(entity.getActivityPoid())
                .activityName(entity.getActivityName())
                .details(entity.getDetails())
                .estimatedDatetime(entity.getEstimatedDatetime())
                .build();
    }

    @Override
    public PortCallOperationDocsCopyDetailResponseDto getDocsCopyDetail(Long transactionPoid, Long detRowId) {
        log.info("Fetching DocsCopyDetail for transactionPoid: {}, detRowId: {}", transactionPoid, detRowId);

        PortCallOperationDocsCopyDtl entity = docsCopyDtlRepository.findById(new PortCallOperationDocsCopyDtlId(transactionPoid, detRowId))
                .orElseThrow(() -> new ResourceNotFoundException("DocsCopyDetail", "Transaction Poid and Det Row Id", String.format("%s, %s", transactionPoid, detRowId)));

        PortCallOperationDocsCopyDetailResponseDto.PortCallOperationDocsCopyDetailResponseDtoBuilder builder = PortCallOperationDocsCopyDetailResponseDto.builder()
                .transactionPoid(entity.getTransactionPoid())
                .emailPoid(entity.getEmailPoid())
                .detRowId(entity.getDetRowId())
                .documentFrom(entity.getDocumentFrom())
                .documentList(entity.getDocumentList())
                .documentSelect(entity.getDocumentSelect())
                .documentAttachments(entity.getDocumentAttachments());

        if (entity.getEmailPoid() != null) {
            docsMsgsDtl1Repository.findByEmailPoid(entity.getEmailPoid())
                    .ifPresent(emailRecord -> {
                        builder.emailSentOn(emailRecord.getEmailSendOn() != null ? emailRecord.getEmailSendOn() : null);
                    });
        }
        return builder.build();
    }

    @Override
    @Transactional
    public PortCallOperationDocsCopyDetailResponseDto createDocsCopyDetail(Long transactionPoid, PortCallOperationDocsCopyDetailRequestDto dto, MultipartFile[] files, String[] remarks, String[] checklistNames) {
        log.info("Creating DocsCopyDetail for transactionPoid: {}", transactionPoid);

        if (!hdrRepository.existsById(transactionPoid)) {
            throw new ResourceNotFoundException("Port call operation", "Transaction Poid", transactionPoid);
        }

        // Validate that files are provided
        if (files == null || files.length == 0) {
            throw new ValidationException("Document attachments are required. At least one file must be provided.");
        }

        Long nextDetRowId = docsCopyDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;

        PortCallOperationDocsCopyDtl entity = PortCallOperationDocsCopyDtl.builder()
                .transactionPoid(transactionPoid)
                .detRowId(nextDetRowId)
                .documentFrom(dto.getDocumentFrom())
                .documentList(dto.getDocumentList())
                .documentSelect(dto.getDocumentSelect())
                .build();

        docsCopyDtlRepository.save(entity);

        // Upload attachments and update documentAttachments field
        screenAttachmentService.uploadDocsCopyAttachments(transactionPoid, nextDetRowId, files, remarks, checklistNames);

        // Reload entity to get updated documentAttachments
        entity = docsCopyDtlRepository.findById(new PortCallOperationDocsCopyDtlId(transactionPoid, nextDetRowId))
                .orElseThrow(() -> new ResourceNotFoundException("DocsCopyDetail", "Transaction Poid and Det Row Id", String.format("%s, %s", transactionPoid, nextDetRowId)));

        // Validate that documentAttachments is not empty after upload
        if (StringUtils.isBlank(entity.getDocumentAttachments())) {
            throw new ValidationException("Failed to upload document attachments. Document attachments cannot be empty.");
        }

        String logDetail = String.format("Row Created on [Port Call Operation Docs Copy Details] with detRowId: %s", entity.getDetRowId());
        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logDetail);

        PortCallOperationDocsCopyDetailResponseDto.PortCallOperationDocsCopyDetailResponseDtoBuilder builder = PortCallOperationDocsCopyDetailResponseDto.builder()
                .transactionPoid(entity.getTransactionPoid())
                .emailPoid(entity.getEmailPoid())
                .detRowId(entity.getDetRowId())
                .documentFrom(entity.getDocumentFrom())
                .documentList(entity.getDocumentList())
                .documentSelect(entity.getDocumentSelect())
                .documentAttachments(entity.getDocumentAttachments());

        if (entity.getEmailPoid() != null) {
            docsMsgsDtl1Repository.findByEmailPoid(entity.getEmailPoid())
                    .ifPresent(emailRecord -> {
                        builder.emailSentOn(emailRecord.getEmailSendOn() != null ? emailRecord.getEmailSendOn() : null);
                    });
        }
        return builder.build();
    }

    @Override
    @Transactional
    public PortCallOperationDocsCopyDetailResponseDto updateDocsCopyDetail(Long transactionPoid, Long detRowId, PortCallOperationDocsCopyDetailRequestDto dto, MultipartFile[] files, String[] remarks, String[] checklistNames) {
        log.info("Updating DocsCopyDetail for transactionPoid: {}, detRowId: {}", transactionPoid, detRowId);

        PortCallOperationDocsCopyDtl entity = docsCopyDtlRepository.findById(new PortCallOperationDocsCopyDtlId(transactionPoid, detRowId))
                .orElseThrow(() -> new ResourceNotFoundException("DocsCopyDetail", "Transaction Poid and Det Row Id", String.format("%s, %s", transactionPoid, detRowId)));

        PortCallOperationDocsCopyDtl oldEntity = new PortCallOperationDocsCopyDtl();
        BeanUtils.copyProperties(entity, oldEntity);

        PortCallOperationDocsCopyDtl latestRecord = docsCopyDtlRepository.findByTransactionPoidOrderByLastModifiedDateDesc(transactionPoid).getFirst();
        if (!latestRecord.getDetRowId().equals(detRowId)) {
            throw new ValidationException("Cannot edit this record. Please select a latest one");
        }

        // Require files only when the record has no existing attachments in DOCUMENT_ATTACHMENTS
        if (StringUtils.isBlank(entity.getDocumentAttachments()) && (files == null || files.length == 0)) {
            throw new ValidationException("Document attachments are required. At least one file must be provided.");
        }

        entity.setDocumentFrom(dto.getDocumentFrom());
        entity.setDocumentList(dto.getDocumentList());
        entity.setDocumentSelect(dto.getDocumentSelect());

        docsCopyDtlRepository.save(entity);

        // Upload attachments when files are provided; existing documentAttachments are preserved when no new files
        if (files != null && files.length > 0) {
            screenAttachmentService.uploadDocsCopyAttachments(transactionPoid, detRowId, files, remarks, checklistNames);
            entity = docsCopyDtlRepository.findById(new PortCallOperationDocsCopyDtlId(transactionPoid, detRowId))
                    .orElseThrow(() -> new ResourceNotFoundException("DocsCopyDetail", "Transaction Poid and Det Row Id", String.format("%s, %s", transactionPoid, detRowId)));
            if (StringUtils.isBlank(entity.getDocumentAttachments())) {
                throw new ValidationException("Failed to upload document attachments. Document attachments cannot be empty.");
            }
        }

        String logDetail = String.format("KeyId = TRANSACTION_POID %s: DET_ROW_ID %s", entity.getTransactionPoid(), entity.getDetRowId());
        loggingService.createLog(oldEntity, entity, PortCallOperationDocsCopyDtl.class, UserContext.getDocumentId(), transactionPoid.toString(), logDetail);

        PortCallOperationDocsCopyDetailResponseDto.PortCallOperationDocsCopyDetailResponseDtoBuilder builder = PortCallOperationDocsCopyDetailResponseDto.builder()
                .transactionPoid(entity.getTransactionPoid())
                .emailPoid(entity.getEmailPoid())
                .detRowId(entity.getDetRowId())
                .documentFrom(entity.getDocumentFrom())
                .documentList(entity.getDocumentList())
                .documentSelect(entity.getDocumentSelect())
                .documentAttachments(entity.getDocumentAttachments());

        if (entity.getEmailPoid() != null) {
            docsMsgsDtl1Repository.findByEmailPoid(entity.getEmailPoid())
                    .ifPresent(emailRecord -> {
                        builder.emailSentOn(emailRecord.getEmailSendOn() != null ? emailRecord.getEmailSendOn() : null);
                    });
        }
        return builder.build();
    }

    @Override
    public List<PortCallOperationEstBertDetailResponseDto> getBerthingDtlById(Long transactionPoid) {
        hdrRepository.findById(transactionPoid).orElseThrow(() -> new ResourceNotFoundException("Port call operation", "Transaction Poid", transactionPoid));
        return mapEstBertDetailsToResponse(estBertDtlRepository.findByTransactionPoid(transactionPoid));
    }

    @Override
    public List<PortCallOperationEstPrearrivalDetailResponseDto> getPrearrivalActivityDtlById(Long transactionPoid) {
        hdrRepository.findById(transactionPoid).orElseThrow(() -> new ResourceNotFoundException("Port call operation", "Transaction Poid", transactionPoid));
        return mapEstPrearrivalDetailsToResponse(estPrearrivalDtlRepository.findByTransactionPoid(transactionPoid));
    }

    @Override
    public List<PortCallOperationActTimingDetailResponseDto> getActualTimingsDtlById(Long transactionPoid) {
        hdrRepository.findById(transactionPoid).orElseThrow(() -> new ResourceNotFoundException("Port call operation", "Transaction Poid", transactionPoid));
        return mapActTimingDetailsToResponse(actTimingDtlRepository.findByTransactionPoid(transactionPoid));
    }


}
