package ar.edu.utn.tpi.orders.api;

import ar.edu.utn.tpi.orders.api.WorkOrderDtos.*;
import ar.edu.utn.tpi.orders.service.WorkOrderManagementService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/work-orders")
public class WorkOrderController {
    private final WorkOrderManagementService service;
    public WorkOrderController(WorkOrderManagementService service) { this.service = service; }
    @PostMapping @PreAuthorize("hasRole('CUSTOMER')") ResponseEntity<WorkOrderResponse> create(@Valid @RequestBody CreateWorkOrderRequest request, Authentication auth) { return ResponseEntity.status(HttpStatus.CREATED).body(service.create(auth.getName(), request)); }
    @GetMapping @PreAuthorize("hasAnyRole('CUSTOMER','DISPATCHER','ADMIN')") List<WorkOrderResponse> list(Authentication auth) { return privileged(auth) ? service.all() : service.byCustomer(auth.getName()); }
    @GetMapping("/{id}") @PreAuthorize("hasAnyRole('CUSTOMER','DISPATCHER','ADMIN')") WorkOrderResponse get(@PathVariable Long id, Authentication auth) { return service.get(id, auth.getName(), privileged(auth)); }
    @PostMapping("/{id}/assign") @PreAuthorize("hasRole('DISPATCHER') or hasRole('ADMIN')") WorkOrderResponse assign(@PathVariable Long id) { return service.retryAssignment(id); }
    @PostMapping("/{id}/cancel") @PreAuthorize("hasAnyRole('CUSTOMER','DISPATCHER','ADMIN')") WorkOrderResponse cancel(@PathVariable Long id, Authentication auth) { return service.cancel(id, auth.getName(), privileged(auth)); }
    @PostMapping("/{id}/start") @PreAuthorize("hasRole('TECHNICIAN')") WorkOrderResponse start(@PathVariable Long id, Authentication auth) { return service.start(id, auth.getName()); }
    @PostMapping("/{id}/complete") @PreAuthorize("hasRole('TECHNICIAN')") WorkOrderResponse complete(@PathVariable Long id, Authentication auth) { return service.complete(id, auth.getName()); }
    private boolean privileged(Authentication auth) { return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DISPATCHER") || a.getAuthority().equals("ROLE_ADMIN")); }
}
