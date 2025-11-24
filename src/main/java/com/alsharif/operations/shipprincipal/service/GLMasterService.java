package com.alsharif.operations.shipprincipal.service;

import com.alsharif.operations.shipprincipal.dto.CreateLedgerResult;
import org.springframework.stereotype.Service;

@Service
public interface GLMasterService {

    CreateLedgerResult createGlMaster(Long groupPoid, Long companyPoid, Long principalId, String userName);

}
