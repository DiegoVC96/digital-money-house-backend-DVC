# Testing exploratorio - Sprint 1

## Objetivo

Explorar el funcionamiento integrado de registro, login, consulta de datos protegidos, creación de cuenta digital y logout en Digital Money House.

## Organización

Las pruebas se organizan en sesiones con duración estimada de 30 minutos. Cada sesión tiene un objetivo, un recorrido exploratorio y evidencia esperada.

## Ambiente

- Java 21
- Spring Boot 3.3.13
- Docker Desktop
- MySQL 8.4
- Keycloak 26
- Eureka Server
- API Gateway
- Swagger/OpenAPI
- PowerShell

## Sesiones exploratorias

| Sesión | Tipo de tour | Objetivo | Recorrido | Resultado |
|---|---|---|---|---|
| ET-01 | Workflow tour | Verificar el flujo principal del usuario | Registro -> generación de cuenta -> login -> consulta de perfil -> consulta de cuenta -> logout | Exitoso |
| ET-02 | Data tour | Validar datos generados y almacenados | Revisar CVU, alias, usuario en Keycloak, usuario en `users_db` y cuenta en `accounts_db` | Exitoso |
| ET-03 | Error tour | Explorar respuestas ante entradas inválidas | Email duplicado, DNI duplicado, campos inválidos, usuario inexistente y contraseña incorrecta | Exitoso |
| ET-04 | Security tour | Verificar control de acceso | Consultar perfil/cuenta sin token, con token válido y realizar logout por Bearer token | Exitoso |
| ET-05 | Integration tour | Verificar comunicación entre servicios | Revisar Eureka, Gateway, Feign, Keycloak y bases de datos durante registro y login | Exitoso |

## Workflows explorados

### Registro y creación de cuenta

1. Enviar datos válidos a `POST /api/auth/register`.
2. Verificar respuesta HTTP 201.
3. Confirmar que la respuesta no contiene contraseña.
4. Confirmar que contiene usuario, CVU de 22 dígitos, alias de tres palabras y tokens.
5. Verificar la persistencia del perfil en `users_db`.
6. Verificar la persistencia de la cuenta en `accounts_db`.
7. Verificar la identidad y el rol `USER` en Keycloak.

### Inicio de sesión

1. Enviar email y contraseña válidos a `POST /api/auth/login`.
2. Verificar respuesta HTTP 200 y tokens.
3. Probar email inexistente y verificar HTTP 404.
4. Probar contraseña incorrecta y verificar HTTP 400.

### Logout

1. Iniciar sesión y conservar el access token.
2. Enviar `Authorization: Bearer <accessToken>` a `POST /api/auth/logout`.
3. Verificar HTTP 200.
4. Intentar renovar el token con el refresh token anterior.
5. Verificar que Keycloak devuelve HTTP 400.

## Hallazgos

- El registro crea de forma automática una cuenta digital asociada al usuario.
- El CVU se genera con 22 dígitos numéricos.
- El alias se forma con tres palabras obtenidas desde `alias-words.txt`.
- Las rutas protegidas rechazan solicitudes sin token.
- Keycloak invalida la sesión después del logout.
- No se identificaron defectos bloqueantes durante las sesiones ejecutadas.

## Evidencias

- Respuestas HTTP 201, 200, 400, 404 y 409 verificadas manualmente.
- Pruebas unitarias aprobadas en Users Service, Account Service y Auth Service.
- Registro de microservicios validado mediante Eureka.