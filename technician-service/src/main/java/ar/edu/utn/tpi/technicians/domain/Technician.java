package ar.edu.utn.tpi.technicians.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "technicians")
public class Technician {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String fullName;
    @Column(nullable = false, unique = true) private String keycloakUserId;
    @ElementCollection(targetClass = ServiceType.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "technician_skills", joinColumns = @JoinColumn(name = "technician_id"))
    @Enumerated(EnumType.STRING) @Column(name = "service_type", nullable = false)
    private Set<ServiceType> skills = new HashSet<>();
    @Column(nullable = false) private LocalDate certificationValidUntil;
    @Column(nullable = false) private boolean active = true;
    @Column(nullable = false) private double latitude;
    @Column(nullable = false) private double longitude;
    protected Technician() { }
    public Technician(String fullName, String keycloakUserId, Set<ServiceType> skills, LocalDate certificationValidUntil, double latitude, double longitude) {
        this.fullName = fullName; this.keycloakUserId = keycloakUserId; this.skills = skills; this.certificationValidUntil = certificationValidUntil; this.latitude = latitude; this.longitude = longitude;
    }
    public Long getId() { return id; } public String getFullName() { return fullName; } public String getKeycloakUserId() { return keycloakUserId; }
    public Set<ServiceType> getSkills() { return skills; } public LocalDate getCertificationValidUntil() { return certificationValidUntil; }
    public boolean isActive() { return active; } public double getLatitude() { return latitude; } public double getLongitude() { return longitude; }
    public void setActive(boolean active) { this.active = active; }
}
