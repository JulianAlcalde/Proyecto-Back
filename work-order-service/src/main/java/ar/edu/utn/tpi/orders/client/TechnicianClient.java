package ar.edu.utn.tpi.orders.client;

import ar.edu.utn.tpi.orders.domain.ServiceType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class TechnicianClient {
    private final RestClient client; private final KeycloakTokenClient tokenClient;
    public TechnicianClient(@Value("${clients.technician.base-url}") String baseUrl, KeycloakTokenClient tokenClient) { this.client = RestClient.builder().baseUrl(baseUrl).build(); this.tokenClient = tokenClient; }
    public List<Candidate> findCandidates(ServiceType type, LocalDateTime startsAt, int durationMinutes) {
        Candidate[] result = client.post().uri("/api/technicians/candidates").headers(h -> h.setBearerAuth(tokenClient.accessToken()))
            .body(new CandidateSearch(type, startsAt, durationMinutes)).retrieve().body(Candidate[].class);
        return result == null ? List.of() : List.of(result);
    }
    public void reserve(Long technicianId, Long workOrderId, LocalDateTime startsAt, int durationMinutes) {
        client.post().uri("/api/technicians/{id}/reservations", technicianId).headers(h -> h.setBearerAuth(tokenClient.accessToken()))
            .body(new Reserve(workOrderId, startsAt, durationMinutes)).retrieve().toBodilessEntity();
    }
    public void cancel(Long workOrderId) { client.post().uri("/api/technicians/reservations/{id}/cancel", workOrderId).headers(h -> h.setBearerAuth(tokenClient.accessToken())).retrieve().toBodilessEntity(); }
    public void complete(Long workOrderId) { client.post().uri("/api/technicians/reservations/{id}/complete", workOrderId).headers(h -> h.setBearerAuth(tokenClient.accessToken())).retrieve().toBodilessEntity(); }
    public Long technicianIdFor(String keycloakUserId) {
        TechnicianIdentity result = client.get().uri("/api/technicians/by-user/{id}", keycloakUserId).headers(h -> h.setBearerAuth(tokenClient.accessToken())).retrieve().body(TechnicianIdentity.class);
        return result == null ? null : result.id();
    }
    public record Candidate(Long id, String fullName, double latitude, double longitude) { }
    private record CandidateSearch(ServiceType serviceType, LocalDateTime startsAt, int durationMinutes) { }
    private record Reserve(Long workOrderId, LocalDateTime startsAt, int durationMinutes) { }
    public record TechnicianIdentity(Long id) { }
}
