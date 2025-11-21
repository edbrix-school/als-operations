package com.alsharif.operations.shipprincipal.service;

import com.alsharif.operations.shipprincipal.dto.*;
import com.alsharif.operations.shipprincipal.entity.*;
import com.alsharif.operations.shipprincipal.repository.*;
import com.alsharif.operations.shipprincipal.util.PrincipalMasterMapper;
import com.alsharif.operations.user.entity.User;
import com.alsharif.operations.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import javax.sql.DataSource;
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
    private AddressMasterRepository addressMasterRepository;
    
    @Mock
    private AddressDetailsRepository addressDetailsRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private DataSource dataSource;
    
    @Mock
    private PrincipalMasterMapper mapper;
    
    @InjectMocks
    private PrincipalMasterService principalMasterService;
    
    private ShipPrincipalMaster mockPrincipal;
    private User mockUser;
    
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
        
        mockUser = new User();
        mockUser.setUserPoid(1L);
        mockUser.setUserName("testuser");
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
        
        PrincipalMasterDto result = principalMasterService.getPrincipal(1L);
        
        assertNotNull(result);
        assertEquals(1L, result.getPrincipalPoid());
        assertEquals("PRIN001", result.getPrincipalCode());
        assertEquals("Test Principal", result.getPrincipalName());
        verify(principalRepository).findById(1L);
    }
    
    @Test
    void testGetPrincipal_NotFound() {
        when(principalRepository.findById(999L)).thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> principalMasterService.getPrincipal(999L));
        verify(principalRepository).findById(999L);
    }
    
    @Test
    void testGetPrincipal_WithAddressDetails() {
        mockPrincipal.setAddressPoid(200L);
        AddressDetails addressDetail = new AddressDetails();
        addressDetail.setContactPerson("John Doe");
        addressDetail.setEmail1("john@example.com");
        
        PrincipalMasterDto mockDto = new PrincipalMasterDto();
        mockDto.setPrincipalPoid(1L);
        
        AddressDetailsDTO addressDto = new AddressDetailsDTO();
        addressDto.setContactPerson("John Doe");
        
        when(principalRepository.findById(1L)).thenReturn(Optional.of(mockPrincipal));
        when(mapper.mapToDetailDTO(any(ShipPrincipalMaster.class))).thenReturn(mockDto);
        when(chargeRepository.findByPrincipalPoidOrderByDetRowIdAsc(1L)).thenReturn(Arrays.asList());
        when(paymentRepository.findByPrincipalPoidOrderByDetRowIdAsc(1L)).thenReturn(Arrays.asList());
        when(addressDetailsRepository.findByAddressMasterPoid(200L)).thenReturn(Arrays.asList(addressDetail));
        when(mapper.mapToAddressDetailDTO(any(AddressDetails.class))).thenReturn(addressDto);
        
        PrincipalMasterDto result = principalMasterService.getPrincipal(1L);
        
        assertNotNull(result);
        assertNotNull(result.getAddressDetails());
        assertEquals(1, result.getAddressDetails().size());
        assertEquals("John Doe", result.getAddressDetails().get(0).getContactPerson());
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
    
    @Test
    void testGetPrincipalList_Success() {
        Page<ShipPrincipalMaster> mockPage = new PageImpl<>(Arrays.asList(mockPrincipal));
        PrincipalMasterListDto mockListDto = new PrincipalMasterListDto();
        mockListDto.setPrincipalPoid(1L);
        
        when(principalRepository.findAllNonDeletedWithSearch(any(), any(PageRequest.class))).thenReturn(mockPage);
        when(mapper.toListDto(any(ShipPrincipalMaster.class))).thenReturn(mockListDto);
        
        Page result = principalMasterService.getPrincipalList(null, PageRequest.of(0, 20));
        
        assertNotNull(result);
        verify(principalRepository).findAllNonDeletedWithSearch(any(), any(PageRequest.class));
    }
}
