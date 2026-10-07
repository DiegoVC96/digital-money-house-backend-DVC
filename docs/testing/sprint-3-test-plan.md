# Plan de pruebas — Sprint 3

## Objetivo

Verificar que una persona usuaria pueda consultar su actividad completa, ver el detalle de cada movimiento e ingresar dinero desde una tarjeta ya asociada a su cuenta.

## Alcance

- Listado de actividad ordenado desde el movimiento más reciente.
- Detalle de un movimiento de la propia cuenta.
- Filtros opcionales por período, dirección (ingreso o egreso) e importe aproximado.
- Ingreso de dinero desde una tarjeta asociada.
- Actualización atómica de saldo y persistencia del movimiento `DEPOSIT`.
- Integración FrontEnd, API Gateway, Keycloak, Users Service y Account Service.
- Pruebas unitarias, smoke API y pruebas exploratorias.

## Ambiente

- Java 21 y Spring Boot 3.3.13.
- MySQL 8.4, Keycloak 26 y Docker Compose.
- Eureka: `http://localhost:8761`.
- API Gateway: `http://localhost:8080`.
- FrontEnd: `http://localhost:3000`.

## Criterios de entrada

- Contenedores de infraestructura disponibles.
- Eureka, Gateway, Auth, Users y Account Service registrados.
- Usuario autenticable con cuenta digital y tarjeta asociada.
- Token de acceso vigente.

## Criterios de salida

- Casos críticos aprobados sin defectos bloqueantes.
- Suite de `account-service` exitosa.
- FrontEnd compilado correctamente.
- Evidencia exploratoria y casos manuales documentados.

## Evidencia automatizada

- `AccountServiceTest` cubre lectura de actividad, control de acceso, filtro de dirección, filtro por importe, período inválido, ingreso válido y tarjeta ajena.
- `CardServiceTest` mantiene la cobertura de la gestión de tarjetas del Sprint 2.
- `AccountApiSmokeTest` continúa verificando login, Gateway y consulta de la cuenta autenticada cuando se configuran las variables `SMOKE_*`.

## Riesgos

- Un token de Keycloak vencido devuelve `401`; se debe iniciar sesión nuevamente.
- Un servicio todavía no registrado en Eureka puede provocar `503` temporal en Gateway.
- La tarjeta debe pertenecer a la cuenta autenticada; una tarjeta ajena no puede acreditar saldo.
- Los filtros de rango incluyen el límite superior para facilitar la interpretación de los importes mostrados.
