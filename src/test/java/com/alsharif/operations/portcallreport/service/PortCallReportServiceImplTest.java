package com.alsharif.operations.portcallreport.service;

import com.alsharif.operations.commonlov.service.LovService;
import com.alsharif.operations.portactivity.repository.PortActivityMasterRepository;
import com.alsharif.operations.portcallreport.dto.PortCallReportDetailDto;
import com.alsharif.operations.portcallreport.dto.PortCallReportDto;
import com.alsharif.operations.portcallreport.dto.PortCallReportResponseDto;
import com.alsharif.operations.portcallreport.entity.PortCallReportDtl;
import com.alsharif.operations.portcallreport.entity.PortCallReportHdr;
import com.alsharif.operations.portcallreport.repository.PortCallReportDtlRepository;
import com.alsharif.operations.portcallreport.repository.PortCallReportHdrRepository;
import com.alsharif.operations.user.entity.User;
import com.alsharif.operations.user.repository.UserRepository;
import com.alsharif.operations.vesseltype.entity.VesselType;
import com.alsharif.operations.vesseltype.repository.VesselTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;

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

    @InjectMocks
    private PortCallReportServiceImpl service;

    @Test
    void getReportList_ShouldReturnPagedReports() {
        PortCallReportHdr hdr = PortCallReportHdr.builder()
                .portCallReportPoid(1L)
                .portCallReportId("PCR00001")
                .portCallReportName("Test Report")
                .build();
        Page<PortCallReportHdr> page = new PageImpl<>(List.of(hdr));

        when(hdrRepository.findAllNonDeletedWithSearch(any(), any(Pageable.class))).thenReturn(page);

        Page<PortCallReportResponseDto> result = service.getReportList("test", Pageable.unpaged());

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("PCR00001", result.getContent().get(0).getPortCallReportId());
        verify(hdrRepository).findAllNonDeletedWithSearch(any(), any(Pageable.class));
    }

    @Test
    void getReportById_ShouldReturnReport() {
        PortCallReportHdr hdr = PortCallReportHdr.builder()
                .portCallReportPoid(1L)
                .portCallReportId("PCR00001")
                .portCallReportName("Test Report")
                .build();

        when(hdrRepository.findById(1L)).thenReturn(Optional.of(hdr));
        when(dtlRepository.findByPortCallReportPoid(1L)).thenReturn(List.of());

        PortCallReportResponseDto result = service.getReportById(1L);

        assertNotNull(result);
        assertEquals("PCR00001", result.getPortCallReportId());
        verify(hdrRepository).findById(1L);
    }

    @Test
    void getReportById_ShouldThrowException_WhenNotFound() {
        when(hdrRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.getReportById(1L));
        verify(hdrRepository).findById(1L);
    }

    @Test
    void createReport_ShouldCreateReport() {
        PortCallReportDto dto = PortCallReportDto.builder()
                .portCallReportName("New Report")
                .active("Y")
                .build();

        User user = User.builder().userPoid(1L).userId("testuser").build();
        PortCallReportHdr savedHdr = PortCallReportHdr.builder()
                .portCallReportPoid(1L)
                .portCallReportId("PCR00001")
                .portCallReportName("New Report")
                .build();

        when(hdrRepository.existsByPortCallReportNameIgnoreCaseAndNotDeleted(any(), any())).thenReturn(false);
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class))).thenReturn(0L);
        when(userRepository.findByUserPoid(1L)).thenReturn(Optional.of(user));
        when(hdrRepository.save(any())).thenReturn(savedHdr);
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(savedHdr));
        when(dtlRepository.findByPortCallReportPoid(1L)).thenReturn(List.of());

        PortCallReportResponseDto result = service.createReport(dto, 1L, 100L);

        assertNotNull(result);
        assertEquals("PCR00001", result.getPortCallReportId());
        verify(hdrRepository).save(any());
    }

    @Test
    void createReport_ShouldThrowException_WhenDuplicateName() {
        PortCallReportDto dto = PortCallReportDto.builder()
                .portCallReportName("Duplicate Report")
                .build();

        when(hdrRepository.existsByPortCallReportNameIgnoreCaseAndNotDeleted(any(), any())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> service.createReport(dto, 1L, 1L));
        verify(hdrRepository, never()).save(any());
    }

    @Test
    void createReport_ShouldThrowException_WhenInvalidVesselType() {
        PortCallReportDto dto = PortCallReportDto.builder()
                .portCallReportName("New Report")
                .portCallApplVesselType(List.of("INVALID"))
                .build();

        when(hdrRepository.existsByPortCallReportNameIgnoreCaseAndNotDeleted(any(), any())).thenReturn(false);
        when(vesselTypeRepository.findAllActive()).thenReturn(List.of());

        assertThrows(RuntimeException.class, () -> service.createReport(dto, 1L, 1L));
        verify(hdrRepository, never()).save(any());
    }

    @Test
    void createReport_ShouldThrowException_WhenActivityTypeNull() {
        PortCallReportDetailDto detailDto = PortCallReportDetailDto.builder()
                .detRowId(1L)
                .portActivityTypePoid(null)
                .build();

        PortCallReportDto dto = PortCallReportDto.builder()
                .portCallReportName("New Report")
                .details(List.of(detailDto))
                .build();

        when(hdrRepository.existsByPortCallReportNameIgnoreCaseAndNotDeleted(any(), any())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> service.createReport(dto, 1L, 1L));
        verify(hdrRepository, never()).save(any());
    }

    @Test
    void createReport_ShouldThrowException_WhenInvalidActivityType() {
        PortCallReportDetailDto detailDto = PortCallReportDetailDto.builder()
                .detRowId(1L)
                .portActivityTypePoid(999L)
                .build();

        PortCallReportDto dto = PortCallReportDto.builder()
                .portCallReportName("New Report")
                .details(List.of(detailDto))
                .build();

        when(hdrRepository.existsByPortCallReportNameIgnoreCaseAndNotDeleted(any(), any())).thenReturn(false);
        when(portActivityMasterRepository.existsById(999L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> service.createReport(dto, 1L, 1L));
        verify(hdrRepository, never()).save(any());
    }

    @Test
    void updateReport_ShouldUpdateReport() {
        PortCallReportDto dto = PortCallReportDto.builder()
                .portCallReportName("Updated Report")
                .active("Y")
                .build();

        User user = User.builder().userPoid(1L).userId("testuser").build();
        PortCallReportHdr existingHdr = PortCallReportHdr.builder()
                .portCallReportPoid(1L)
                .portCallReportId("PCR00001")
                .portCallReportName("Old Report")
                .build();

        when(hdrRepository.existsByPortCallReportNameIgnoreCaseAndNotDeleted(any(), eq(1L))).thenReturn(false);
        when(userRepository.findByUserPoid(1L)).thenReturn(Optional.of(user));
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(existingHdr));
        when(hdrRepository.save(any())).thenReturn(existingHdr);
        when(dtlRepository.findByPortCallReportPoid(1L)).thenReturn(List.of());

        PortCallReportResponseDto result = service.updateReport(1L, dto, 1L, 100L);

        assertNotNull(result);
        verify(hdrRepository).save(any());
    }

    @Test
    void deleteReport_ShouldMarkAsDeleted() {
        PortCallReportHdr hdr = PortCallReportHdr.builder()
                .portCallReportPoid(1L)
                .portCallReportId("PCR00001")
                .build();

        when(hdrRepository.findById(1L)).thenReturn(Optional.of(hdr));

        service.deleteReport(1L);

        verify(hdrRepository).save(argThat(h -> "Y".equals(h.getDeleted()) && "N".equals(h.getActive())));
    }

    @Test
    void getVesselTypes_ShouldReturnVesselTypes() {
        VesselType vesselType = VesselType.builder()
                .vesselTypeCode("TANKER")
                .vesselTypeName("Tanker")
                .build();

        when(vesselTypeRepository.findAllActive()).thenReturn(List.of(vesselType));

        var result = service.getVesselTypes();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TANKER", result.get(0).get("value"));
        verify(vesselTypeRepository).findAllActive();
    }
}
