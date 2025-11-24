package com.alsharif.operations.portcallreport.service;

import com.alsharif.operations.portactivity.repository.PortActivityMasterRepository;
import com.alsharif.operations.portcallreport.dto.PortCallReportDetailDto;
import com.alsharif.operations.portcallreport.dto.PortCallReportDto;
import com.alsharif.operations.portcallreport.entity.PortCallReportDtl;
import com.alsharif.operations.portcallreport.entity.PortCallReportHdr;
import com.alsharif.operations.portcallreport.repository.PortCallReportDtlRepository;
import com.alsharif.operations.portcallreport.repository.PortCallReportHdrRepository;
import com.alsharif.operations.user.entity.User;
import com.alsharif.operations.user.repository.UserRepository;
import com.alsharif.operations.vesseltype.entity.VesselType;
import com.alsharif.operations.vesseltype.repository.VesselTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortCallReportServiceImpl implements PortCallReportService {

    private final JdbcTemplate jdbcTemplate;
    private final PortCallReportHdrRepository hdrRepository;
    private final PortCallReportDtlRepository dtlRepository;
    private final UserRepository userRepository;
    private final VesselTypeRepository vesselTypeRepository;
    private final PortActivityMasterRepository portActivityMasterRepository;

    @Override
    public Page<PortCallReportDto> getReportList(String search, Pageable pageable) {
        log.info("Fetching port call report list with search: {}", search);
        
        Page<PortCallReportHdr> hdrPage = hdrRepository.findAllNonDeletedWithSearch(search, pageable);
        
        List<PortCallReportDto> reports = hdrPage.getContent().stream()
                .map(hdr -> {
                    String vesselTypes = hdr.getPortCallApplVesselType();
                    return PortCallReportDto.builder()
                            .portCallReportPoid(hdr.getPortCallReportPoid())
                            .portCallReportId(hdr.getPortCallReportId())
                            .portCallReportName(hdr.getPortCallReportName())
                            .portCallApplVesselType(vesselTypes != null ? List.of(vesselTypes.split(",")) : null)
                            .active(hdr.getActive())
                            .seqno(hdr.getSeqno())
                            .build();
                })
                .collect(Collectors.toList());
        
        return new PageImpl<>(reports, pageable, hdrPage.getTotalElements());
    }
    
    private String generateReportId() {
        Long maxId = jdbcTemplate.queryForObject(
                "SELECT NVL(MAX(TO_NUMBER(REGEXP_SUBSTR(PORT_CALL_REPORT_ID, '[0-9]+'))), 0) FROM OPS_PORT_CALL_REPORT_HDR",
                Long.class);
        return "PCR" + String.format("%05d", maxId + 1);
    }

    @Override
    public PortCallReportDto getReportById(Long id) {
        log.info("Fetching port call report by id: {}", id);
        
        PortCallReportHdr hdr = hdrRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Port call report not found"));
        
        List<PortCallReportDtl> details = dtlRepository.findByPortCallReportPoid(id);
        
        String vesselTypes = hdr.getPortCallApplVesselType();
        List<PortCallReportDetailDto> detailDtos = details.stream()
                .map(dtl -> PortCallReportDetailDto.builder()
                        .detRowId(dtl.getDetRowId())
                        .portCallReportPoid(dtl.getPortCallReportPoid())
                        .portActivityTypePoid(dtl.getPortActivityTypePoid())
                        .portActivityTypeName(dtl.getPortActivityMaster() != null ? dtl.getPortActivityMaster().getPortActivityTypeName() : null)
                        .activityMandatory(dtl.getActivityMandatory())
                        .build())
                .collect(Collectors.toList());
        
        return PortCallReportDto.builder()
                .portCallReportPoid(hdr.getPortCallReportPoid())
                .portCallReportId(hdr.getPortCallReportId())
                .portCallReportName(hdr.getPortCallReportName())
                .portCallApplVesselType(vesselTypes != null ? List.of(vesselTypes.split(",")) : null)
                .active(hdr.getActive())
                .seqno(hdr.getSeqno())
                .remarks(hdr.getRemarks())
                .details(detailDtos)
                .build();
    }

    @Override
    @Transactional
    public PortCallReportDto createReport(PortCallReportDto dto, Long userPoid) {
        log.info("Creating port call report: {}", dto.getPortCallReportName());
        
        if (hdrRepository.existsByPortCallReportNameIgnoreCaseAndNotDeleted(dto.getPortCallReportName(), null)) {
            throw new RuntimeException("Port call report name already exists");
        }
        
        if (dto.getPortCallApplVesselType() != null && !dto.getPortCallApplVesselType().isEmpty()) {
            List<String> validVesselTypes = vesselTypeRepository.findAllActive().stream()
                    .map(VesselType::getVesselTypeCode)
                    .toList();
            for (String vesselType : dto.getPortCallApplVesselType()) {
                if (!validVesselTypes.contains(vesselType)) {
                    throw new RuntimeException("Invalid vessel type: " + vesselType);
                }
            }
        }
        
        if (dto.getDetails() != null && !dto.getDetails().isEmpty()) {
            for (PortCallReportDetailDto detail : dto.getDetails()) {
                if (detail.getPortActivityTypePoid() == null) {
                    throw new RuntimeException("Port activity type is required");
                }
                if (!portActivityMasterRepository.existsById(detail.getPortActivityTypePoid())) {
                    throw new RuntimeException("Invalid activity type ID: " + detail.getPortActivityTypePoid());
                }
            }
        }
        
        String reportId = generateReportId();
        
        User user = userRepository.findByUserPoid(userPoid)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        PortCallReportHdr hdr = PortCallReportHdr.builder()
                .portCallReportId(reportId)
                .portCallReportName(dto.getPortCallReportName())
                .portCallApplVesselType(dto.getPortCallApplVesselType() != null ? String.join(",", dto.getPortCallApplVesselType()) : null)
                .active(dto.getActive())
                .seqno(dto.getSeqno())
                .remarks(dto.getRemarks())
                .createdBy(user.getUserId())
                .build();
        
        hdr = hdrRepository.save(hdr);
        
        if (dto.getDetails() != null && !dto.getDetails().isEmpty()) {
            Long reportPoid = hdr.getPortCallReportPoid();
            List<PortCallReportDtl> details = dto.getDetails().stream()
                    .map(detailDto -> PortCallReportDtl.builder()
                            .portCallReportPoid(reportPoid)
                            .detRowId(detailDto.getDetRowId())
                            .portActivityTypePoid(detailDto.getPortActivityTypePoid())
                            .activityMandatory(detailDto.getActivityMandatory())
                            .createdBy(user.getUserId())
                            .build())
                    .collect(Collectors.toList());
            dtlRepository.saveAll(details);
        }
        
        return getReportById(hdr.getPortCallReportPoid());
    }

    @Override
    @Transactional
    public PortCallReportDto updateReport(Long id, PortCallReportDto dto, Long userPoid) {
        log.info("Updating port call report id: {}", id);
        
        if (hdrRepository.existsByPortCallReportNameIgnoreCaseAndNotDeleted(dto.getPortCallReportName(), id)) {
            throw new RuntimeException("Port call report name already exists");
        }
        
        if (dto.getPortCallApplVesselType() != null && !dto.getPortCallApplVesselType().isEmpty()) {
            List<String> validVesselTypes = vesselTypeRepository.findAllActive().stream()
                    .map(VesselType::getVesselTypeCode)
                    .toList();
            for (String vesselType : dto.getPortCallApplVesselType()) {
                if (!validVesselTypes.contains(vesselType)) {
                    throw new RuntimeException("Invalid vessel type: " + vesselType);
                }
            }
        }
        
        if (dto.getDetails() != null && !dto.getDetails().isEmpty()) {
            for (PortCallReportDetailDto detail : dto.getDetails()) {
                if (detail.getPortActivityTypePoid() == null) {
                    throw new RuntimeException("Port activity type is required");
                }
                if (!portActivityMasterRepository.existsById(detail.getPortActivityTypePoid())) {
                    throw new RuntimeException("Invalid activity type ID: " + detail.getPortActivityTypePoid());
                }
            }
        }
        
        User user = userRepository.findByUserPoid(userPoid)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        PortCallReportHdr hdr = hdrRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Port call report not found"));
        
        hdr.setPortCallReportName(dto.getPortCallReportName());
        hdr.setPortCallApplVesselType(dto.getPortCallApplVesselType() != null ? String.join(",", dto.getPortCallApplVesselType()) : null);
        hdr.setActive(dto.getActive());
        hdr.setSeqno(dto.getSeqno());
        hdr.setRemarks(dto.getRemarks());
        hdr.setLastModifiedBy(user.getUserId());
        
        hdrRepository.save(hdr);
        
        dtlRepository.deleteByPortCallReportPoid(id);
        
        if (dto.getDetails() != null && !dto.getDetails().isEmpty()) {
            List<PortCallReportDtl> details = dto.getDetails().stream()
                    .map(detailDto -> PortCallReportDtl.builder()
                            .portCallReportPoid(id)
                            .detRowId(detailDto.getDetRowId())
                            .portActivityTypePoid(detailDto.getPortActivityTypePoid())
                            .activityMandatory(detailDto.getActivityMandatory())
                            .createdBy(user.getUserId())
                            .build())
                    .collect(Collectors.toList());
            dtlRepository.saveAll(details);
        }
        
        return getReportById(id);
    }

    @Override
    @Transactional
    public void deleteReport(Long id) {
        log.info("Deleting port call report id: {}", id);
        
        PortCallReportHdr hdr = hdrRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Port call report not found"));
        
        hdr.setActive("N");
        hdr.setDeleted("Y");
        hdrRepository.save(hdr);
    }

    @Override
    public List<Map<String, Object>> getPortActivities(Long userPoid) {
        log.info("Fetching port activities");
        
        String sql = "{call PROC_PORT_ACTIVITIES_GET_LIST(?, ?)}";
        
        return jdbcTemplate.execute((Connection conn) -> {
            try (CallableStatement cs = conn.prepareCall(sql)) {
                cs.setLong(1, userPoid);
                cs.registerOutParameter(2, Types.REF_CURSOR);
                cs.execute();
                
                List<Map<String, Object>> result = new ArrayList<>();
                try (ResultSet rs = (ResultSet) cs.getObject(2)) {
                    while (rs.next()) {
                        Map<String, Object> activity = new HashMap<>();
                        activity.put("portActivityTypePoid", rs.getLong("PORT_ACTIVITY_TYPE_POID"));
                        activity.put("portActivityTypeCode", rs.getString("PORT_ACTIVITY_TYPE_CODE"));
                        activity.put("portActivityTypeName", rs.getString("PORT_ACTIVITY_TYPE_NAME"));
                        activity.put("portActivityTypeName2", rs.getString("PORT_ACTIVITY_TYPE_NAME2"));
                        result.add(activity);
                    }
                }
                return result;
            }
        });
    }

    @Override
    public List<Map<String, Object>> getVesselTypes() {
        log.info("Fetching vessel types");
        
        List<VesselType> vesselTypes = vesselTypeRepository.findAllActive();
        
        return vesselTypes.stream()
                .map(vt -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("value", vt.getVesselTypeCode());
                    map.put("label", vt.getVesselTypeName());
                    return map;
                })
                .collect(Collectors.toList());
    }
}
