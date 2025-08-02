package co.onmind.microhex.transverse;

import org.springframework.http.ResponseEntity;

public interface WebClientGeneric {
    <T> ResponseEntity<String> post(String url, T body);
}