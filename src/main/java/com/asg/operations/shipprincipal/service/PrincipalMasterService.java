package com.asg.operations.shipprincipal.service;

import com.asg.operations.shipprincipal.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface PrincipalMasterService {

    Page<PrincipalMasterListDto> getPrincipalList(String search, Pageable pageable);

    PrincipalMasterDto getPrincipal(Long id);

    PrincipalMasterDto createPrincipal(PrincipalCreateDTO dto, Long groupPoid, Long userPoid);

    PrincipalMasterDto updatePrincipal(Long id, PrincipalUpdateDTO dto, Long groupPoid, Long userPoid);

    void deletePrincipal(Long id);

    CreateLedgerResponseDto createLedger(Long principalPoid, Long groupPoid, Long companyPoid, Long userPoid);

    void toggleActive(Long id);

}
