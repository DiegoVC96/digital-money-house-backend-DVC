# Testing exploratorio — Sprint 3

## Objetivo

Explorar los recorridos de actividad e ingreso de dinero, priorizando consistencia de saldo, seguridad por propietario y claridad frente a filtros o errores.

## Organización

Las sesiones se organizan en recorridos de aproximadamente 30 minutos. Cada recorrido parte de una cuenta autenticada, con al menos una tarjeta asociada y saldo inicial conocido.

## Sesiones exploratorias

| Sesión | Tipo de tour | Recorrido | Resultado esperado |
|---|---|---|---|
| ET-01 | Workflow tour | Login -> Cargar dinero -> seleccionar tarjeta -> confirmar -> Inicio -> Actividad -> detalle. | Saldo y movimiento son consistentes en todas las pantallas. |
| ET-02 | Data tour | Comparar importe y fecha de respuesta API con Dashboard y detalle visual. | El movimiento no expone número completo ni CVC de la tarjeta. |
| ET-03 | Filter tour | Alternar ingresos/egresos, todos los rangos, período de un día, período vacío y Limpiar. | La lista cambia sin conservar filtros anteriores ni datos obsoletos. |
| ET-04 | Error tour | Probar importe cero, tarjeta inexistente, movimiento inexistente y período invertido. | Respuestas 400 o 404 claras, sin saldo modificado ante error. |
| ET-05 | Security tour | Consultar actividad, detalle e ingreso sobre cuenta ajena o sin token. | Acceso denegado con 401/403 según corresponda. |
| ET-06 | Integration tour | Reiniciar servicios en orden infraestructura -> Eureka -> Gateway -> servicios y repetir login. | Los servicios se registran y Gateway vuelve a enrutar las solicitudes. |

## Hallazgos y decisiones

- La actividad se obtiene desde Account Service para mantener saldo y movimientos en el mismo límite transaccional.
- El ingreso verifica que la tarjeta pertenezca a la cuenta antes de acreditar saldo.
- La interfaz transmite el identificador de tarjeta; los últimos cuatro dígitos se usan sólo como información visual.
- Los filtros se aplican en backend y el frontend restaura el listado completo con el botón Limpiar.
