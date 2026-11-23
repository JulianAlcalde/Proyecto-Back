package ar.edu.utn.tpi.technicians.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "technician_reservations", uniqueConstraints = @UniqueConstraint(name = "uk_order_reservation", columnNames = "workOrderId"))
public class TechnicianReservation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) private Technician technician;
    @Column(nullable = false) private Long workOrderId;
    @Column(nullable = false) private LocalDateTime startsAt;
    @Column(nullable = false) private LocalDateTime endsAt;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ReservationStatus status = ReservationStatus.RESERVED;
    protected TechnicianReservation() { }
    public TechnicianReservation(Technician technician, Long workOrderId, LocalDateTime startsAt, LocalDateTime endsAt) { this.technician = technician; this.workOrderId = workOrderId; this.startsAt = startsAt; this.endsAt = endsAt; }
    public Long getId() { return id; } public Technician getTechnician() { return technician; } public Long getWorkOrderId() { return workOrderId; }
    public LocalDateTime getStartsAt() { return startsAt; } public LocalDateTime getEndsAt() { return endsAt; } public ReservationStatus getStatus() { return status; }
    public void cancel() { status = ReservationStatus.CANCELLED; } public void complete() { status = ReservationStatus.COMPLETED; }
}
