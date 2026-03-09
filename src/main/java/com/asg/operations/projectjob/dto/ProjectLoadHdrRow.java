package com.asg.operations.projectjob.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProjectLoadHdrRow {

    private Long transactionPoid;
    private String docRef;
    private String billingTo;
    private Long billingPartyPoid;
    private Long projectCustomerPoid;
    private Long principalPoid;
    private Long salesmanPoid;
    private String shipmentMode;
    private String transportationMode;
    private String projectReference;
    private List<String> commodityPoids;
}
