package com.asg.operations.portcalloperation.service;

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
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
    private DocumentSearchService documentSearchService;
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

        PortCallOperationHdr hdr = PortCallOperationHdr.builder()
                .transactionPoid(transactionPoid)
                .vesselVoyagePoid(1L)
                .build();

        when(hdrRepository.existsById(transactionPoid)).thenReturn(true);
        when(hdrRepository.findById(transactionPoid)).thenReturn(Optional.of(hdr));
        when(hdrRepository.findMaxTransactionPoidByVesselVoyagePoid(1L)).thenReturn(transactionPoid);
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

        PortCallOperationHdr hdr = PortCallOperationHdr.builder()
                .transactionPoid(transactionPoid)
                .vesselVoyagePoid(1L)
                .build();

        when(estPrearrivalActDtlRepository.findById(any())).thenReturn(Optional.of(entity));
        when(hdrRepository.findById(transactionPoid)).thenReturn(Optional.of(hdr));
        when(hdrRepository.findMaxTransactionPoidByVesselVoyagePoid(1L)).thenReturn(transactionPoid);
        when(portActivityMasterRepository.existsByPortActivityTypePoid(2L)).thenReturn(true);
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

        PortCallOperationHdr hdr = PortCallOperationHdr.builder()
                .transactionPoid(transactionPoid)
                .vesselVoyagePoid(1L)
                .build();

        when(hdrRepository.existsById(transactionPoid)).thenReturn(true);
        when(hdrRepository.findById(transactionPoid)).thenReturn(Optional.of(hdr));
        when(hdrRepository.findMaxTransactionPoidByVesselVoyagePoid(1L)).thenReturn(transactionPoid);
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

        PortCallOperationHdr hdr = PortCallOperationHdr.builder()
                .transactionPoid(transactionPoid)
                .vesselVoyagePoid(1L)
                .build();

        when(actTimingsActvtyDtlRepository.findById(any())).thenReturn(Optional.of(entity));
        when(hdrRepository.findById(transactionPoid)).thenReturn(Optional.of(hdr));
        when(hdrRepository.findMaxTransactionPoidByVesselVoyagePoid(1L)).thenReturn(transactionPoid);
        when(portActivityMasterRepository.existsByPortActivityTypePoid(2L)).thenReturn(true);
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
        when(hdrRepository.findMaxTransactionPoidByVesselVoyagePoid(1L)).thenReturn(transactionPoid);
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
        when(docsCopyDtlRepository.findByTransactionPoid(transactionPoid)).thenReturn(Arrays.asList());
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
                .build();

        PortCallOperationHdr hdr = PortCallOperationHdr.builder()
                .transactionPoid(transactionPoid)
                .vesselVoyagePoid(1L)
                .build();

        when(docsCopyDtlRepository.findById(any())).thenReturn(Optional.of(entity));
        when(hdrRepository.findMaxTransactionPoidByVesselVoyagePoid(1L)).thenReturn(transactionPoid);
        when(docsCopyDtlRepository.save(any())).thenReturn(entity);
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
}