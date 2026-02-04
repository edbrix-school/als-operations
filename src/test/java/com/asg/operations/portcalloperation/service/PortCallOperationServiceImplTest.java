package com.asg.operations.portcalloperation.service;

import com.asg.common.lib.service.LovDataService;
import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.operations.exceptions.ResourceNotFoundException;
import com.asg.operations.finaldisbursementaccount.repository.PdaFdaHdrRepository;
import com.asg.operations.finaldisbursementaccount.repository.ShipVoyageHdrRepository;
import com.asg.operations.pdaentryform.repository.PdaEntryHdrRepository;
import com.asg.operations.pdaporttariffmaster.repository.ShipPortMasterRepository;
import com.asg.operations.portcalloperation.dto.*;
import com.asg.operations.portcalloperation.entity.*;
import com.asg.operations.portcalloperation.repository.*;
import com.asg.operations.portcallreport.repository.PortCallReportDtlRepository;
import com.asg.operations.portcallreport.repository.PortCallReportHdrRepository;
import com.asg.operations.shipprincipal.repository.ShipPrincipalRepository;
import com.asg.operations.portactivitiesmaster.repository.PortActivityMasterRepository;
import com.asg.operations.common.repository.GlobalParameterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PortCallOperationServiceImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private PortCallOperationHdrRepository hdrRepository;
    @Mock
    private PortCallOperationCargoDtlRepository cargoDtlRepository;
    @Mock
    private PortCallOperationMailDtlRepository mailDtlRepository;
    @Mock
    private PortCallOperationEstBertDtlRepository estBertDtlRepository;
    @Mock
    private PortCallOperationEstPrearrivalDtlRepository estPrearrivalDtlRepository;
    @Mock
    private PortCallOperationEstPrearrivalActDtlRepository estPrearrivalActDtlRepository;
    @Mock
    private PortCallOperationActTimingDtlRepository actTimingDtlRepository;
    @Mock
    private PortCallOperationActTimingsActvtyDtlRepository actTimingsActvtyDtlRepository;
    @Mock
    private PortCallOperationActCondDtlRepository actCondDtlRepository;
    @Mock
    private PortCallOperationActRmksDtlRepository actRmksDtlRepository;
    @Mock
    private PortCallOperationActProgDtlRepository actProgDtlRepository;
    @Mock
    private PortCallOperationActCargoFigDtlRepository actCargoFigDtlRepository;
    @Mock
    private PortCallOperationActBunkerDtlRepository actBunkerDtlRepository;
    @Mock
    private PortCallOperationHusbandryCrewDtlRepository husbandryCrewDtlRepository;
    @Mock
    private PortCallOperationHusbandryOthDtlRepository husbandryOthDtlRepository;
    @Mock
    private PortCallOperationDocsCopyDtlRepository docsCopyDtlRepository;
    @Mock
    private PortCallOperationDocsMsgsDtl1Repository docsMsgsDtl1Repository;
    @Mock
    private PortCallOperationDocsMsgsDtl2Repository docsMsgsDtl2Repository;
    @Mock
    private DocumentSearchService documentService;
    @Mock
    private DocumentDeleteService documentDeleteService;
    @Mock
    private LoggingService loggingService;
    @Mock
    private ShipVoyageHdrRepository shipVoyageHdrRepository;
    @Mock
    private ShipPrincipalRepository shipPrincipalRepository;
    @Mock
    private ShipPortMasterRepository shipPortMasterRepository;
    @Mock
    private PdaEntryHdrRepository pdaEntryHdrRepository;
    @Mock
    private PdaFdaHdrRepository pdaFdaHdrRepository;
    @Mock
    private OpsPcDocsMsgsDtl1Repository msgsDtl1Repository;
    @Mock
    private PortCallReportDtlRepository dtlRepository;
    @Mock
    private PortCallReportHdrRepository portCallReportHdrRepository;
    @Mock
    private StockUnitMasterRepository stockUnitMasterRepository;
    @Mock
    private PortActivityMasterRepository portActivityMasterRepository;
    @Mock
    private LovDataService lovDataService;
    @Mock
    private GlobalUserRepository globalUserRepository;
    @Mock
    private GlobalParameterRepository globalParameterRepository;

    @InjectMocks
    private PortCallOperationServiceImpl service;

    private Long transactionPoid;
    private Long detRowId;

    @BeforeEach
    void setUp() {
        transactionPoid = 1L;
        detRowId = 1L;
    }

    @Test
    void getEstBertDetail_Success() {
        PortCallOperationEstBertDtl entity = PortCallOperationEstBertDtl.builder()
                .transactionPoid(transactionPoid)
                .detRowId(detRowId)
                .eta(LocalDateTime.now())
                .etb(LocalDateTime.now())
                .berthingAttachments("attachment")
                .emailPoid(1L)
                .lastModifiedBy("user")
                .lastModifiedDate(LocalDateTime.now())
                .build();

        PortCallOperationDocsMsgsDtl1 emailEntity = PortCallOperationDocsMsgsDtl1.builder()
                .transactionPoid(transactionPoid)
                .detRowId(detRowId)
                .emailPoid(1L)
                .emailSendOn(LocalDate.now())
                .emailRemarks("test remarks")
                .build();

        when(estBertDtlRepository.findById(any())).thenReturn(Optional.of(entity));
        when(docsMsgsDtl1Repository.findByEmailPoid(1L))
                .thenReturn(Optional.of(emailEntity));

        PortCallOperationEstBertDetailResponseDto result = service.getEstBertDetail(transactionPoid, detRowId);

        assertNotNull(result);
        assertEquals(transactionPoid, result.getTransactionPoid());
        assertEquals(detRowId, result.getDetRowId());
        assertEquals("test remarks", result.getRemarks());
        assertNotNull(result.getEmailSentOn());
        verify(estBertDtlRepository).findById(any());
        verify(docsMsgsDtl1Repository).findByEmailPoid(1L);
    }

    @Test
    void getEstBertDetail_NotFound() {
        when(estBertDtlRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.getEstBertDetail(transactionPoid, detRowId));
    }

    @Test
    void listEstPrearrivalActDetails_Success() {
        List<PortCallOperationEstPrearrivalActDtl> entities = Collections.singletonList(
                PortCallOperationEstPrearrivalActDtl.builder()
                        .transactionPoid(transactionPoid)
                        .detRowId(detRowId)
                        .preActivityDtlPoid(1L)
                        .activityPoid(1L)
                        .otherDescription("description")
                        .estimatedDatetime(LocalDateTime.now())
                        .build()
        );

        when(estPrearrivalActDtlRepository.findByTransactionPoidAndDetRowId(transactionPoid, detRowId))
                .thenReturn(entities);

        List<PortCallOperationEstPrearrivalActDetailResponseDto> result =
                service.listEstPrearrivalActDetails(transactionPoid, detRowId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(transactionPoid, result.getFirst().getTransactionPoid());
        assertEquals(detRowId, result.getFirst().getDetRowId());
    }

    @Test
    void listActTimingsActvtyDetails_Success() {
        List<PortCallOperationActTimingsActvtyDtl> entities = Collections.singletonList(
                PortCallOperationActTimingsActvtyDtl.builder()
                        .transactionPoid(transactionPoid)
                        .detRowId(detRowId)
                        .actualsTimingDtlPoid(1L)
                        .activityPoid(1L)
                        .details("details")
                        .estimatedDatetime(LocalDateTime.now())
                        .build()
        );

        when(actTimingsActvtyDtlRepository.findByTransactionPoidAndDetRowId(transactionPoid, detRowId))
                .thenReturn(entities);

        List<PortCallOperationActTimingsActvtyDetailResponseDto> result =
                service.listActTimingsActvtyDetails(transactionPoid, detRowId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(transactionPoid, result.getFirst().getTransactionPoid());
        assertEquals(detRowId, result.getFirst().getDetRowId());
    }

    @Test
    void getDocsCopyDetail_Success() {
        PortCallOperationDocsCopyDtl entity = PortCallOperationDocsCopyDtl.builder()
                .transactionPoid(transactionPoid)
                .detRowId(detRowId)
                .documentFrom("from")
                .documentList("list")
                .documentSelect("select")
                .documentAttachments("attachments")
                .build();

        when(docsCopyDtlRepository.findById(any())).thenReturn(Optional.of(entity));

        PortCallOperationDocsCopyDetailResponseDto result = service.getDocsCopyDetail(transactionPoid, detRowId);

        assertNotNull(result);
        assertEquals(transactionPoid, result.getTransactionPoid());
        assertEquals(detRowId, result.getDetRowId());
        verify(docsCopyDtlRepository).findById(any());
    }

    @Test
    void getDocsCopyDetail_NotFound() {
        when(docsCopyDtlRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.getDocsCopyDetail(transactionPoid, detRowId));
    }

    @Test
    void deleteOperation_Success() {
        PortCallOperationHdr hdr = PortCallOperationHdr.builder()
                .transactionPoid(transactionPoid)
                .transactionDate(LocalDate.now())
                .build();

        DeleteReasonDto deleteReasonDto = new DeleteReasonDto();

        when(hdrRepository.findById(transactionPoid)).thenReturn(Optional.of(hdr));

        assertDoesNotThrow(() -> service.deleteOperation(transactionPoid, deleteReasonDto));
        verify(hdrRepository).findById(transactionPoid);
        verify(documentDeleteService).deleteDocument(any(), any(), any(), any(), any());
    }

    @Test
    void deleteOperation_NotFound() {
        DeleteReasonDto deleteReasonDto = new DeleteReasonDto();

        when(hdrRepository.findById(transactionPoid)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.deleteOperation(transactionPoid, deleteReasonDto));
    }
}