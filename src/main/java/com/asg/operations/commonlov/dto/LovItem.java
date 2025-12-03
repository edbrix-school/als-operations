package com.asg.operations.commonlov.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LovItem {
    private Long poid;
    private String code;
    private String description;
    private String label;
    private Long value;
    private Integer seqNo;
}
