package com.asg.operations.pdaratetypemaster.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.operations.common.Util.FormulaValidator;
import com.asg.operations.pdaratetypemaster.dto.PdaRateTypeRequestDTO;
import com.asg.operations.pdaratetypemaster.dto.PdaRateTypeResponseDTO;
import com.asg.operations.pdaratetypemaster.entity.PdaRateTypeMaster;
import com.asg.operations.pdaratetypemaster.repository.PdaRateTypeRepository;
import com.asg.operations.pdaratetypemaster.util.PdaRateTypeMapper;
import com.asg.operations.exceptions.ResourceNotFoundException;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PdaRateTypeServiceImplTest {

    @Mock
    private PdaRateTypeRepository repository;
    
    @Mock
    private PdaRateTypeMapper mapper;
    
    @Mock
    private FormulaValidator formulaValidator;

    @Mock
    private com.asg.common.lib.service.DocumentDeleteService documentDeleteService;

    @Mock
    private com.asg.common.lib.service.LoggingService loggingService;

    @InjectMocks
    private PdaRateTypeServiceImpl service;

    private PdaRateTypeRequestDTO requestDTO;
    private PdaRateTypeMaster entity;

    @BeforeEach
    void setup() {
        requestDTO = new PdaRateTypeRequestDTO();
        requestDTO.setRateTypeCode("GRT");
        requestDTO.setRateTypeName("Gross Tonnage Rate");
        requestDTO.setRateTypeFormula("GRT * 0.5");
        requestDTO.setDefQty("GRT");
        requestDTO.setDefDays(BigDecimal.ONE);
        requestDTO.setSeqNo(BigInteger.ONE);
        requestDTO.setActive("Y");

        entity = PdaRateTypeMaster.builder()
                .rateTypePoid(1L)
                .groupPoid(1L)
                .rateTypeCode("GRT")
                .rateTypeName("Gross Tonnage Rate")
                .rateTypeFormula("GRT * 0.5")
                .defQty("GRT")
                .defDays(BigDecimal.ONE)
                .seqno(BigInteger.ONE)
                .active("Y")
                .deleted("N")
                .createdBy("SYSTEM")
                .createdDate(LocalDateTime.now())
                .build();
    }

    @Test
    void testCreate_Success() {
        when(repository.existsByRateTypeCodeAndGroupPoid("GRT", BigDecimal.ONE)).thenReturn(false);
        when(repository.existsByRateTypeNameAndGroupPoid("Gross Tonnage Rate", BigDecimal.ONE)).thenReturn(false);
        when(formulaValidator.validate(anyString(), any())).thenReturn(
            new FormulaValidator.FormulaValidationResult(true, new ArrayList<>(), new ArrayList<>(), "GRT * 0.5", Arrays.asList("GRT"))
        );
        when(mapper.toEntity(requestDTO, BigDecimal.ONE, "testUser")).thenReturn(entity);
        when(repository.save(any(PdaRateTypeMaster.class))).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(new PdaRateTypeResponseDTO());

        PdaRateTypeResponseDTO result = service.createRateType(requestDTO, 1L, "testUser");

        assertNotNull(result);
        verify(repository).save(any(PdaRateTypeMaster.class));
    }

    @Test
    void testCreate_DuplicateCode() {
        when(repository.existsByRateTypeCodeAndGroupPoid("GRT", BigDecimal.ONE)).thenReturn(true);

        ValidationException exception = assertThrows(ValidationException.class, 
            () -> service.createRateType(requestDTO, 1L, "testUser"));
        
        assertEquals("Rate type code already exists: GRT", exception.getMessage());
    }

    @Test
    void testGetById_Success() {
        when(repository.findByRateTypePoidAndGroupPoid(1L, BigDecimal.ONE)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(new PdaRateTypeResponseDTO());

        PdaRateTypeResponseDTO result = service.getRateTypeById(1L, 1L);

        assertNotNull(result);
        verify(mapper).toResponse(entity);
    }

    @Test
    void testGetById_NotFound() {
        when(repository.findByRateTypePoidAndGroupPoid(1L, BigDecimal.ONE)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
            () -> service.getRateTypeById(1L, 1L));
        
        assertTrue(exception.getMessage().contains("rateTypePoid"));
    }

    @Test
    void testSoftDelete_Success() {
        when(repository.findByRateTypePoidAndGroupPoid(1L, BigDecimal.ONE)).thenReturn(Optional.of(entity));

        assertDoesNotThrow(() -> service.deleteRateType(1L, 1L, "testUser",  new DeleteReasonDto()));
        
        verify(documentDeleteService).deleteDocument(eq(1L), anyString(), anyString(), any(), any());
    }


}