package com.asg.operations.projectjob.service.impl;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.operations.common.repository.GlobalAddressDetailsRepository;
import com.asg.operations.common.repository.GlobalAddressMasterRepository;
import com.asg.operations.exceptions.ResourceNotFoundException;
import com.asg.operations.exceptions.ValidationException;
import com.asg.operations.projectjob.dto.*;
import com.asg.operations.projectjob.entity.*;
import com.asg.operations.projectjob.repository.*;
import com.asg.operations.projects.repository.FFProjectsCtrlSheetDtlRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectJobServiceImplTest {

    @Mock private FFManifestHdrRepository hdrRepository;
    @Mock private FFManifestChargesDtlRepository chargesRepository;
    @Mock private FFManifestAirPkgDtlRepository airPkgRepository;
    @Mock private FFManifestBayanDtlRepository bayanRepository;
    @Mock private FFManifestContainerDtlRepository containerRepository;
    @Mock private FFManifestTruckDtlRepository truckRepository;
    @Mock private ProjectJobStoredProcRepository spRepostirory;
    @Mock private LoggingService loggingService;
    @Mock private DocumentDeleteService documentDeleteService;
    @Mock private DocumentSearchService documentSearchService;
    @Mock private FFProjectsCtrlSheetDtlRepository ctrlSheetDtlRepository;
    @Mock private GlobalAddressMasterRepository addressMasterRepository;
    @Mock private GlobalAddressDetailsRepository addressDetailsRepository;

    private ProjectJobServiceImpl projectJobService;
    private MockedStatic<UserContext> userContextMockedStatic;

    @BeforeEach
    void setUp() {
        // Explicit constructor injection avoids @InjectMocks matching issues with
        // @RequiredArgsConstructor when the constructor arity changes.
        projectJobService = new ProjectJobServiceImpl(
                hdrRepository, chargesRepository, airPkgRepository, bayanRepository,
                containerRepository, truckRepository, spRepostirory, loggingService,
                documentDeleteService, documentSearchService, ctrlSheetDtlRepository,
                addressMasterRepository, addressDetailsRepository);

        userContextMockedStatic = mockStatic(UserContext.class);
        userContextMockedStatic.when(UserContext::getGroupPoid).thenReturn(1L);
        userContextMockedStatic.when(UserContext::getCompanyPoid).thenReturn(2L);
        userContextMockedStatic.when(UserContext::getUserPoid).thenReturn(3L);
        userContextMockedStatic.when(UserContext::getUserId).thenReturn("USER1");
        userContextMockedStatic.when(UserContext::getDocumentId).thenReturn("DOC123");
    }

    @AfterEach
    void tearDown() {
        userContextMockedStatic.close();
    }

    private void stubSaveAndFlush(FFManifestHdr hdr) {
        doReturn(hdr).when(hdrRepository).saveAndFlush(any());
    }

    // Stubs findById for both the post-save refresh and the final getById call.
    private void stubFindById(FFManifestHdr hdr) {
        doReturn(Optional.of(hdr)).when(hdrRepository).findById(any());
    }

    @Test
    void testCreate() {
        ProjectJobRequest request = new ProjectJobRequest();
        FFManifestHdr hdr = new FFManifestHdr();
        hdr.setTransactionPoid(100L);

        stubSaveAndFlush(hdr);
        stubFindById(hdr);

        ProjectJobResponse response = projectJobService.create(request);

        assertNotNull(response);
        verify(loggingService).createLogSummaryEntry(anyString(), anyString(), anyString());
    }

    @Test
    void testUpdate_Success() {
        ProjectJobRequest request = new ProjectJobRequest();
        FFManifestHdr hdr = new FFManifestHdr();
        hdr.setTransactionPoid(100L);

        when(hdrRepository.findById(100L)).thenReturn(Optional.of(hdr));
        when(hdrRepository.save(any(FFManifestHdr.class))).thenReturn(hdr);

        ProjectJobResponse response = projectJobService.update(100L, request);

        assertNotNull(response);
        verify(loggingService).logChanges(any(), any(), any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void testUpdate_NotFound() {
        when(hdrRepository.findById(100L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> projectJobService.update(100L, new ProjectJobRequest()));
    }

    @Test
    void testProcessDetails_ISCREATED_New() {
        ProjectJobAirPkgDtoRequest airPkgDto = new ProjectJobAirPkgDtoRequest();
        airPkgDto.setActionType("ISCREATED");
        ProjectJobRequest request = new ProjectJobRequest();
        request.setAirPackages(Collections.singletonList(airPkgDto));

        FFManifestHdr hdr = new FFManifestHdr();
        hdr.setTransactionPoid(100L);
        stubSaveAndFlush(hdr);
        stubFindById(hdr);
        when(airPkgRepository.getMaxDetRowId(100L)).thenReturn(0L);
        when(airPkgRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        projectJobService.create(request);

        verify(airPkgRepository).saveAll(anyList());
    }

    @Test
    void testProcessDetails_ISCREATED_ExistingDetRowId() {
        ProjectJobAirPkgDtoRequest airPkgDto = new ProjectJobAirPkgDtoRequest();
        airPkgDto.setActionType("ISCREATED");
        airPkgDto.setDetRowId(5L);
        ProjectJobRequest request = new ProjectJobRequest();
        request.setAirPackages(Collections.singletonList(airPkgDto));

        FFManifestHdr hdr = new FFManifestHdr();
        hdr.setTransactionPoid(100L);
        stubSaveAndFlush(hdr);
        stubFindById(hdr);
        when(airPkgRepository.getMaxDetRowId(100L)).thenReturn(0L);
        when(airPkgRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        projectJobService.create(request);

        verify(airPkgRepository).saveAll(argThat(list ->
                ((List<FFManifestAirPkgDtl>) list).get(0).getDetRowId() == 5L));
    }

    @Test
    void testProcessDetails_ISUPDATED_Success() {
        ProjectJobAirPkgDtoRequest airPkgDto = new ProjectJobAirPkgDtoRequest();
        airPkgDto.setActionType("ISUPDATED");
        airPkgDto.setDetRowId(1L);
        ProjectJobRequest request = new ProjectJobRequest();
        request.setAirPackages(Collections.singletonList(airPkgDto));

        FFManifestHdr hdr = new FFManifestHdr();
        hdr.setTransactionPoid(100L);
        stubSaveAndFlush(hdr);
        stubFindById(hdr);
        when(airPkgRepository.findByTransactionPoidAndDetRowId(100L, 1L))
                .thenReturn(Optional.of(new FFManifestAirPkgDtl()));

        projectJobService.create(request);

        verify(airPkgRepository).saveAll(anyCollection());
        verify(loggingService).createLogBatch(anyList());
    }

    @Test
    void testProcessDetails_ISUPDATED_NotFound() {
        ProjectJobAirPkgDtoRequest airPkgDto = new ProjectJobAirPkgDtoRequest();
        airPkgDto.setActionType("ISUPDATED");
        airPkgDto.setDetRowId(1L);
        ProjectJobRequest request = new ProjectJobRequest();
        request.setAirPackages(Collections.singletonList(airPkgDto));

        FFManifestHdr hdr = new FFManifestHdr();
        hdr.setTransactionPoid(100L);
        stubSaveAndFlush(hdr);
        stubFindById(hdr);
        when(airPkgRepository.findByTransactionPoidAndDetRowId(100L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectJobService.create(request));
    }

    @Test
    void testProcessDetails_ISDELETED() {
        ProjectJobAirPkgDtoRequest airPkgDto = new ProjectJobAirPkgDtoRequest();
        airPkgDto.setActionType("ISDELETED");
        airPkgDto.setDetRowId(1L);
        ProjectJobRequest request = new ProjectJobRequest();
        request.setAirPackages(Collections.singletonList(airPkgDto));

        FFManifestHdr hdr = new FFManifestHdr();
        hdr.setTransactionPoid(100L);
        stubSaveAndFlush(hdr);
        stubFindById(hdr);

        projectJobService.create(request);

        verify(airPkgRepository).deleteByTransactionPoidAndDetRowIdIn(eq(100L), anyList());
        verify(loggingService).logDelete(any(), anyString(), anyString());
    }

    @Test
    void testProcessDetails_ActionTypeMissing() {
        ProjectJobAirPkgDtoRequest airPkgDto = new ProjectJobAirPkgDtoRequest();
        airPkgDto.setActionType("");
        ProjectJobRequest request = new ProjectJobRequest();
        request.setAirPackages(Collections.singletonList(airPkgDto));

        FFManifestHdr hdr = new FFManifestHdr();
        hdr.setTransactionPoid(100L);
        stubSaveAndFlush(hdr);
        stubFindById(hdr);

        assertThrows(ValidationException.class, () -> projectJobService.create(request));
    }

    @Test
    void testProcessDetails_DefaultAction() {
        ProjectJobAirPkgDtoRequest airPkgDto = new ProjectJobAirPkgDtoRequest();
        airPkgDto.setActionType("UNKNOWN");
        ProjectJobRequest request = new ProjectJobRequest();
        request.setAirPackages(Collections.singletonList(airPkgDto));

        FFManifestHdr hdr = new FFManifestHdr();
        hdr.setTransactionPoid(100L);
        stubSaveAndFlush(hdr);
        stubFindById(hdr);

        projectJobService.create(request);

        verify(airPkgRepository, never()).saveAll(anyList());
    }

    @Test
    void testGetById_Success() {
        FFManifestHdr hdr = new FFManifestHdr();
        hdr.setTransactionPoid(100L);
        when(hdrRepository.findById(100L)).thenReturn(Optional.of(hdr));
        when(airPkgRepository.findByTransactionPoid(100L)).thenReturn(Collections.singletonList(new FFManifestAirPkgDtl()));
        when(bayanRepository.findByTransactionPoid(100L)).thenReturn(Collections.singletonList(new FFManifestBayanDtl()));
        when(chargesRepository.findByTransactionPoid(100L)).thenReturn(Collections.singletonList(new FFManifestChargesDtl()));
        when(containerRepository.findByTransactionPoid(100L)).thenReturn(Collections.singletonList(new FFManifestContainerDtl()));
        when(truckRepository.findByTransactionPoid(100L)).thenReturn(Collections.singletonList(new FFManifestTruckDtl()));

        ProjectJobResponse response = projectJobService.getById(100L);

        assertNotNull(response);
        assertEquals(1, response.getAirPackages().size());
    }

    @Test
    void testGetById_NotFound() {
        when(hdrRepository.findById(100L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> projectJobService.getById(100L));
    }

    @Test
    void testDeleteById_Success() {
        FFManifestHdr hdr = new FFManifestHdr();
        hdr.setTransactionPoid(100L);
        hdr.setTransactionDate(LocalDate.now());
        when(hdrRepository.findById(100L)).thenReturn(Optional.of(hdr));

        projectJobService.deleteById(100L, 1L, 2L, 3L, new DeleteReasonDto());

        verify(documentDeleteService).deleteDocument(eq(100L), anyString(), anyString(), any(), any());
    }

    @Test
    void testDeleteById_NotFound() {
        when(hdrRepository.findById(100L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> projectJobService.deleteById(100L, 1L, 2L, 3L, new DeleteReasonDto()));
    }

    @Test
    void testGetAllProjectJobsWithFilters() {
        FilterRequestDto filterRequest = new FilterRequestDto("AND", "N", new ArrayList<>());
        RawSearchResult rawResult = new RawSearchResult(new ArrayList<>(), new HashMap<>(), 1L);

        when(documentSearchService.resolveOperator(any())).thenReturn("AND");
        when(documentSearchService.resolveIsDeleted(any())).thenReturn("N");
        when(documentSearchService.resolveDateFilters(any(), anyString(), any(), any())).thenReturn(new ArrayList<>());
        when(documentSearchService.search(
                anyString(), anyList(), anyString(), any(Pageable.class),
                anyString(), anyString(), anyString())).thenReturn(rawResult);

        Map<String, Object> result = projectJobService.getAllProjectJobsWithFilters(
                "DOC123", filterRequest, Pageable.unpaged(), null, null);

        assertNotNull(result);
    }

    @Test
    void testReopenJob() {
        when(spRepostirory.callReopenJobProc(anyLong(), anyLong())).thenReturn("Success");
        assertEquals("Success", projectJobService.reopenJob(100L));
    }

    @Test
    void testLoadJobs() {
        ProjectLoadInJobsProcResponse response = new ProjectLoadInJobsProcResponse();
        when(spRepostirory.callProjectsLoadInJobsProc(100L)).thenReturn(response);
        assertEquals(response, projectJobService.loadJobs(100L));
    }
}
