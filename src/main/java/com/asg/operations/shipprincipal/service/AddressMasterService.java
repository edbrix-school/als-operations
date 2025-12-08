package com.asg.operations.shipprincipal.service;

import com.asg.operations.shipprincipal.dto.AddressMasterResponse;
import com.asg.operations.shipprincipal.dto.AddressTypeMapDTO;
import com.asg.operations.shipprincipal.entity.AddressMaster;
import org.springframework.stereotype.Service;

@Service
public interface AddressMasterService {

    AddressMasterResponse getMasterWithDetails(Long poid);

    void saveAllDetails(AddressTypeMapDTO typeMap, AddressMaster master, String currentUser);

}
