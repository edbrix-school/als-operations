package com.asg.operations.shipprincipal.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.operations.shipprincipal.dto.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

@Service
public interface PrincipalMasterService {

    Map<String, Object> getAllPrincipalsWithFilters(String documentId, FilterRequestDto filters, Pageable pageable, LocalDate periodFrom, LocalDate periodTo);

    PrincipalMasterDto getPrincipal(Long id);

    PrincipalMasterDto createPrincipal(PrincipalCreateDTO dto, Long groupPoid, Long userPoid);

    PrincipalMasterDto updatePrincipal(Long id, PrincipalUpdateDTO dto, Long groupPoid, Long userPoid);

    void deletePrincipal(Long id, @Valid DeleteReasonDto deleteReasonDto);

    CreateLedgerResponseDto createLedger(Long principalPoid, Long groupPoid, Long companyPoid, Long userPoid);

    void toggleActive(Long id);

}
