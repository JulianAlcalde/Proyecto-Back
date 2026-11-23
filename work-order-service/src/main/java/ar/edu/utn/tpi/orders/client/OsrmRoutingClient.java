package ar.edu.utn.tpi.orders.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.List;

@Component
public class OsrmRoutingClient {
    private final RestClient client;
    public OsrmRoutingClient(@Value("${clients.osrm.base-url}") String baseUrl) { this.client = RestClient.builder().baseUrl(baseUrl).build(); }
    public double routeDistanceKm(double fromLat, double fromLon, double toLat, double toLon) {
        OsrmResponse response = client.get().uri("/route/v1/driving/{fromLon},{fromLat};{toLon},{toLat}?overview=false", fromLon, fromLat, toLon, toLat).retrieve().body(OsrmResponse.class);
        if (response == null || response.routes() == null || response.routes().isEmpty()) throw new IllegalStateException("OSRM no devolvió una ruta");
        return response.routes().get(0).distance() / 1000d;
    }
    public record OsrmResponse(List<Route> routes) { }
    public record Route(double distance) { }
}
