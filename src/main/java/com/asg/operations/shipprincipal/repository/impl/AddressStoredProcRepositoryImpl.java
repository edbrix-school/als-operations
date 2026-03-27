package com.asg.operations.shipprincipal.repository.impl;

import com.asg.operations.shipprincipal.dto.AddressDetailsDTO;
import com.asg.operations.shipprincipal.dto.AddressLoadListResponse;
import com.asg.operations.shipprincipal.repository.AddressStoredProcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AddressStoredProcRepositoryImpl implements AddressStoredProcRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public AddressLoadListResponse callAddressLoadListProc(BigDecimal groupPoid, BigDecimal addressMasterPoid) {

        String proc = "{call PROC_ADDRESS_LOADLIST(?, ?, ?)}";

        return jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall(proc)) {

                cs.setBigDecimal(1, groupPoid);
                cs.setBigDecimal(2, addressMasterPoid);
                cs.registerOutParameter(3, Types.REF_CURSOR);

                cs.execute();

                List<AddressDetailsDTO> addresses = new ArrayList<>();
                BigDecimal masterCountryPoid = null;

                try (ResultSet rs = (ResultSet) cs.getObject(3)) {
                    if (rs != null) {
                        while (rs.next()) {
                            AddressDetailsDTO item = new AddressDetailsDTO();

                            BigDecimal addressPoid = rs.getBigDecimal("ADDRESS_POID");
                            item.setAddressPoid(addressPoid != null ? addressPoid.toPlainString() : null);
                            item.setAddressType(rs.getString("ADDRESS_TYPE"));
                            item.setArea(rs.getString("AREA_CITY"));
                            item.setBldg(rs.getString("BLDG"));
                            item.setContactPerson(rs.getString("CONTACT_PERSON"));
                            item.setDesignation(rs.getString("DESIGNATION"));

                            List<String> emails = new ArrayList<>();
                            String email1 = rs.getString("EMAIL1");
                            String email2 = rs.getString("EMAIL2");
                            if (email1 != null && !email1.isBlank()) emails.add(email1);
                            if (email2 != null && !email2.isBlank()) emails.add(email2);
                            item.setEmail(emails);

                            item.setFax(rs.getString("FAX"));
                            item.setLandMark(rs.getString("LAND_MARK"));
                            item.setMobile(rs.getString("MOBILE"));
                            item.setOffNo(rs.getString("OFF_NO"));
                            item.setOffTel1(rs.getString("OFF_TEL1"));
                            item.setOffTel2(rs.getString("OFF_TEL2"));
                            item.setPoBox(rs.getString("PO_BOX"));
                            item.setRoad(rs.getString("ROAD"));

                            String state = rs.getString("STATE");
                            if (state != null && !state.isBlank()) {
                                item.setState(Arrays.stream(state.split(","))
                                        .map(String::trim)
                                        .filter(s -> !s.isEmpty())
                                        .toList());
                            } else {
                                item.setState(List.of());
                            }

                            item.setWebsite(rs.getString("WEBSITE"));
                            item.setVerified(rs.getString("VERIFIED"));
                            item.setVerifiedBy(rs.getString("VERIFIED_BY"));
                            item.setVerifiedDate(parseDate(rs.getString("VERIFIED_DATE")));

                            if (rs.getObject("MASTER_COUNTRY_POID") != null) {
                                masterCountryPoid = rs.getBigDecimal("MASTER_COUNTRY_POID");
                            }

                            addresses.add(item);
                        }
                    }
                }

                return new AddressLoadListResponse(addresses, masterCountryPoid);

            } catch (SQLException ex) {
                throw new RuntimeException("Error calling PROC_ADDRESS_LOADLIST: " + ex.getMessage(), ex);
            }
        });
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        for (DateTimeFormatter fmt : List.of(
                DateTimeFormatter.ofPattern("dd-MMM-yyyy"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"))) {
            try {
                return LocalDate.parse(dateStr, fmt);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }
}
