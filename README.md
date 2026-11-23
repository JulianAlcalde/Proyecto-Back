# TPI - Coordinación de servicios técnicos domiciliarios

Backend de microservicios para gestionar reparaciones domiciliarias. Una persona solicita una visita; el sistema busca técnicos activos, certificados y sin turnos solapados; consulta a OSRM la distancia vial desde cada candidato al domicilio; reserva al técnico más cercano y permite iniciar, completar o cancelar la orden.

## Alcance del MVP

El proceso central es una orden de reparación desde la solicitud hasta su resolución:

1. Un cliente crea una orden con especialidad, dirección georreferenciada, fecha y duración.
2. `work-order-service` consulta candidatos disponibles en `technician-service`.
3. Por cada candidato, consulta OSRM para obtener distancia vial, elige el menor resultado y solicita una reserva.
4. La orden queda `ASSIGNED`, o `ASSIGNMENT_FAILED` cuando no hay candidatos o falla una dependencia. Un despachador puede reintentar la asignación.
5. El técnico asignado inicia y completa la orden. El cliente puede cancelar una orden que aún no haya iniciado.

Reglas principales:

- Un técnico debe estar activo, certificado al día y tener la especialidad solicitada.
- No se permiten reservas de turnos superpuestos ni dos reservas para una misma orden.
- Sólo el técnico asignado puede iniciar o completar una orden.
- Un cliente sólo consulta o cancela sus propias órdenes.
- Las órdenes iniciadas o finalizadas no pueden cancelarse.
- Una falla al consultar OSRM o al reservar no asigna de manera parcial: la orden queda en estado reintentable y conserva el motivo.

## Arquitectura

| Componente | Responsabilidad |
| --- | --- |
| `api-gateway` | Punto de entrada, routing y filtro de seguridad JWT. Publica Swagger consolidado. |
| `technician-service` | Técnicos, certificaciones, especialidades, disponibilidad y reservas. |
| `work-order-service` | Órdenes, transiciones de estado y orquestación de la asignación. |
| `shared-kernel` | JAR Maven reutilizado por ambos servicios con manejo uniforme de errores y conversión de roles de Keycloak. |
| Keycloak | OIDC, JWT, usuarios, roles y cuenta de servicio. |
| PostgreSQL | Una base para órdenes y otra para técnicos. |
| OSRM | API pública que aporta la distancia vial usada para decidir la asignación. |

Los diagramas C4 están en [C1](docs/c1-context.md) y [C2](docs/c2-containers.md).

## Ejecución

Se requiere Docker Desktop con acceso a Internet para descargar imágenes y consultar OSRM.

```powershell
docker compose up --build
```

Puntos de acceso:

- Gateway y Swagger: `http://localhost:8080/swagger-ui.html`
- Keycloak: `http://localhost:8180`
- Administración de Keycloak: `admin` / `admin`

Para recomenzar desde una base vacía, detener la solución y eliminar el volumen de PostgreSQL:

```powershell
docker compose down -v
```

## Usuarios de demostración

| Usuario | Contraseña | Rol |
| --- | --- | --- |
| `admin` | `admin123` | `ADMIN`, `DISPATCHER` |
| `despachador` | `despachador123` | `DISPATCHER` |
| `cliente` | `cliente123` | `CUSTOMER` |
| `tecnico` | `tecnico123` | `TECHNICIAN` |

Obtener un token para cada usuario con el cliente público `tpi-api`:

```powershell
$token = (Invoke-RestMethod -Method Post -Uri 'http://localhost:8180/realms/tpi/protocol/openid-connect/token' -ContentType 'application/x-www-form-urlencoded' -Body 'client_id=tpi-api&grant_type=password&username=cliente&password=cliente123').access_token
```

En Postman o Swagger, enviar `Authorization: Bearer <token>`. La colección incluida en [docs/TPI.postman_collection.json](docs/TPI.postman_collection.json) contiene las operaciones principales.

## Recorrido de demostración

1. Obtener token de `despachador` y crear un técnico. Para vincular el usuario de demostración `tecnico`, usar el id `44444444-4444-4444-4444-444444444444` en `keycloakUserId`.

```json
POST /api/technicians
{
  "fullName": "María Gómez",
  "keycloakUserId": "44444444-4444-4444-4444-444444444444",
  "skills": ["PLOMERIA"],
  "certificationValidUntil": "2027-12-31",
  "latitude": -31.4201,
  "longitude": -64.1888
}
```

2. Obtener token de `cliente` y crear una orden para un turno futuro.

```json
POST /api/work-orders
{
  "serviceType": "PLOMERIA",
  "description": "Pérdida de agua debajo de la mesada",
  "address": "Nueva Córdoba, Córdoba",
  "latitude": -31.4286,
  "longitude": -64.1842,
  "requestedStart": "2027-06-20T10:00:00",
  "durationMinutes": 60
}
```

3. Confirmar que la respuesta está `ASSIGNED` e incluye `technicianId` y `routeDistanceKm`.
4. Obtener token de `tecnico`, ejecutar `POST /api/work-orders/{id}/start` y luego `POST /api/work-orders/{id}/complete`.

Para demostrar la contingencia, detener OSRM mediante una URL inválida en `OSRM_BASE_URL` antes de crear una orden: la respuesta quedará `ASSIGNMENT_FAILED`; restaurar la URL y usar `POST /api/work-orders/{id}/assign` con token de despachador.

## Desarrollo local y pruebas

La compilación local instala primero el JAR reutilizable en el repositorio Maven local y ejecuta las pruebas:

```powershell
mvn clean install
```

Cada servicio cuenta con OpenAPI; el Gateway agrega ambas definiciones en su Swagger. La configuración de Docker no publica los puertos de los microservicios: el acceso de usuarios se realiza exclusivamente por el Gateway.

Las tres aplicaciones exponen `GET /actuator/health`. Es el único endpoint operativo sin autenticación y permite que un orquestador o una herramienta de monitoreo compruebe la disponibilidad de cada componente.
