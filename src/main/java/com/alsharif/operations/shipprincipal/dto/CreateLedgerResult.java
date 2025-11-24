package com.alsharif.operations.shipprincipal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLedgerResult {
    private String result;
    private Long newGlPoid;
    private String glAcctno;

    public boolean isSuccess() {
        return result != null && result.contains("SUCCESS");
    }

    public boolean isError() {
        return result != null && result.contains("ERROR");
    }
}