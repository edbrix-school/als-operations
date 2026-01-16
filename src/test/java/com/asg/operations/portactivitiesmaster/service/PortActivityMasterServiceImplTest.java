package com.asg.operations.portactivitiesmaster.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.operations.commonlov.dto.LovItem;
import com.asg.operations.commonlov.service.LovService;
import com.asg.operations.exceptions.ResourceNotFoundException;
import com.asg.operations.portactivitiesmaster.dto.GetAllPortActivityFilterRequest;
import com.asg.operations.portactivitiesmaster.dto.PortActivityMasterRequest;
import com.asg.operations.portactivitiesmaster.dto.PortActivityMasterResponse;
import com.asg.operations.portactivitiesmaster.dto.PortActivityListResponse;
import com.asg.operations.portactivitiesmaster.entity.PortActivityMaster;
import com.asg.operations.portactivitiesmaster.repository.PortActivityMasterRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortActivityMasterServiceImplTest {

    @Mock
    private PortActivityMasterRepository repository;

    @Mock
    private LovService lovService;

    @Mock
    private EntityManager entityManager;

    @Mock
    private com.asg.common.lib.service.LoggingService loggingService;

    @Mock
    private com.asg.common.lib.service.DocumentDeleteService documentDeleteService;

    @Mock
    private Query query;

    @Mock
    private Query countQuery;

    private MockedStatic<com.asg.common.lib.security.util.UserContext> mockedUserContext;

    @InjectMocks
    private PortActivityMasterServiceImpl service;

    private PortActivityMaster entity;
    private PortActivityMasterRequest request;
    private GetAllPortActivityFilterRequest filterRequest;
    private Long groupPoid = 1L;
    private String userId = "testUser";

    @BeforeEach
    void setUp() {
        mockedUserContext = mockStatic(com.asg.common.lib.security.util.UserContext.class);
        mockedUserContext.when(com.asg.common.lib.security.util.UserContext::getGroupPoid).thenReturn(100L);
        mockedUserContext.when(com.asg.common.lib.security.util.UserContext::getCompanyPoid).thenReturn(200L);
        mockedUserContext.when(com.asg.common.lib.security.util.UserContext::getUserPoid).thenReturn(1L);

        entity = PortActivityMaster.builder()
                .portActivityTypePoid(1L)
                .groupPoid(groupPoid)
                .portActivityTypeCode("PA1")
                .portActivityTypeName("Test Activity")
                .portActivityTypeName2("Test Activity 2")
                .active("Y")
                .seqno(1L)
                .createdBy(userId)
                .createdDate(LocalDateTime.now())
                .deleted("N")
                .remarks("Test remarks")
                .build();

        request = PortActivityMasterRequest.builder()
                .portActivityTypeName("Test Activity")
                .portActivityTypeName2("Test Activity 2")
                .active("Y")
                .seqno(1L)
                .remarks("Test remarks")
                .build();

        filterRequest = new GetAllPortActivityFilterRequest();
        filterRequest.setIsDeleted("N");
        filterRequest.setOperator("AND");
        filterRequest.setFilters(Collections.emptyList());

        LovItem mockLovItem = new LovItem(1L, "GRP1", "Test Group", "Test Group", 1L, 1);
        lenient().when(lovService.getLovItemByPoid(any(), any(), any(), any(), any())).thenReturn(mockLovItem);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        if (mockedUserContext != null) {
            mockedUserContext.close();
        }
    }

    @Test
    void getAllPortActivitiesWithFilters_ShouldReturnPageResponse() {
        Object[] mockRow = {
            1L, 1L, "PA1", "Test Activity", "Test Activity 2", "Y", 1L,
            "testUser", Timestamp.valueOf(LocalDateTime.now()), "testUser",
            Timestamp.valueOf(LocalDateTime.now()), "N", "Test remarks"
        };

        when(entityManager.createNativeQuery(anyString())).thenReturn(query).thenReturn(countQuery);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setFirstResult(anyInt())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(countQuery.setParameter(anyString(), any())).thenReturn(countQuery);
        List<Object[]> mockResultList = new java.util.ArrayList<>();
        mockResultList.add(mockRow);
        when(query.getResultList()).thenReturn(mockResultList);
        when(countQuery.getSingleResult()).thenReturn(1L);

        Page<PortActivityListResponse> result = service.getAllPortActivitiesWithFilters(
                groupPoid, filterRequest, 0, 20, null);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("PA1", result.getContent().get(0).getPortActivityTypeCode());
        verify(entityManager, times(2)).createNativeQuery(anyString());
    }

    @Test
    void getPortActivityById_ShouldReturnResponse_WhenExists() {
        when(repository.findByPortActivityTypePoidAndGroupPoid(1L, groupPoid))
                .thenReturn(Optional.of(entity));

        PortActivityMasterResponse result = service.getPortActivityById(1L, groupPoid);

        assertNotNull(result);
        assertEquals(1L, result.getPortActivityTypePoid());
        assertEquals("PA1", result.getPortActivityTypeCode());
    }

    @Test
    void getPortActivityById_ShouldThrowException_WhenNotFound() {
        when(repository.findByPortActivityTypePoidAndGroupPoid(1L, groupPoid))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getPortActivityById(1L, groupPoid));
    }

    @Test
    void createPortActivity_ShouldReturnResponse() {
        when(repository.findMaxCodeSequence("PA", groupPoid)).thenReturn(0);
        when(repository.save(any(PortActivityMaster.class))).thenReturn(entity);

        PortActivityMasterResponse result = service.createPortActivity(request, groupPoid, userId);

        assertNotNull(result);
        assertEquals("PA1", result.getPortActivityTypeCode());
        verify(repository).save(any(PortActivityMaster.class));
    }

    @Test
    void updatePortActivity_ShouldReturnUpdatedResponse() {
        when(repository.findByPortActivityTypePoidAndGroupPoid(1L, groupPoid))
                .thenReturn(Optional.of(entity));
        when(repository.save(any(PortActivityMaster.class))).thenReturn(entity);

        PortActivityMasterResponse result = service.updatePortActivity(1L, request, groupPoid, userId);

        assertNotNull(result);
        verify(repository).save(entity);
    }

    @Test
    void updatePortActivity_ShouldThrowException_WhenNotFound() {
        when(repository.findByPortActivityTypePoidAndGroupPoid(1L, groupPoid))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                service.updatePortActivity(1L, request, groupPoid, userId));
    }

    @Test
    void deletePortActivity_SoftDelete_ShouldMarkAsDeleted() {
        when(repository.findByPortActivityTypePoidAndGroupPoid(1L, groupPoid))
                .thenReturn(Optional.of(entity));

        service.deletePortActivity(1L, groupPoid, userId, new DeleteReasonDto());

        verify(documentDeleteService).deleteDocument(eq(1L), anyString(), anyString(), any(), any());
    }

    @Test
    void deletePortActivity_HardDelete_ShouldDeleteEntity() {
        when(repository.findByPortActivityTypePoidAndGroupPoid(1L, groupPoid))
                .thenReturn(Optional.of(entity));

        service.deletePortActivity(1L, groupPoid, userId,  new DeleteReasonDto());

        verify(documentDeleteService).deleteDocument(eq(1L), anyString(), anyString(), any(), any());
    }

    @Test
    void deletePortActivity_ShouldThrowException_WhenNotFound() {
        when(repository.findByPortActivityTypePoidAndGroupPoid(1L, groupPoid))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                service.deletePortActivity(1L, groupPoid, userId,  new DeleteReasonDto()));
    }
}