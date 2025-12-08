package com.asg.operations.finaldisbursementaccount.service;

import com.asg.operations.common.PageResponse;
import com.asg.operations.exceptions.CustomException;
import com.asg.operations.exceptions.ResourceNotFoundException;
import com.asg.operations.finaldisbursementaccount.dto.*;
import com.asg.operations.finaldisbursementaccount.entity.PdaFdaDtl;
import com.asg.operations.finaldisbursementaccount.entity.PdaFdaHdr;
import com.asg.operations.finaldisbursementaccount.key.PdaFdaDtlId;
import com.asg.operations.finaldisbursementaccount.repository.FdaCustomRepository;
import com.asg.operations.finaldisbursementaccount.repository.PdaFdaDtlRepository;
import com.asg.operations.finaldisbursementaccount.repository.PdaFdaHdrRepository;
import com.asg.operations.finaldisbursementaccount.util.CalculationUtils;
import com.asg.operations.finaldisbursementaccount.util.ChargesMapper;
import com.asg.operations.finaldisbursementaccount.util.HeaderMapper;
import com.asg.operations.finaldisbursementaccount.util.ValidationUtils;
import com.asg.operations.pdaentryform.entity.PdaEntryHdr;
import com.asg.operations.pdaentryform.repository.PdaEntryHdrRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
//import net.sf.jasperreports.engine.*;
//import net.sf.jasperreports.engine.util.JRLoader;
//import org.springframework.core.io.ByteArrayResource;
//import org.springframework.core.io.Resource;
//import org.springframework.jdbc.core.JdbcTemplate;
//import java.io.InputStream;
//import java.sql.Connection;
//import java.sql.SQLException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FdaServiceImpl implements FdaService {

    private final PdaFdaHdrRepository pdaFdaHdrRepository;
    private final PdaFdaDtlRepository pdaFdaDtlRepository;
    private final FdaCustomRepository fdaCustomRepository;
    private final PdaEntryHdrRepository pdaEntryHdrRepository;
    private final ValidationUtils validationUtils;
