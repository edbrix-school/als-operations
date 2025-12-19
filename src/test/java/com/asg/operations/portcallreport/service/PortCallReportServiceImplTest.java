package com.asg.operations.portcallreport.service;

import com.asg.operations.commonlov.dto.LovItem;
import com.asg.operations.commonlov.dto.LovResponse;
import com.asg.operations.commonlov.service.LovService;
import com.asg.operations.exceptions.CustomException;
import com.asg.operations.exceptions.ResourceAlreadyExistsException;
import com.asg.operations.exceptions.ResourceNotFoundException;
import com.asg.operations.portactivitiesmaster.repository.PortActivityMasterRepository;
import com.asg.operations.portcallreport.dto.*;
import com.asg.operations.portcallreport.entity.PortCallReportDtl;
import com.asg.operations.portcallreport.entity.PortCallReportHdr;
import com.asg.operations.portcallreport.repository.PortCallReportDtlRepository;
import com.asg.operations.portcallreport.repository.PortCallReportHdrRepository;
import com.asg.operations.user.entity.User;
import com.asg.operations.user.repository.UserRepository;
import com.asg.operations.vesseltype.entity.VesselType;
import com.asg.operations.vesseltype.repository.VesselTypeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortCallReportServiceImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private PortCallReportHdrRepository hdrRepository;

    @Mock
    private PortCallReportDtlRepository dtlRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VesselTypeRepository vesselTypeRepository;

    @Mock
    private PortActivityMasterRepository portActivityMasterRepository;

    @Mock
    private LovService lovService;

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    @Mock
    private Query countQuery;

    @InjectMocks
    private PortCallReportServiceImpl service;

    private PortCallReportHdr entity;
    private PortCallReportDto request;
    private GetAllPortCallReportFilterRequest filterRequest;
    private Long groupPoid = 1L;
    private Long userPoid = 1L;

    @BeforeEach
    void setUp() {
        entity = PortCallReportHdr.builder()
                .portCallReportPoid(1L)
                .portCallReportId("PCR00001")
                .portCallReportName("Test Report")
                .portCallApplVesselType("1,2")
                .active("Y")
                .seqno(1L)
                .remarks("Test remarks")
                .build();

        request = PortCallReportDto.builder()
                .portCallReportName("Test Report")
                .portCallApplVesselType(List.of("1", "2"))
                .active("Y")
                .seqno(1L)
                .remarks("Test remarks")
                .build();

        filterRequest = new GetAllPortCallReportFilterRequest();
        filterRequest.setIsDeleted("N");
        filterRequest.setOperator("AND");
        filterRequest.setFilters(Collections.emptyList());

        LovResponse mockLovResponse = new LovResponse();
        mockLovResponse.setItems(List.of(new LovItem(1L, "VT1", "Vessel Type 1", "Vessel Type 1", 1L, 1)));
        lenient().when(lovService.getLovList(any(), any(), any(), any(), any(), any())).thenReturn(mockLovResponse);
    }

    @Test
    void getAllPortCallReportsWithFilters_ShouldReturnPageResponse() {
        Object[] mockRow = {
            1L, "PCR00001", "Test Report", "1,2", "Y", 1L, "Test remarks",
            "testUser", Timestamp.valueOf(LocalDateTime.now()), "testUser",
            Timestamp.valueOf(LocalDateTime.now()), "N"
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

        Page<PortCallReportListResponse> result = service.getAllPortCallReportsWithFilters(
                groupPoid, filterRequest, 0, 20, null);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("PCR00001", result.getContent().get(0).getPortCallReportId());
        verify(entityManager, times(2)).createNativeQuery(anyString());
    }
}