# Plan de pruebas — Sprint 1

## Proyecto

Digital Money House — Backend

## Objetivo

Verificar que el módulo de registro, autenticación, cierre de sesión y creación de cuenta digital funcione correctamente dentro de la arquitectura de microservicios.

## Alcance

Se probarán las siguientes funcionalidades:

- Registro de usuario mediante `POST /api/auth/register`.
- Creación automática del usuario en Keycloak.
- Creación del perfil en Users Service.
- Creación automática de la cuenta digital en Account Service.
- Generación de CVU de 22 dígitos.
- Generación de alias con tres palabras separadas por puntos.
- Inicio de sesión mediante `POST /api/auth/login`.
- Acceso a endpoints protegidos mediante token Bearer.
- Cierre de sesión mediante `POST /api/auth/logout`.
- Invalidación del refresh token en Keycloak.
- Validaciones, errores de datos y duplicidad de usuarios.

## Ambiente de pruebas

- Java 21
- Spring Boot 3.3.13
- MySQL 8.4
- Keycloak 26
- Docker Desktop
- PowerShell
- Swagger/OpenAPI
- Eureka Server: `http://localhost:8761`
- API Gateway: `http://localhost:8080`
- Auth Service: `http://localhost:8081`
- Users Service: `http://localhost:8082`
- Account Service: `http://localhost:8083`

## Criterios de entrada

- Contenedores MySQL y Keycloak iniciados.
- Realm, clientes y roles configurados en Keycloak.
- Eureka Server, Gateway y microservicios en ejecución.
- Bases de datos disponibles.
- Compilación Maven exitosa.

## Criterios de salida

- Casos críticos de registro, login y logout ejecutados.
- Las pruebas unitarias finalizan sin errores.
- No existen fallos bloqueantes abiertos.
- Los resultados de pruebas manuales están documentados.

## Riesgos identificados

- Interrupción de comunicación entre microservicios mediante Eureka.
- Datos de usuario duplicados.
- Token vencido o inválido.
- Indisponibilidad de Keycloak.
- Error al crear una cuenta luego de crear el usuario.

## Evidencia ejecutada

- Registro exitoso de usuario y cuenta digital.
- CVU generado con 22 dígitos.
- Alias generado con formato de tres palabras.
- Login exitoso con emisión de tokens OAuth2.
- Logout exitoso con respuesta HTTP 200.
- Refresh token rechazado con HTTP 400 después del logout.
- Pruebas unitarias exitosas en Users Service, Account Service y Auth Service.