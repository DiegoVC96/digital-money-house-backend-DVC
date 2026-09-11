# Casos de prueba manuales — Sprint 1

| ID | Funcionalidad | Escenario | Datos / acción | Resultado esperado | Estado |
|---|---|---|---|---|---|
| CP-01 | Registro | Registro válido | Enviar datos válidos a `POST /api/auth/register` | HTTP 201; devuelve usuario, access token, refresh token, CVU de 22 dígitos y alias de tres palabras | Ejecutado OK |
| CP-02 | Registro | Email duplicado | Registrar un email ya existente | HTTP 409; mensaje indicando que el usuario ya existe | Pendiente |
| CP-03 | Registro | DNI duplicado | Registrar un DNI ya existente con otro email | HTTP 409; mensaje de conflicto | Pendiente |
| CP-04 | Registro | Datos inválidos | Enviar email inválido, contraseña corta o campos vacíos | HTTP 400; detalle de validación | Pendiente |
| CP-05 | Login | Credenciales válidas | Enviar email y contraseña correctos a `POST /api/auth/login` | HTTP 200; devuelve access token y refresh token | Ejecutado OK |
| CP-06 | Login | Contraseña inválida | Enviar email válido con contraseña incorrecta | HTTP 401; mensaje “Credenciales inválidas” | Pendiente |
| CP-07 | Seguridad | Acceso sin token | Consultar perfil o cuenta sin encabezado Authorization | HTTP 401 | Ejecutado OK |
| CP-08 | Seguridad | Acceso con token propio | Consultar perfil y cuenta mediante token Bearer del usuario | HTTP 200; devuelve únicamente sus datos | Ejecutado OK |
| CP-09 | Cuenta | Formato de CVU | Revisar el CVU obtenido después de registrar | Exactamente 22 caracteres numéricos | Ejecutado OK |
| CP-10 | Cuenta | Formato de alias | Revisar el alias obtenido después de registrar | Tres palabras separadas por puntos | Ejecutado OK |
| CP-11 | Logout | Cierre de sesión válido | Enviar refresh token a `POST /api/auth/logout` | HTTP 200 | Ejecutado OK |
| CP-12 | Logout | Refresh token invalidado | Solicitar token a Keycloak con el refresh token luego del logout | HTTP 400; Keycloak rechaza el token | Ejecutado OK |

## Datos de prueba sugeridos

| Campo | Valor |
|---|---|
| Nombre | Martina |
| Apellido | Lopez |
| Teléfono | 1187654321 |
| DNI | 45678901 |
| Email | martina.sprint1@example.com |
| Contraseña | ClaveSegura123 |

## Notas de testing exploratorio

Durante las pruebas se debe observar especialmente:

- Si los cinco servicios aparecen disponibles en Eureka.
- Si un usuario registrado existe tanto en Keycloak como en `users_db`.
- Si una cuenta creada existe en `accounts_db`.
- Si el Gateway puede enrutar correctamente las solicitudes.
- Si el token permite acceder únicamente a recursos del propietario.
- Si los mensajes y códigos de error son coherentes.