//    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(readOnly = true)
    public FdaHeaderDto getFdaHeader(Long transactionPoid, Long groupPoid, Long companyPoid) {

        PdaFdaHdr entity = pdaFdaHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("FDA Header", "transactionPoid", transactionPoid));

        FdaHeaderDto fdaHeaderDto = HeaderMapper.mapHeaderEntityToDto(entity);

        List<PdaFdaDtl> dtls = pdaFdaDtlRepository.findByIdTransactionPoid(transactionPoid);

        List<FdaChargeDto> charges = dtls.stream()
                .map(ChargesMapper::mapChargeEntityToDto)
                .collect(Collectors.toList());

        CalculationUtils.computeProfitLossRuntime(charges, fdaHeaderDto);

        fdaHeaderDto.setCharges(charges);

        return fdaHeaderDto;
    }

    @Override
    @Transactional
    public FdaHeaderDto createFdaHeader(FdaHeaderDto dto, Long groupPoid, Long companyPoid, String userId) {

        validationUtils.validateHeaderBeforeSave(dto);

        if (dto.getArrivalDate() != null && dto.getVesselSailDate() != null &&
                dto.getVesselSailDate().isBefore(dto.getArrivalDate())) {
            throw new CustomException("Vessel sail date cannot be before arrival date", 422);
        }

        PdaFdaHdr entity = new PdaFdaHdr();
        HeaderMapper.mapHeaderDtoToEntity(dto, entity, userId);

        entity.setCreatedBy(userId);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setDocRef(validationUtils.generateDocRef((groupPoid)));
        entity = pdaFdaHdrRepository.save(entity);

        if (dto.getCharges() != null && !dto.getCharges().isEmpty()) {
            saveCharges(entity.getTransactionPoid(), dto.getCharges(), userId, groupPoid, companyPoid);
        }

        return getFdaHeader(entity.getTransactionPoid(), groupPoid, companyPoid);
    }

    @Override
    @Transactional
    public FdaHeaderDto updateFdaHeader(Long transactionPoid, UpdateFdaHeaderRequest dto, Long groupPoid, Long companyPoid, String userId) {

        validationUtils.validateHeaderBeforeUpdate(dto);

        PdaFdaHdr entity = pdaFdaHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("FDA Header", "transactionPoid", transactionPoid));

        HeaderMapper.mapUpdateHeaderDtoToEntity(dto, entity, userId);
        pdaFdaHdrRepository.save(entity);

        if (dto.getCharges() != null) {
            saveCharges(transactionPoid, dto.getCharges(), userId, groupPoid, companyPoid);
        }

        return getFdaHeader(transactionPoid, groupPoid, companyPoid);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FdaHeaderDto> getFdaList(Long groupPoid, Long companyPoid, Long transactionPoid, String vesselName, LocalDate etaFrom, LocalDate etaTo, Pageable pageable) {

//        can be used in future
//        Page<PdaFdaHdr> page = pdaFdaHdrRepository.searchFdaHeaders(groupPoid, companyPoid, transactionPoid, vesselName, etaFrom, etaTo, pageable);

        Page<PdaFdaHdr> page = pdaFdaHdrRepository.searchFdaHeaders(groupPoid, companyPoid, transactionPoid, etaFrom, etaTo, pageable);

        List<FdaHeaderDto> content = page.getContent().stream()
                .map(HeaderMapper::mapHeaderEntityToDto)
                .collect(Collectors.toList());

        return new PageResponse<>(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast(), page.getNumberOfElements());
    }

    @Override
    @Transactional
    public void softDeleteFda(Long transactionPoid, String userId) {

        PdaFdaHdr hdr = pdaFdaHdrRepository.findById(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException("FDA Header", "transactionPoid", transactionPoid));

        hdr.setDeleted("Y");
        hdr.setLastModifiedBy(userId);
        hdr.setLastModifiedDate(LocalDateTime.now());
        pdaFdaHdrRepository.save(hdr);

        List<PdaFdaDtl> details = pdaFdaDtlRepository.findByIdTransactionPoid(transactionPoid);
        pdaFdaDtlRepository.deleteAll(details);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FdaChargeDto> getCharges(Long transactionPoid, Long groupPoid, Long companyPoid, Pageable pageable) {

        pdaFdaHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("FDA Header", "transactionPoid", transactionPoid));

        Page<FdaChargeDto> dtoPage = pdaFdaDtlRepository.findByTransactionPoid(transactionPoid, pageable)
                .map(ChargesMapper::mapChargeEntityToDto);

        CalculationUtils.computeProfitLossRuntime(dtoPage.getContent(), null);

        return new PageResponse<>(
                dtoPage.getContent(),
                dtoPage.getNumber(),
                dtoPage.getSize(),
                dtoPage.getTotalElements(),
                dtoPage.getTotalPages(),
                dtoPage.isFirst(),
                dtoPage.isLast(),
                dtoPage.getNumberOfElements()
        );
    }

    @Override
    @Transactional
    public void saveCharges(Long transactionPoid, List<FdaChargeDto> chargeDtos, String userId, Long groupPoid, Long companyPoid) {
        List<PdaFdaDtl> toSave = new ArrayList<>();

        for (FdaChargeDto dto : chargeDtos) {
            String action = StringUtils.isNotBlank(dto.getActionType()) ? dto.getActionType().toLowerCase() : "";

            switch (action) {
                case "isdeleted":
                    if (dto.getDetRowId() != null) {
                        PdaFdaDtlId id = new PdaFdaDtlId(transactionPoid, dto.getDetRowId());
                        pdaFdaDtlRepository.findById(id).ifPresent(entity -> {
                            if ("N".equalsIgnoreCase(entity.getManual())) {
                                throw new CustomException("Cannot delete system-generated charge lines", 403);
                            }
                            pdaFdaDtlRepository.delete(entity);
                        });
                    }
                    break;
                case "iscreated":
                case "isupdated":
                    validationUtils.handleCreateOrUpdate(transactionPoid, dto, toSave, userId);
                    break;
                default:
                    // ignore unknown actions
            }
        }

        if (!toSave.isEmpty()) {
            pdaFdaDtlRepository.saveAll(toSave);
        }

        validationUtils.recalculateHeaderTotals(transactionPoid, userId, groupPoid, companyPoid);
    }

    @Override
    @Transactional
    public void deleteCharge(Long transactionPoid, Long detRowId, String userId) {
        PdaFdaDtlId id = new PdaFdaDtlId(transactionPoid, detRowId);

        PdaFdaDtl entity = pdaFdaDtlRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("FDA Detail", "detRowId", detRowId));

        if ("N".equalsIgnoreCase(entity.getManual())) {
            throw new CustomException("Cannot delete system-generated charge lines", 403);
        }

        pdaFdaDtlRepository.delete(entity);
    }

    @Override
    @Transactional
    public String closeFda(Long groupPoid, Long companyPoid, Long userPoid, Long transactionPoid) {

        PdaFdaHdr hdr = pdaFdaHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("FDA Header", "transactionPoid", transactionPoid));

        if ("C".equalsIgnoreCase(hdr.getStatus())) {
            throw new CustomException("FDA is already closed.", 400);
        }

        if (hdr.getVesselSailDate() == null) {
            throw new CustomException("Actual Vessel Sail Date is required for closing the FDA.", 400);
        }

        if (!"Y".equalsIgnoreCase(hdr.getAccountsVerified())) {
            throw new CustomException("Accounts verification is required before closing the FDA.", 400);
        }

        if (hdr.getTotalAmount() == null || hdr.getTotalAmount().compareTo(BigDecimal.ZERO) == 0) {
            throw new CustomException("FDA total amount is zero. Please use Close Without Amount option.", 400);
        }

        return fdaCustomRepository.closeFda(groupPoid, companyPoid, userPoid, transactionPoid);
    }

    @Override
    @Transactional
    public String reopenFda(Long groupPoid, Long companyPoid, Long userPoid, Long transactionPoid, FdaReOpenDto fdaReOpenDto) {

        PdaFdaHdr hdr = pdaFdaHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("FDA Header", "transactionPoid", transactionPoid));

        if (!"CLOSED".equalsIgnoreCase(hdr.getStatus())) {
            throw new CustomException("Only closed FDAs can be reopened.", 400);
        }

        return fdaCustomRepository.reopenFda(groupPoid, companyPoid, userPoid, transactionPoid, fdaReOpenDto.getComment());
    }

    @Override
    @Transactional
    public String submitFda(Long groupPoid, Long companyPoid, Long userPoid, Long transactionPoid) {

        PdaFdaHdr hdr = pdaFdaHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("FDA Header", "transactionPoid", transactionPoid));

        if ("CLOSED".equalsIgnoreCase(hdr.getStatus())) {
            throw new CustomException("Closed FDAs cannot be submitted.", 400);
        }

        return fdaCustomRepository.submitFda(groupPoid, companyPoid, userPoid, transactionPoid);
    }

    @Override
    @Transactional
    public String verifyFda(Long groupPoid, Long companyPoid, Long userPoid, Long transactionPoid) {

        PdaFdaHdr hdr = pdaFdaHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("FDA Header", "transactionPoid", transactionPoid));

        if ("CLOSED".equalsIgnoreCase(hdr.getStatus())) {
            throw new CustomException("Closed FDAs cannot be verified.", 400);
        }

        hdr.setAccountsVerified("Y");
        hdr.setLastModifiedBy(String.valueOf(userPoid));
        hdr.setLastModifiedDate(LocalDateTime.now());
        pdaFdaHdrRepository.save(hdr);

        return fdaCustomRepository.verifyFda(groupPoid, companyPoid, userPoid, transactionPoid);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String returnFda(Long groupPoid, Long companyPoid, Long userPoid,
                            Long transactionPoid, String correctionRemarks) {
        if (StringUtils.isBlank(correctionRemarks)) {
            throw new CustomException("Correction remarks are required", 400);
        }

        PdaFdaHdr hdr = pdaFdaHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new RuntimeException("FDA not found: " + transactionPoid));

        hdr.setOpsCorrectionRemarks(correctionRemarks);
        hdr.setOpsReturnedDate(LocalDate.now());
        hdr.setAccountsVerified("N");
        hdr.setLastModifiedBy("SYSTEM");
        hdr.setLastModifiedDate(LocalDateTime.now());

        pdaFdaHdrRepository.save(hdr);

        return fdaCustomRepository.returnFda(groupPoid, companyPoid, userPoid, transactionPoid, correctionRemarks);
    }

    @Override
    @Transactional
    public String supplementaryFda(Long groupPoid, Long companyPoid, Long userPoid, Long transactionPoid) {
        return fdaCustomRepository.supplementaryFda(groupPoid, companyPoid, userPoid, transactionPoid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FdaSupplementaryInfoDto> getSupplementaryInfo(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid) {
        return fdaCustomRepository.getSupplementaryInfo(transactionPoid, groupPoid, companyPoid, userPoid);
    }

    @Override
    @Transactional
    public String closeFdaWithoutAmount(Long transactionPoid, Long groupPoid, Long companyPoid,
                                        Long userPoid, String closedRemark) {

        PdaFdaHdr hdr = pdaFdaHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("FDA Header", "transactionPoid", transactionPoid));

        if ("CLOSED".equalsIgnoreCase(hdr.getStatus())) {
            throw new CustomException("FDA is already closed.", 400);
        }

        if (StringUtils.isBlank(closedRemark)) {
            throw new CustomException("Closed remarks are required for closing FDA without amount.", 400);
        }

        if (hdr.getVesselSailDate() == null) {
            throw new CustomException("Actual Vessel Sail Date is required for closing the FDA.", 400);
        }

        if (!"Y".equalsIgnoreCase(hdr.getAccountsVerified())) {
            throw new CustomException("Accounts verification is required before closing the FDA.", 400);
        }

        if (hdr.getTotalAmount() != null && hdr.getTotalAmount().compareTo(BigDecimal.ZERO) != 0) {
            throw new CustomException("Close Without Amount is allowed only when FDA total amount is zero.", 400);
        }

        return fdaCustomRepository.closeFdaWithoutAmount(transactionPoid, groupPoid, companyPoid, userPoid, closedRemark);
    }

    @Override
    @Transactional(readOnly = true)
    public PartyGlResponse getPartyGl(Long groupPoid, Long companyPoid, Long userPoid, Long partyPoid, String partyType) {
        return fdaCustomRepository.getPartyGl(groupPoid, companyPoid, userPoid, partyPoid, partyType);
    }

    @Override
    @Transactional
    public String createFdaFromPda(Long groupPoid, Long companyPoid, Long userPoid, Long pdaTransactionPoid) {
        return fdaCustomRepository.createFdaFromPda(groupPoid, companyPoid, userPoid, pdaTransactionPoid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PdaLogResponse> getPdaLogs(Long transactionPoid, Long groupPoid, Long companyPoid) {

        Optional<PdaFdaHdr> fdaHdrOptional = pdaFdaHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoidAndDeleted(transactionPoid, groupPoid, companyPoid, "N");

        if (fdaHdrOptional.isEmpty()) {
            return List.of();
        }
        PdaFdaHdr fdaHeader = fdaHdrOptional.get();

        List<PdaFdaHdr> logs = pdaFdaHdrRepository
                .findByPdaRefAndGroupPoidAndCompanyPoidAndDeleted(fdaHeader.getPdaRef(), groupPoid, companyPoid, "N");

        if (logs.isEmpty()) {
            return List.of();
        }

        List<Long> pdaTransactionIds = logs.stream()
                .map(fda -> {
                    try {
                        return Long.valueOf(fda.getPdaRef());
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<String, PdaEntryHdr> pdaMap = pdaEntryHdrRepository.findAllById(pdaTransactionIds).stream()
                .collect(Collectors.toMap(pda -> String.valueOf(pda.getTransactionPoid()), pda -> pda));

        return logs.stream().map(fda -> {
            PdaLogResponse response = new PdaLogResponse();

            PdaEntryHdr pda = pdaMap.get(fda.getPdaRef());
            if (pda != null) {
                response.setPdaTransactionPoid(pda.getTransactionPoid());
                response.setPdaDocRef(pda.getDocRef());
                response.setPdaTransactionDate(pda.getTransactionDate());
            } else {
                response.setPdaTransactionPoid(null);
                response.setPdaDocRef(fda.getPdaRef());
                response.setPdaTransactionDate(null);
            }

            response.setFdaTransactionPoid(fda.getTransactionPoid());
            response.setFdaDocRef(fda.getDocRef());
            response.setFdaTransactionDate(fda.getTransactionDate());
            response.setFdaStatus(fda.getStatus());

            response.setDocumentSubmittedDate(fda.getDocumentSubmittedDate());
            response.setDocumentSubmittedBy(fda.getDocumentSubmittedBy());
            response.setDocumentSubmittedStatus(fda.getDocumentSubmittedStatus());

            response.setVerificationAcceptedDate(fda.getVerificationAcceptedDate());
            response.setVerificationAcceptedBy(fda.getVerificationAcceptedBy());
            response.setDocumentReceivedStatus(fda.getDocumentReceivedStatus());

            response.setCreatedBy(fda.getCreatedBy());
            response.setCreatedDate(fda.getCreatedDate());
            response.setLastModifiedBy(fda.getLastModifiedBy());
            response.setLastModifiedDate(fda.getLastModifiedDate());

            return response;
        }).collect(Collectors.toList());
    }

//    @Override
//    @Transactional
//    public Resource generateFdaReport(Long transactionPoid, String reportType,
//                                      Long companyId, Long userId, Long groupId) {
//        Connection connection = null;
//        try {
//            String reportFileName = CalculationUtils.getReportFileName(reportType);
//
//            InputStream reportStream = getClass().getClassLoader()
//                    .getResourceAsStream("reports/" + reportFileName);
//
//            if (reportStream == null) {
//                throw new RuntimeException("Report template not found: reports/" + reportFileName);
//            }
//
//            JasperReport jasperReport;
//            try {
//                InputStream compiledStream = getClass().getClassLoader()
//                        .getResourceAsStream("reports/" + reportFileName.replace(".jrxml", ".jasper"));
//                if (compiledStream != null) {
//                    jasperReport = (JasperReport) JRLoader.loadObject(compiledStream);
//                } else {
//                    jasperReport = JasperCompileManager.compileReport(reportStream);
//                }
//            } finally {
//                reportStream.close();
//            }
//
//            Map<String, Object> parameters = new HashMap<>();
//            parameters.put("DOC_KEY_POID", transactionPoid);
//            parameters.put("LOGIN_COMP_POID", companyId);
//            parameters.put("LOGIN_USER_POID", userId);
//            parameters.put("LOGIN_GROUP_POID", groupId);
//
//            connection = jdbcTemplate.getDataSource().getConnection();
//
//            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);
//
//            byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);
//
//            return new ByteArrayResource(pdfBytes);
//
//        } catch (Exception e) {
//            throw new RuntimeException("Error generating FDA report: " + e.getMessage(), e);
//        } finally {
//            if (connection != null) {
//                try {
//                    connection.close();
//                } catch (SQLException ignored) {
//                }
//            }
//        }
//    }

}
