package co.onmind.hex.infrastructure.webclients.dto;

public record AbcToken(String type, String token) {

    public static final String BEARER = "bearer";
    public static final String BASIC = "basic";
    public static final String NONE = "none";

    public AbcToken {
        if (type == null || type.isBlank()) type = NONE;
        if (token == null) token = "";
    }

    public static AbcToken none() {
        return new AbcToken(NONE, "");
    }

    public static AbcToken bearer(String jwt) {
        return new AbcToken(BEARER, jwt != null ? jwt : "");
    }

    public static AbcToken basic(String user, String pass) {
        String encoded = java.util.Base64.getEncoder()
            .encodeToString((user + ":" + pass).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return new AbcToken(BASIC, encoded);
    }

    public String toHeaderValue() {
        return switch (type) {
            case BEARER -> "Bearer " + token;
            case BASIC -> "Basic " + token;
            default -> "";
        };
    }
}
