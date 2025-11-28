Postman – Tienda Yuliana

Ambiente y variables
- Importa `postman/TiendaYuliana.postman_environment.json` y selecciona el ambiente.
- `{{baseUrl}}`: URL del backend (por defecto `http://localhost:8080`).
- `{{token}}`: se autocompleta al ejecutar `Auth > Login` en cada colección.

Colecciones disponibles
- General: `postman/TiendaYuliana.postman_collection.json` (útil como referencia completa).
- Por rol:
  - `postman/TiendaYuliana.ADMIN.postman_collection.json`
  - `postman/TiendaYuliana.SUPERVISOR.postman_collection.json`
  - `postman/TiendaYuliana.EMPLEADO.postman_collection.json`

Credenciales de prueba
- ADMIN: `admin / admin`
- SUPERVISOR: `supervisor / supervisor`
- EMPLEADO: `empleado / empleado`

Notas de permisos (según SecurityConfig)
- Público: `GET /dashboard/summary`, `POST /auth/login`.
- Productos: `GET /productos/stock-bajo` (ADMIN/EMPLEADO/SUPERVISOR).
  - Resto de `/productos/**`: ADMIN y SUPERVISOR. Crear producto es solo ADMIN (método con `@PreAuthorize`).
- Clientes y Proveedores: `GET` para todos los roles; `POST/PUT/DELETE` para ADMIN y SUPERVISOR.
- Resto: `/ventas/**`, `/pagos-venta/**`, `/reportes/**`, `/compras/**`, `/pagos-compra/**`, `/mermas/**`, `/caducidades/**`, `/devoluciones-proveedor/**`, `/fiados/**` accesibles para los tres roles.

Flujos típicos
1) Login y token
   - Ejecuta `Auth > Login` con el usuario del rol. Se guardará `{{token}}` automáticamente.

2) Alta de producto (ADMIN)
   - `POST /productos` con cuerpo de ejemplo.
   - `PUT /productos/{id}` para editar.
   - `DELETE /productos/{id}` para activar/desactivar.

3) Compra a proveedor
   - `POST /compras` con `idProveedor` y `items` (cada item con `idProducto`, `cantidad`, `costoUnitario`, opcional `fechaVencimiento`).
   - (Opcional) `POST /pagos-compra` para registrar abono/pago.

4) Venta en mostrador
   - `POST /ventas` con `tipo` (MOSTRADOR|MAYOREO|FIADO), `items` y `pago`.
   - (Opcional) `POST /pagos-venta` para cobrar en partes.
   - Ejemplos incluidos como requests separados: “Registrar Venta (MAYOREO)” y “Registrar Venta (FIADO)”.

5) Fiados
   - Registrar venta con `tipo = FIADO` (ajusta el body de la venta).
   - `POST /fiados/abono` para abonar saldo de cliente.

6) Mermas y caducidades
   - `POST /mermas` para descontar stock por merma.
   - `GET /caducidades/por-vencer?dias=30` para lotes próximos a vencer.

7) Devolución a proveedor
   - `POST /devoluciones-proveedor` con `idProveedor`, `motivo` e `items` (puedes incluir `idLote`).

8) Reportes
   - `GET /reportes/ventas|mas-vendidos|utilidades` con `tipo` (diario|quincenal|mensual) y `fecha` (YYYY-MM-DD).
   - Exportaciones: `GET /reportes/export/excel` y `/reportes/export/pdf`.

Sugerencias
- Si recibes 401/403, revisa que el rol concuerde con el endpoint y que `{{token}}` esté vigente en el ambiente.
- Cambia IDs de ejemplo (`1`, `2`, `3`) según tus datos reales.

Pruebas automáticas en Postman
- Las colecciones por rol incluyen tests que validan:
  - Código de estado (200/201; 200/204 en DELETE; 403 en creación de producto para SUPERVISOR).
  - Estructura básica del JSON en respuestas clave (por ejemplo, `idVenta` y `total` en ventas; campos de `dashboard/summary`; arrays en listados; campos en reportes).
- Consulta la pestaña Tests/Console de Postman para ver resultados de aserciones.
