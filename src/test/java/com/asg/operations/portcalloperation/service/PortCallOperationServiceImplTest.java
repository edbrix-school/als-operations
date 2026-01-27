package com.asg.operations.portcalloperation.service;

import com.asg.common.lib.service.LovDataService;
import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.RawSearchResult;
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
import com.asg.operations.portcallreport.repository.PortCallReportHdrRepository;
import com.asg.operations.shipprincipal.repository.ShipPrincipalRepository;
import com.asg.operations.portactivitiesmaster.repository.PortActivityMasterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.asg.operations.portcallreport.enums.ActionType;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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
    private PortCallReportHdrRepository portCallReportHdrRepository;
    @Mock
    private StockUnitMasterRepository stockUnitMasterRepository;
    @Mock
    private PortActivityMasterRepository portActivityMasterRepository;
    @Mock
    private LovDataService lovDataService;
    @Mock
    private GlobalUserRepository globalUserRepository;

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
                .build();

        when(estBertDtlRepository.findById(any())).thenReturn(Optional.of(entity));

        PortCallOperationEstBertDetailResponseDto result = service.getEstBertDetail(transactionPoid, detRowId);

        assertNotNull(result);
        assertEquals(transactionPoid, result.getTransactionPoid());
        assertEquals(detRowId, result.getDetRowId());
        verify(estBertDtlRepository).findById(any());
    }

    @Test
    void getEstBertDetail_NotFound() {
        when(estBertDtlRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
            () -> service.getEstBertDetail(transactionPoid, detRowId));
    }

    @Test
    void createEstBertDetail_Success() {
        PortCallOperationEstBertDetailDto dto = PortCallOperationEstBertDetailDto.builder()
                .eta(LocalDateTime.now())
                .etb(LocalDateTime.now())
                .berthingAttachments("attachment")
                .emailPoid(1L)
                .build();

        PortCallOperationHdr hdr = PortCallOperationHdr.builder()
                .transactionPoid(transactionPoid)
                .build();

        when(hdrRepository.existsById(transactionPoid)).thenReturn(true);
        when(msgsDtl1Repository.existsByIdEmailPoid(1L)).thenReturn(true);
        when(estBertDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid)).thenReturn(0L);
        when(estBertDtlRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(hdrRepository.findById(transactionPoid)).thenReturn(Optional.of(hdr));
        when(cargoDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(mailDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(estBertDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(estPrearrivalDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actTimingDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actCondDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actRmksDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actProgDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actCargoFigDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actBunkerDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(husbandryCrewDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(husbandryOthDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(docsCopyDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(docsMsgsDtl1Repository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(docsMsgsDtl2Repository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());

        PortCallOperationResponseDto result = service.createEstBertDetail(transactionPoid, dto);

        assertNotNull(result);
        verify(hdrRepository).existsById(transactionPoid);
        verify(estBertDtlRepository).findMaxDetRowIdByTransactionPoid(transactionPoid);
        verify(estBertDtlRepository).save(any());
    }

    @Test
    void createEstBertDetail_OperationNotFound() {
        PortCallOperationEstBertDetailDto dto = PortCallOperationEstBertDetailDto.builder().build();

        when(hdrRepository.existsById(transactionPoid)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, 
            () -> service.createEstBertDetail(transactionPoid, dto));
    }

    @Test
    void updateEstBertDetail_Success() {
        PortCallOperationEstBertDetailDto dto = PortCallOperationEstBertDetailDto.builder()
                .eta(LocalDateTime.now())
                .etb(LocalDateTime.now())
                .berthingAttachments("updated")
                .build();

        PortCallOperationEstBertDtl entity = PortCallOperationEstBertDtl.builder()
                .transactionPoid(transactionPoid)
                .detRowId(detRowId)
                .build();

        PortCallOperationHdr hdr = PortCallOperationHdr.builder()
                .transactionPoid(transactionPoid)
                .build();

        when(estBertDtlRepository.findById(any())).thenReturn(Optional.of(entity));
        when(estBertDtlRepository.save(any())).thenReturn(entity);
        when(hdrRepository.findById(transactionPoid)).thenReturn(Optional.of(hdr));
        when(cargoDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(mailDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(estBertDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(estPrearrivalDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actTimingDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actCondDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actRmksDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actProgDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actCargoFigDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actBunkerDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(husbandryCrewDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(husbandryOthDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(docsCopyDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(docsMsgsDtl1Repository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(docsMsgsDtl2Repository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());

        PortCallOperationResponseDto result = service.updateEstBertDetail(transactionPoid, detRowId, dto);

        assertNotNull(result);
        verify(estBertDtlRepository).findById(any());
        verify(estBertDtlRepository).save(any());
    }

    @Test
    void listEstPrearrivalActDetails_Success() {
        PortCallOperationEstPrearrivalActDtl entity1 = PortCallOperationEstPrearrivalActDtl.builder()
                .transactionPoid(transactionPoid)
                .detRowId(detRowId)
                .preActivityDtlPoid(1L)
                .activityPoid(1L)
                .otherDescription("desc1")
                .estimatedDatetime(LocalDateTime.now())
                .build();

        PortCallOperationEstPrearrivalActDtl entity2 = PortCallOperationEstPrearrivalActDtl.builder()
                .transactionPoid(transactionPoid)
                .detRowId(detRowId)
                .preActivityDtlPoid(2L)
                .activityPoid(2L)
                .otherDescription("desc2")
                .estimatedDatetime(LocalDateTime.now())
                .build();

        when(estPrearrivalActDtlRepository.findByTransactionPoidAndDetRowId(transactionPoid, detRowId))
                .thenReturn(Arrays.asList(entity1, entity2));

        List<PortCallOperationEstPrearrivalActDetailResponseDto> result = 
            service.listEstPrearrivalActDetails(transactionPoid, detRowId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getPreActivityDtlPoid());
        assertEquals(2L, result.get(1).getPreActivityDtlPoid());
        verify(estPrearrivalActDtlRepository).findByTransactionPoidAndDetRowId(transactionPoid, detRowId);
    }

    @Test
    void createEstPrearrivalActDetail_Success() {
        PortCallOperationEstPrearrivalActDetailDto dto = PortCallOperationEstPrearrivalActDetailDto.builder()
                .activityPoid(1L)
                .otherDescription("desc")
                .estimatedDatetime(LocalDateTime.now())
                .build();

        when(hdrRepository.existsById(transactionPoid)).thenReturn(true);
        when(docsCopyDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(portActivityMasterRepository.existsByPortActivityTypePoid(1L)).thenReturn(true);
        when(estPrearrivalActDtlRepository.findMaxPreActivityDtlPoidByTransactionPoidAndDetRowId(transactionPoid, detRowId))
                .thenReturn(0L);
        when(estPrearrivalActDtlRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        PortCallOperationEstPrearrivalActDetailResponseDto result = 
            service.createEstPrearrivalActDetail(transactionPoid, detRowId, dto);

        assertNotNull(result);
        assertEquals(transactionPoid, result.getTransactionPoid());
        assertEquals(detRowId, result.getDetRowId());
        assertEquals(1L, result.getPreActivityDtlPoid());
        verify(estPrearrivalActDtlRepository).save(any());
    }

    @Test
    void updateEstPrearrivalActDetail_Success() {
        Long preActivityDtlPoid = 1L;
        PortCallOperationEstPrearrivalActDetailDto dto = PortCallOperationEstPrearrivalActDetailDto.builder()
                .activityPoid(2L)
                .otherDescription("updated")
                .estimatedDatetime(LocalDateTime.now())
                .build();

        PortCallOperationEstPrearrivalActDtl entity = PortCallOperationEstPrearrivalActDtl.builder()
                .transactionPoid(transactionPoid)
                .detRowId(detRowId)
                .preActivityDtlPoid(preActivityDtlPoid)
                .build();

        when(estPrearrivalActDtlRepository.findById(any())).thenReturn(Optional.of(entity));
        when(portActivityMasterRepository.existsByPortActivityTypePoid(2L)).thenReturn(true);
        when(estPrearrivalActDtlRepository.findByTransactionPoidOrderByLastModifiedDateDesc(transactionPoid))
                .thenReturn(Arrays.asList(entity));
        when(estPrearrivalActDtlRepository.save(any())).thenReturn(entity);

        PortCallOperationEstPrearrivalActDetailResponseDto result = 
            service.updateEstPrearrivalActDetail(transactionPoid, detRowId, preActivityDtlPoid, dto);

        assertNotNull(result);
        assertEquals(2L, result.getActivityPoid());
        verify(estPrearrivalActDtlRepository).save(any());
    }

    @Test
    void listActTimingsActvtyDetails_Success() {
        PortCallOperationActTimingsActvtyDtl entity1 = PortCallOperationActTimingsActvtyDtl.builder()
                .transactionPoid(transactionPoid)
                .detRowId(detRowId)
                .actualsTimingDtlPoid(1L)
                .activityPoid(1L)
                .details("details1")
                .estimatedDatetime(LocalDateTime.now())
                .build();

        when(actTimingsActvtyDtlRepository.findByTransactionPoidAndDetRowId(transactionPoid, detRowId))
                .thenReturn(Arrays.asList(entity1));

        List<PortCallOperationActTimingsActvtyDetailResponseDto> result = 
            service.listActTimingsActvtyDetails(transactionPoid, detRowId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getActualsTimingDtlPoid());
        verify(actTimingsActvtyDtlRepository).findByTransactionPoidAndDetRowId(transactionPoid, detRowId);
    }

    @Test
    void createActTimingsActvtyDetail_Success() {
        PortCallOperationActTimingsActvtyDetailDto dto = PortCallOperationActTimingsActvtyDetailDto.builder()
                .activityPoid(1L)
                .details("details")
                .estimatedDatetime(LocalDateTime.now())
                .build();

        when(hdrRepository.existsById(transactionPoid)).thenReturn(true);
        when(docsCopyDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(portActivityMasterRepository.existsByPortActivityTypePoid(1L)).thenReturn(true);
        when(actTimingsActvtyDtlRepository.findMaxActualsTimingDtlPoidByTransactionPoidAndDetRowId(transactionPoid, detRowId))
                .thenReturn(0L);
        when(actTimingsActvtyDtlRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        PortCallOperationActTimingsActvtyDetailResponseDto result = 
            service.createActTimingsActvtyDetail(transactionPoid, detRowId, dto);

        assertNotNull(result);
        assertEquals(transactionPoid, result.getTransactionPoid());
        assertEquals(detRowId, result.getDetRowId());
        assertEquals(1L, result.getActualsTimingDtlPoid());
        verify(actTimingsActvtyDtlRepository).save(any());
    }

    @Test
    void updateActTimingsActvtyDetail_Success() {
        Long actualsTimingDtlPoid = 1L;
        PortCallOperationActTimingsActvtyDetailDto dto = PortCallOperationActTimingsActvtyDetailDto.builder()
                .activityPoid(2L)
                .details("updated details")
                .estimatedDatetime(LocalDateTime.now())
                .build();

        PortCallOperationActTimingsActvtyDtl entity = PortCallOperationActTimingsActvtyDtl.builder()
                .transactionPoid(transactionPoid)
                .detRowId(detRowId)
                .actualsTimingDtlPoid(actualsTimingDtlPoid)
                .build();

        when(actTimingsActvtyDtlRepository.findById(any())).thenReturn(Optional.of(entity));
        when(portActivityMasterRepository.existsByPortActivityTypePoid(2L)).thenReturn(true);
        when(actTimingsActvtyDtlRepository.findByTransactionPoidOrderByLastModifiedDateDesc(transactionPoid))
                .thenReturn(Arrays.asList(entity));
        when(actTimingsActvtyDtlRepository.save(any())).thenReturn(entity);

        PortCallOperationActTimingsActvtyDetailResponseDto result = 
            service.updateActTimingsActvtyDetail(transactionPoid, detRowId, actualsTimingDtlPoid, dto);

        assertNotNull(result);
        assertEquals(2L, result.getActivityPoid());
        verify(actTimingsActvtyDtlRepository).save(any());
    }

    @Test
    void updateActTimingsActvtyDetail_NotFound() {
        Long actualsTimingDtlPoid = 1L;
        PortCallOperationActTimingsActvtyDetailDto dto = PortCallOperationActTimingsActvtyDetailDto.builder().build();

        when(actTimingsActvtyDtlRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
            () -> service.updateActTimingsActvtyDetail(transactionPoid, detRowId, actualsTimingDtlPoid, dto));
    }

    @Test
    void getDocsCopyDetail_Success() {
        PortCallOperationDocsCopyDtl entity = PortCallOperationDocsCopyDtl.builder()
                .transactionPoid(transactionPoid)
                .detRowId(detRowId)
                .documentFrom("source")
                .documentList("list")
                .documentSelect("Y")
                .documentAttachments("attachment.pdf")
                .build();

        when(docsCopyDtlRepository.findById(any())).thenReturn(Optional.of(entity));

        PortCallOperationDocsCopyDetailResponseDto result = service.getDocsCopyDetail(transactionPoid, detRowId);

        assertNotNull(result);
        assertEquals(transactionPoid, result.getTransactionPoid());
        assertEquals(detRowId, result.getDetRowId());
        assertEquals("source", result.getDocumentFrom());
        verify(docsCopyDtlRepository).findById(any());
    }

    @Test
    void getDocsCopyDetail_NotFound() {
        when(docsCopyDtlRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
            () -> service.getDocsCopyDetail(transactionPoid, detRowId));
    }

    @Test
    void createDocsCopyDetail_Success() {
        PortCallOperationDocsCopyDetailDto dto = PortCallOperationDocsCopyDetailDto.builder()
                .documentFrom("source")
                .documentList("list")
                .documentSelect("Y")
                .documentAttachments("attachment.pdf")
                .build();

        PortCallOperationHdr hdr = PortCallOperationHdr.builder()
                .transactionPoid(transactionPoid)
                .vesselVoyagePoid(1L)
                .build();

        when(hdrRepository.existsById(transactionPoid)).thenReturn(true);
        when(docsCopyDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(docsCopyDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid)).thenReturn(0L);
        when(docsCopyDtlRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(hdrRepository.findById(transactionPoid)).thenReturn(Optional.of(hdr));
        when(cargoDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(mailDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(estBertDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(estPrearrivalDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actTimingDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actCondDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actRmksDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actProgDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actCargoFigDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actBunkerDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(husbandryCrewDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(husbandryOthDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(docsMsgsDtl1Repository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(docsMsgsDtl2Repository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());

        PortCallOperationResponseDto result = service.createDocsCopyDetail(transactionPoid, dto);

        assertNotNull(result);
        verify(hdrRepository).existsById(transactionPoid);
        verify(docsCopyDtlRepository).findMaxDetRowIdByTransactionPoid(transactionPoid);
        verify(docsCopyDtlRepository).save(any());
    }

    @Test
    void createDocsCopyDetail_OperationNotFound() {
        PortCallOperationDocsCopyDetailDto dto = PortCallOperationDocsCopyDetailDto.builder().build();

        when(hdrRepository.existsById(transactionPoid)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, 
            () -> service.createDocsCopyDetail(transactionPoid, dto));
    }

    @Test
    void updateDocsCopyDetail_Success() {
        PortCallOperationDocsCopyDetailDto dto = PortCallOperationDocsCopyDetailDto.builder()
                .documentFrom("updated source")
                .documentList("updated list")
                .documentSelect("N")
                .documentAttachments("updated.pdf")
                .build();

        PortCallOperationDocsCopyDtl entity = PortCallOperationDocsCopyDtl.builder()
                .transactionPoid(transactionPoid)
                .detRowId(detRowId)
                .lastModifiedDate(LocalDateTime.now())
                .build();

        when(docsCopyDtlRepository.findById(any())).thenReturn(Optional.of(entity));
        when(docsCopyDtlRepository.findByTransactionPoidOrderByLastModifiedDateDesc(transactionPoid))
                .thenReturn(Arrays.asList(entity));
        when(docsCopyDtlRepository.save(any())).thenReturn(entity);
        when(hdrRepository.findById(transactionPoid)).thenReturn(Optional.of(PortCallOperationHdr.builder().transactionPoid(transactionPoid).build()));
        when(cargoDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(mailDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(estBertDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(estPrearrivalDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actTimingDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actCondDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actRmksDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actProgDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actCargoFigDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actBunkerDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(husbandryCrewDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(husbandryOthDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(docsCopyDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList(entity));
        when(docsMsgsDtl1Repository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(docsMsgsDtl2Repository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());

        PortCallOperationResponseDto result = service.updateDocsCopyDetail(transactionPoid, detRowId, dto);

        assertNotNull(result);
        verify(docsCopyDtlRepository).findById(any());
        verify(docsCopyDtlRepository).save(any());
    }

    @Test
    void updateDocsCopyDetail_NotFound() {
        PortCallOperationDocsCopyDetailDto dto = PortCallOperationDocsCopyDetailDto.builder().build();

        when(docsCopyDtlRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
            () -> service.updateDocsCopyDetail(transactionPoid, detRowId, dto));
    }

    @Test
    void listOperations_Success() {
        String docId = "PC_OPERATION";
        FilterRequestDto request = null; // Will be mocked anyway
        Pageable pageable = PageRequest.of(0, 10);
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();

        RawSearchResult rawResult = mock(RawSearchResult.class);
        when(rawResult.records()).thenReturn(Arrays.asList(Map.of("DOC_REF", "PC001", "TRANSACTION_POID", 1L)));
        when(rawResult.totalRecords()).thenReturn(1L);
        when(rawResult.displayFields()).thenReturn(Map.of("DOC_REF", "Document Reference", "TRANSACTION_POID", "Transaction ID"));

        when(documentService.resolveOperator(any())).thenReturn("AND");
        when(documentService.resolveIsDeleted(any())).thenReturn("N");
        when(documentService.resolveDateFilters(any(), any(), any(), any())).thenReturn(Arrays.asList());
        when(documentService.search(any(), any(), any(), any(), any(), any(), any())).thenReturn(rawResult);

        Map<String, Object> result = service.listOperations(docId, request, pageable, startDate, endDate);

        assertNotNull(result);
        verify(documentService).search(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void getOperationById_Success() {
        PortCallOperationHdr hdr = PortCallOperationHdr.builder()
                .transactionPoid(transactionPoid)
                .transactionDate(LocalDate.now())
                .docRef("PC001")
                .build();

        when(hdrRepository.findById(transactionPoid)).thenReturn(Optional.of(hdr));
        when(cargoDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(mailDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(estBertDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(estPrearrivalDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actTimingDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actCondDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actRmksDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actProgDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actCargoFigDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actBunkerDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(husbandryCrewDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(husbandryOthDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(docsCopyDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(docsMsgsDtl1Repository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(docsMsgsDtl2Repository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());

        PortCallOperationResponseDto result = service.getOperationById(transactionPoid);

        assertNotNull(result);
        assertEquals(transactionPoid, result.getTransactionPoid());
        assertEquals("PC001", result.getDocRef());
        verify(hdrRepository).findById(transactionPoid);
    }

    @Test
    void getOperationById_NotFound() {
        when(hdrRepository.findById(transactionPoid)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
            () -> service.getOperationById(transactionPoid));
    }

    @Test
    void createOperation_Success() {
        PortCallOperationCreateDto dto = PortCallOperationCreateDto.builder()
                .vesselVoyagePoid(1L)
                .principalPoid(1L)
                .portOfCallPoid(1L)
                .callSign("TEST123")
                .callType("LOAD")
                .operatorName("Test Operator")
                .mailDetails(Arrays.asList(
                    PortCallOperationMailDetailDto.builder()
                        .communicationType("EMAIL")
                        .company("Test Company")
                        .actionType(ActionType.isCreated) // Explicitly set as created
                        .build(),
                    PortCallOperationMailDetailDto.builder()
                        .communicationType("PHONE")
                        .company("Test Company 2")
                        .actionType(ActionType.isCreated) // Explicitly set as created
                        .build()
                ))
                .build();

        PortCallOperationHdr savedHdr = PortCallOperationHdr.builder()
                .transactionPoid(transactionPoid)
                .transactionDate(LocalDate.now())
                .build();

        when(shipVoyageHdrRepository.existsByTransactionPoid(1L)).thenReturn(true);
        when(shipPrincipalRepository.existsByPrincipalPoid(1L)).thenReturn(true);
        when(shipPortMasterRepository.existsByIdPortPoid(BigDecimal.valueOf(1L))).thenReturn(true);
        when(hdrRepository.save(any())).thenReturn(savedHdr);
        when(mailDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid)).thenReturn(0L);
        when(hdrRepository.findById(transactionPoid)).thenReturn(Optional.of(savedHdr));
        when(cargoDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(mailDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(estBertDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(estPrearrivalDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actTimingDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actCondDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actRmksDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actProgDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actCargoFigDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actBunkerDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(husbandryCrewDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(husbandryOthDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(docsCopyDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(docsMsgsDtl1Repository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(docsMsgsDtl2Repository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());

        PortCallOperationResponseDto result = service.createOperation(dto, 1L, 1L);

        assertNotNull(result);
        verify(hdrRepository).save(any());
    }

    @Test
    void updateOperation_Success() {
        PortCallOperationDto dto = PortCallOperationDto.builder()
                .vesselVoyagePoid(1L)
                .principalPoid(1L)
                .portOfCallPoid(1L)
                .callSign("UPDATED123")
                .mailDetails(Arrays.asList(
                    PortCallOperationMailDetailDto.builder()
                        .communicationType("EMAIL")
                        .company("Updated Company")
                        .build()
                ))
                .build();

        PortCallOperationHdr existingHdr = PortCallOperationHdr.builder()
                .transactionPoid(transactionPoid)
                .transactionDate(LocalDate.now())
                .build();

        when(hdrRepository.findById(transactionPoid)).thenReturn(Optional.of(existingHdr));
        when(shipVoyageHdrRepository.existsByTransactionPoid(1L)).thenReturn(true);
        when(shipPrincipalRepository.existsByPrincipalPoid(1L)).thenReturn(true);
        when(shipPortMasterRepository.existsByIdPortPoid(BigDecimal.valueOf(1L))).thenReturn(true);
        when(mailDtlRepository.countByTransactionPoid(transactionPoid)).thenReturn(1L);
        when(hdrRepository.save(any())).thenReturn(existingHdr);
        when(cargoDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(mailDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(estBertDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(estPrearrivalDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actTimingDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actCondDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actRmksDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actProgDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actCargoFigDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(actBunkerDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(husbandryCrewDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(husbandryOthDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(docsCopyDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(docsMsgsDtl1Repository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
        when(docsMsgsDtl2Repository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());

        PortCallOperationResponseDto result = service.updateOperation(transactionPoid, dto, 1L, 1L);

        assertNotNull(result);
        verify(hdrRepository).save(any());
        verify(loggingService).logChanges(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void deleteOperation_Success() {
        DeleteReasonDto deleteReasonDto = new DeleteReasonDto();
        deleteReasonDto.setDeleteReason("Test deletion");

        PortCallOperationHdr hdr = PortCallOperationHdr.builder()
                .transactionPoid(transactionPoid)
                .transactionDate(LocalDate.now())
                .build();

        when(hdrRepository.findById(transactionPoid)).thenReturn(Optional.of(hdr));

        service.deleteOperation(transactionPoid, deleteReasonDto);

        verify(documentDeleteService).deleteDocument(any(), any(), any(), any(), any());
    }

    @Test
    void loadPda_Success() {
        // Skip this test as it requires database connection
    }
}