package com.asg.operations.shipprincipal.repository;

import com.asg.operations.shipprincipal.dto.AddressLoadListResponse;

import java.math.BigDecimal;

public interface AddressStoredProcRepository {

    AddressLoadListResponse callAddressLoadListProc(BigDecimal groupPoid, BigDecimal addressMasterPoid);
}
