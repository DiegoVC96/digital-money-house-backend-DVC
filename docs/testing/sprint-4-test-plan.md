# Plan de pruebas — Sprint 4

## Objetivo

Verificar transferencias por alias o CVU, sus controles de seguridad y negocio, los destinatarios recientes y el comprobante de la operación.

## Alcance

- Transferencias entre cuentas existentes, con débito y crédito atómicos.
- Consulta de hasta cinco destinatarios recientes.
- Errores de destino inexistente, saldo insuficiente, cuenta ajena, cuenta propia e importe inválido.
- Autenticación obligatoria para las APIs de transferencia.
- Detalle de operación y descarga de comprobante PDF.
- Pruebas de concurrencia del débito de saldo, controladores y smoke test contra el ambiente integrado.

## Suites

| Suite | Propósito | Casos incluidos |
|---|---|---|
| Smoke | Verificar los flujos críticos disponibles en un ambiente integrado. | Cuenta propia, actividad, transferencia y autenticación obligatoria. |
| Regression | Verificar reglas de negocio, errores HTTP, seguridad y experiencia de usuario. | Destino, saldo, cuenta ajena, cuenta propia, monto, destinatarios, comprobante y concurrencia. |

## Evidencia automatizada

- `AccountServiceTest` cubre transferencia exitosa por alias, destino inexistente, fondos insuficientes, cuenta propia, cuenta ajena, importe inválido, destinatarios y débito concurrente.
- `AccountControllerTest` valida HTTP 200 para transferencia válida, HTTP 400 para importe inválido y HTTP 410 para fondos insuficientes.
- `AccountApiSmokeTest` cubre cuenta, actividad, depósito, detalle y, cuando se configuran las variables requeridas, transferencia integrada, destinatarios, 401 sin token, 400 por cuenta propia, monto inválido, destino inexistente y 410 por saldo insuficiente.
- Para ejecutar la suite integrada se requieren `SMOKE_BASE_URL`, `SMOKE_EMAIL`, `SMOKE_PASSWORD`, `SMOKE_CARD_ID` y, para la transferencia real, `SMOKE_RECIPIENT_ALIAS`. `SMOKE_OTHER_ACCOUNT_ID` habilita el control 403 de cuenta ajena.
- El 21/10/2026, `mvn test` finalizó con **46 pruebas aprobadas**, sin fallos ni errores. `AccountApiSmokeTest` se omitió al no contar con las variables de un ambiente integrado.

```powershell
mvn test -pl account-service -am
```

## Estado de ejecución manual

La planilla `Digital-Money-House-Test-Suites.xlsx` es la fuente única para el seguimiento manual.

| Métrica | Cantidad |
|---|---:|
| Casos diseñados (Sprints 2 a 4) | 17 |
| Casos ejecutados | 17 |
| Casos aprobados | 17 |
| Casos pendientes | 0 |

El 21/10/2026 se ejecutaron en el entorno cloud los casos S4-07, S4-08 y S4-09 mediante la colección Postman: transferencia a cuenta propia (HTTP 400), importe inválido (HTTP 400) y solicitud sin token (HTTP 401). Los 17 casos manuales diseñados quedaron ejecutados y aprobados.

## Ejecución con Postman

Importar `docs/postman/Digital-Money-House.postman_collection.json`, completar `baseUrl`, `accessToken`, `accountId`, `accountAlias`, `recipientAlias` y `otherAccountId`, y ejecutar la carpeta **Transferencias**. Los seis escenarios de error verifican código HTTP y, cuando corresponde, el mensaje recibido.
