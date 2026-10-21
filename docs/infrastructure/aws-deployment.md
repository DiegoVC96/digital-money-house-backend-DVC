# Despliegue en AWS con Docker Compose

## Alcance

`docker-compose.cloud.yml` permite desplegar la aplicación completa en una única instancia EC2. Construye las imágenes de los microservicios Java desde el `Dockerfile` raíz y la del FrontEnd desde `FrontEnd/Dockerfile`.

El FrontEnd expone el puerto 80 y Nginx reenvía `/api` al API Gateway. Los demás componentes quedan únicamente en la red privada de Docker.

Keycloak se ejecuta en modo de producción (`start --optimized`) y utiliza una base MySQL dedicada. El realm se importa únicamente durante la inicialización; usuarios, credenciales y sesiones persistentes se conservan en el volumen de esa base aunque se recree el contenedor de Keycloak.

## Preparación de la instancia

1. Crear una instancia EC2 Linux y permitir tráfico entrante TCP 80 desde los orígenes necesarios.
2. Instalar Docker Engine y el complemento Docker Compose.
3. Clonar el repositorio y entrar al directorio `BackEnd`.
4. Copiar `.env.example` como `.env` y establecer valores privados para todas las contraseñas y secretos.

```bash
cp .env.example .env
# editar .env con valores seguros y propios
```

El secreto `KEYCLOAK_AUTH_SERVICE_SECRET` debe tener 64 caracteres aleatorios. Nunca se versiona el archivo `.env`.

En AWS conservar `FRONTEND_PORT=80`. Si el despliegue se prueba en una PC que ya utiliza ese puerto, por ejemplo con XAMPP, se puede usar temporalmente `FRONTEND_PORT=8088`.

## Construcción y arranque

```bash
docker compose -f docker-compose.cloud.yml --env-file .env up --build -d
docker compose -f docker-compose.cloud.yml --env-file .env ps
```

La primera construcción descarga dependencias y puede tardar algunos minutos. El estado esperado es `healthy` para bases, Keycloak, Eureka, microservicios y Gateway; `keycloak-bootstrap` debe terminar con código cero.

Para consultar un servicio puntual:

```bash
docker compose -f docker-compose.cloud.yml --env-file .env logs -f api-gateway
```

## Operación y seguridad

- Se publica solamente el puerto 80 del FrontEnd.
- MySQL, MailHog, Keycloak, Eureka y las APIs no se exponen públicamente.
- El Compose usa volúmenes Docker para persistir las cuatro bases MySQL, incluida la de Keycloak.
- Para detener el ambiente sin borrar datos: `docker compose -f docker-compose.cloud.yml --env-file .env down`.
- Para borrar también los datos persistidos: agregar `-v` (acción destructiva).

## Siguiente paso para producción

Este diseño corresponde a una demostración en una única instancia. Para una operación productiva, usar Amazon RDS para las bases de datos, AWS Secrets Manager o Parameter Store para secretos, HTTPS mediante ALB/ACM y Amazon ECR para distribuir imágenes ya construidas.
