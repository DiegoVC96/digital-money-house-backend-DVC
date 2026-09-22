# Casos de prueba — Sprint 2

| ID | Funcionalidad | Pasos resumidos | Resultado esperado |
|---|---|---|---|
| S2-01 | Dashboard | Iniciar sesión y abrir Inicio. | Se muestra saldo y hasta cinco movimientos. |
| S2-02 | Movimientos | Consultar `GET /api/accounts/{accountId}/transactions?limit=5` con token del propietario. | HTTP 200 y lista de máximo cinco movimientos. |
| S2-03 | Límite de movimientos | Consultar movimientos con `limit=6`. | HTTP 400. |
| S2-04 | Seguridad de cuenta | Consultar una cuenta ajena con token de otro usuario. | HTTP 403. |
| S2-05 | Ver perfil | Consultar `GET /api/users/{userId}` con token propio. | HTTP 200 con los datos del perfil. |
| S2-06 | Actualizar usuario | Modificar nombre, apellido y teléfono mediante `PATCH /api/users/{userId}`. | HTTP 200; email y DNI no cambian. |
| S2-07 | Actualizar alias | Modificar alias mediante `PATCH /api/accounts/{accountId}`. | HTTP 200; CVU y saldo no cambian. |
| S2-08 | Alias inválido | Enviar un alias que no tenga tres palabras minúsculas separadas por puntos. | HTTP 400. |
| S2-09 | Alta de tarjeta | Asociar `4111111111111111`, vencimiento `1026` y CVC `123`. | HTTP 201; respuesta con VISA y últimos cuatro `1111`. |
| S2-10 | Datos sensibles | Revisar respuesta del alta y listado de tarjeta. | No contiene PAN completo, CVC ni huella. |
| S2-11 | Tarjeta duplicada | Asociar nuevamente la misma tarjeta. | HTTP 409. |
| S2-12 | Detalle de tarjeta | Consultar `GET /api/accounts/{accountId}/cards/{cardId}`. | HTTP 200 con datos enmascarados. |
| S2-13 | Eliminar tarjeta | Ejecutar `DELETE /api/accounts/{accountId}/cards/{cardId}`. | HTTP 200. |
| S2-14 | Tarjeta eliminada | Consultar la tarjeta eliminada. | HTTP 404. |
| S2-15 | Frontend | Crear y eliminar una tarjeta desde Mis tarjetas. | La lista se actualiza y persiste al recargar. |
| S2-16 | Smoke API | Ejecutar `AccountApiSmokeTest` con variables `SMOKE_*`. | Login, Gateway y consulta de cuenta responden correctamente. |

## Ejecución registrada

- S2-01: aprobado — Dashboard conectado al Gateway y sin movimientos iniciales.
- S2-03: aprobado — `limit=6` devuelve HTTP 400.
- S2-06: aprobado — teléfono actualizado; email y DNI sin cambios.
- S2-07: aprobado — alias actualizado; CVU y saldo sin cambios.
- S2-09: aprobado — tarjeta VISA creada con últimos cuatro `1111`.
- S2-11: aprobado — tarjeta duplicada devuelve HTTP 409.
- S2-12: aprobado — detalle de tarjeta disponible.
- S2-13 y S2-14: aprobados — eliminación HTTP 200 y consulta posterior HTTP 404.
- S2-15: aprobado — alta y eliminación verificadas desde el frontend.
- S2-16: aprobado — RestAssured ejecutó una prueba contra Gateway con resultado exitoso.