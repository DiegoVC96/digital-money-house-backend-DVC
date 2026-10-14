# Casos de prueba — Sprint 4

| ID | Escenario | Resultado esperado |
|---|---|---|
| S4-01 | Transferir a un alias existente con saldo suficiente. | HTTP 200, movimiento `TRANSFER_OUT`, débito en origen y crédito en destino. |
| S4-02 | Transferir a un CVU existente de 22 dígitos. | HTTP 200 y transferencia aplicada. |
| S4-03 | Consultar `GET /api/accounts/{accountId}/transferences` como propietario. | HTTP 200 y destinatarios recientes sin repeticiones. |
| S4-04 | Consultar destinatarios de una cuenta ajena. | HTTP 403. |
| S4-05 | Transferir a alias o CVU inexistente. | HTTP 400 y mensaje de cuenta de destino inexistente. |
| S4-06 | Transferir un importe superior al saldo. | HTTP 410 y saldos sin cambios. |
| S4-07 | Intentar transferir a la propia cuenta. | HTTP 400. |
| S4-08 | FrontEnd: enviar dinero y abrir detalle. | Saldo actualizado y detalle con importe y descripción. |
| S4-09 | FrontEnd: seleccionar una cuenta dentro de los últimos destinatarios. | Se muestran hasta cinco destinatarios únicos y se completa el destino de la transferencia. |
| S4-10 | FrontEnd: descargar el comprobante desde el detalle de una operación. | Se descarga un PDF con importe, operación, descripción, fecha y número de operación iguales al detalle mostrado. |
