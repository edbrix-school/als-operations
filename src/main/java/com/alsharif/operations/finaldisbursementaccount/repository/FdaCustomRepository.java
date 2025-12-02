package com.alsharif.operations.finaldisbursementaccount.repository;

import com.alsharif.operations.finaldisbursementaccount.dto.FdaSupplementaryInfoDto;
import com.alsharif.operations.finaldisbursementaccount.dto.PartyGlResponse;

import java.util.List;

public interface FdaCustomRepository {

    String closeFda(Long groupPoid, Long companyPoid, Long userPoid, Long fdaPoid);

    String reopenFda(Long groupPoid, Long companyPoid, Long userPoid, Long fdaPoid, String comment);

    String submitFda(Long groupPoid, Long companyPoid, Long userPoid, Long fdaPoid);

    String verifyFda(Long groupPoid, Long companyPoid, Long userPoid, Long fdaPoid);

    String returnFda(Long groupPoid, Long companyPoid, Long userPoid, Long fdaPoid, String correctionRemarks);

    String supplementaryFda(Long groupPoid, Long companyPoid, Long userPoid, Long fdaPoid);

    List<FdaSupplementaryInfoDto> getSupplementaryInfo(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid);

    String closeFdaWithoutAmount(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid, String closedRemark);

    PartyGlResponse getPartyGl(Long groupPoid, Long companyPoid, Long userPoid, Long partyPoid, String partyType);

    String createFdaFromPda(Long groupPoid, Long companyPoid, Long userPoid, Long pdaTransactionPoid);
}
