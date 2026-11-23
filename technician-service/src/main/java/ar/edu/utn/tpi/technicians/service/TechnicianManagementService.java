package ar.edu.utn.tpi.technicians.service;

import ar.edu.utn.tpi.shared.error.DomainException;
import ar.edu.utn.tpi.technicians.api.TechnicianDtos.*;
import ar.edu.utn.tpi.technicians.domain.*;
import ar.edu.utn.tpi.technicians.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.List;

@Service
public class TechnicianManagementService {
    private final TechnicianRepository technicians; private final ReservationRepository reservations;
    public TechnicianManagementService(TechnicianRepository technicians, ReservationRepository reservations) { this.technicians = technicians; this.reservations = reservations; }
    @Transactional public TechnicianResponse create(CreateTechnicianRequest request) {
        Technician technician = technicians.save(new Technician(request.fullName(), request.keycloakUserId(), request.skills(), request.certificationValidUntil(), request.latitude(), request.longitude()));
        return toResponse(technician);
    }
    @Transactional(readOnly = true) public List<TechnicianResponse> list() { return technicians.findAll().stream().map(this::toResponse).toList(); }
    @Transactional(readOnly = true) public TechnicianResponse byKeycloakUserId(String userId) {
        return technicians.findByKeycloakUserId(userId).map(this::toResponse)
            .orElseThrow(() -> new DomainException(HttpStatus.NOT_FOUND, "TECHNICIAN_NOT_FOUND", "No existe un técnico asociado al usuario"));
    }
    @Transactional public TechnicianResponse setActive(Long id, boolean active) { Technician technician = technician(id); technician.setActive(active); return toResponse(technician); }
    @Transactional(readOnly = true) public List<CandidateResponse> candidates(CandidateSearchRequest request) {
        LocalDateTime end = request.startsAt().plusMinutes(request.durationMinutes());
        return technicians.findAll().stream().filter(Technician::isActive).filter(t -> t.getSkills().contains(request.serviceType()))
            .filter(t -> !t.getCertificationValidUntil().isBefore(request.startsAt().toLocalDate()))
            .filter(t -> reservations.findConflicts(t.getId(), request.startsAt(), end, ReservationStatus.RESERVED).isEmpty())
            .map(t -> new CandidateResponse(t.getId(), t.getFullName(), t.getLatitude(), t.getLongitude())).toList();
    }
    @Transactional public ReservationResponse reserve(Long technicianId, ReserveRequest request) {
        Technician technician = technician(technicianId); LocalDateTime end = request.startsAt().plusMinutes(request.durationMinutes());
        if (!technician.isActive() || technician.getCertificationValidUntil().isBefore(request.startsAt().toLocalDate())) throw conflict("TECHNICIAN_NOT_ELIGIBLE", "El técnico no está habilitado para recibir asignaciones");
        if (reservations.findByWorkOrderId(request.workOrderId()).isPresent()) throw conflict("ORDER_ALREADY_RESERVED", "La orden ya posee una reserva de técnico");
        if (!reservations.findConflicts(technicianId, request.startsAt(), end, ReservationStatus.RESERVED).isEmpty()) throw conflict("TECHNICIAN_NOT_AVAILABLE", "El técnico ya posee una reserva superpuesta");
        TechnicianReservation reservation = reservations.save(new TechnicianReservation(technician, request.workOrderId(), request.startsAt(), end));
        return toResponse(reservation);
    }
    @Transactional public void cancelReservation(Long workOrderId) { reservations.findByWorkOrderId(workOrderId).ifPresent(TechnicianReservation::cancel); }
    @Transactional public void completeReservation(Long workOrderId) { reservations.findByWorkOrderId(workOrderId).ifPresent(TechnicianReservation::complete); }
    private Technician technician(Long id) { return technicians.findById(id).orElseThrow(() -> new DomainException(HttpStatus.NOT_FOUND, "TECHNICIAN_NOT_FOUND", "No existe el técnico solicitado")); }
    private DomainException conflict(String code, String message) { return new DomainException(HttpStatus.CONFLICT, code, message); }
    private TechnicianResponse toResponse(Technician t) { return new TechnicianResponse(t.getId(), t.getFullName(), t.getKeycloakUserId(), t.getSkills(), t.getCertificationValidUntil(), t.isActive(), t.getLatitude(), t.getLongitude()); }
    private ReservationResponse toResponse(TechnicianReservation r) { return new ReservationResponse(r.getId(), r.getWorkOrderId(), r.getTechnician().getId(), r.getStartsAt(), r.getEndsAt(), r.getStatus().name()); }
}
