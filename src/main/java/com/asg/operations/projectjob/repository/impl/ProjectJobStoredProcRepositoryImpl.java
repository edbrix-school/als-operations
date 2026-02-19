package com.asg.operations.projectjob.repository.impl;

import com.asg.operations.projectjob.dto.ProjectLoadDtlRow;
import com.asg.operations.projectjob.dto.ProjectLoadHdrRow;
import com.asg.operations.projectjob.dto.ProjectLoadInJobsProcResponse;
import com.asg.operations.projectjob.repository.ProjectJobStoredProcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProjectJobStoredProcRepositoryImpl implements ProjectJobStoredProcRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public ProjectLoadInJobsProcResponse callProjectsLoadInJobsProc(Long transactionPoid) {

        String proc = "{call PROC_PROJECTS_LOAD_IN_JOBS(?, ?, ?, ?)}";

        return jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall(proc)) {

                cs.setLong(1, transactionPoid);
                cs.registerOutParameter(2, Types.VARCHAR);
                cs.registerOutParameter(3, Types.REF_CURSOR);
                cs.registerOutParameter(4, Types.REF_CURSOR);

                cs.execute();

                String status = cs.getString(2);

                List<ProjectLoadHdrRow> hdrList = new ArrayList<>();
                try (ResultSet rs = (ResultSet) cs.getObject(3)) {
                    if (rs != null) {
                        while (rs.next()) {
                            ProjectLoadHdrRow row = new ProjectLoadHdrRow();
                            row.setTransactionPoid(rs.getLong("TRANSACTION_POID"));
                            row.setDocRef(rs.getString("DOC_REF"));
                            row.setBillingTo(rs.getString("BILLING_TO"));
                            row.setBillingPartyPoid(rs.getLong("BILLING_PARTY_POID"));
                            row.setProjectCustomerPoid(rs.getLong("PROJECT_CUSTOMER_POID"));
                            row.setPrincipalPoid(rs.getLong("PRINCIPAL_POID"));
                            row.setSalesmanPoid(rs.getLong("SALESMAN_POID"));
                            row.setShipmentMode(rs.getString("SHIPMENT_MODE"));
                            row.setTransportationMode(rs.getString("TRANSPORTATION_MODE"));
                            row.setProjectReference(rs.getString("PROJECT_REFERENCE"));
                            row.setCommodity(rs.getString("COMMODITY"));
                            hdrList.add(row);
                        }
                    }
                }

                List<ProjectLoadDtlRow> dtlList = new ArrayList<>();
                try (ResultSet rs = (ResultSet) cs.getObject(4)) {
                    if (rs != null) {
                        while (rs.next()) {
                            ProjectLoadDtlRow row = new ProjectLoadDtlRow();
                            row.setTransactionPoid(rs.getLong("TRANSACTION_POID"));
                            row.setDetRowId(rs.getLong("DET_ROW_ID"));
                            row.setQuotationRefPoid(rs.getLong("QUOTATION_REF_POID"));
                            row.setChargePoid(rs.getLong("CHARGE_POID"));
                            row.setPrintableChargeDesc(rs.getString("PRINTABLE_CHARGE_DESC"));
                            row.setQuantity(rs.getBigDecimal("QUANTITY"));
                            row.setUnitPoid(rs.getLong("UNIT_POID"));
                            row.setBuyCurrencyCode(rs.getString("BUY_CURRENCY_CODE"));
                            row.setBuyCurrencyRate(rs.getBigDecimal("BUY_CURRENCY_RATE"));
                            row.setBuyUnitRate(rs.getBigDecimal("BUY_UNIT_RATE"));
                            row.setBuyTotalLc(rs.getBigDecimal("BUY_TOTAL_LC"));
                            row.setSellUnitRateFc(rs.getBigDecimal("SELL_UNIT_RATE_FC"));
                            row.setSellTotalFc(rs.getBigDecimal("SELL_TOTAL_FC"));
                            row.setTaxPoid(rs.getLong("TAX_POID"));
                            row.setTaxPercentage(rs.getBigDecimal("TAX_PERCENTAGE"));
                            row.setTaxAmountFc(rs.getBigDecimal("TAX_AMOUNT_FC"));
                            row.setSellGrandTotalFc(rs.getBigDecimal("SELL_GRAND_TOTAL_FC"));
                            row.setTaxAmountLc(rs.getBigDecimal("TAX_AMOUNT_LC"));
                            row.setSellGrandTotalLc(rs.getBigDecimal("SELL_GRAND_TOTAL_LC"));
                            row.setMarginAmountLc(rs.getBigDecimal("MARGIN_AMOUNT_LC"));
                            row.setRemarks(rs.getString("REMARKS"));
                            dtlList.add(row);
                        }
                    }
                }

                return new ProjectLoadInJobsProcResponse(status, hdrList, dtlList);

            } catch (SQLException ex) {
                throw new RuntimeException("Error calling PROC_PROJECTS_LOAD_IN_JOBS: " + ex.getMessage());
            }
        });
    }

    @Override
    public String callReopenJobProc(Long loginUserPoid, Long docKeyPoid) {

        String proc = "{call PROC_FF_REOPEN_JOB(?, ?, ?)}";

        return jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall(proc)) {

                cs.setLong(1, loginUserPoid);
                cs.setLong(2, docKeyPoid);
                cs.registerOutParameter(3, Types.VARCHAR);

                cs.execute();

                return cs.getString(3);

            } catch (SQLException ex) {
                throw new RuntimeException(
                        "Error calling PROC_FF_REOPEN_JOB: " + ex.getMessage(), ex);
            }
        });
    }
}
