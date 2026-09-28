# Plan de pruebas — Sprint 2

## Objetivo

Verificar que el usuario autenticado pueda consultar su cuenta y movimientos, actualizar datos permitidos de su perfil y gestionar tarjetas asociadas a su cuenta.

## Alcance

- Dashboard: saldo disponible y últimos cinco movimientos.
- Perfil: consulta y actualización de alias; actualización de nombre, apellido y teléfono.
- Inmutabilidad de CVU, saldo, email y DNI.
- Tarjetas: alta, listado, detalle y eliminación.
- Seguridad: solo el propietario o un administrador puede acceder a recursos de una cuenta.
- Validaciones: alias, límites de movimientos, formato de tarjeta, duplicados y recursos inexistentes.
- Pruebas unitarias de `users-service` y `account-service`.
- Smoke API con RestAssured a través de API Gateway.

## Ambiente

- Java 21
- Spring Boot 3.3.13
- MySQL 8.4
- Keycloak 26
- Eureka Server: `http://localhost:8761`
- API Gateway: `http://localhost:8080`
- Users Service: `http://localhost:8082`
- Account Service: `http://localhost:8083`
- FrontEnd: `http://localhost:3000`

## Criterios de entrada

- Contenedores de Docker iniciados.
- Keycloak disponible y realm importado.
- Eureka, Gateway, Auth Service, Users Service y Account Service activos.
- Usuario de pruebas registrado y autenticable.
- Build Maven exitoso.

## Criterios de salida

- Casos críticos ejecutados sin fallos bloqueantes.
- Pruebas unitarias exitosas.
- Smoke API exitoso con RestAssured.
- Frontend construido correctamente.
- Casos manuales documentados.

## Riesgos

- Token Keycloak vencido durante una prueba.
- Servicio aún no registrado en Eureka, provocando `503` en Gateway.
- Datos persistentes de pruebas que generen conflictos de alias o tarjeta.
- El número de tarjeta o CVC no debe aparecer en respuestas ni persistirse.

## Evidencia automatizada

- `AccountServiceTest`: cuenta, movimientos y actualización de alias.
- `CardServiceTest`: alta segura, duplicados y control de acceso.
- `UserServiceTest`: actualización de datos editables.
- `AccountApiSmokeTest`: login, token OAuth2, Gateway y consulta de cuenta.