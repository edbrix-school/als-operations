package com.asg.operations.salesquotationprojects.repository;

import com.asg.common.lib.exception.CustomException;
import com.asg.operations.salesquotationprojects.dto.TempAddressProcedureResponse;
import com.asg.operations.salesquotationprojects.dto.TempNewAddressRow;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SalesQuoteProjectsStoredProcRepository {


    private final JdbcTemplate jdbcTemplate;

    public List<TempNewAddressRow> callNewTempAddressLoadListProc(
            Long loginGroupPoid,
            Long loginCompanyPoid,
            Long loginUserPoid,
            String docId,
            Long docKeyPoid
    ) {
        String proc = "{call PROC_NEW_ADDRESS_LOADLIST(?, ?, ?, ?, ?, ?)}";

        return jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall(proc)) {
                cs.setLong(1, loginGroupPoid);
                cs.setLong(2, loginCompanyPoid);
                cs.setLong(3, loginUserPoid);
                cs.setString(4, docId);
                cs.setLong(5, docKeyPoid);
                cs.registerOutParameter(6, Types.REF_CURSOR);

                cs.execute();

                List<TempNewAddressRow> rows = new ArrayList<>();
                try (ResultSet rs = (ResultSet) cs.getObject(6)) {
                    if (rs != null) {
                        while (rs.next()) {
                            TempNewAddressRow row = new TempNewAddressRow();
                            row.setNewAddressPoid(rs.getObject("NEW_ADDRESS_POID") != null ? rs.getLong("NEW_ADDRESS_POID") : null);
                            row.setDocFieldName(rs.getString("DOC_FIELD_NAME"));
                            row.setAddressName(rs.getString("ADDRESS_NAME"));
                            row.setOffTel1(rs.getString("OFF_TEL1"));
                            row.setOffTel2(rs.getString("OFF_TEL2"));
                            row.setContactPerson(rs.getString("CONTACT_PERSON"));
                            row.setDesignation(rs.getString("DESIGNATION"));
                            row.setMobile(rs.getString("MOBILE"));
                            row.setFax(rs.getString("FAX"));
                            row.setEmail1(rs.getString("EMAIL1"));
                            row.setEmail2(rs.getString("EMAIL2"));
                            row.setWebsite(rs.getString("WEBSITE"));
                            row.setPoBox(rs.getString("PO_BOX"));
                            row.setOffNo(rs.getString("OFF_NO"));
                            row.setBldg(rs.getString("BLDG"));
                            row.setRoad(rs.getString("ROAD"));
                            row.setAreaCity(rs.getString("AREA_CITY"));
                            row.setState(rs.getString("STATE"));
                            row.setCountryPoid(rs.getObject("COUNTRY_POID") != null ? rs.getLong("COUNTRY_POID") : null);
                            row.setLandMark(rs.getString("LAND_MARK"));
                            rows.add(row);
                        }
                    }
                }

                return rows;
            } catch (SQLException ex) {
                throw new CustomException("Error calling PROC_NEW_ADDRESS_LOADLIST: " + ex.getMessage());
            }
        });
    }

    /**
     * PROC_NEW_ADDRESS_CREATE_UPDATE
     * Create/Update a temporary "new address" record for a document (GLOBAL_NEW_ADDRESS_DETAILS)
     * Legacy keys for Sales Quotation (SCH): DocId=350-101, DocFieldName=CustomerPoid
     */
    public  TempAddressProcedureResponse callNewTempAddressCreateUpdateProc(Long loginGroupPoid,
                                                                                  Long loginUserPoid,
                                                                                  String docId,
                                                                                  Long docKeyPoid,
                                                                                  String docFieldName,
                                                                                  String addressName,
                                                                                  Long newAddressPoid,
                                                                                  String offTel1,
                                                                                  String offTel2,
                                                                                  String contactPerson,
                                                                                  String designation,
                                                                                  String mobile,
                                                                                  String fax,
                                                                                  String email1,
                                                                                  String email2,
                                                                                  String website,
                                                                                  String poBox,
                                                                                  String offNo,
                                                                                  String bldg,
                                                                                  String road,
                                                                                  String areaCity,
                                                                                  String state,
                                                                                  Long countryPoid,
                                                                                  String landMark,
                                                                                  String action) {
        String proc = "{call PROC_NEW_ADDRESS_CREATE_UPDATE(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";

        return jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall(proc)) {
                cs.setLong(1, loginGroupPoid);
                cs.setLong(2, loginUserPoid);
                cs.setString(3, docId);
                cs.setLong(4, docKeyPoid);
                cs.setString(5, docFieldName);

                cs.setString(6, addressName);
                if (newAddressPoid == null) {
                    cs.setNull(7, Types.NUMERIC);
                } else {
                    cs.setLong(7, newAddressPoid);
                }
                cs.setString(8, offTel1);
                cs.setString(9, offTel2);

                cs.setString(10, contactPerson);
                cs.setString(11, designation);
                cs.setString(12, mobile);
                cs.setString(13, fax);

                cs.setString(14, email1);
                cs.setString(15, email2);
                cs.setString(16, website);
                cs.setString(17, poBox);

                cs.setString(18, offNo);
                cs.setString(19, bldg);
                cs.setString(20, road);
                cs.setString(21, areaCity);

                cs.setString(22, state);
                if (countryPoid == null) {
                    cs.setNull(23, Types.NUMERIC);
                } else {
                    cs.setLong(23, countryPoid);
                }
                cs.setString(24, landMark);
                cs.setString(25, action);

                cs.registerOutParameter(26, Types.VARCHAR); // P_ACTION_RESULT
                cs.registerOutParameter(27, Types.NUMERIC); // P_RESULT_NEW_ADDRESS_POID

                cs.execute();

                String result = cs.getString(26);
                BigDecimal resultNewAddressPoid = (BigDecimal) cs.getObject(27);

                TempAddressProcedureResponse response = new TempAddressProcedureResponse();
                if (result != null && !result.trim().isEmpty() && result.toUpperCase().contains("ERROR")) {
                    response.setSuccess(false);
                    response.setErrorMessage(result);
                    response.setMessage("Temp address save failed");
                    response.setNewAddressPoid(null);
                } else {
                    response.setSuccess(true);
                    response.setMessage(result != null ? result : "Temp address saved successfully");
                    response.setErrorMessage(null);
                    response.setNewAddressPoid(resultNewAddressPoid != null ? resultNewAddressPoid.longValue() : null);
                }

                return response;
            } catch (SQLException ex) {
                throw new CustomException("Error calling PROC_NEW_ADDRESS_CREATE_UPDATE: " + ex.getMessage());
            }
        });
    }
}
