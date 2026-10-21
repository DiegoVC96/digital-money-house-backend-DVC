# QA Sign Off — Sprint 4

## Alcance validado

Transferencias por alias o CVU, actualización de saldos, movimientos de origen y destino, destinatarios recientes, autorización y errores de negocio.

## Métricas

| Métrica | Cantidad | Evidencia |
|---|---:|---|
| Casos manuales diseñados | 17 | `Digital-Money-House-Test-Suites.xlsx` |
| Casos manuales ejecutados | 14 | `Digital-Money-House-Test-Suites.xlsx` |
| Casos manuales aprobados | 14 | Planilla de suites y evidencia exploratoria |
| Casos manuales fallidos | 0 | Planilla de suites |
| Casos manuales pendientes | 3 | S4-07, S4-08 y S4-09 en la colección Postman |
| Defectos reportados | 1 | DEF-S4-01 |
| Defectos resueltos | 1 | Corrección de navegación al detalle |
| Defectos abiertos | 0 | Testing exploratorio |
| Pruebas automatizadas ejecutadas | 46 | `mvn test` el 21/10/2026 |
| Pruebas automatizadas aprobadas | 46 | Common 2, Auth 5, Users 4 y Account 35 |
| Smoke test de API integrado | 0 ejecutadas | Requiere variables `SMOKE_*`; se omitió de forma controlada |

## Resultado

**Pendiente de revalidación manual.** Las pruebas ya ejecutadas no dejan defectos abiertos. Antes de cerrar la entrega deben ejecutarse y registrar los tres casos Postman pendientes para que el total de casos diseñados y aprobados coincida.
