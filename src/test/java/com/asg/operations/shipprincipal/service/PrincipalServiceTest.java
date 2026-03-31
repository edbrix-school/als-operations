package com.asg.operations.shipprincipal.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.LoggingService;
import com.asg.operations.shipprincipal.dto.*;
import com.asg.operations.shipprincipal.entity.ShipPrincipalMaster;
import com.asg.operations.shipprincipal.repository.*;
import com.asg.operations.shipprincipal.util.PrincipalMasterMapper;
import com.asg.operations.commonlov.service.LovService;
import com.asg.operations.vesseltype.repository.VesselTypeRepository;
import com.asg.operations.user.repository.UserRepository;
import com.asg.operations.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrincipalServiceTest {

    @Mock
    private ShipPrincipalRepository principalRepository;

    @Mock
    private ShipPrincipalDetailRepository chargeRepository;

    @Mock
    private ShipPrincipalPaymentDetailRepository paymentRepository;

    @Mock
    private ShipPrincipalPaRptDtlRepository paRptDtlRepository;

    @Mock
    private AddressMasterRepository addressMasterRepository;

    @Mock
    private AddressDetailsRepository addressDetailsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PrincipalMasterMapper mapper;

    @Mock
    private LovService lovService;

    @Mock
    private GLMasterService glMasterService;

    @Mock
    private AddressMasterService addressMasterService;

    @Mock
    private VesselTypeRepository vesselTypeRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private DocumentDeleteService documentDeleteService;

    @Mock
    private LoggingService loggingService;

    @InjectMocks
    private PrincipalMasterServiceImpl principalMasterService;

    private ShipPrincipalMaster mockPrincipal;

    @BeforeEach
    void setUp() {
        mockPrincipal = new ShipPrincipalMaster();
        mockPrincipal.setPrincipalPoid(1L);
        mockPrincipal.setGroupPoid(100L);
        mockPrincipal.setPrincipalCode("PRIN001");
        mockPrincipal.setPrincipalName("Test Principal");
        mockPrincipal.setActive("Y");
        mockPrincipal.setCompanyPoid(10L);
        mockPrincipal.setCreatedBy("testuser");
        mockPrincipal.setCreatedDate(LocalDateTime.now());
    }

    @Test
    void testGetPrincipal_Success() {
        PrincipalMasterDto mockDto = new PrincipalMasterDto();
        mockDto.setPrincipalPoid(1L);
        mockDto.setPrincipalCode("PRIN001");
        mockDto.setPrincipalName("Test Principal");

        when(principalRepository.findById(1L)).thenReturn(Optional.of(mockPrincipal));
        when(mapper.mapToDetailDTO(any(ShipPrincipalMaster.class))).thenReturn(mockDto);
        when(chargeRepository.findByPrincipalPoidOrderByDetRowIdAsc(1L)).thenReturn(Arrays.asList());
        when(paymentRepository.findByPrincipalPoidOrderByDetRowIdAsc(1L)).thenReturn(Arrays.asList());
        when(paRptDtlRepository.findByPrincipalPoidOrderByDetRowIdAsc(1L)).thenReturn(Arrays.asList());

        PrincipalMasterDto result = principalMasterService.getPrincipal(1L);

        assertNotNull(result);
        assertEquals(1L, result.getPrincipalPoid());
        verify(principalRepository).findById(1L);
    }

    @Test
    void testGetPrincipal_NotFound() {
        when(principalRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> principalMasterService.getPrincipal(999L));
        verify(principalRepository).findById(999L);
    }

    @Test
    void testToggleActive_Success() {
        when(principalRepository.findById(1L)).thenReturn(Optional.of(mockPrincipal));
        when(principalRepository.save(any(ShipPrincipalMaster.class))).thenReturn(mockPrincipal);

        principalMasterService.toggleActive(1L);

        verify(principalRepository).findById(1L);
        verify(principalRepository).save(any(ShipPrincipalMaster.class));
    }

    @Test
    void testDeletePrincipal_Success() {
        when(principalRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(mockPrincipal));
        when(documentDeleteService.deleteDocument(eq(1L), anyString(), anyString(), any(), any())).thenReturn("Success");

        principalMasterService.deletePrincipal(1L, new DeleteReasonDto());

        verify(principalRepository).findByIdAndNotDeleted(1L);
        verify(documentDeleteService).deleteDocument(eq(1L), anyString(), anyString(), any(), any());
    }
}