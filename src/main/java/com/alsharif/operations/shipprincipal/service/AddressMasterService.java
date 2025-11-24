package com.alsharif.operations.shipprincipal.service;

import com.alsharif.operations.shipprincipal.dto.AddressMasterResponse;
import com.alsharif.operations.shipprincipal.dto.AddressTypeMapDTO;
import com.alsharif.operations.shipprincipal.entity.AddressMaster;
import org.springframework.stereotype.Service;

@Service
public interface AddressMasterService {

    AddressMasterResponse getMasterWithDetails(Long poid);

    void saveAllDetails(AddressTypeMapDTO typeMap, AddressMaster master, String currentUser);

}
