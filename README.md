# Digital Money House — Sprint 1

Proyecto full-stack de billetera digital con microservicios Spring Boot, Keycloak y React.

## Estructura

- `discovery-server`: Eureka Server, puerto `8761`.
- `api-gateway`: punto de entrada, puerto `8080`.
- `auth-service`: registro, login, logout, recuperación y verificación de email, puerto `8081`.
- `users-service`: perfiles de usuario, puerto `8082`.
- `account-service`: cuenta digital, CVU y alias, puerto `8083`.
- `FrontEnd`: aplicación React, puerto `3000`.
- `keycloak/import`: definición reproducible del realm.
- `keycloak/bootstrap`: configuración automática del secreto del cliente técnico.
- `docs/testing`: plan, casos y evidencias de pruebas.

## Requisitos

- Java 21
- Maven 3.9+
- Docker Desktop
- Node.js y npm

## Inicio desde una instalación limpia

### 1. Configurar variables locales

Copiar el archivo de ejemplo:

```powershell
Copy-Item .env.example .env
```

Generar un secreto local de 64 caracteres para el cliente técnico:

```powershell
$newSecret = [guid]::NewGuid().ToString("N") + [guid]::NewGuid().ToString("N")

(Get-Content .env) `
  -replace '^KEYCLOAK_AUTH_SERVICE_SECRET=.*$', "KEYCLOAK_AUTH_SERVICE_SECRET=$newSecret" |
  Set-Content .env
```

No versionar `.env` ni compartir sus valores. Docker Compose toma las variables desde ese archivo.

> Si la terminal tuviera una variable anterior con el mismo nombre, eliminarla antes de levantar Docker:
>
> ```powershell
> Remove-Item Env:\KEYCLOAK_AUTH_SERVICE_SECRET -ErrorAction SilentlyContinue
> ```

### 2. Iniciar infraestructura

Desde la raíz del repositorio:

```powershell
docker compose up -d
docker compose ps
docker compose logs keycloak-bootstrap
```

Esto inicia MySQL para usuarios, cuentas y autenticación; Keycloak, MailHog y el bootstrap de Keycloak.

- Keycloak: `http://localhost:8180`
- MailHog: `http://localhost:8025`
- Eureka: `http://localhost:8761`

En una instalación limpia, Keycloak importa automáticamente el realm `digital-money-house`. El contenedor `keycloak-bootstrap` aplica el secreto local al cliente técnico `auth-service`; debe finalizar con código `0` y el mensaje `Cliente técnico de Keycloak configurado.`

El realm incluye una Service Account para `auth-service`, con el rol `SERVICE` y los permisos mínimos de `realm-management` necesarios para administrar usuarios. No se requiere configuración manual en Keycloak.

### 3. Validar y compilar el backend

```powershell
mvn clean test
mvn install -pl common
```

### 4. Iniciar microservicios

Abrir una terminal por servicio e iniciarlos en este orden:

```powershell
mvn spring-boot:run -pl discovery-server
```

```powershell
mvn spring-boot:run -pl users-service
```

```powershell
mvn spring-boot:run -pl account-service
```

Para Auth Service, cargar el mismo secreto de `.env` en la terminal:

```powershell
$env:KEYCLOAK_AUTH_SERVICE_SECRET = (
  Get-Content .env |
  Where-Object { $_ -like "KEYCLOAK_AUTH_SERVICE_SECRET=*" }
).Split("=", 2)[1]

mvn spring-boot:run -pl auth-service
```

Finalmente:

```powershell
mvn spring-boot:run -pl api-gateway
```

Comprobar en `http://localhost:8761` que estén registrados `USERS-SERVICE`, `ACCOUNT-SERVICE`, `AUTH-SERVICE` y `API-GATEWAY`.

### 5. Iniciar el frontend

En otra terminal:

```powershell
Set-Location FrontEnd
npm install
npm start
```

Abrir `http://localhost:3000`.

El frontend consume el Gateway en `http://localhost:8080/api`; no requiere `json-server`.

## Validación reproducible

Para repetir una prueba completamente limpia, eliminando únicamente datos locales de Docker:

```powershell
docker compose down -v
docker compose up -d
```

Luego esperar a que `keycloak-bootstrap` finalice correctamente, iniciar los microservicios y registrar un usuario mediante:

```text
POST http://localhost:8080/api/auth/register
```

El registro crea el usuario en Keycloak, el perfil en Users Service y una cuenta digital en Account Service. La respuesta incluye un CVU de 22 dígitos y un alias de tres palabras.

## Funcionalidades

- Registro de usuario y creación automática de cuenta digital.
- CVU de 22 dígitos y alias de tres palabras.
- Login con Keycloak y tokens OAuth2/OIDC.
- Acceso protegido con roles `USER`, `ADMIN` y `SERVICE`.
- Logout seguro.
- Recuperación de contraseña por código de seis dígitos.
- Verificación de email por código de seis dígitos.
- Códigos visibles en desarrollo desde MailHog.

## Pruebas

Ejecutar la suite automatizada:

```powershell
mvn clean test
```

Los casos manuales, plan de pruebas y evidencias del Sprint 1 están en `docs/testing`.

## Seguridad

`.env` está excluido del repositorio. `.env.example` contiene solo valores de ejemplo; cada instalación debe generar su propio secreto técnico.