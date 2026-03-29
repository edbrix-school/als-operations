package com.asg.operations.shipprincipal.service;

import com.asg.operations.shipprincipal.dto.AddressLoadListResponse;
import com.asg.operations.shipprincipal.dto.AddressMasterResponse;
import com.asg.operations.shipprincipal.dto.AddressTypeMapDTO;
import com.asg.operations.shipprincipal.entity.AddressMaster;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public interface AddressMasterService {

    AddressMasterResponse getMasterWithDetails(Long poid);

    void saveAllDetails(AddressTypeMapDTO typeMap, AddressMaster master, String currentUser, String parentPoid);

    AddressLoadListResponse loadAddressCollection(BigDecimal addressMasterPoid);

}
