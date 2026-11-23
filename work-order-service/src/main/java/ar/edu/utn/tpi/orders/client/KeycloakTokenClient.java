package ar.edu.utn.tpi.orders.client;

import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class KeycloakTokenClient {
    private final RestClient client = RestClient.create();
    private final String tokenUri; private final String clientId; private final String clientSecret;
    public KeycloakTokenClient(@Value("${keycloak.token-uri}") String tokenUri, @Value("${keycloak.client-id}") String clientId, @Value("${keycloak.client-secret}") String clientSecret) {
        this.tokenUri = tokenUri; this.clientId = clientId; this.clientSecret = clientSecret;
    }
    public String accessToken() {
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials"); form.add("client_id", clientId); form.add("client_secret", clientSecret);
        TokenResponse response = client.post().uri(tokenUri).contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form).retrieve().body(TokenResponse.class);
        if (response == null || response.accessToken() == null) throw new IllegalStateException("Keycloak no devolvió un token de servicio");
        return response.accessToken();
    }
    public record TokenResponse(@JsonProperty("access_token") String accessToken) { }
}
