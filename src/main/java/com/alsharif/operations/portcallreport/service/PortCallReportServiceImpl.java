package com.alsharif.operations.portcallreport.service;

import com.alsharif.operations.commonlov.dto.LovItem;
import com.alsharif.operations.commonlov.dto.LovResponse;
import com.alsharif.operations.commonlov.service.LovService;
import com.alsharif.operations.portactivitiesmaster.repository.PortActivityMasterRepository;
import com.alsharif.operations.portcallreport.dto.PortActivityResponseDto;
import com.alsharif.operations.portcallreport.dto.PortCallReportDetailDto;
import com.alsharif.operations.portcallreport.dto.PortCallReportDetailResponseDto;
import com.alsharif.operations.portcallreport.dto.PortCallReportDto;
import com.alsharif.operations.portcallreport.dto.PortCallReportResponseDto;
import com.alsharif.operations.portcallreport.entity.PortCallReportDtl;
import com.alsharif.operations.portcallreport.entity.PortCallReportDtlId;
import com.alsharif.operations.portcallreport.entity.PortCallReportHdr;
import com.alsharif.operations.portcallreport.enums.ActionType;
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
    private final LovService lovService;

    @Override
    public Page<PortCallReportResponseDto> getReportList(String search, Pageable pageable) {
        log.info("Fetching port call report list with search: {}", search);

        Page<PortCallReportHdr> hdrPage = hdrRepository.findAllNonDeletedWithSearch(search, pageable);

        LovResponse vesselTypeLov = lovService.getLovList("VESSEL_TYPE_MASTER", null, null);
        Map<String, LovItem> vesselTypeMap = new HashMap<>();
        if (vesselTypeLov != null && vesselTypeLov.getItems() != null) {
            vesselTypeMap = vesselTypeLov.getItems().stream()
                    .collect(Collectors.toMap(LovItem::getCode, item -> item));
        }

        Map<Long, LovItem> vesselTypeByPoidMap = new HashMap<>();
        if (vesselTypeLov != null && vesselTypeLov.getItems() != null) {
            vesselTypeByPoidMap = vesselTypeLov.getItems().stream()
                    .collect(Collectors.toMap(LovItem::getPoid, item -> item));
        }

        Map<Long, LovItem> finalVesselTypeByPoidMap = vesselTypeByPoidMap;
        List<PortCallReportResponseDto> reports = hdrPage.getContent().stream()
                .map(hdr -> {
                    String vesselTypes = hdr.getPortCallApplVesselType();
                    List<LovItem> vesselTypeLovItems = null;
                    if (vesselTypes != null) {
                        vesselTypeLovItems = List.of(vesselTypes.split(",")).stream()
                                .map(Long::parseLong)
                                .map(finalVesselTypeByPoidMap::get)
                                .filter(item -> item != null)
                                .collect(Collectors.toList());
                    }
                    return PortCallReportResponseDto.builder()
                            .portCallReportPoid(hdr.getPortCallReportPoid())
                            .portCallReportId(hdr.getPortCallReportId())
                            .portCallReportName(hdr.getPortCallReportName())
                            .portCallApplVesselType(vesselTypeLovItems)
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
    public PortCallReportResponseDto getReportById(Long id) {
        log.info("Fetching port call report by id: {}", id);

        PortCallReportHdr hdr = hdrRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Port call report not found"));

        List<PortCallReportDtl> details = dtlRepository.findByPortCallReportPoid(id);

        String vesselTypes = hdr.getPortCallApplVesselType();
        List<LovItem> vesselTypeLovItems = null;
        if (vesselTypes != null) {
            LovResponse lovResponse = lovService.getLovList("VESSEL_TYPE_MASTER", null, null);
            if (lovResponse != null && lovResponse.getItems() != null) {
                Map<Long, LovItem> vesselTypeByPoidMap = lovResponse.getItems().stream()
                        .collect(Collectors.toMap(LovItem::getPoid, item -> item));
                vesselTypeLovItems = List.of(vesselTypes.split(",")).stream()
                        .map(Long::parseLong)
                        .map(vesselTypeByPoidMap::get)
                        .filter(item -> item != null)
                        .collect(Collectors.toList());
            }
        }

        LovResponse portActivityLov = lovService.getLovList("PORT_ACTIVITY", null, null);
        Map<Long, LovItem> portActivityMap = new HashMap<>();
        if (portActivityLov != null && portActivityLov.getItems() != null) {
            portActivityMap = portActivityLov.getItems().stream()
                    .collect(Collectors.toMap(LovItem::getPoid, item -> item));
        }

        Map<Long, LovItem> finalPortActivityMap = portActivityMap;
        List<PortCallReportDetailResponseDto> detailDtos = details.stream()
                .map(dtl -> PortCallReportDetailResponseDto.builder()
                        .detRowId(dtl.getDetRowId())
                        .portCallReportPoid(dtl.getPortCallReportPoid())
                        .portActivityTypePoid(dtl.getPortActivityTypePoid())
                        .portActivityTypeName(dtl.getPortActivityMaster() != null ? dtl.getPortActivityMaster().getPortActivityTypeName() : null)
                        .activityMandatory(dtl.getActivityMandatory())
                        .portActivityDet(finalPortActivityMap.get(dtl.getPortActivityTypePoid()))
                        .build())
                .collect(Collectors.toList());

        return PortCallReportResponseDto.builder()
                .portCallReportPoid(hdr.getPortCallReportPoid())
                .portCallReportId(hdr.getPortCallReportId())
                .portCallReportName(hdr.getPortCallReportName())
                .portCallApplVesselType(vesselTypeLovItems)
                .active(hdr.getActive())
                .seqno(hdr.getSeqno())
                .remarks(hdr.getRemarks())
                .details(detailDtos)
                .build();
    }

    @Override
    @Transactional
    public PortCallReportResponseDto createReport(PortCallReportDto dto, Long userPoid, Long groupPoid) {
        log.info("Creating port call report: {}", dto.getPortCallReportName());

        if (groupPoid == null) {
            throw new RuntimeException("Group POID is required");
        }

        if (hdrRepository.existsByPortCallReportNameIgnoreCaseAndNotDeleted(dto.getPortCallReportName(), null)) {
            throw new RuntimeException("Port call report name already exists");
        }

        if (dto.getPortCallApplVesselType() != null && !dto.getPortCallApplVesselType().isEmpty()) {
            List<Long> validVesselTypePoids = vesselTypeRepository.findAllActive().stream()
                    .map(VesselType::getVesselTypePoid)
                    .toList();
            for (String vesselTypePoid : dto.getPortCallApplVesselType()) {
                if (!validVesselTypePoids.contains(Long.parseLong(vesselTypePoid))) {
                    throw new RuntimeException("Invalid vessel type POID: " + vesselTypePoid);
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
                .groupPoid(groupPoid)
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
            Long nextDetRowId = dtlRepository.findMaxDetRowIdByPortCallReportPoid(reportPoid) + 1;
            List<PortCallReportDtl> details = new ArrayList<>();
            for (PortCallReportDetailDto detailDto : dto.getDetails()) {
                details.add(PortCallReportDtl.builder()
                        .portCallReportPoid(reportPoid)
                        .detRowId(nextDetRowId++)
                        .portActivityTypePoid(detailDto.getPortActivityTypePoid())
                        .activityMandatory(detailDto.getActivityMandatory())
                        .createdBy(user.getUserId())
                        .build());
            }
            dtlRepository.saveAll(details);
        }

        return getReportById(hdr.getPortCallReportPoid());
    }

    @Override
    @Transactional
    public PortCallReportResponseDto updateReport(Long id, PortCallReportDto dto, Long userPoid, Long groupPoid) {
        log.info("Updating port call report id: {}", id);

        if (groupPoid == null) {
            throw new RuntimeException("Group POID is required");
        }

        if (hdrRepository.existsByPortCallReportNameIgnoreCaseAndNotDeleted(dto.getPortCallReportName(), id)) {
            throw new RuntimeException("Port call report name already exists");
        }

        if (dto.getPortCallApplVesselType() != null && !dto.getPortCallApplVesselType().isEmpty()) {
            List<Long> validVesselTypePoids = vesselTypeRepository.findAllActive().stream()
                    .map(VesselType::getVesselTypePoid)
                    .toList();
            for (String vesselTypePoid : dto.getPortCallApplVesselType()) {
                if (!validVesselTypePoids.contains(Long.parseLong(vesselTypePoid))) {
                    throw new RuntimeException("Invalid vessel type POID: " + vesselTypePoid);
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

        hdr.setGroupPoid(groupPoid);
        hdr.setPortCallReportName(dto.getPortCallReportName());
        hdr.setPortCallApplVesselType(dto.getPortCallApplVesselType() != null ? String.join(",", dto.getPortCallApplVesselType()) : null);
        hdr.setActive(dto.getActive());
        hdr.setSeqno(dto.getSeqno());
        hdr.setRemarks(dto.getRemarks());
        hdr.setLastModifiedBy(user.getUserId());

        hdrRepository.save(hdr);

        if (dto.getDetails() != null && !dto.getDetails().isEmpty()) {
            for (PortCallReportDetailDto detailDto : dto.getDetails()) {
                ActionType action = detailDto.getActionType();

                if (action == ActionType.isCreated) {
                    Long nextDetRowId = dtlRepository.findMaxDetRowIdByPortCallReportPoid(id) + 1;
                    PortCallReportDtl newDetail = PortCallReportDtl.builder()
                            .portCallReportPoid(id)
                            .detRowId(nextDetRowId)
                            .portActivityTypePoid(detailDto.getPortActivityTypePoid())
                            .activityMandatory(detailDto.getActivityMandatory())
                            .createdBy(user.getUserId())
                            .build();
                    dtlRepository.save(newDetail);
                } else if (action == ActionType.isUpdated) {
                    dtlRepository.findById(new PortCallReportDtlId(id, detailDto.getDetRowId()))
                            .ifPresent(existing -> {
                                existing.setPortActivityTypePoid(detailDto.getPortActivityTypePoid());
                                existing.setActivityMandatory(detailDto.getActivityMandatory());
                                existing.setLastModifiedBy(user.getUserId());
                                dtlRepository.save(existing);
                            });
                } else if (action == ActionType.isDeleted) {
                    dtlRepository.deleteById(new PortCallReportDtlId(id, detailDto.getDetRowId()));
                }
            }
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
    public List<PortActivityResponseDto> getPortActivities(Long userPoid) {
        log.info("Fetching port activities");

        String sql = "{call PROC_PORT_ACTIVITIES_GET_LIST(?, ?)}";

        List<PortActivityResponseDto> activities = jdbcTemplate.execute((Connection conn) -> {
            try (CallableStatement cs = conn.prepareCall(sql)) {
                cs.setLong(1, userPoid);
                cs.registerOutParameter(2, Types.REF_CURSOR);
                cs.execute();

                List<PortActivityResponseDto> result = new ArrayList<>();
                try (ResultSet rs = (ResultSet) cs.getObject(2)) {
                    while (rs.next()) {
                        result.add(PortActivityResponseDto.builder()
                                .portActivityTypePoid(rs.getLong("PORT_ACTIVITY_TYPE_POID"))
                                .portActivityTypeCode(rs.getString("PORT_ACTIVITY_TYPE_CODE"))
                                .portActivityTypeName(rs.getString("PORT_ACTIVITY_TYPE_NAME"))
                                .portActivityTypeName2(rs.getString("PORT_ACTIVITY_TYPE_NAME2"))
                                .build());
                    }
                }
                return result;
            }
        });

        LovResponse portActivityLov = lovService.getLovList("PORT_ACTIVITY", null, null);
        if (portActivityLov != null && portActivityLov.getItems() != null) {
            Map<Long, LovItem> lovMap = portActivityLov.getItems().stream()
                    .collect(Collectors.toMap(LovItem::getPoid, item -> item));
            activities.forEach(activity -> activity.setPortActivityDet(lovMap.get(activity.getPortActivityTypePoid())));
        }

        return activities;
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
