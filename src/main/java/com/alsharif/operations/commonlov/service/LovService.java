package com.alsharif.operations.commonlov.service;

import com.alsharif.operations.commonlov.dto.LovItem;
import com.alsharif.operations.commonlov.dto.LovResponse;
import org.springframework.stereotype.Service;

@Service
public interface LovService {
    LovResponse getLovList(String lovName, Long docKeyPoid, String filterValue);
}
