package co.onmind.microhex.infrastructure.webclients;

import co.onmind.microhex.transverse.WebClientGeneric;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class NotificationWebClient implements WebClientGeneric {
    
    private final RestTemplate restTemplate;
    private final String notificationUrl;
    
    public NotificationWebClient(RestTemplate restTemplate, 
                               @Value("${notification.service.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.notificationUrl = baseUrl + "/api/notify";
    }
    
    @Override
    public <T> ResponseEntity<String> post(String url, T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<T> entity = new HttpEntity<>(body, headers);
        return restTemplate.postForEntity(notificationUrl, entity, String.class);
    }
}