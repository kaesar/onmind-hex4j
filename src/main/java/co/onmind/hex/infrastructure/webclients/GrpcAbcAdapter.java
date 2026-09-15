package co.onmind.hex.infrastructure.webclients;

import co.onmind.grpc.proto.AbcServiceGrpc;
import co.onmind.hex.application.ports.out.AbcPort;
import co.onmind.hex.infrastructure.webclients.dto.AbcRequest;
import co.onmind.hex.infrastructure.webclients.dto.AbcResponse;
import co.onmind.hex.transverse.resilience.CircuitBreakerGeneric;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class GrpcAbcAdapter implements AbcPort {

    private static final Logger logger = LoggerFactory.getLogger(GrpcAbcAdapter.class);

    private final AbcServiceGrpc.AbcServiceBlockingStub stub;
    private final CircuitBreaker circuitBreaker;
    private final ObjectMapper objectMapper;

    public GrpcAbcAdapter(AbcServiceGrpc.AbcServiceBlockingStub stub,
                          CircuitBreaker abcCircuitBreaker, ObjectMapper objectMapper) {
        this.stub = stub;
        this.circuitBreaker = abcCircuitBreaker;
        this.objectMapper = objectMapper;
    }

    @Override
    public AbcResponse sheet(String show, String from, String some) {
        AbcRequest httpRequest = AbcRequest.builder()
            .what("find")
            .from(from != null ? from : "xykit")
            .some(some != null ? some : "sheet")
            .show(show != null ? show : "kit01 sheetid, kit02 name, kit03 title, kit05 model")
            .build();

        co.onmind.grpc.proto.AbcRequest grpcRequest = toGrpcRequest(httpRequest);
        logger.debug("XDB gRPC sheet request: from={}, some={}", httpRequest.from(), httpRequest.some());

        AbcResponse response = CircuitBreakerGeneric.withCircuitBreaker(
            () -> toAbcResponse(stub.execute(grpcRequest)), circuitBreaker);
        logger.debug("XDB gRPC sheet response: ok={}, total={}", response.ok(), response.total());
        return response;
    }

    @Override
    public AbcResponse exec(AbcRequest request) {
        co.onmind.grpc.proto.AbcRequest grpcRequest = toGrpcRequest(request);
        logger.debug("XDB gRPC exec request: what={}, from={}", request.what(), request.from());

        AbcResponse response = CircuitBreakerGeneric.withCircuitBreaker(
            () -> toAbcResponse(stub.execute(grpcRequest)), circuitBreaker);
        logger.debug("XDB gRPC exec response: ok={}, status={}", response.ok(), response.status());
        return response;
    }

    co.onmind.grpc.proto.AbcRequest toGrpcRequest(AbcRequest req) {
        co.onmind.grpc.proto.AbcRequest.Builder builder = co.onmind.grpc.proto.AbcRequest.newBuilder()
            .setWay(req.way() != null ? req.way() : "sql")
            .setWhat(req.what() != null ? req.what() : "!")
            .setFrom(req.from() != null ? req.from() : "xykit")
            .setSome(req.some() != null ? req.some() : "");

        if (req.with() != null) builder.setWith(req.with());
        if (req.show() != null) builder.setShow(req.show());
        if (req.call() != null) builder.setCall(req.call());

        Map<String, Object> extras = new HashMap<>();
        if (req.where() != null) extras.put("where", req.where());
        if (req.sort() != null) extras.put("sort", req.sort());
        if (req.limit() != null) extras.put("limit", req.limit());
        if (req.offset() != null) extras.put("offset", req.offset());

        String putsJson = req.puts() != null ? req.puts().toString() : null;
        if (!extras.isEmpty()) {
            try {
                Map<String, Object> combined = new HashMap<>();
                if (putsJson != null) {
                    combined.putAll(objectMapper.readValue(putsJson, Map.class));
                }
                combined.putAll(extras);
                builder.setPuts(objectMapper.writeValueAsString(combined));
            } catch (Exception e) {
                logger.warn("Failed to encode where/sort/limit/offset into puts JSON", e);
            }
        } else if (putsJson != null) {
            builder.setPuts(putsJson);
        }

        return builder.build();
    }

    AbcResponse toAbcResponse(co.onmind.grpc.proto.AbcResponse grpcResponse) {
        Boolean ok = grpcResponse.getOk();
        Integer status;
        try {
            status = Integer.parseInt(grpcResponse.getStatus());
        } catch (NumberFormatException e) {
            logger.warn("Invalid gRPC status: {}", grpcResponse.getStatus());
            status = null;
        }

        Object data = null;
        String dataJson = grpcResponse.getDataJson();
        if (dataJson != null && !dataJson.isBlank()) {
            try {
                data = objectMapper.readTree(dataJson);
            } catch (Exception e) {
                logger.warn("Failed to parse data_json: {}", dataJson, e);
                data = dataJson;
            }
        }

        return new AbcResponse(ok, status, grpcResponse.getMessage(),
            grpcResponse.getTotal(), data);
    }
}
