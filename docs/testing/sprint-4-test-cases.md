# Casos de prueba — Sprint 4

| ID | Suite | Escenario | Resultado esperado | Estado manual |
|---|---|---|---|---|
| S4-01 | Smoke | Transferir a un alias existente con saldo suficiente. | HTTP 200, movimiento `TRANSFER_OUT`, débito y crédito aplicados. | Aprobado |
| S4-02 | Regression | Transferir a un CVU existente de 22 dígitos. | HTTP 200 y transferencia aplicada. | Aprobado |
| S4-03 | Regression | Consultar destinatarios como propietario. | HTTP 200, hasta cinco destinatarios únicos. | Aprobado |
| S4-04 | Regression | Consultar destinatarios de una cuenta ajena. | HTTP 403. | Aprobado |
| S4-05 | Regression | Transferir a alias o CVU inexistente. | HTTP 400 y mensaje de destino inexistente. | Aprobado |
| S4-06 | Regression | Transferir un importe superior al saldo. | HTTP 410 y saldos sin cambios. | Aprobado |
| S4-07 | Regression | Transferir a la propia cuenta. | HTTP 400 y saldo sin cambios. | Aprobado — Postman cloud 21/10/2026 |
| S4-08 | Regression | Transferir importe cero o negativo. | HTTP 400 y sin movimientos. | Aprobado — Postman cloud 21/10/2026 |
| S4-09 | Smoke | Enviar una transferencia sin token. | HTTP 401. | Aprobado — Postman cloud 21/10/2026 |
| S4-10 | Regression | Intentar una transferencia desde una cuenta ajena. | HTTP 403. | Cubierto por prueba automática |
| S4-11 | Regression | Ejecutar dos débitos concurrentes por el saldo total. | Solo un débito es exitoso y el saldo no queda negativo. | Cubierto por prueba automática |
| S4-12 | Regression | Validar el controlador con transferencia válida, importe inválido y fondos insuficientes. | HTTP 200, 400 y 410 respectivamente. | Cubierto por prueba automática |
| S4-13 | Smoke | Transferir a un destinatario configurado y consultar recientes. | HTTP 200 y destinatario presente en la respuesta. | Cubierto por smoke test configurable |
| S4-14 | Regression | Ver detalle de transferencia desde el FrontEnd. | Monto y descripción correctos. | Aprobado |
| S4-15 | Regression | Descargar comprobante de una operación. | PDF con los mismos datos del detalle. | Aprobado |
| S4-16 | Regression | Seleccionar un destinatario reciente en Enviar dinero. | Se completan los datos del destino seleccionado. | Aprobado |
