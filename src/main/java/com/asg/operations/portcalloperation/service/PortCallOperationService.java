package com.asg.operations.portcalloperation.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.operations.portcalloperation.dto.*;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface PortCallOperationService {

    Map<String, Object> listOperations(String docId, FilterRequestDto request, Pageable pageable, LocalDate startDateValue, LocalDate endDateValue);

    PortCallOperationResponseDto getOperationById(Long id);

    PortCallOperationResponseDto createOperation(PortCallOperationCreateDto dto, Long userPoid, Long groupPoid);

    /**
     * Updates a port call operation.
     *
     * @param husbandryCrewDetRowIdByDetailIndexOut optional; when non-null, length must equal {@code dto.getHusbandryCrewDetails().size()}.
     *                                                 After update, each position i holds the DB {@code detRowId} for {@code husbandryCrewDetails.get(i)}
     *                                                 (including rows created in this request).
     * @param husbandryOthDetRowIdByDetailIndexOut   same for {@code husbandryOthDetails}
     */
    PortCallOperationResponseDto updateOperation(Long id, PortCallOperationDto dto, Long userPoid, Long groupPoid,
                                                   Long[] husbandryCrewDetRowIdByDetailIndexOut,
                                                   Long[] husbandryOthDetRowIdByDetailIndexOut);

    void deleteOperation(Long id, DeleteReasonDto deleteReasonDto);

    // Stored Procedure Methods
    Map<String, Object> loadPda(String pdaPoid, Long groupPoid, Long companyPoid, Long userPoid);

    Map<String, Object> loadFda(String fdaPoid, Long groupPoid, Long companyPoid, Long userPoid);

    Map<String, Object> loadVoyage(Long voyagePoid, Long groupPoid, Long companyPoid, Long userPoid);

    Map<String, Object> loadEmailList(String transactionPoid, Long groupPoid, Long companyPoid, Long userPoid);

    Map<String, Object> getMailTemplate(String transactionPoid, Long templatePoid, Long groupPoid, Long companyPoid, Long userPoid);

    Map<String, Object> getPortReportActivities(String transactionPoid, Long portReportPoid, Long groupPoid, Long companyPoid, Long userPoid);

    Map<String, Object> getEmailRecord(Long emailPoid, String transactionPoid, Long groupPoid, Long companyPoid, Long userPoid);

    Map<String, Object> getEmailHistory(String transactionPoid, Long groupPoid, Long companyPoid, Long userPoid);

    // EstBertDtl CRUD operations
    PortCallOperationEstBertDetailResponseDto getEstBertDetail(Long transactionPoid, Long detRowId);

    PortCallOperationEstBertDetailResponseDto createEstBertDetail(Long transactionPoid, PortCallOperationEstBertDetailRequestDto dto, MultipartFile[] files, String[] remarks, String[] checklistNames);

    PortCallOperationEstBertDetailResponseDto updateEstBertDetail(Long transactionPoid, Long detRowId, PortCallOperationEstBertDetailRequestDto dto, MultipartFile[] files, String[] remarks, String[] checklistNames);

    // EstPrearrivalActDtl CRUD operations
    List<PortCallOperationEstPrearrivalActDetailResponseDto> listEstPrearrivalActDetails(Long transactionPoid, Long detRowId);

    PortCallOperationEstPrearrivalActDetailResponseDto createEstPrearrivalActDetail(Long transactionPoid, PortCallOperationEstPrearrivalActDetailDto dto, MultipartFile[] files, String[] remarks, String[] checklistNames);

    PortCallOperationEstPrearrivalActDetailResponseDto updateEstPrearrivalActDetail(Long transactionPoid, Long detRowId, PortCallOperationEstPrearrivalActDetailDto dto, MultipartFile[] files, String[] remarks, String[] checklistNames);

    // ActTimingsActvtyDtl CRUD operations
    List<PortCallOperationActTimingsActvtyDetailResponseDto> listActTimingsActvtyDetails(Long transactionPoid, Long detRowId);

    PortCallOperationActTimingsActvtyDetailResponseDto createActTimingsActvtyDetail(Long transactionPoid, PortCallOperationActTimingsActivityDetailDto dto, MultipartFile[] files, String[] remarks, String[] checklistNames);

    PortCallOperationActTimingsActvtyDetailResponseDto updateActTimingsActvtyDetail(Long transactionPoid, Long detRowId, PortCallOperationActTimingsActivityDetailDto dto, MultipartFile[] files, String[] remarks, String[] checklistNames);

    // DocsCopyDtl CRUD operations
    PortCallOperationDocsCopyDetailResponseDto getDocsCopyDetail(Long transactionPoid, Long detRowId);

    PortCallOperationDocsCopyDetailResponseDto createDocsCopyDetail(Long transactionPoid, PortCallOperationDocsCopyDetailRequestDto dto, MultipartFile[] files, String[] remarks, String[] checklistNames);

    PortCallOperationDocsCopyDetailResponseDto updateDocsCopyDetail(Long transactionPoid, Long detRowId, PortCallOperationDocsCopyDetailRequestDto dto, MultipartFile[] files, String[] remarks, String[] checklistNames);

}
