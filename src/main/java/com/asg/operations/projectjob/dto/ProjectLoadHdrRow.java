package com.asg.operations.projectjob.dto;

import java.util.List;

import com.asg.common.lib.dto.LovGetListDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectLoadHdrRow {

    private Long transactionPoid;
    private String docRef;
    private String billingTo;
    private LovGetListDto billingToLov;
    private Long billingPartyPoid;
    private Long projectCustomerPoid;
    private Long principalPoid;
    private Long salesmanPoid;
    private Long linePoid;
    private String shipmentMode;
    private String transportationMode;
    private String projectReference;
    private List<String> commodityPoids;
}
