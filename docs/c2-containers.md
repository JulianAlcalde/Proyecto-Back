# C2 - Diagrama de contenedores

```mermaid
flowchart LR
    user[Usuarios] --> gateway[API Gateway :8080]
    gateway --> orders[Work Order Service :8082]
    gateway --> technicians[Technician Service :8081]
    gateway --> keycloak[Keycloak :8180]
    orders -->|cuenta de servicio JWT| technicians
    orders -->|consulta rutas| osrm[OSRM público]
    orders --> ordersDb[(orders_db)]
    technicians --> techniciansDb[(technicians_db)]
    keycloak --> keycloakDb[(Base interna Keycloak)]
    shared[shared-kernel JAR] -. dependencia Maven .-> orders
    shared -. dependencia Maven .-> technicians
```

El Gateway es el único servicio de aplicación publicado. Cada microservicio persiste su información en una base independiente y valida el JWT nuevamente. El servicio de órdenes no accede a la base de técnicos: usa su API con una cuenta de servicio de Keycloak.
