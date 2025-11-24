package com.alsharif.operations.shipprincipal.service;

import com.alsharif.operations.shipprincipal.dto.CreateLedgerResult;
import com.alsharif.operations.shipprincipal.entity.ShipPrincipalMaster;
import com.alsharif.operations.shipprincipal.repository.ShipPrincipalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

@Service
@RequiredArgsConstructor
@Slf4j
public class GLMasterServiceImpl implements GLMasterService {

    private final ShipPrincipalRepository principalRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public CreateLedgerResult createGlMaster(Long groupPoid,
                                             Long companyPoid,
                                             Long principalId,
                                             String userName) {

        log.debug("Calling GL create procedure for principal: {}", principalId);
        ShipPrincipalMaster principal = principalRepository.findById(principalId).orElseThrow();

        String sql = "{call PROC_GL_MASTER_CREATE(?, ?, ?, ?, ?, ?, ?, ?)}";

        return jdbcTemplate.execute((Connection connection) -> {
            try (CallableStatement cs = connection.prepareCall(sql)) {
                cs.setBigDecimal(1, BigDecimal.valueOf(groupPoid));
                cs.setBigDecimal(2, BigDecimal.valueOf(companyPoid));
                cs.setString(3, userName);
                cs.setString(4, principal.getPrincipalCode());
                cs.setString(5, principal.getPrincipalName());
                cs.setString(6, "TRADE_PRINCIPAL");
                cs.registerOutParameter(7, Types.VARCHAR);
                cs.registerOutParameter(8, Types.NUMERIC);

                cs.execute();

                String status = cs.getString(7);
                Long newGlPoid = cs.getLong(8);

                String glAcctno = null;
                if (newGlPoid != null) {
                    glAcctno = getGlAcctno(newGlPoid);
                }

                return new CreateLedgerResult(status, newGlPoid, glAcctno);
            } catch (SQLException e) {
                log.error("Error calling GL procedure for principal: {}", principalId, e);
                throw new RuntimeException("Error calling PROC_GL_MASTER_CREATE: " + e.getMessage(), e);
            }
        });
    }

    private String getGlAcctno(Long glPoid) {
        try {
            String sql = "SELECT GL_ACCTNO FROM GL_MASTER WHERE GL_CODE_POID = ?";
            return jdbcTemplate.queryForObject(sql, String.class, glPoid);
        } catch (Exception e) {
            log.warn("Could not fetch GL_ACCTNO for GL_CODE_POID: {}", glPoid);
            return null;
        }
    }
}
