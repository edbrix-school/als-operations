package com.asg.operations.commonlov.controller;

import com.asg.common.lib.security.util.UserContext;
import com.asg.operations.commonlov.dto.LovResponse;
import com.asg.operations.commonlov.service.LovServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static com.asg.operations.common.ApiResponse.success;

@Controller
@RequestMapping("/v1/lov")
@Slf4j
public class LovController {

    @Autowired
    LovServiceImpl lovService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getLovList(
                                  @RequestParam("lovName") String lovName,
                                  @RequestParam(value = "docKeyPoid", required = false) Long docKeyPoid,
                                  @RequestParam(value = "filterValue", required = false) String filterValue) {
        log.info("getLovList started for lovName={} docKeyPoid={} filterValue={} groupPoid={} companyPoid={} userId={}", 
                lovName, docKeyPoid, filterValue, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid());
        LovResponse lovResponse = lovService.getLovList(lovName, docKeyPoid, filterValue, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid());
        log.info("getLovList completed for lovName={} itemsReturned={}", lovName,
                lovResponse != null && lovResponse.getItems() != null ? lovResponse.getItems().size() : 0);
        return  success("Task fetched successfully", lovResponse);

    }
}
