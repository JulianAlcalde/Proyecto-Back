# C1 - Diagrama de contexto

```mermaid
flowchart LR
    customer[Cliente] -->|Solicita y consulta reparaciones| system[Backend Servicios Domiciliarios]
    dispatcher[Despachador] -->|Administra técnicos y reasigna| system
    technician[Técnico] -->|Inicia y finaliza su trabajo| system
    system -->|OIDC / JWT / roles| keycloak[Keycloak]
    system -->|Distancia vial para asignar| osrm[API pública OSRM]
```

El sistema asigna una orden al técnico elegible cuya ruta vial al domicilio es más corta. Keycloak es responsable de la identidad; OSRM proporciona un dato que el sistema no posee y que influye directamente en la asignación.
