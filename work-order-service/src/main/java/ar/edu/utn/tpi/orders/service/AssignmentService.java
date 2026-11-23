package ar.edu.utn.tpi.orders.service;

import ar.edu.utn.tpi.orders.client.*;
import ar.edu.utn.tpi.orders.domain.WorkOrder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import java.util.Comparator;

@Service
public class AssignmentService {
    private final TechnicianClient technicians; private final OsrmRoutingClient osrm;
    public AssignmentService(TechnicianClient technicians, OsrmRoutingClient osrm) { this.technicians = technicians; this.osrm = osrm; }
    public void assign(WorkOrder order) {
        try {
            var best = technicians.findCandidates(order.getServiceType(), order.getRequestedStart(), order.getDurationMinutes()).stream()
                .map(candidate -> new RankedCandidate(candidate, osrm.routeDistanceKm(candidate.latitude(), candidate.longitude(), order.getLatitude(), order.getLongitude())))
                .min(Comparator.comparingDouble(RankedCandidate::distanceKm));
            if (best.isEmpty()) { order.assignmentFailed("No hay técnicos certificados y disponibles para el turno solicitado"); return; }
            technicians.reserve(best.get().candidate().id(), order.getId(), order.getRequestedStart(), order.getDurationMinutes());
            order.assign(best.get().candidate().id(), best.get().distanceKm());
        } catch (RestClientException | IllegalStateException exception) {
            order.assignmentFailed("No se pudo asignar: una dependencia externa no está disponible");
        }
    }
    private record RankedCandidate(TechnicianClient.Candidate candidate, double distanceKm) { }
}
