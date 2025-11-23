package com.alsharif.operations.commonlov.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
    public class PdaRateTypeRequestDTO {
        @NotBlank(message = "Rate type code is mandatory")
        @Size(max = 20, message = "Rate type code must not exceed 20 characters")
        private String rateTypeCode;

        @NotBlank(message = "Rate type name is mandatory")
        @Size(max = 100, message = "Rate type name must not exceed 100 characters")
        private String rateTypeName;

        @Size(max = 100, message = "Rate type name2 must not exceed 100 characters")
        private String rateTypeName2;

        @Size(max = 1000, message = "Rate type formula must not exceed 1000 characters")
        private String rateTypeFormula;

        @Size(max = 100, message = "Default quantity must not exceed 100 characters")
        private String defQty;

        private BigDecimal defDays;

        private BigInteger seqNo;

        @Size(max = 1)
        private String active;

        // Getters and Setters
        public String getRateTypeCode() {
            return rateTypeCode;
        }

        public void setRateTypeCode(String rateTypeCode) {
            this.rateTypeCode = rateTypeCode;
        }

        public String getRateTypeName() {
            return rateTypeName;
        }

        public void setRateTypeName(String rateTypeName) {
            this.rateTypeName = rateTypeName;
        }

        public String getRateTypeName2() {
            return rateTypeName2;
        }

        public void setRateTypeName2(String rateTypeName2) {
            this.rateTypeName2 = rateTypeName2;
        }

        public String getRateTypeFormula() {
            return rateTypeFormula;
        }

        public void setRateTypeFormula(String rateTypeFormula) {
            this.rateTypeFormula = rateTypeFormula;
        }

        public String getDefQty() {
            return defQty;
        }

        public void setDefQty(String defQty) {
            this.defQty = defQty;
        }

        public BigDecimal getDefDays() {
            return defDays;
        }

        public void setDefDays(BigDecimal defDays) {
            this.defDays = defDays;
        }

        public BigInteger getSeqNo() {
            return seqNo;
        }

        public void setSeqNo(BigInteger seqNo) {
            this.seqNo = seqNo;
        }

        public String getActive() {
            return active;
        }

        public void setActive(String active) {
            this.active = active;
        }
    }

