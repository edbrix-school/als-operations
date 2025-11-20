package com.alsharif.operations.shipprincipal.service;

import com.alsharif.operations.shipprincipal.dto.*;
import com.alsharif.operations.shipprincipal.entity.*;
import com.alsharif.operations.shipprincipal.repository.*;
import com.alsharif.operations.user.entity.User;
import com.alsharif.operations.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    
    @InjectMocks
    private PrincipalService principalService;
    
    private ShipPrincipalEntity mockPrincipal;
    private User mockUser;
    
    @BeforeEach
    void setUp() {
        mockPrincipal = new ShipPrincipalEntity();
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
        when(principalRepository.findById(1L)).thenReturn(Optional.of(mockPrincipal));
        when(chargeRepository.findByPrincipalPoidOrderByDetRowIdAsc(1L)).thenReturn(Arrays.asList());
        when(paymentRepository.findByPrincipalPoidOrderByDetRowIdAsc(1L)).thenReturn(Arrays.asList());
        
        PrincipalDetailDTO result = principalService.getPrincipal(1L);
        
        assertNotNull(result);
        assertEquals(1L, result.getPrincipalPoid());
        assertEquals("PRIN001", result.getPrincipalCode());
        assertEquals("Test Principal", result.getPrincipalName());
        verify(principalRepository).findById(1L);
    }
    
    @Test
    void testGetPrincipal_NotFound() {
        when(principalRepository.findById(999L)).thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> principalService.getPrincipal(999L));
        verify(principalRepository).findById(999L);
    }
    
    @Test
    void testGetPrincipal_WithAddressDetails() {
        mockPrincipal.setAddressPoid(200L);
        AddressDetails addressDetail = new AddressDetails();
        addressDetail.setContactPerson("John Doe");
        addressDetail.setEmail1("john@example.com");
        
        when(principalRepository.findById(1L)).thenReturn(Optional.of(mockPrincipal));
        when(chargeRepository.findByPrincipalPoidOrderByDetRowIdAsc(1L)).thenReturn(Arrays.asList());
        when(paymentRepository.findByPrincipalPoidOrderByDetRowIdAsc(1L)).thenReturn(Arrays.asList());
        when(addressDetailsRepository.findByAddressMasterPoid(200L)).thenReturn(Arrays.asList(addressDetail));
        
        PrincipalDetailDTO result = principalService.getPrincipal(1L);
        
        assertNotNull(result);
        assertNotNull(result.getAddressDetails());
        assertEquals(1, result.getAddressDetails().size());
        assertEquals("John Doe", result.getAddressDetails().get(0).getContactPerson());
    }
    
    @Test
    void testToggleActive_Success() {
        when(principalRepository.findById(1L)).thenReturn(Optional.of(mockPrincipal));
        when(principalRepository.save(any(ShipPrincipalEntity.class))).thenReturn(mockPrincipal);
        
        principalService.toggleActive(1L);
        
        verify(principalRepository).findById(1L);
        verify(principalRepository).save(any(ShipPrincipalEntity.class));
    }
    
    @Test
    void testDeletePrincipal_Success() {
        when(principalRepository.findById(1L)).thenReturn(Optional.of(mockPrincipal));
        when(principalRepository.save(any(ShipPrincipalEntity.class))).thenReturn(mockPrincipal);
        
        principalService.deletePrincipal(1L);
        
        verify(principalRepository).findById(1L);
        verify(principalRepository).save(any(ShipPrincipalEntity.class));
    }
}
