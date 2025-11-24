package com.alsharif.operations.pdaporttariffmaster.util;

import com.alsharif.operations.pdaporttariffmaster.repository.PdaPortTariffHdrRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class PortTariffDocumentRefGenerator {

    private final PdaPortTariffHdrRepository tariffHdrRepository;

    public String generateDocRef(BigDecimal groupPoid) {
        String prefix = "ASG-";
        Integer maxSeq = tariffHdrRepository.findMaxSequence(prefix, groupPoid);
        int nextSeq = (maxSeq == null) ? 1 : maxSeq + 1;
        return prefix + nextSeq;
    }
}