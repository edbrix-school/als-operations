package com.alsharif.operations.commonlov.service;

import com.alsharif.operations.commonlov.dto.LovItem;
import com.alsharif.operations.commonlov.dto.LovResponse;
import com.alsharif.operations.commonlov.repository.LovRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class LovServiceImpl implements LovService {

    @Autowired
    LovRepository lovRepository;

    @Override
    public LovResponse getLovList(String lovName, Long docKeyPoid, String filterValue) {
        log.info("Fetching LOV list for lovName={} docKeyPoid={} filterValue={}", lovName, docKeyPoid, filterValue);
        LovResponse response = lovRepository.getLovList(lovName, docKeyPoid, filterValue);
        log.info("Fetched LOV list for lovName={} itemCount={}", lovName,
                response != null && response.getItems() != null ? response.getItems().size() : 0);
        return response;
    }
}
