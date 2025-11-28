# Tienda POS - Full Stack (PostgreSQL + Spring Boot + Flutter Web)

Estructura del proyecto:

- `db/` scripts SQL (`schema.sql`, `data.sql`).
- `backend/` API REST (Spring Boot 3, JWT, JPA).
- `flutter_app/` Frontend Flutter Web (reemplaza al antiguo HTML/JS).
- `frontend/` (solo documentación): carpeta informativa; los artefactos a servir se generan con `flutter build web` en `flutter_app/build/web`.

## Requisitos

- Java 17+
- Maven 3.9+
- PostgreSQL 14+

## Base de datos (PostgreSQL)

1. Crear la base `tienda` en PostgreSQL.
2. Ejecutar `db/schema.sql` y luego `db/data.sql` (por ejemplo con DBeaver).

Credenciales por defecto (data.sql):

- admin / admin (ROL ADMIN)
- empleado / empleado (ROL EMPLEADO)
- supervisor / supervisor (ROL SUPERVISOR)

## Backend (Spring Boot)

Config por defecto (`backend/src/main/resources/application.properties`):

```
spring.datasource.url=jdbc:postgresql://localhost:5432/tienda
spring.datasource.username=postgres
spring.datasource.password=postgres
```

Arrancar API:

```
cd backend
mvn spring-boot:run
```

La API corre en `http://localhost:8080`.

Endpoints clave:

- `POST /auth/login` (username/password) → token JWT
- `GET /productos`, `POST /productos`, `PUT /productos/{id}`, `DELETE /productos/{id}`, `GET /productos/stock-bajo`
- `POST /ventas`, `GET /ventas` (rango opcional `inicio`, `fin`)
- `GET /reportes/ventas?tipo=diario|quincenal|mensual&fecha=YYYY-MM-DD`
- `GET /reportes/mas-vendidos?tipo=diario|quincenal|mensual&fecha=YYYY-MM-DD`
- `GET /reportes/utilidades?tipo=diario|quincenal|mensual&fecha=YYYY-MM-DD` (utilidad estimada por promedio de costo)

Roles:

- ADMIN: total acceso
- EMPLEADO y SUPERVISOR: pueden ventas, reportes y ver stock-bajo

## Frontend (Flutter Web)

El frontend se ha migrado completamente a Flutter Web y vive en `flutter_app/`.

Dos modos de uso:

- Desarrollo (sin cambiar CORS):
  - Requisitos: Flutter SDK.
  - Comando: `cd flutter_app && flutter run -d chrome --web-port 5500`
  - Abre `http://localhost:5500` (el backend ya permite este origen por defecto).

- Producción con Docker (Nginx):
  - Construir: `cd flutter_app && flutter build web`
  - Levantar: `docker compose up -d --build`
  - Frontend: `http://localhost:5500` (servido por Nginx desde `flutter_app/build/web`).

Si prefieres otro puerto en desarrollo, usa `--web-port <puerto>` y ajusta `CORS_ALLOWED_ORIGINS` en el backend (o añade ese origen a tu `.env`).

## Notas

- Lógica crítica implementada: autenticación (JWT), registro de productos (unicidad de código), ventas con validación y descuento de stock, promociones simples, mayoreo (regla simple), pagos con cálculo de cambio, alertas de stock bajo y reportes (diario/quincenal/mensual).
- Quedan listas para extender: pagos adicionales de venta, fiados avanzados, compras y devoluciones con más detalle, exportaciones PDF/Excel.

## Docker (opcional)

Levantar PostgreSQL y Backend con Docker Compose:

```
docker compose up -d --build
```

- Backend: `http://localhost:8080`
- Frontend: `http://localhost:5500` (requiere haber ejecutado `flutter build web` antes del compose)
- PostgreSQL: `localhost:5432` (DB: tienda, user/pass por defecto postgres/postgres)

Variables (puedes exportarlas o crear un `.env`):

- `POSTGRES_USER`, `POSTGRES_PASSWORD`
- `JWT_SECRET` (cambiar en producción)
- `CORS_ALLOWED_ORIGINS` (por defecto incluye `http://localhost:5500,http://127.0.0.1:5500`)

Los scripts `db/schema.sql` y `db/data.sql` se aplican automáticamente al crear el contenedor de PostgreSQL por primera vez.

Nota: El container `frontend` sirve estáticos desde `./flutter_app/build/web`. Ejecuta `flutter build web` cuando cambies la app para refrescar los artefactos.
