package com.asg.operations.finaldisbursementaccount.repository;

import com.asg.operations.finaldisbursementaccount.dto.FdaSupplementaryInfoDto;
import com.asg.operations.finaldisbursementaccount.dto.PartyGlResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Repository
public class FdaCustomRepositoryImpl implements FdaCustomRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public String closeFda(Long groupPoid, Long companyPoid, Long userPoid, Long fdaPoid) {

        String docRef = (String) em.createNativeQuery(
                        "SELECT DOC_REF FROM PDA_FDA_HDR WHERE TRANSACTION_POID = :poid")
                .setParameter("poid", fdaPoid)
                .getSingleResult();

        StoredProcedureQuery query = em.createStoredProcedureQuery("PROC_PDA_FDA_FOR_CLOSING");

        query.registerStoredProcedureParameter("P_LOGIN_GROUP_POID", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("P_LOGIN_COMPANY_POID", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("P_LOGIN_USER_POID", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("P_DOC_ID", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("P_TRANSACTION_POID", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("P_DOC_REF", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("P_STATUS", String.class, ParameterMode.OUT);

        query.setParameter("P_LOGIN_GROUP_POID", groupPoid);
        query.setParameter("P_LOGIN_COMPANY_POID", companyPoid);
        query.setParameter("P_LOGIN_USER_POID", userPoid);
        query.setParameter("P_DOC_ID", "110-161");
        query.setParameter("P_TRANSACTION_POID", fdaPoid);
        query.setParameter("P_DOC_REF", docRef);

        query.execute();

        return (String) query.getOutputParameterValue("P_STATUS");
    }

    @Override
    public String reopenFda(Long groupPoid, Long companyPoid, Long userPoid, Long fdaPoid, String comment) {

        StoredProcedureQuery query = em.createStoredProcedureQuery("PROC_PDA_FDA_REOPEN");

        query.registerStoredProcedureParameter("P_LOGIN_GROUP_POID", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("P_LOGIN_COMPANY_POID", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("P_LOGIN_USER_POID", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("P_FDA_POID", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("P_COMMENT", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("P_RESULT", String.class, ParameterMode.OUT);

        query.setParameter("P_LOGIN_GROUP_POID", groupPoid);
        query.setParameter("P_LOGIN_COMPANY_POID", companyPoid);
        query.setParameter("P_LOGIN_USER_POID", userPoid);
        query.setParameter("P_FDA_POID", fdaPoid);
        query.setParameter("P_COMMENT", comment);

        query.execute();

        return (String) query.getOutputParameterValue("P_RESULT");
    }

    @Override
    public String submitFda(Long groupPoid, Long companyPoid, Long userPoid, Long fdaPoid) {

        StoredProcedureQuery query = em.createStoredProcedureQuery("PROC_FDA_TO_PDA_DOC_SUBMISSION");

        query.registerStoredProcedureParameter("P_LOGIN_GROUP_POID", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("P_LOGIN_COMPANY_POID", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("P_LOGIN_USER_POID", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("P_FDA_POID", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("P_RESULT", String.class, ParameterMode.OUT);
        query.registerStoredProcedureParameter("OUTDATA", Class.class, ParameterMode.REF_CURSOR);

        query.setParameter("P_LOGIN_GROUP_POID", groupPoid);
        query.setParameter("P_LOGIN_COMPANY_POID", companyPoid);
        query.setParameter("P_LOGIN_USER_POID", userPoid);
        query.setParameter("P_FDA_POID", fdaPoid);

        query.execute();

        return (String) query.getOutputParameterValue("P_RESULT");
    }

    @Override
    public String verifyFda(Long groupPoid, Long companyPoid, Long userPoid, Long fdaPoid) {

        StoredProcedureQuery query = em.createStoredProcedureQuery("PROC_FDA_VERIFY_THE_PDA_DOCS");

        query.registerStoredProcedureParameter("P_LOGIN_GROUP_POID", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("P_LOGIN_COMPANY_POID", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("P_LOGIN_USER_POID", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("P_FDA_POID", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("P_RESULT", String.class, ParameterMode.OUT);
        query.registerStoredProcedureParameter("OUTDATA", Object.class, ParameterMode.REF_CURSOR);

        query.setParameter("P_LOGIN_GROUP_POID", groupPoid);
        query.setParameter("P_LOGIN_COMPANY_POID", companyPoid);
        query.setParameter("P_LOGIN_USER_POID", userPoid);
        query.setParameter("P_FDA_POID", fdaPoid);

        query.execute();

        return (String) query.getOutputParameterValue("P_RESULT");
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public String returnFda(Long groupPoid, Long companyPoid, Long userPoid, Long fdaPoid, String correctionRemarks) {

        StoredProcedureQuery query = em.createStoredProcedureQuery("PROC_FDA_REJECT_THE_PDA_DOCS");

        query.registerStoredProcedureParameter("P_LOGIN_GROUP_POID", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("P_LOGIN_COMPANY_POID", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("P_LOGIN_USER_POID", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("P_FDA_POID", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("P_CORRECTION_REMARKS", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("P_RESULT", String.class, ParameterMode.OUT);
        query.registerStoredProcedureParameter("OUTDATA", Object.class, ParameterMode.REF_CURSOR);

        query.setParameter("P_LOGIN_GROUP_POID", groupPoid);
        query.setParameter("P_LOGIN_COMPANY_POID", companyPoid);
        query.setParameter("P_LOGIN_USER_POID", userPoid);
        query.setParameter("P_FDA_POID", fdaPoid);
        query.setParameter("P_CORRECTION_REMARKS", correctionRemarks);

        query.execute();

        return (String) query.getOutputParameterValue("P_RESULT");
    }


    @Override
    public String supplementaryFda(Long groupPoid, Long companyPoid, Long userPoid, Long fdaPoid) {

        StoredProcedureQuery query = em.createStoredProcedureQuery("PROC_PDA_SUPPLEMENTARY_FDA");

        query.registerStoredProcedureParameter("P_TRANSACTION_POID", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("P_LOGIN_USER_POID", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("P_STATUS", String.class, ParameterMode.OUT);

        query.setParameter("P_TRANSACTION_POID", fdaPoid);
        query.setParameter("P_LOGIN_USER_POID", userPoid);

        query.execute();

        return (String) query.getOutputParameterValue("P_STATUS");
    }

    @Override
    public List<FdaSupplementaryInfoDto> getSupplementaryInfo(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid) {

        StoredProcedureQuery query = em.createStoredProcedureQuery("PROC_FDA_SUPPLEMENTARY_DTL")
                .registerStoredProcedureParameter("P_LOGIN_GROUP_POID", Long.class, ParameterMode.IN)
                .registerStoredProcedureParameter("P_LOGIN_COMPANY_POID", Long.class, ParameterMode.IN)
                .registerStoredProcedureParameter("P_LOGIN_USER_POID", Long.class, ParameterMode.IN)
                .registerStoredProcedureParameter("P_DOC_ID", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("P_DOC_KEY_POID", Long.class, ParameterMode.IN)
                .registerStoredProcedureParameter("OUTDATA", Object.class, ParameterMode.REF_CURSOR)
                .setParameter("P_LOGIN_GROUP_POID", groupPoid)
                .setParameter("P_LOGIN_COMPANY_POID", companyPoid)
                .setParameter("P_LOGIN_USER_POID", userPoid)
                .setParameter("P_DOC_ID", "110-161") // or pass a real value if needed
                .setParameter("P_DOC_KEY_POID", transactionPoid);

        query.execute();

        @SuppressWarnings("unchecked")
        List<Object> results = query.getResultList(); // Use Object instead of Object[]

        List<FdaSupplementaryInfoDto> dtos = new ArrayList<>();
        if (results != null) {
            for (Object row : results) {
                if (row == null) {
                    dtos.add(new FdaSupplementaryInfoDto(null));
                    continue;
                }

                String value;

                if (row instanceof Object[] r) { // multi-column row
                    value = r[0] != null ? r[0].toString() : null;
                } else { // single-column row (String)
                    value = row.toString();
                }

                dtos.add(new FdaSupplementaryInfoDto(value));
            }
        }

        return dtos;
    }


    @Override
    @Transactional
    public String closeFdaWithoutAmount(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid, String closedRemark) {

        StoredProcedureQuery query = em.createStoredProcedureQuery("PROC_FDA_CLOSED_WITHOUT_AMT")
                .registerStoredProcedureParameter("P_LOGIN_GROUP_POID", Long.class, ParameterMode.IN)
                .registerStoredProcedureParameter("P_LOGIN_COMPANY_POID", Long.class, ParameterMode.IN)
                .registerStoredProcedureParameter("P_LOGIN_USER_POID", Long.class, ParameterMode.IN)
                .registerStoredProcedureParameter("P_FDA_POID", Long.class, ParameterMode.IN)
                .registerStoredProcedureParameter("P_CLOSED_REMARK", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("P_RESULT", String.class, ParameterMode.OUT)
                .setParameter("P_LOGIN_GROUP_POID", groupPoid)
                .setParameter("P_LOGIN_COMPANY_POID", companyPoid)
                .setParameter("P_LOGIN_USER_POID", userPoid)
                .setParameter("P_FDA_POID", transactionPoid)
                .setParameter("P_CLOSED_REMARK", closedRemark);

        query.execute();
        return (String) query.getOutputParameterValue("P_RESULT");
    }

    @Override
    public PartyGlResponse getPartyGl(Long groupPoid, Long companyPoid, Long userPoid, Long partyPoid, String partyType) {

        StoredProcedureQuery query = em.createStoredProcedureQuery("PROC_GL_GET_DR_PARTY_GLPOID")
                .registerStoredProcedureParameter("P_LOGIN_GROUP_POID", Long.class, ParameterMode.IN)
                .registerStoredProcedureParameter("P_LOGIN_COMPANY_POID", Long.class, ParameterMode.IN)
                .registerStoredProcedureParameter("P_LOGIN_USER_POID", Long.class, ParameterMode.IN)
                .registerStoredProcedureParameter("P_PARTY_POID", Long.class, ParameterMode.IN)
                .registerStoredProcedureParameter("P_PARTY_TYPE", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("P_PARTY_GLPOID", Long.class, ParameterMode.OUT)
                .registerStoredProcedureParameter("P_CREDIT_PERIOD", String.class, ParameterMode.OUT)
                .setParameter("P_LOGIN_GROUP_POID", groupPoid)
                .setParameter("P_LOGIN_COMPANY_POID", companyPoid)
                .setParameter("P_LOGIN_USER_POID", userPoid)
                .setParameter("P_PARTY_POID", partyPoid)
                .setParameter("P_PARTY_TYPE", partyType.toUpperCase());

        query.execute();

        Long glPoid = (Long) query.getOutputParameterValue("P_PARTY_GLPOID");
        String creditPeriodStr = (String) query.getOutputParameterValue("P_CREDIT_PERIOD");
        Integer creditPeriod = creditPeriodStr != null ? Integer.valueOf(creditPeriodStr) : 0;

        return new PartyGlResponse(glPoid, creditPeriod);
    }

    @Override
    public String createFdaFromPda(Long groupPoid, Long companyPoid, Long userPoid, Long pdaTransactionPoid) {

        StoredProcedureQuery query = em.createStoredProcedureQuery("PROC_PDA_FDA_CREATE_FROM_PDA")
                .registerStoredProcedureParameter("P_LOGIN_GROUP_POID", Long.class, ParameterMode.IN)
                .registerStoredProcedureParameter("P_LOGIN_COMPANY_POID", Long.class, ParameterMode.IN)
                .registerStoredProcedureParameter("P_LOGIN_USER_POID", Long.class, ParameterMode.IN)
                .registerStoredProcedureParameter("P_PDA_POID", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("P_RESULT", String.class, ParameterMode.OUT)

                .setParameter("P_LOGIN_GROUP_POID", groupPoid)
                .setParameter("P_LOGIN_COMPANY_POID", companyPoid)
                .setParameter("P_LOGIN_USER_POID", userPoid)
                .setParameter("P_PDA_POID", pdaTransactionPoid.toString());

        query.execute();

        return (String) query.getOutputParameterValue("P_RESULT");
    }
}
