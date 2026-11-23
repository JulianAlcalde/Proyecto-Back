package ar.edu.utn.tpi.orders.api;

import ar.edu.utn.tpi.orders.domain.*;
import jakarta.validation.constraints.*;
import java.time.*;

public final class WorkOrderDtos {
    private WorkOrderDtos() { }
    public record CreateWorkOrderRequest(@NotNull ServiceType serviceType, @NotBlank @Size(max = 1000) String description, @NotBlank @Size(max = 250) String address,
        @DecimalMin("-90.0") @DecimalMax("90.0") double latitude, @DecimalMin("-180.0") @DecimalMax("180.0") double longitude,
        @NotNull @Future LocalDateTime requestedStart, @Min(30) @Max(480) int durationMinutes) { }
    public record WorkOrderResponse(Long id, String customerId, ServiceType serviceType, String description, String address, LocalDateTime requestedStart, int durationMinutes,
        WorkOrderStatus status, Long technicianId, Double routeDistanceKm, String assignmentNote, Instant createdAt) { }
}
