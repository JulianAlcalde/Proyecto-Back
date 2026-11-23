package ar.edu.utn.tpi.technicians.repository;

import ar.edu.utn.tpi.technicians.domain.Technician;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TechnicianRepository extends JpaRepository<Technician, Long> { Optional<Technician> findByKeycloakUserId(String keycloakUserId); }
