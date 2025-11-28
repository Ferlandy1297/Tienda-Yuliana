-- PostgreSQL schema for Tienda POS
create table if not exists usuarios (
    id bigserial primary key,
    username varchar(100) not null unique,
    password varchar(255) not null,
    nombre_completo varchar(200),
    rol varchar(20) not null
);

create table if not exists clientes (
    id bigserial primary key,
    nombre varchar(200) not null,
    telefono varchar(50),
    es_mayorista boolean default false,
    limite_credito numeric,
    saldo_credito numeric default 0,
    esta_bloqueado boolean default false
);

create table if not exists proveedores (
    id bigserial primary key,
    nombre varchar(200) not null,
    telefono varchar(50),
    direccion varchar(300),
    es_nacional boolean not null
);

create table if not exists productos (
    id bigserial primary key,
    nombre varchar(200) not null,
    codigo_barras varchar(60) not null unique,
    precio_unitario numeric not null,
    stock_actual integer not null,
    stock_minimo integer not null,
    id_proveedor bigint references proveedores(id),
    activo boolean not null default true
);
create index if not exists idx_productos_codigo_barras on productos(codigo_barras);

create table if not exists lotes_producto (
    id bigserial primary key,
    id_producto bigint not null references productos(id),
    fecha_vencimiento date,
    cantidad integer not null,
    costo_unitario numeric,
    estado varchar(20)
);

create table if not exists promociones (
    id bigserial primary key,
    id_producto bigint not null references productos(id),
    tipo varchar(20),
    valor numeric,
    fecha_inicio date,
    fecha_fin date,
    activa boolean
);

create table if not exists ventas (
    id bigserial primary key,
    fecha_hora timestamp,
    id_usuario bigint references usuarios(id),
    id_cliente bigint references clientes(id),
    tipo varchar(20),
    total numeric,
    estado varchar(20)
);
create index if not exists idx_ventas_fecha on ventas(fecha_hora);

create table if not exists ventas_detalle (
    id bigserial primary key,
    id_venta bigint not null references ventas(id) on delete cascade,
    id_producto bigint not null references productos(id),
    cantidad integer,
    precio_unitario numeric,
    subtotal numeric
);

create table if not exists pagos_venta (
    id bigserial primary key,
    id_venta bigint not null references ventas(id) on delete cascade,
    metodo varchar(20),
    monto_pagado numeric,
    denominacion numeric,
    cambio numeric
);

create table if not exists cuentas_corrientes_clientes (
    id bigserial primary key,
    id_cliente bigint not null references clientes(id),
    saldo_actual numeric
);

create table if not exists movimientos_cuenta_corriente (
    id bigserial primary key,
    id_cuenta bigint not null references cuentas_corrientes_clientes(id) on delete cascade,
    tipo varchar(10),
    monto numeric,
    fecha timestamp,
    descripcion varchar(300)
);

create table if not exists compras (
    id bigserial primary key,
    id_proveedor bigint not null references proveedores(id),
    fecha_hora timestamp,
    total numeric,
    estado varchar(20)
);
create index if not exists idx_compras_fecha on compras(fecha_hora);

create table if not exists compras_detalle (
    id bigserial primary key,
    id_compra bigint not null references compras(id) on delete cascade,
    id_producto bigint not null references productos(id),
    cantidad integer,
    costo_unitario numeric,
    fecha_vencimiento date,
    subtotal numeric
);

create table if not exists pagos_compra (
    id bigserial primary key,
    id_compra bigint not null references compras(id) on delete cascade,
    metodo varchar(20),
    monto_pagado numeric,
    fecha timestamp
);

create table if not exists mermas (
    id bigserial primary key,
    id_producto bigint not null references productos(id),
    id_lote bigint references lotes_producto(id),
    cantidad integer,
    motivo varchar(50),
    fecha timestamp
);

create table if not exists devoluciones_proveedor (
    id bigserial primary key,
    id_proveedor bigint not null references proveedores(id),
    fecha timestamp,
    motivo varchar(300)
);

create table if not exists devoluciones_detalle (
    id bigserial primary key,
    id_devolucion bigint not null references devoluciones_proveedor(id) on delete cascade,
    id_producto bigint not null references productos(id),
    cantidad integer,
    id_lote bigint references lotes_producto(id)
);
