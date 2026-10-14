# QA Sign Off — Sprint 4

## Alcance validado

Transferencias por alias o CVU, actualización de saldos, movimientos de origen y destino, destinatarios recientes, autorización y errores de negocio.

## Métricas

| Métrica | Cantidad | Evidencia |
|---|---:|---|
| Casos manuales ejecutados | 14 | `Digital-Money-House-Test-Suites.xlsx` |
| Casos manuales aprobados | 14 | Planilla de suites y evidencia exploratoria |
| Casos manuales fallidos | 0 | Planilla de suites |
| Defectos reportados | 1 | DEF-S4-01 |
| Defectos resueltos | 1 | Corrección de navegación al detalle |
| Defectos abiertos | 0 | Testing exploratorio |
| Casos automatizados ejecutados | 28 | `mvn test -pl account-service -am` |
| Casos automatizados aprobados | 28 | `AccountServiceTest` y `CardServiceTest` |

## Resultado

**Aprobado para entrega.** Las pruebas manuales, exploratorias y automatizadas no dejan defectos abiertos dentro del alcance del Sprint 4.
