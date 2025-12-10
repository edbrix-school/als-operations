package com.asg.operations.portactivitiesmaster.service;

import com.asg.operations.commonlov.dto.LovItem;
import com.asg.operations.commonlov.service.LovService;
import com.asg.operations.portactivitiesmaster.dto.PageResponse;
import com.asg.operations.portactivitiesmaster.dto.PortActivityMasterRequest;
import com.asg.operations.portactivitiesmaster.dto.PortActivityMasterResponse;
import com.asg.operations.portactivitiesmaster.entity.PortActivityMaster;
import com.asg.operations.portactivitiesmaster.repository.PortActivityMasterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
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

    @InjectMocks
    private PortActivityMasterServiceImpl service;

    private PortActivityMaster entity;
    private PortActivityMasterRequest request;
    private Long groupPoid = 1L;
    private String userId = "testUser";

    @BeforeEach
    void setUp() {
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

        // Mock LovService with lenient stubbing to avoid UnnecessaryStubbingException
        LovItem mockLovItem = new LovItem(1L, "GRP1", "Test Group", "Test Group", 1L, 1);
        lenient().when(lovService.getLovItemByPoid(any(), any(), any(), any(), any())).thenReturn(mockLovItem);
    }

    @Test
    void getPortActivityList_ShouldReturnPageResponse() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<PortActivityMaster> page = new PageImpl<>(List.of(entity));
        when(repository.findByGroupPoidAndFilters(eq(groupPoid), any(), any(), any(), eq(pageable)))
                .thenReturn(page);

        // When
        PageResponse<PortActivityMasterResponse> result = service.getPortActivityList(
                "PA1", "Test", "Y", groupPoid, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("PA1", result.getContent().get(0).getPortActivityTypeCode());
        verify(repository).findByGroupPoidAndFilters(groupPoid, "PA1", "Test", "Y", pageable);
    }

    @Test
    void getPortActivityById_ShouldReturnResponse_WhenExists() {
        // Given
        when(repository.findByPortActivityTypePoidAndGroupPoidAndDeleted(1L, groupPoid, "N"))
                .thenReturn(Optional.of(entity));

        // When
        PortActivityMasterResponse result = service.getPortActivityById(1L, groupPoid);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getPortActivityTypePoid());
        assertEquals("PA1", result.getPortActivityTypeCode());
    }

    @Test
    void getPortActivityById_ShouldThrowException_WhenNotFound() {
        // Given
        when(repository.findByPortActivityTypePoidAndGroupPoidAndDeleted(1L, groupPoid, "N"))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> service.getPortActivityById(1L, groupPoid));
    }

    @Test
    void createPortActivity_ShouldReturnResponse() {
        // Given
        when(repository.findMaxCodeSequence("PA", groupPoid)).thenReturn(0);
        when(repository.save(any(PortActivityMaster.class))).thenReturn(entity);

        // When
        PortActivityMasterResponse result = service.createPortActivity(request, groupPoid, userId);

        // Then
        assertNotNull(result);
        assertEquals("PA1", result.getPortActivityTypeCode());
        verify(repository).save(any(PortActivityMaster.class));
    }

    @Test
    void updatePortActivity_ShouldReturnUpdatedResponse() {
        // Given
        when(repository.findByPortActivityTypePoidAndGroupPoidAndDeleted(1L, groupPoid, "N"))
                .thenReturn(Optional.of(entity));
        when(repository.save(any(PortActivityMaster.class))).thenReturn(entity);

        // When
        PortActivityMasterResponse result = service.updatePortActivity(1L, request, groupPoid, userId);

        // Then
        assertNotNull(result);
        verify(repository).save(entity);
    }

    @Test
    void updatePortActivity_ShouldThrowException_WhenNotFound() {
        // Given
        when(repository.findByPortActivityTypePoidAndGroupPoidAndDeleted(1L, groupPoid, "N"))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> 
                service.updatePortActivity(1L, request, groupPoid, userId));
    }

    @Test
    void deletePortActivity_SoftDelete_ShouldMarkAsDeleted() {
        // Given
        when(repository.findByPortActivityTypePoidAndGroupPoidAndDeleted(1L, groupPoid, "N"))
                .thenReturn(Optional.of(entity));

        // When
        service.deletePortActivity(1L, groupPoid, userId, false);

        // Then
        assertEquals("Y", entity.getDeleted());
        assertEquals("N", entity.getActive());
        verify(repository).save(entity);
        verify(repository, never()).delete(any());
    }

    @Test
    void deletePortActivity_HardDelete_ShouldDeleteEntity() {
        // Given
        when(repository.findByPortActivityTypePoidAndGroupPoidAndDeleted(1L, groupPoid, "N"))
                .thenReturn(Optional.of(entity));

        // When
        service.deletePortActivity(1L, groupPoid, userId, true);

        // Then
        verify(repository).delete(entity);
        verify(repository, never()).save(any());
    }

    @Test
    void deletePortActivity_ShouldThrowException_WhenNotFound() {
        // Given
        when(repository.findByPortActivityTypePoidAndGroupPoidAndDeleted(1L, groupPoid, "N"))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> 
                service.deletePortActivity(1L, groupPoid, userId, false));
    }
}