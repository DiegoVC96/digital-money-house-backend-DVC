# Casos de prueba — Sprint 3

| ID | Funcionalidad | Pasos resumidos | Resultado esperado |
|---|---|---|---|
| S3-01 | Actividad completa | Consultar `GET /api/accounts/{accountId}/activity` con token del propietario. | HTTP 200 y movimientos ordenados del más reciente al más antiguo. |
| S3-02 | Detalle de actividad | Consultar `GET /api/accounts/{accountId}/activity/{transactionId}` con movimiento de la cuenta. | HTTP 200 y detalle del movimiento. |
| S3-03 | Movimiento inexistente | Consultar el detalle con `transactionId` inexistente. | HTTP 404 con `Movimiento no encontrado`. |
| S3-04 | Cuenta ajena | Consultar actividad de otra cuenta con token de usuario. | HTTP 403. |
| S3-05 | Período inválido | Enviar `from` posterior a `to`. | HTTP 400. |
| S3-06 | Filtro por dirección | Consultar con `type=INCOME` o `type=EXPENSE`. | HTTP 200 y sólo movimientos de la dirección solicitada. |
| S3-07 | Filtro por importe | Consultar con un rango, por ejemplo `range=FROM_1000_TO_5000`. | HTTP 200 y movimientos dentro del rango. |
| S3-08 | Alta de dinero | Ejecutar `POST /api/accounts/{accountId}/transferences` con `cardId` propio y `amount` positivo. | HTTP 201, movimiento `DEPOSIT` y saldo incrementado. |
| S3-09 | Importe inválido | Intentar un ingreso con importe cero, negativo o más de dos decimales. | HTTP 400. |
| S3-10 | Tarjeta ajena o inexistente | Intentar un ingreso con una tarjeta no asociada a la cuenta. | HTTP 404 con `Tarjeta no encontrada`. |
| S3-11 | FrontEnd: ingreso | Seleccionar tarjeta, ingresar importe y confirmar. | Se actualiza el saldo y aparece el movimiento en Actividad. |
| S3-12 | FrontEnd: filtros | Aplicar tipo, rango y período desde Actividad. | La lista se actualiza y Limpiar restablece todos los movimientos. |
| S3-13 | Fecha vencida | Intentar asociar una tarjeta con vencimiento anterior al mes actual. | HTTP 400; mensaje indicando que la tarjeta está vencida. |
| S3-14 | Fecha inválida | Intentar asociar una tarjeta con mes fuera de `01` a `12`. | HTTP 400; campo `expiration` y formato `MMYY` informado. |
| S3-15 | Rango inválido | Consultar actividad con `range` no reconocido. | HTTP 400; enumera los valores de rango permitidos. |

## Ejecución registrada

- S3-01 y S3-02: aprobados — actividad y detalle consultados mediante Gateway.
- S3-06 y S3-07: aprobados — filtros de ingreso y rango `$1.000 a $5.000`, y hasta `$1.000` verificados en FrontEnd.
- S3-08: aprobado — depósito de `$1.500` acreditado con tarjeta terminada en `1111`; saldo actualizado.
- S3-11: aprobado — depósito adicional de `$500` realizado desde FrontEnd; saldo final `$2.000`.
- S3-12: aprobado — filtros por tipo, rango y fecha del día verificados visualmente.
- S3-13 y S3-14: aprobados — tarjetas vencida e inválida rechazadas con mensajes específicos.
- S3-15: aprobado — `range=INVALIDO` rechazado e informa todas las alternativas permitidas.
- S3-01, S3-02, S3-03, S3-08 y S3-09: aprobados por `AccountApiSmokeTest` contra Gateway el 2026-10-13. El smoke autenticó, consultó actividad sin filtros, acreditó `$1,00`, obtuvo el detalle y rechazó importe cero.
- S3-04: cubierto por `AccountServiceTest` (acceso de un usuario ajeno rechazado). El equivalente smoke es opcional y se habilita con `SMOKE_OTHER_ACCOUNT_ID`.
