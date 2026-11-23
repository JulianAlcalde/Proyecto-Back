package ar.edu.utn.tpi.technicians.repository;

import ar.edu.utn.tpi.technicians.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.*;

public interface ReservationRepository extends JpaRepository<TechnicianReservation, Long> {
    Optional<TechnicianReservation> findByWorkOrderId(Long workOrderId);
    @Query("select r from TechnicianReservation r where r.technician.id = :technicianId and r.status = :status and r.startsAt < :endsAt and r.endsAt > :startsAt")
    List<TechnicianReservation> findConflicts(@Param("technicianId") Long technicianId, @Param("startsAt") LocalDateTime startsAt, @Param("endsAt") LocalDateTime endsAt, @Param("status") ReservationStatus status);
}
