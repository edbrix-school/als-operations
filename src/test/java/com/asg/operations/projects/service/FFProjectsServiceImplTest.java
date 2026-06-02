package com.asg.operations.projects.service;

import com.asg.common.lib.service.LoggingService;
import com.asg.operations.exceptions.FFValidationException;
import com.asg.operations.projects.dto.FFProjectsCtrlSheetDetailRequest;
import com.asg.operations.projects.dto.FFProjectsCtrlSheetDetailResponse;
import com.asg.operations.projects.entity.FFProjectsCtrlSheetDtl;
import com.asg.operations.projects.entity.FFProjectsHdr;
import com.asg.operations.projects.repository.FFProjectsChargesDtlRepository;
import com.asg.operations.projects.repository.FFProjectsCtrlSheetDtlRepository;
import com.asg.operations.projects.repository.FFProjectsHdrRepository;
import com.asg.operations.projects.repository.FFProjectsStoredProcRepository;
import com.asg.operations.projects.repository.FreightJobProjectionRepository;
import com.asg.operations.projects.util.ProjectMapper;
import com.asg.operations.projectjob.repository.FFManifestAirPkgDtlRepository;
import com.asg.operations.projectjob.repository.FFManifestBayanDtlRepository;
import com.asg.operations.projectjob.repository.FFManifestChargesDtlRepository;
import com.asg.operations.projectjob.repository.FFManifestContainerDtlRepository;
import com.asg.operations.projectjob.repository.FFManifestHdrRepository;
import com.asg.operations.projectjob.repository.FFManifestTruckDtlRepository;
import com.asg.operations.projectjob.util.ProjectJobMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class FFProjectsServiceImplTest {

    private TestCtrlSheetRepository ctrlSheetRepository;
    private TestHdrRepository hdrRepository;
    private RecordingLoggingService loggingService;
    private TestProjectMapper mapper;
    private FFProjectsServiceImpl service;

    @BeforeEach
    void setUp() {
        hdrRepository = new TestHdrRepository();
        ctrlSheetRepository = new TestCtrlSheetRepository();
        loggingService = new RecordingLoggingService();
        mapper = new TestProjectMapper();

        service = new FFProjectsServiceImpl(
                hdrRepository.proxy,
                null,
                ctrlSheetRepository.proxy,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                loggingService,
                mapper,
                null,
                null,
                null,
                null
        );
    }

    @Test
    void updateControlSheet_allowsInactiveWhenJobNumberProvidedInRequest() {
        Long transactionPoid = 100L;
        Long detRowId = 10L;

        FFProjectsCtrlSheetDtl existing = FFProjectsCtrlSheetDtl.builder()
                .transactionPoid(transactionPoid)
                .detRowId(detRowId)
                .freightType("AIR")
                .jobNoPoid(null)
                .active("Y")
                .build();

        hdrRepository.project = new FFProjectsHdr();
        ctrlSheetRepository.existingByKey = existing;

        FFProjectsCtrlSheetDetailResponse expectedResponse = FFProjectsCtrlSheetDetailResponse.builder()
                .transactionPoid(transactionPoid)
                .detRowId(detRowId)
                .active("N")
                .build();
        mapper.responseToReturn = expectedResponse;

        FFProjectsCtrlSheetDetailRequest request = FFProjectsCtrlSheetDetailRequest.builder()
                .freightType("AIR")
                .jobNoPoid(55L)
                .active("N")
                .originPoid(1L)
                .destinationPoid(2L)
                .etd(LocalDate.of(2026, 5, 1))
                .etaAta(LocalDate.of(2026, 5, 2))
                .arrivalDate(LocalDate.of(2026, 5, 3))
                .noOfPackages(3.0)
                .weight(10.5)
                .cbm(2.5)
                .carrierPoid(7L)
                .linePoid(8L)
                .truckNumber("TRK-01")
                .description("Updated row")
                .sailDate(LocalDate.of(2026, 5, 4))
                .sfPOL("10")
                .sfPOD("11")
                .build();

        FFProjectsCtrlSheetDetailResponse result = service.updateControlSheet(transactionPoid, detRowId, request);

        assertSame(expectedResponse, result);
        assertEquals("N", existing.getActive());
        assertEquals(55L, existing.getJobNoPoid());
        assertEquals(1, ctrlSheetRepository.saveCount.get());
        assertEquals(1, loggingService.batchLogCount.get());
    }

    @Test
    void updateControlSheet_rejectsInactiveWhenNoJobNumberExists() {
        Long transactionPoid = 100L;
        Long detRowId = 10L;

        FFProjectsCtrlSheetDtl existing = FFProjectsCtrlSheetDtl.builder()
                .transactionPoid(transactionPoid)
                .detRowId(detRowId)
                .freightType("SEA")
                .jobNoPoid(null)
                .active("Y")
                .build();

        hdrRepository.project = new FFProjectsHdr();
        ctrlSheetRepository.existingByKey = existing;

        FFProjectsCtrlSheetDetailRequest request = FFProjectsCtrlSheetDetailRequest.builder()
                .freightType("SEA")
                .active("N")
                .build();

        FFValidationException exception = assertThrows(
                FFValidationException.class,
                () -> service.updateControlSheet(transactionPoid, detRowId, request)
        );

        assertTrue(exception.getMessage().contains("job number"));
        assertEquals(0, ctrlSheetRepository.saveCount.get());
        assertEquals(0, loggingService.batchLogCount.get());
    }

    @Test
    void batchControlSheetOperations_allowsInactiveWhenJobNumberProvidedInRequest() {
        Long transactionPoid = 100L;
        Long detRowId = 10L;

        FFProjectsCtrlSheetDtl existing = FFProjectsCtrlSheetDtl.builder()
                .transactionPoid(transactionPoid)
                .detRowId(detRowId)
                .freightType("AIR")
                .jobNoPoid(null)
                .active("Y")
                .build();

        hdrRepository.project = new FFProjectsHdr();
        ctrlSheetRepository.existingByKey = existing;

        FFProjectsCtrlSheetDetailResponse expectedResponse = FFProjectsCtrlSheetDetailResponse.builder()
                .transactionPoid(transactionPoid)
                .detRowId(detRowId)
                .active("N")
                .build();
        mapper.responseToReturn = expectedResponse;

        FFProjectsCtrlSheetDetailRequest request = FFProjectsCtrlSheetDetailRequest.builder()
                .actionType("ISUPDATED")
                .detRowId(detRowId)
                .freightType("AIR")
                .jobNoPoid(55L)
                .active("N")
                .build();

        List<FFProjectsCtrlSheetDetailResponse> result =
                service.batchControlSheetOperations(transactionPoid, List.of(request));

        assertEquals(1, result.size());
        assertSame(expectedResponse, result.get(0));
        assertEquals("N", existing.getActive());
        assertEquals(55L, existing.getJobNoPoid());
        assertEquals(1, ctrlSheetRepository.saveAllCount.get());
        assertEquals(1, loggingService.batchLogCount.get());
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                handler));
    }

    private static class TestHdrRepository implements InvocationHandler {
        private FFProjectsHdr project;
        private final FFProjectsHdrRepository proxy = proxy(FFProjectsHdrRepository.class, this);

        @Override
        public Object invoke(Object p, Method method, Object[] args) {
            if ("findByTransactionPoidAndDeleted".equals(method.getName())) {
                return Optional.ofNullable(project);
            }
            return defaultValue(method.getReturnType());
        }
    }

    private static class TestCtrlSheetRepository implements InvocationHandler {
        private FFProjectsCtrlSheetDtl existingByKey;
        private final AtomicInteger saveCount = new AtomicInteger();
        private final AtomicInteger saveAllCount = new AtomicInteger();
        private final FFProjectsCtrlSheetDtlRepository proxy = proxy(FFProjectsCtrlSheetDtlRepository.class, this);

        @Override
        public Object invoke(Object p, Method method, Object[] args) {
            return switch (method.getName()) {
                case "findByTransactionPoidAndDetRowId" -> Optional.ofNullable(existingByKey);
                case "findByTransactionPoid" -> existingByKey == null ? List.of() : List.of(existingByKey);
                case "findByTransactionPoidAndActive" -> existingByKey == null ? List.of() : List.of(existingByKey);
                case "findByTransactionPoidAndFreightType" -> existingByKey == null ? List.of() : List.of(existingByKey);
                case "findByTransactionPoidAndFreightTypeAndActive" -> existingByKey == null ? List.of() : List.of(existingByKey);
                case "save" -> {
                    saveCount.incrementAndGet();
                    yield args[0];
                }
                case "saveAll" -> {
                    saveAllCount.incrementAndGet();
                    yield args[0];
                }
                case "deleteByTransactionPoidAndDetRowIdIn", "deleteByTransactionPoid" -> null;
                default -> defaultValue(method.getReturnType());
            };
        }
    }

    private static class RecordingLoggingService extends LoggingService {
        private final AtomicInteger batchLogCount = new AtomicInteger();

        @Override
        public <T> void createLogBatch(List<com.asg.common.lib.dto.request.LogRequestDto<T>> logRequests) {
            batchLogCount.incrementAndGet();
        }
    }

    private static class TestProjectMapper extends ProjectMapper {
        private FFProjectsCtrlSheetDetailResponse responseToReturn;

        @Override
        public FFProjectsCtrlSheetDetailResponse mapCtrlSheetDetailToResponse(FFProjectsCtrlSheetDtl dtl) {
            if (responseToReturn != null) {
                return responseToReturn;
            }
            return FFProjectsCtrlSheetDetailResponse.builder()
                    .transactionPoid(dtl.getTransactionPoid())
                    .detRowId(dtl.getDetRowId())
                    .active(dtl.getActive())
                    .build();
        }
    }

    private static Object defaultValue(Class<?> returnType) {
        if (returnType == Void.TYPE) return null;
        if (returnType == boolean.class) return false;
        if (returnType == byte.class) return (byte) 0;
        if (returnType == short.class) return (short) 0;
        if (returnType == int.class) return 0;
        if (returnType == long.class) return 0L;
        if (returnType == float.class) return 0f;
        if (returnType == double.class) return 0d;
        if (returnType == char.class) return '\0';
        if (List.class.isAssignableFrom(returnType)) return List.of();
        if (Optional.class.isAssignableFrom(returnType)) return Optional.empty();
        return null;
    }
}
