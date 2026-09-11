# Casos de prueba manuales — Sprint 1

| ID | Funcionalidad | Escenario | Resultado esperado | Suite | Estado |
|---|---|---|---|---|---|
| CP-01 | Registro | Registro válido | HTTP 201; usuario sin contraseña, tokens, CVU de 22 dígitos y alias de tres palabras | Smoke y Regression | Ejecutado OK |
| CP-02 | Registro | Email duplicado | HTTP 409; mensaje indicando que el usuario ya existe | Regression | Ejecutado OK |
| CP-03 | Registro | DNI duplicado | HTTP 409; mensaje de conflicto | Regression | Ejecutado OK |
| CP-04 | Registro | Datos inválidos | HTTP 400; detalle de validación | Regression | Ejecutado OK |
| CP-05 | Login | Credenciales válidas | HTTP 200; access token y refresh token | Smoke y Regression | Ejecutado OK |
| CP-06 | Login | Contraseña incorrecta | HTTP 400; mensaje “Contraseña incorrecta” | Regression | Ejecutado OK |
| CP-07 | Seguridad | Acceso sin token | HTTP 401 | Regression | Ejecutado OK |
| CP-08 | Seguridad | Acceso con token propio | HTTP 200; devuelve únicamente sus datos | Smoke y Regression | Ejecutado OK |
| CP-09 | Cuenta | Formato de CVU | Exactamente 22 caracteres numéricos | Regression | Ejecutado OK |
| CP-10 | Cuenta | Formato de alias | Tres palabras separadas por puntos y obtenidas de TXT | Regression | Ejecutado OK |
| CP-11 | Logout | Cierre de sesión con Bearer token | HTTP 200 | Smoke y Regression | Ejecutado OK |
| CP-12 | Logout | Refresh token invalidado | HTTP 400; Keycloak rechaza el token | Regression | Ejecutado OK |
| CP-13 | Sesión | Persistencia al recargar | Iniciar sesión y presionar F5 en una ruta protegida | La sesión continúa activa y el perfil se recupera con Bearer token | Smoke y Regression | Ejecutado OK |
| CP-14 | Recuperación | Solicitud con email existente | HTTP 200 y envío de código de seis dígitos a MailHog | Smoke y Regression | Ejecutado OK |
| CP-15 | Recuperación | Confirmación con código válido | HTTP 200; contraseña anterior rechazada y contraseña nueva aceptada | Smoke y Regression | Ejecutado OK |
| CP-16 | Verificación email | Solicitud autenticada de código | HTTP 200 y envío de código de seis dígitos a MailHog | Regression | Ejecutado OK |
| CP-17 | Verificación email | Confirmación con código válido | HTTP 200; `Email verified` activado en Keycloak | Smoke y Regression | Ejecutado OK |

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

## Ejecución de suites

| Suite | Casos ejecutados | Resultado |
|---|---|---|
| Smoke | CP-01, CP-05, CP-08, CP-11 | Aprobada |
| Regression | CP-01 a CP-12 | Aprobada |

## Resultado final

Se ejecutaron 17 casos de prueba manuales. No se identificaron defectos bloqueantes. Las suites Smoke y Regression finalizaron aprobadas.