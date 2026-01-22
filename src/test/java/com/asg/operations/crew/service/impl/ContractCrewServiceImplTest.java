package com.asg.operations.crew.service.impl;

import com.asg.operations.crew.dto.*;
import com.asg.operations.crew.entity.ContractCrew;
import com.asg.operations.crew.entity.ContractCrewDtl;
import com.asg.operations.crew.entity.ContractCrewDtlId;
import com.asg.operations.crew.repository.ContractCrewDtlRepository;
import com.asg.operations.crew.repository.ContractCrewRepository;
import com.asg.operations.crew.util.CrewCodeGenerator;
import com.asg.operations.crew.util.EntityMapper;
import com.asg.operations.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContractCrewServiceImplTest {

    @Mock
    private ContractCrewRepository crewRepository;

    @Mock
    private ContractCrewDtlRepository crewDtlRepository;

    @Mock
    private EntityMapper entityMapper;

    @Mock
    private CrewCodeGenerator codeGenerator;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private ContractCrewServiceImpl service;

    @Test
    @DisplayName("getCrewById returns mapped response when found")
    void getCrewById_ok() {
        ContractCrew entity = new ContractCrew();
        ContractCrewResponse mapped = new ContractCrewResponse();
        when(crewRepository.findByCrewPoid(10L)).thenReturn(Optional.of(entity));
        when(entityMapper.toContractCrewRes(entity)).thenReturn(mapped);

        ContractCrewResponse res = service.getCrewById(10L);

        assertNotNull(res);
        assertSame(mapped, res);
    }

    @Test
    @DisplayName("getCrewById throws when not found")
    void getCrewById_notFound() {
        when(crewRepository.findByCrewPoid(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getCrewById(99L));
    }

    @Test
    @DisplayName("deleteCrewDetail deletes when crew and detail exist")
    void deleteCrewDetail_ok() {
        long companyPoid = 1L, crewPoid = 2L, detRowId = 3L;
        when(crewRepository.findByCrewPoidAndCompanyPoid(crewPoid, companyPoid)).thenReturn(Optional.of(new ContractCrew()));
        ContractCrewDtlId id = new ContractCrewDtlId(crewPoid, detRowId);
        ContractCrewDtl dtl = new ContractCrewDtl();
        dtl.setId(id);
        when(crewDtlRepository.findByIdCrewPoidAndIdDetRowId(crewPoid, detRowId)).thenReturn(dtl);

        service.deleteCrewDetail(companyPoid, crewPoid, detRowId);

        then(crewDtlRepository).should().deleteById(id);
    }

    @Test
    @DisplayName("deleteCrewDetail throws when crew not found")
    void deleteCrewDetail_crewNotFound() {
        when(crewRepository.findByCrewPoidAndCompanyPoid(2L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.deleteCrewDetail(1L, 2L, 3L));
    }

    @Test
    @DisplayName("getCrewDetails returns mapped details when crew exists")
    void getCrewDetails_ok() {
        long companyPoid = 1L, crewPoid = 2L;
        when(crewRepository.findByCrewPoidAndCompanyPoid(crewPoid, companyPoid)).thenReturn(Optional.of(new ContractCrew()));
        ContractCrewDtl d = new ContractCrewDtl();
        d.setId(new ContractCrewDtlId(crewPoid, 10L));
        when(crewDtlRepository.findByIdCrewPoidOrderByIdDetRowId(crewPoid)).thenReturn(List.of(d));
        // mapper used internally; we don't need to assert mapped fields for this smoke test
        when(entityMapper.toContractCrewDtlResponse(any(ContractCrewDtl.class))).thenReturn(new ContractCrewDtlResponse());

        CrewDetailsResponse res = service.getCrewDetails(companyPoid, crewPoid);

        assertEquals(crewPoid, res.getCrewPoid());
        assertEquals(1, res.getDetails().size());
    }
}