# Testing exploratorio — Sprint 4

## Organización

Se realizaron sesiones breves orientadas por charters, combinando recorridos de datos, flujos de usuario y escenarios de error. El objetivo fue complementar los casos definidos de Smoke y Regression con comportamientos que dependen de la integración completa.

## Sesiones y charters

| Sesión | Charter | Recorrido | Resultado |
|---|---|---|---|
| ET-01 | Transferir dinero entre dos cuentas creadas para prueba. | Login, alta de tarjeta, ingreso de dinero, transferencia por alias y consulta de saldos. | Aprobado. Se transfirieron `$400`; origen `$1.100`, destino `$400`. |
| ET-02 | Explorar el historial de destinatarios. | Transferir, volver a Enviar dinero, consultar últimas cuentas y seleccionar una. | Aprobado. Se mostró `rio.rio.mar` como destinatario reciente. |
| ET-03 | Buscar límites de saldo y de destino. | Alias inexistente, importe superior al saldo y acceso al historial de cuenta ajena. | Aprobado. Respuestas `400`, `410` y `403`. |
| ET-04 | Recorrer la experiencia FrontEnd después de transferir. | Nueva cuenta, importe, confirmación, detalle y continuar. | Aprobado tras corrección. El detalle muestra monto y descripción. |

## Tours aplicados

- **Tour de flujo principal:** se siguió el camino completo desde la autenticación hasta el detalle del movimiento.
- **Tour de errores:** se ingresaron identificadores inexistentes y montos que superan el saldo disponible.
- **Tour de navegación:** se verificaron regreso a Enviar dinero, historial de destinatarios y navegación al detalle de actividad.
- **Tour de datos:** se contrastaron movimiento saliente, movimiento entrante y saldos de las dos cuentas involucradas.

## Hallazgo y resolución

| ID | Hallazgo | Severidad | Resolución | Estado |
|---|---|---|---|---|
| DEF-S4-01 | El FrontEnd redirigía al detalle con el parámetro `step` en vez de `id`, por lo que el detalle no mostraba los datos del movimiento. | Media | Se corrigió la navegación para enviar `id` y se recompiló el FrontEnd. | Resuelto |

## Conclusión

Las sesiones no identificaron defectos abiertos. La transferencia, los destinatarios recientes, los errores de negocio y el detalle de actividad operan de acuerdo con el alcance del Sprint 4.
