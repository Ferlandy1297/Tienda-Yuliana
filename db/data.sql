-- Sample data
insert into usuarios (username, password, nombre_completo, rol) values
 ('admin', '{noop}admin', 'Administrador', 'ADMIN'),
 ('empleado', '{noop}empleado', 'Empleado Caja', 'EMPLEADO'),
 ('supervisor', '{noop}supervisor', 'Supervisor', 'SUPERVISOR');

insert into proveedores (nombre, telefono, direccion, es_nacional) values
 ('Proveedor Nacional SA', '555-111', 'Calle 1', true),
 ('Proveedor Importado LLC', '555-222', 'Avenida 2', false);

insert into clientes (nombre, telefono, es_mayorista, limite_credito, saldo_credito, esta_bloqueado) values
 ('Cliente Mostrador', '555-000', false, null, 0, false),
 ('Cliente Mayorista', '555-123', true, 5000, 0, false);

insert into productos (nombre, codigo_barras, precio_unitario, stock_actual, stock_minimo, id_proveedor, activo) values
 ('Arroz 1kg', '750000000001', 25.50, 100, 20, 1, true),
 ('Frijol 1kg', '750000000002', 28.00, 80, 15, 1, true),
 ('Aceite 900ml', '750000000003', 45.00, 60, 10, 2, true);

insert into lotes_producto (id_producto, fecha_vencimiento, cantidad, costo_unitario, estado) values
 (1, current_date + interval '180 days', 50, 18.00, 'NORMAL'),
 (1, current_date + interval '365 days', 50, 18.50, 'NORMAL'),
 (2, current_date + interval '120 days', 80, 22.00, 'NORMAL'),
 (3, current_date + interval '200 days', 60, 35.00, 'NORMAL');

insert into promociones (id_producto, tipo, valor, fecha_inicio, fecha_fin, activa) values
 (1, 'PORCENTAJE', 10, current_date - interval '1 day', current_date + interval '10 days', true),
 (3, 'MONTO_FIJO', 5, current_date - interval '1 day', current_date + interval '5 days', true);
