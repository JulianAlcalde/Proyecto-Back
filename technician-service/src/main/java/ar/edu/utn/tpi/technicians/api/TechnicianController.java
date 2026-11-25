package ar.edu.utn.tpi.technicians.api;

import ar.edu.utn.tpi.technicians.api.TechnicianDtos.*;
import ar.edu.utn.tpi.technicians.service.TechnicianManagementService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/technicians")
public class TechnicianController {
    private final TechnicianManagementService service;
    public TechnicianController(TechnicianManagementService service) { this.service = service; }
    @PostMapping @PreAuthorize("hasRole('DISPATCHER') or hasRole('ADMIN')") ResponseEntity<TechnicianResponse> create(@Valid @RequestBody CreateTechnicianRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request)); }
    @GetMapping @PreAuthorize("hasRole('DISPATCHER') or hasRole('ADMIN')") List<TechnicianResponse> list() { return service.list(); }
    @GetMapping("/by-user/{keycloakUserId}") @PreAuthorize("isAuthenticated()") TechnicianResponse byUser(@PathVariable("keycloakUserId") String keycloakUserId) { return service.byKeycloakUserId(keycloakUserId); }
    @PatchMapping("/{id}/active") @PreAuthorize("hasRole('DISPATCHER') or hasRole('ADMIN')") TechnicianResponse active(@PathVariable("id") Long id, @RequestParam("value") boolean value) { return service.setActive(id, value); }
    @PostMapping("/candidates") @PreAuthorize("isAuthenticated()") List<CandidateResponse> candidates(@Valid @RequestBody CandidateSearchRequest request) { return service.candidates(request); }
    @PostMapping("/{id}/reservations") @PreAuthorize("isAuthenticated()") ResponseEntity<ReservationResponse> reserve(@PathVariable("id") Long id, @Valid @RequestBody ReserveRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(service.reserve(id, request)); }
    @PostMapping("/reservations/{workOrderId}/cancel") @PreAuthorize("isAuthenticated()") @ResponseStatus(HttpStatus.NO_CONTENT) void cancel(@PathVariable("workOrderId") Long workOrderId) { service.cancelReservation(workOrderId); }
    @PostMapping("/reservations/{workOrderId}/complete") @PreAuthorize("isAuthenticated()") @ResponseStatus(HttpStatus.NO_CONTENT) void complete(@PathVariable("workOrderId") Long workOrderId) { service.completeReservation(workOrderId); }
}
