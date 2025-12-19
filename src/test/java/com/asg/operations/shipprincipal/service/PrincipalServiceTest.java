package com.asg.operations.shipprincipal.service;

import com.asg.operations.shipprincipal.dto.*;
import com.asg.operations.shipprincipal.entity.ShipPrincipalMaster;
import com.asg.operations.shipprincipal.repository.*;
import com.asg.operations.shipprincipal.util.PrincipalMasterMapper;
import com.asg.operations.commonlov.service.LovService;
import com.asg.operations.vesseltype.repository.VesselTypeRepository;
import com.asg.operations.user.entity.User;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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
    void testGetAllPrincipalsWithFilters_Success() {
        GetAllPrincipalFilterRequest filterRequest = new GetAllPrincipalFilterRequest();
        filterRequest.setIsDeleted("N");

        jakarta.persistence.Query mockQuery = mock(jakarta.persistence.Query.class);
        jakarta.persistence.Query mockCountQuery = mock(jakarta.persistence.Query.class);

        when(entityManager.createNativeQuery(anyString())).thenReturn(mockQuery).thenReturn(mockCountQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockCountQuery.setParameter(anyString(), any())).thenReturn(mockCountQuery);
        when(mockQuery.setFirstResult(anyInt())).thenReturn(mockQuery);
        when(mockQuery.setMaxResults(anyInt())).thenReturn(mockQuery);
        when(mockCountQuery.getSingleResult()).thenReturn(1L);

        Object[] mockRow = new Object[29];
        mockRow[0] = 1L; // PRINCIPAL_POID (Number)
        mockRow[1] = "PRIN001"; // PRINCIPAL_CODE (String)
        mockRow[2] = "Test Principal"; // PRINCIPAL_NAME (String)
        mockRow[3] = "Test Principal 2"; // PRINCIPAL_NAME2 (String)
        mockRow[4] = 100L; // GROUP_POID (Number)
        mockRow[5] = 200L; // COMPANY_POID (Number)
        mockRow[6] = "Test Group"; // GROUP_NAME (String)
        mockRow[7] = 1L; // COUNTRY_POID (Number)
        mockRow[8] = 1L; // ADDRESS_POID (Number)
        mockRow[9] = 30L; // CREDIT_PERIOD (Number)
        mockRow[10] = 45L; // AGREED_PERIOD (Number)
        mockRow[11] = "USD"; // CURRENCY_CODE (String)
        mockRow[12] = new java.math.BigDecimal("1.0"); // CURRENCY_RATE (BigDecimal)
        mockRow[13] = new java.math.BigDecimal("1.0"); // BUYING_RATE (BigDecimal)
        mockRow[14] = new java.math.BigDecimal("1.0"); // SELLING_RATE (BigDecimal)
        mockRow[15] = 1L; // GL_CODE_POID (Number)
        mockRow[16] = "GL001"; // GL_ACCTNO (String)
        mockRow[17] = "TIN123"; // TIN_NUMBER (String)
        mockRow[18] = "SLAB1"; // TAX_SLAB (String)
        mockRow[19] = "Exemption reason"; // EXEMPTION_REASON (String)
        mockRow[20] = "Test remarks"; // REMARKS (String)
        mockRow[21] = 1; // SEQNO (Number)
        mockRow[22] = "Y"; // ACTIVE (String)
        mockRow[23] = "OLD001"; // PRINCIPAL_CODE_OLD (String)
        mockRow[24] = "N"; // DELETED (String)
        mockRow[25] = "testuser"; // CREATED_BY (String)
        mockRow[26] = new java.sql.Timestamp(System.currentTimeMillis()); // CREATED_DATE (Timestamp)
        mockRow[27] = "testuser"; // LASTMODIFIED_BY (String)
        mockRow[28] = new java.sql.Timestamp(System.currentTimeMillis()); // LASTMODIFIED_DATE (Timestamp)

        java.util.List<Object[]> mockResults = new java.util.ArrayList<>();
        mockResults.add(mockRow);
        when(mockQuery.getResultList()).thenReturn(mockResults);

        Page<PrincipalListResponse> result = principalMasterService.getAllPrincipalsWithFilters(
                100L, filterRequest, 0, 10, "principalName,asc");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
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
        when(principalRepository.save(any(ShipPrincipalMaster.class))).thenReturn(mockPrincipal);

        principalMasterService.deletePrincipal(1L);

        verify(principalRepository).findByIdAndNotDeleted(1L);
        verify(principalRepository).save(any(ShipPrincipalMaster.class));
    }
}