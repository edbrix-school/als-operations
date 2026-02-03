package com.asg.operations.salesquotationprojects.service;

import com.asg.common.lib.dto.*;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.operations.exceptions.ResourceNotFoundException;
import com.asg.operations.exceptions.CustomException;
import com.asg.operations.finaldisbursementaccount.repository.*;
import com.asg.operations.pdaporttariffmaster.repository.ShipChargeMasterRepository;
import com.asg.operations.salesquotationprojects.dto.*;
import com.asg.operations.salesquotationprojects.entity.SalesQuoteProjectsHdr;
import com.asg.operations.salesquotationprojects.key.ShipCommodityMasterId;
import com.asg.operations.salesquotationprojects.repository.*;
import com.asg.operations.shipprincipal.repository.AddressDetailsRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalesQuoteProjectsServiceImplTest {

    @Mock private JdbcTemplate jdbcTemplate;
    @Mock private SalesQuoteProjectsHdrRepository repository;
    @Mock private SalesQuoteProjectsStoredProcRepository salesQuoteProjectsStoredProcRepository;
    @Mock private SalesQuoteProjectsChargeDtlRepository chargeDtlRepository;
    @Mock private SalesQuoteProjectsNotesDtlRepository notesDtlRepository;
    @Mock private SalesQuoteProjectsTcDtlRepository tcDtlRepository;
    @Mock private DocumentSearchService documentSearchService;
    @Mock private DocumentDeleteService documentDeleteService;
    @Mock private AddressDetailsRepository addressDetailsRepository;
    @Mock private ApSupplierMasterRepository apSupplierMasterRepository;
    @Mock private SalesSalesmanMasterRepository salesSalesmanMasterRepository;
    @Mock private ShipCommodityMasterRepository shipCommodityMasterRepository;
    @Mock private ShipLineMasterRepository shipLineMasterRepository;
    @Mock private AirLineMasterRepository airLineMasterRepository;
    @Mock private TermsTemplateRepository termsTemplateRepository;
    @Mock private GLBankMasterRepository glBankMasterRepository;
    @Mock private ProjectsHdrRepository projectsHdrRepository;
    @Mock private GlobalCurrencyMasterRepository globalCurrencyMasterRepository;
    @Mock private ShipChargeMasterRepository shipChargeMasterRepository;
    @Mock private GlobalTaxMasterRepository globalTaxMasterRepository;
    @Mock private LoggingService loggingService;
    @Mock private EntityManager entityManager;

    @InjectMocks
    private SalesQuoteProjectsServiceImpl service;

    private SalesQuoteProjectsHdr mockEntity;
    private SalesQuoteProjectsRequest mockRequest;
    private final Long transactionPoid = 1L;

    @BeforeEach
    void setUp() {
        mockEntity = createMockEntity();
        mockRequest = createMockRequest();
    }

    @Test
    void listSalesQuoteProjectsWithFilters_Success() {
        // Arrange
        String documentId = "DOC123";
        FilterRequestDto filterRequestDto = new FilterRequestDto("AND", "N", new ArrayList<>());
        Pageable pageable = PageRequest.of(0, 10);
        LocalDate periodFrom = LocalDate.now().minusDays(30);
        LocalDate periodTo = LocalDate.now();

        when(documentSearchService.resolveOperator(filterRequestDto)).thenReturn("AND");
        when(documentSearchService.resolveIsDeleted(filterRequestDto)).thenReturn("N");
        when(documentSearchService.resolveDateFilters(any(), any(), any(), any())).thenReturn(new ArrayList<>());
        
        RawSearchResult rawResult = mock(RawSearchResult.class);
        when(rawResult.records()).thenReturn(Arrays.asList(Map.of("id", 1L, "name", "Test")));
        when(rawResult.totalRecords()).thenReturn(10L);
        when(rawResult.displayFields()).thenReturn(Map.of("id", "ID", "name", "Name"));
        when(documentSearchService.search(any(), any(), any(), any(), any(), any(), any())).thenReturn(rawResult);

        // Act
        Map<String, Object> result = service.listSalesQuoteProjectsWithFilters(documentId, filterRequestDto, pageable, periodFrom, periodTo);

        // Assert
        assertNotNull(result);
        verify(documentSearchService).search(documentId, new ArrayList<>(), "AND", pageable, "N", "DOC_REF", "TRANSACTION_POID");
    }

    @Test
    void getSalesQuoteProjectById_Success() {
        // Arrange
        when(repository.findById(transactionPoid)).thenReturn(Optional.of(mockEntity));
        when(chargeDtlRepository.findByIdTransactionPoid(transactionPoid)).thenReturn(new ArrayList<>());
        when(notesDtlRepository.findByIdTransactionPoid(transactionPoid)).thenReturn(new ArrayList<>());
        when(tcDtlRepository.findByIdTransactionPoid(transactionPoid)).thenReturn(new ArrayList<>());
        when(salesQuoteProjectsStoredProcRepository.callNewTempAddressLoadListProc(any(), any(), any(), any(), any()))
            .thenReturn(new ArrayList<>());

        try (MockedStatic<UserContext> userContextMock = mockStatic(UserContext.class)) {
            userContextMock.when(UserContext::getGroupPoid).thenReturn(1L);
            userContextMock.when(UserContext::getCompanyPoid).thenReturn(1L);
            userContextMock.when(UserContext::getUserPoid).thenReturn(1L);
            userContextMock.when(UserContext::getDocumentId).thenReturn("100");

            // Act
            SalesQuoteProjectsResponse result = service.getSalesQuoteProjectById(transactionPoid);

            // Assert
            assertNotNull(result);
            assertEquals(transactionPoid, result.getTransactionPoid());
            verify(repository).findById(transactionPoid);
        }
    }

    @Test
    void getSalesQuoteProjectById_NotFound() {
        // Arrange
        when(repository.findById(transactionPoid)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> service.getSalesQuoteProjectById(transactionPoid)
        );
        assertTrue(exception.getMessage().contains("Sales Quote Project not found with ID: " + transactionPoid));
    }

    @Test
    void createSalesQuoteProject_Success() {
        try (MockedStatic<UserContext> userContextMock = mockStatic(UserContext.class)) {
            // Arrange
            userContextMock.when(UserContext::getCompanyPoid).thenReturn(1L);
            userContextMock.when(UserContext::getUserId).thenReturn("testUser");
            userContextMock.when(UserContext::getGroupPoid).thenReturn(1L);
            userContextMock.when(UserContext::getDocumentId).thenReturn("100");
            userContextMock.when(UserContext::getUserPoid).thenReturn(1L);

            when(addressDetailsRepository.existsByAddressPoid(any())).thenReturn(true);
            when(globalCurrencyMasterRepository.existsByCurrencyCodeIgnoreCase(any())).thenReturn(true);
            when(repository.saveAndFlush(any(SalesQuoteProjectsHdr.class))).thenReturn(mockEntity);
            when(chargeDtlRepository.findByIdTransactionPoid(transactionPoid)).thenReturn(new ArrayList<>());
            when(notesDtlRepository.findByIdTransactionPoid(transactionPoid)).thenReturn(new ArrayList<>());
            when(tcDtlRepository.findByIdTransactionPoid(transactionPoid)).thenReturn(new ArrayList<>());
            doNothing().when(entityManager).refresh(any());
            doNothing().when(loggingService).createLogSummaryEntry(any(com.asg.common.lib.enums.LogDetailsEnum.class), any(String.class), any(String.class));

            // Act
            SalesQuoteProjectsResponse result = service.createSalesQuoteProject(mockRequest);

            // Assert
            assertNotNull(result);
            verify(repository).saveAndFlush(any(SalesQuoteProjectsHdr.class));
        }
    }

    @Test
    void createSalesQuoteProject_CustomerNotFound() {
        // Arrange
        when(addressDetailsRepository.existsByAddressPoid(any())).thenReturn(false);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> service.createSalesQuoteProject(mockRequest)
        );
        assertTrue(exception.getMessage().contains("Customer"));
    }

    @Test
    void updateSalesQuoteProject_Success() {
        try (MockedStatic<UserContext> userContextMock = mockStatic(UserContext.class)) {
            // Arrange
            userContextMock.when(UserContext::getUserId).thenReturn("testUser");
            userContextMock.when(UserContext::getDocumentId).thenReturn("100");
            userContextMock.when(UserContext::getUserPoid).thenReturn(1L);
            userContextMock.when(UserContext::getGroupPoid).thenReturn(1L);
            
            when(repository.findById(transactionPoid)).thenReturn(Optional.of(mockEntity));
            when(addressDetailsRepository.existsByAddressPoid(any())).thenReturn(true);
            when(globalCurrencyMasterRepository.existsByCurrencyCodeIgnoreCase(any())).thenReturn(true);
            when(repository.save(any(SalesQuoteProjectsHdr.class))).thenReturn(mockEntity);
            when(chargeDtlRepository.findByIdTransactionPoid(transactionPoid)).thenReturn(new ArrayList<>());
            when(notesDtlRepository.findByIdTransactionPoid(transactionPoid)).thenReturn(new ArrayList<>());
            when(tcDtlRepository.findByIdTransactionPoid(transactionPoid)).thenReturn(new ArrayList<>());
            doNothing().when(loggingService).logChanges(any(), any(), any(), any(), any(), any(), any());

            // Act
            SalesQuoteProjectsResponse result = service.updateSalesQuoteProject(transactionPoid, mockRequest);

            // Assert
            assertNotNull(result);
            verify(repository).save(any(SalesQuoteProjectsHdr.class));
        }
    }

    @Test
    void updateSalesQuoteProject_NotFound() {
        // Arrange
        when(repository.findById(transactionPoid)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> service.updateSalesQuoteProject(transactionPoid, mockRequest)
        );
        assertTrue(exception.getMessage().contains("Sales Quote Project not found with ID: " + transactionPoid));
    }

    @Test
    void deleteSalesQuoteProject_Success() {
        // Arrange
        DeleteReasonDto deleteReasonDto = new DeleteReasonDto();
        deleteReasonDto.setDeleteReason("Test deletion");
        when(repository.findById(transactionPoid)).thenReturn(Optional.of(mockEntity));

        // Act
        service.deleteSalesQuoteProject(transactionPoid, deleteReasonDto);

        // Assert
        verify(documentDeleteService).deleteDocument(
            transactionPoid,
            "SALES_QUOTE_PROJECTS_HDR",
            "TRANSACTION_POID",
            deleteReasonDto,
            mockEntity.getTransactionDate()
        );
    }

    @Test
    void deleteSalesQuoteProject_NotFound() {
        // Arrange
        DeleteReasonDto deleteReasonDto = new DeleteReasonDto();
        when(repository.findById(transactionPoid)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> service.deleteSalesQuoteProject(transactionPoid, deleteReasonDto)
        );
        assertTrue(exception.getMessage().contains("Sales Quote Project not found with ID: " + transactionPoid));
    }

    @Test
    void createSalesQuoteProject_InvalidCustomerType() {
        // Arrange
        mockRequest.setCustomerType("invalid");

        // Act & Assert
        CustomException exception = assertThrows(
            CustomException.class,
            () -> service.createSalesQuoteProject(mockRequest)
        );
        assertTrue(exception.getMessage().contains("Customer Type should be either Existing or New"));
    }

    @Test
    void getCustomerDetailsById_Success() {
        // Arrange
        BigDecimal addressPoid = BigDecimal.valueOf(1L);
        Object[] mockResult = {addressPoid, "Test Customer", "John Doe", "test@example.com", "123456789", "987654321", "PO Box 123", "555-0123"};
        when(addressDetailsRepository.findAddressDetailsWithNameByAddressPoid(addressPoid))
            .thenReturn(Optional.of(mockResult));

        // Act
        AddressDetailsDto result = service.getCustomerDetailsById(addressPoid);

        // Assert
        assertNotNull(result);
        assertEquals(addressPoid, result.getAddressPoid());
        assertEquals("Test Customer", result.getAddressName());
        assertEquals("John Doe", result.getContactPerson());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("123456789", result.getTelephone());
        assertEquals("987654321", result.getMobile());
    }

    @Test
    void getCustomerDetailsById_NotFound() {
        // Arrange
        BigDecimal addressPoid = BigDecimal.valueOf(1L);
        when(addressDetailsRepository.findAddressDetailsWithNameByAddressPoid(addressPoid))
            .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> service.getCustomerDetailsById(addressPoid)
        );
        assertTrue(exception.getMessage().contains("Address"));
    }

    private SalesQuoteProjectsHdr createMockEntity() {
        SalesQuoteProjectsHdr entity = new SalesQuoteProjectsHdr();
        entity.setTransactionPoid(transactionPoid);
        entity.setTransactionDate(LocalDate.now());
        entity.setCompanyPoid(1L);
        entity.setDocRef("DOC123");
        entity.setCustomerType("existing");
        entity.setCustomerPoid(BigDecimal.valueOf(1L));
        entity.setCustomerName("Test Customer");
        entity.setCustomerContact("John Doe");
        entity.setCustomerEmail("test@example.com");
        entity.setCustomerTelephone("123456789");
        entity.setCustomerMobile("987654321");
        entity.setBillingCurrencyCode("USD");
        entity.setDeleted("N");
        entity.setCreatedBy("testUser");
        entity.setCreatedDate(LocalDateTime.now());
        entity.setLastModifiedBy("testUser");
        entity.setLastModifiedDate(LocalDateTime.now());
        return entity;
    }

    private SalesQuoteProjectsRequest createMockRequest() {
        SalesQuoteProjectsRequest request = new SalesQuoteProjectsRequest();
        request.setCustomerType("existing");
        request.setCustomerPoid(BigDecimal.valueOf(1L));
        request.setCustomerName("Test Customer");
        request.setCustomerContact("John Doe");
        request.setCustomerEmail("test@example.com");
        request.setCustomerTelephone("123456789");
        request.setCustomerMobile("987654321");
        request.setBillingCurrencyCode("USD");
        request.setChargeDetails(new ArrayList<>());
        request.setNotesDetails(new ArrayList<>());
        request.setTcDetails(new ArrayList<>());
        return request;
    }
}