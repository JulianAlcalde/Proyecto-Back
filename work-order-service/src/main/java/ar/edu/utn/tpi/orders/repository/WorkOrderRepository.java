package ar.edu.utn.tpi.orders.repository;

import ar.edu.utn.tpi.orders.domain.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> { List<WorkOrder> findByCustomerIdOrderByCreatedAtDesc(String customerId); }
