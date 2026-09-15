package co.onmind.hex.application.ports.out;

import co.onmind.hex.infrastructure.webclients.dto.AbcRequest;
import co.onmind.hex.infrastructure.webclients.dto.AbcResponse;

public interface AbcPort {

    AbcResponse sheet(String show, String from, String some);

    AbcResponse exec(AbcRequest request);
}
