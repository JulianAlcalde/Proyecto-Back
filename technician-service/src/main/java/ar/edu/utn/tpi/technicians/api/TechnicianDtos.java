package ar.edu.utn.tpi.technicians.api;

import ar.edu.utn.tpi.technicians.domain.ServiceType;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.Set;

public final class TechnicianDtos {
    private TechnicianDtos() { }
    public record CreateTechnicianRequest(@NotBlank String fullName, @NotBlank String keycloakUserId, @NotEmpty Set<ServiceType> skills,
        @NotNull @FutureOrPresent LocalDate certificationValidUntil, @DecimalMin("-90.0") @DecimalMax("90.0") double latitude,
        @DecimalMin("-180.0") @DecimalMax("180.0") double longitude) { }
    public record TechnicianResponse(Long id, String fullName, String keycloakUserId, Set<ServiceType> skills, LocalDate certificationValidUntil, boolean active, double latitude, double longitude) { }
    public record CandidateSearchRequest(@NotNull ServiceType serviceType, @NotNull @FutureOrPresent LocalDateTime startsAt, @Min(30) @Max(480) int durationMinutes) { }
    public record CandidateResponse(Long id, String fullName, double latitude, double longitude) { }
    public record ReserveRequest(@NotNull @Positive Long workOrderId, @NotNull @FutureOrPresent LocalDateTime startsAt, @Min(30) @Max(480) int durationMinutes) { }
    public record ReservationResponse(Long reservationId, Long workOrderId, Long technicianId, LocalDateTime startsAt, LocalDateTime endsAt, String status) { }
}
