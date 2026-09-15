package co.onmind.hex.application.ports.out;

import co.onmind.hex.domain.models.StoreItem;
import co.onmind.hex.infrastructure.webclients.dto.AbcResponse;

import java.util.List;

public interface ScriptServicesPort {

    AbcResponse abcSheet(String show, String from, String some);

    AbcResponse abcExec(String what, String from, String some, String with, String puts);

    void publish(String topic, String key, String payload);

    String invoke(String functionName, String payload);

    void invokeAsync(String functionName, String payload);

    List<StoreItem> listItems(String bucket);

    void sendEmail(String to, String subject, String body);

    String cacheGet(String key);

    void cacheSet(String key, String value);

    void cacheEvict(String key);
}
