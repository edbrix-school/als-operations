package com.asg.operations.shipprincipal.service;

import com.asg.operations.shipprincipal.dto.CreateLedgerResult;
import org.springframework.stereotype.Service;

@Service
public interface GLMasterService {

    CreateLedgerResult createGlMaster(Long groupPoid, Long companyPoid, Long principalId, String userName);

}
