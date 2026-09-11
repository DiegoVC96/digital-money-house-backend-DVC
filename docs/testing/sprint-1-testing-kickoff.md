# Testing kickoff - Sprint 1

## Cómo escribir un caso de prueba

Todo caso de prueba debe contener:

- Identificador único.
- Funcionalidad o módulo evaluado.
- Objetivo del caso.
- Precondiciones.
- Datos de prueba.
- Pasos de ejecución claros y numerados.
- Resultado esperado.
- Resultado obtenido.
- Estado: Pendiente, Ejecutado OK, Falló o Bloqueado.
- Evidencia: respuesta HTTP, captura, log o consulta de base de datos.
- Clasificación: Smoke, Regression o ambas.

Ejemplo:

| Campo | Contenido |
|---|---|
| ID | CP-REG-01 |
| Funcionalidad | Registro |
| Precondición | Servicios y bases de datos disponibles |
| Datos | Datos válidos de un usuario no registrado |
| Pasos | Enviar `POST /api/auth/register` |
| Resultado esperado | HTTP 201, usuario, CVU, alias y tokens |
| Clasificación | Smoke y Regression |

## Cómo reportar un defecto

Un defecto debe incluir:

- Identificador único, por ejemplo `BUG-S1-001`.
- Título breve y descriptivo.
- Módulo afectado.
- Ambiente donde ocurrió.
- Precondiciones.
- Pasos para reproducirlo.
- Resultado esperado.
- Resultado obtenido.
- Severidad: crítica, alta, media o baja.
- Prioridad: alta, media o baja.
- Evidencia técnica.
- Estado: abierto, en progreso, resuelto, validado o cerrado.

Ejemplo:

| Campo | Contenido |
|---|---|
| ID | BUG-S1-001 |
| Título | Registro permite un DNI ya existente |
| Módulo | Auth Service / Users Service |
| Severidad | Alta |
| Prioridad | Alta |
| Estado | Abierto |
| Evidencia | Respuesta HTTP y registros de base de datos |

## Criterios para suite Smoke

Un caso se incluye en Smoke cuando:

- Verifica una funcionalidad crítica para usar el producto.
- Tiene ejecución rápida.
- No depende de escenarios complejos.
- Permite saber si una compilación es apta para continuar probando.
- Su falla bloquea pruebas posteriores.

Para Sprint 1, Smoke incluye:

- Registro exitoso.
- Login exitoso.
- Consulta protegida con token válido.
- Logout exitoso.
- Disponibilidad de Auth Service, Users Service y Account Service.

## Criterios para suite Regression

Un caso se incluye en Regression cuando:

- Verifica una funcionalidad existente que podría afectarse por un cambio.
- Incluye escenarios exitosos y de error.
- Valida reglas de negocio, seguridad e integraciones.
- Puede requerir más tiempo o datos preparados.
- Debe ejecutarse antes de liberar una versión.

Para Sprint 1, Regression incluye:

- Registro exitoso.
- Email duplicado.
- DNI duplicado.
- Datos de registro inválidos.
- Login exitoso.
- Usuario inexistente.
- Contraseña incorrecta.
- Acceso sin token.
- Acceso con token propio.
- Validación de CVU.
- Validación de alias.
- Logout.
- Invalidación de refresh token.