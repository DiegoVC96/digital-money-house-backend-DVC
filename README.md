# Digital Money House — Sprint 1

Proyecto full-stack de billetera digital con microservicios Spring Boot, Keycloak y React.

## Estructura

- `discovery-server`: Eureka Server, puerto `8761`.
- `api-gateway`: punto de entrada, puerto `8080`.
- `auth-service`: registro, login, logout, recuperación y verificación de email, puerto `8081`.
- `users-service`: perfiles de usuario, puerto `8082`.
- `account-service`: cuenta digital, CVU y alias, puerto `8083`.
- `FrontEnd`: aplicación React, puerto `3000`.
- `keycloak/import`: realm versionado para importar automáticamente.
- `docs/testing`: plan, casos y evidencias de pruebas.

## Requisitos

- Java 21
- Maven 3.9+
- Docker Desktop
- Node.js y npm

## Inicio rápido

### 1. Infraestructura

Desde la raíz del repositorio:

```bash
docker compose up -d
```

Esto inicia MySQL para usuarios, cuentas y autenticación; Keycloak y MailHog.

- Keycloak: `http://localhost:8180`
- MailHog: `http://localhost:8025`
- Eureka: `http://localhost:8761`

En una instalación limpia, Keycloak importa automáticamente el realm `digital-money-house`.

### 2. Validar y compilar el backend

```bash
mvn clean install
```

### 3. Iniciar microservicios

Abrir una terminal por servicio e iniciarlos en este orden:

```bash
mvn spring-boot:run -pl discovery-server
mvn spring-boot:run -pl users-service
mvn spring-boot:run -pl account-service
mvn spring-boot:run -pl auth-service
mvn spring-boot:run -pl api-gateway
```

### 4. Iniciar el frontend

En otra terminal:

```bash
cd FrontEnd
npm install
npm start
```

Abrir `http://localhost:3000`.

El frontend consume el Gateway en `http://localhost:8080/api`; no requiere `json-server`.

## Funcionalidades

- Registro de usuario y creación automática de cuenta digital.
- CVU de 22 dígitos y alias de tres palabras.
- Login con Keycloak y tokens OAuth2/OIDC.
- Acceso protegido con roles `USER` y `ADMIN`.
- Logout seguro.
- Recuperación de contraseña por código de seis dígitos.
- Verificación de email por código de seis dígitos.
- Códigos visibles en desarrollo desde MailHog.

## Pruebas

Ejecutar la suite automatizada:

```bash
mvn clean test
```

Los casos manuales y la evidencia del Sprint 1 están en `docs/testing`.

## Nota de seguridad

Las credenciales incluidas son exclusivas para desarrollo local. En producción deben reemplazarse por variables de entorno o un gestor de secretos.