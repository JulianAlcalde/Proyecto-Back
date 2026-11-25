package ar.edu.utn.tpi.orders.service;

import ar.edu.utn.tpi.orders.client.*;
import ar.edu.utn.tpi.orders.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AssignmentServiceTest {
    @Test
    void assignsTheAvailableTechnicianWithTheShortestRoadRoute() {
        TechnicianClient technicians = mock(TechnicianClient.class);
        OsrmRoutingClient osrm = mock(OsrmRoutingClient.class);
        WorkOrder order = new WorkOrder("customer", ServiceType.PLOMERIA, "Pérdida de agua", "Córdoba", -31.42, -64.18, LocalDateTime.now().plusDays(2), 60);
        when(technicians.findCandidates(any(), any(), anyInt())).thenReturn(List.of(new TechnicianClient.Candidate(7L, "Lejano", -31.4, -64.3), new TechnicianClient.Candidate(8L, "Cercano", -31.4, -64.2)));
        when(osrm.routeDistanceKm(-31.4, -64.3, -31.42, -64.18)).thenReturn(12d);
        when(osrm.routeDistanceKm(-31.4, -64.2, -31.42, -64.18)).thenReturn(3d);

        new AssignmentService(technicians, osrm).assign(order);

        verify(technicians).reserve(eq(8L), any(), any(), eq(60));
        assertThat(order.getTechnicianId()).isEqualTo(8L);
        assertThat(order.getRouteDistanceKm()).isEqualTo(3d);
        assertThat(order.getStatus()).isEqualTo(WorkOrderStatus.ASSIGNED);
    }

    @Test
    void marksAssignmentFailedWhenNoTechniciansAreAvailable() {
        TechnicianClient technicians = mock(TechnicianClient.class);
        OsrmRoutingClient osrm = mock(OsrmRoutingClient.class);
        WorkOrder order = new WorkOrder("customer", ServiceType.PLOMERIA, "Pérdida de agua", "Córdoba", -31.42, -64.18, LocalDateTime.now().plusDays(2), 60);
        when(technicians.findCandidates(any(), any(), anyInt())).thenReturn(List.of());

        new AssignmentService(technicians, osrm).assign(order);

        verify(technicians, never()).reserve(anyLong(), any(), any(), anyInt());
        verifyNoInteractions(osrm);
        assertThat(order.getTechnicianId()).isNull();
        assertThat(order.getStatus()).isNotEqualTo(WorkOrderStatus.ASSIGNED);
    }

    @Test
    void marksAssignmentFailedWhenExternalDependencyThrowsException() {
        TechnicianClient technicians = mock(TechnicianClient.class);
        OsrmRoutingClient osrm = mock(OsrmRoutingClient.class);
        WorkOrder order = new WorkOrder("customer", ServiceType.PLOMERIA, "Pérdida de agua", "Córdoba", -31.42, -64.18, LocalDateTime.now().plusDays(2), 60);
        when(technicians.findCandidates(any(), any(), anyInt())).thenReturn(List.of(new TechnicianClient.Candidate(8L, "Cercano", -31.4, -64.2)));
        when(osrm.routeDistanceKm(anyDouble(), anyDouble(), anyDouble(), anyDouble())).thenThrow(new RestClientException("OSRM timeout"));

        new AssignmentService(technicians, osrm).assign(order);

        verify(technicians, never()).reserve(anyLong(), any(), any(), anyInt());
        assertThat(order.getTechnicianId()).isNull();
        assertThat(order.getStatus()).isNotEqualTo(WorkOrderStatus.ASSIGNED);
    }
}