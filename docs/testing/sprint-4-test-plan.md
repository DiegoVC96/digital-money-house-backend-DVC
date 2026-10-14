# Plan de pruebas — Sprint 4

## Objetivo

Verificar que una persona usuaria pueda transferir saldo disponible a otra cuenta mediante CVU o alias y consultar sus destinatarios recientes.

## Alcance

- Transferencias entre dos cuentas digitales existentes.
- Débito y crédito atómicos, con movimientos `TRANSFER_OUT` y `TRANSFER_IN`.
- Consulta de hasta cinco destinatarios recientes.
- Descarga de comprobante PDF desde el detalle de una operación.
- Validación de destino inexistente, fondos insuficientes y acceso a cuentas ajenas.
- Integración con FrontEnd, API Gateway, Keycloak y Account Service.

## Evidencia automatizada

- `AccountServiceTest` valida transferencia exitosa, destino inexistente, fondos insuficientes, destinatarios recientes y acceso ajeno.
- La suite de `account-service` finalizó correctamente con 28 pruebas: 22 de `AccountServiceTest` y 6 de `CardServiceTest`.

## Evidencia manual registrada

- Transferencia de `$400` por alias completada: saldo de origen `$1.100`, saldo de destino `$400` y destinatario reciente `rio.rio.mar`.
- Destino inexistente: HTTP `400`.
- Fondos insuficientes: HTTP `410`.
- Consulta de destinatarios de una cuenta ajena: HTTP `403`.
- FrontEnd: transferencia de `$100` y visualización correcta de su detalle tras corregir la redirección por `id`.

## Entregables de calidad

- Casos Smoke y Regression: `Digital-Money-House-Test-Suites.xlsx`.
- Notas de testing exploratorio: `sprint-4-exploratory-testing.md`.
- Métricas y aprobación QA: `sprint-4-qa-sign-off.md`.
