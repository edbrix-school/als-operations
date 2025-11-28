package com.alsharif.operations.commonlov.controller;

import com.alsharif.operations.commonlov.dto.LovResponse;
import com.alsharif.operations.commonlov.service.LovServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static com.alsharif.operations.common.ApiResponse.success;

@Controller
@RequestMapping("lov")
@Slf4j
public class LovController {

    @Autowired
    LovServiceImpl lovService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getLovList(
                                  @RequestParam("lovName") String lovName,
                                  @RequestParam(value = "docKeyPoid", required = false) Long docKeyPoid,
                                  @RequestParam(value = "filterValue", required = false) String filterValue,
                                  @org.springframework.web.bind.annotation.RequestHeader("X-Group-Poid") Long groupPoid,
                                  @org.springframework.web.bind.annotation.RequestHeader("X-Company-Poid") Long companyPoid,
                                  @org.springframework.web.bind.annotation.RequestHeader(value = "X-User-Id", required = false) String userId) {
        log.info("getLovList started for lovName={} docKeyPoid={} filterValue={} groupPoid={} companyPoid={} userId={}", 
                lovName, docKeyPoid, filterValue, groupPoid, companyPoid, userId);
        LovResponse lovResponse = lovService.getLovList(lovName, docKeyPoid, filterValue, groupPoid, companyPoid, userId);
        log.info("getLovList completed for lovName={} itemsReturned={}", lovName,
                lovResponse != null && lovResponse.getItems() != null ? lovResponse.getItems().size() : 0);
        return  success("Task fetched successfully", lovResponse);

    }
}
