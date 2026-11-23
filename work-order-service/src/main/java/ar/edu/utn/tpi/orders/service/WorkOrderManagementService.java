package ar.edu.utn.tpi.orders.service;

import ar.edu.utn.tpi.shared.error.DomainException;
import ar.edu.utn.tpi.orders.api.WorkOrderDtos.*;
import ar.edu.utn.tpi.orders.client.TechnicianClient;
import ar.edu.utn.tpi.orders.domain.*;
import ar.edu.utn.tpi.orders.repository.WorkOrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class WorkOrderManagementService {
    private final WorkOrderRepository orders; private final AssignmentService assignment; private final TechnicianClient technicians;
    public WorkOrderManagementService(WorkOrderRepository orders, AssignmentService assignment, TechnicianClient technicians) { this.orders = orders; this.assignment = assignment; this.technicians = technicians; }
    @Transactional public WorkOrderResponse create(String customerId, CreateWorkOrderRequest request) {
        WorkOrder order = orders.save(new WorkOrder(customerId, request.serviceType(), request.description(), request.address(), request.latitude(), request.longitude(), request.requestedStart(), request.durationMinutes()));
        assignment.assign(order);
        return response(order);
    }
    @Transactional(readOnly = true) public List<WorkOrderResponse> byCustomer(String customerId) { return orders.findByCustomerIdOrderByCreatedAtDesc(customerId).stream().map(this::response).toList(); }
    @Transactional(readOnly = true) public List<WorkOrderResponse> all() { return orders.findAll().stream().map(this::response).toList(); }
    @Transactional(readOnly = true) public WorkOrderResponse get(Long id, String requesterId, boolean privileged) { WorkOrder order = order(id); authorizeCustomer(order, requesterId, privileged); return response(order); }
    @Transactional public WorkOrderResponse retryAssignment(Long id) { WorkOrder order = order(id); if (!order.canAttemptAssignment()) throw conflict("ORDER_CANNOT_BE_ASSIGNED", "La orden no admite un nuevo intento de asignación"); assignment.assign(order); return response(order); }
    @Transactional public WorkOrderResponse cancel(Long id, String requesterId, boolean privileged) {
        WorkOrder order = order(id); authorizeCustomer(order, requesterId, privileged); try { order.cancel(); } catch (IllegalStateException ex) { throw conflict("ORDER_CANNOT_BE_CANCELLED", ex.getMessage()); }
        if (order.getTechnicianId() != null) technicians.cancel(order.getId()); return response(order);
    }
    @Transactional public WorkOrderResponse start(Long id, String technicianUserId) { WorkOrder order = order(id); assertAssignedTechnician(order, technicianUserId); try { order.markInProgress(); } catch (IllegalStateException ex) { throw conflict("INVALID_STATUS_TRANSITION", ex.getMessage()); } return response(order); }
    @Transactional public WorkOrderResponse complete(Long id, String technicianUserId) { WorkOrder order = order(id); assertAssignedTechnician(order, technicianUserId); try { order.complete(); } catch (IllegalStateException ex) { throw conflict("INVALID_STATUS_TRANSITION", ex.getMessage()); } technicians.complete(order.getId()); return response(order); }
    private WorkOrder order(Long id) { return orders.findById(id).orElseThrow(() -> new DomainException(HttpStatus.NOT_FOUND, "WORK_ORDER_NOT_FOUND", "No existe la orden solicitada")); }
    private void authorizeCustomer(WorkOrder order, String requesterId, boolean privileged) { if (!privileged && !order.getCustomerId().equals(requesterId)) throw new DomainException(HttpStatus.FORBIDDEN, "ORDER_ACCESS_DENIED", "No tiene permiso para acceder a esta orden"); }
    private void assertAssignedTechnician(WorkOrder order, String userId) {
        Long technicianId = technicians.technicianIdFor(userId);
        if (technicianId == null || !technicianId.equals(order.getTechnicianId())) throw new DomainException(HttpStatus.FORBIDDEN, "TECHNICIAN_NOT_ASSIGNED", "La orden no está asignada al técnico autenticado");
    }
    private DomainException conflict(String code, String message) { return new DomainException(HttpStatus.CONFLICT, code, message); }
    private WorkOrderResponse response(WorkOrder order) { return new WorkOrderResponse(order.getId(), order.getCustomerId(), order.getServiceType(), order.getDescription(), order.getAddress(), order.getRequestedStart(), order.getDurationMinutes(), order.getStatus(), order.getTechnicianId(), order.getRouteDistanceKm(), order.getAssignmentNote(), order.getCreatedAt()); }
}
