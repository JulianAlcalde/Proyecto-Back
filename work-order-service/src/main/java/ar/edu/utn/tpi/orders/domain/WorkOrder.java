package ar.edu.utn.tpi.orders.domain;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "work_orders")
public class WorkOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String customerId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ServiceType serviceType;
    @Column(nullable = false, length = 1000) private String description;
    @Column(nullable = false) private String address;
    @Column(nullable = false) private double latitude;
    @Column(nullable = false) private double longitude;
    @Column(nullable = false) private LocalDateTime requestedStart;
    @Column(nullable = false) private int durationMinutes;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private WorkOrderStatus status = WorkOrderStatus.PENDING_ASSIGNMENT;
    private Long technicianId; private Double routeDistanceKm;
    @Column(length = 500) private String assignmentNote;
    @Column(nullable = false, updatable = false) private Instant createdAt = Instant.now();
    protected WorkOrder() { }
    public WorkOrder(String customerId, ServiceType serviceType, String description, String address, double latitude, double longitude, LocalDateTime requestedStart, int durationMinutes) {
        this.customerId = customerId; this.serviceType = serviceType; this.description = description; this.address = address; this.latitude = latitude; this.longitude = longitude; this.requestedStart = requestedStart; this.durationMinutes = durationMinutes;
    }
    public Long getId() { return id; } public String getCustomerId() { return customerId; } public ServiceType getServiceType() { return serviceType; }
    public String getDescription() { return description; } public String getAddress() { return address; } public double getLatitude() { return latitude; } public double getLongitude() { return longitude; }
    public LocalDateTime getRequestedStart() { return requestedStart; } public int getDurationMinutes() { return durationMinutes; } public WorkOrderStatus getStatus() { return status; }
    public Long getTechnicianId() { return technicianId; } public Double getRouteDistanceKm() { return routeDistanceKm; } public String getAssignmentNote() { return assignmentNote; } public Instant getCreatedAt() { return createdAt; }
    public void assign(Long technicianId, double routeDistanceKm) { this.technicianId = technicianId; this.routeDistanceKm = routeDistanceKm; this.assignmentNote = null; this.status = WorkOrderStatus.ASSIGNED; }
    public void assignmentFailed(String note) { this.assignmentNote = note; this.status = WorkOrderStatus.ASSIGNMENT_FAILED; }
    public void markInProgress() { if (status != WorkOrderStatus.ASSIGNED) throw new IllegalStateException("La orden debe estar asignada para iniciarse"); status = WorkOrderStatus.IN_PROGRESS; }
    public void complete() { if (status != WorkOrderStatus.IN_PROGRESS) throw new IllegalStateException("La orden debe estar en curso para finalizarse"); status = WorkOrderStatus.COMPLETED; }
    public void cancel() { if (status == WorkOrderStatus.IN_PROGRESS || status == WorkOrderStatus.COMPLETED) throw new IllegalStateException("La orden no puede cancelarse en su estado actual"); status = WorkOrderStatus.CANCELLED; }
    public boolean canAttemptAssignment() { return status == WorkOrderStatus.PENDING_ASSIGNMENT || status == WorkOrderStatus.ASSIGNMENT_FAILED; }
}
