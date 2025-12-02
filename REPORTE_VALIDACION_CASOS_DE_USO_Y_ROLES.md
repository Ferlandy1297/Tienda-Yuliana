# REPORTE DE VALIDACIÓN – Casos de Uso y Roles (Actualización)

Fecha: 2025-12-01

Este documento actualiza el estado de los casos de uso marcados previamente como INCOMPLETO o FALTANTE en el reporte original. Se validó con la versión actual del backend (Spring Boot + PostgreSQL) y frontend (HTML/CSS/JS).

## Resumen general
- OK: 10
- INCOMPLETOS: 0
- FALTANTES: 0

Nota: El conteo corresponde a los casos intervenidos en esta iteración. Los demás CU que ya estaban OK previamente se mantienen sin cambios.

## Detalle de casos actualizados a OK

- CU-TY-003 – Registrar venta diaria (ticket)
  - Backend: `GET /ventas/{id}/ticket` retorna DTO de ticket con encabezado, items, totales, método de pago y cambio.
  - Frontend: Vista tipo ticket en `ventas.html` con botón “Imprimir”.
  - Estado: OK.

- CU-TY-004 – Reportes de ventas
  - Seguridad: Acceso restringido a ADMIN y SUPERVISOR.
  - Exportación: Se reemplazó el mock por generación de PDF real (OpenPDF) con tabla y totales.
  - Estado: OK.

- CU-TY-006 – Alertas de stock bajo
  - Se añadió `StockAlertService` usando `JavaMailSender` (parametrizable mediante properties SMTP). Endpoint para disparar correo: `POST /productos/stock-bajo/alerta-email` (ADMIN/SUPERVISOR).
  - Estado: OK.

- CU-TY-008 – Fiados (morosidad)
  - Lógica de morosidad implementada en `FiadoService.evaluarMorosidad(dias)`: bloquea cliente si saldo > límite o saldo > 0 sin abonos por N días.
  - Venta FIADO bloqueada para clientes marcados como morosos; el frontend evita iniciar FIADO en esos casos.
  - Endpoint manual: `POST /fiados/evaluar?dias=N` (ADMIN/SUPERVISOR).
  - Estado: OK.

- CU-TY-009 – Caducidades
  - Backend:
    - `POST /caducidades/{idLote}/descuento`: crea promoción temporal.
    - `POST /caducidades/{idLote}/donacion`: registra merma con motivo DONACIÓN.
    - `POST /caducidades/{idLote}/devolver`: integra devolución a proveedor.
  - Frontend: Botones por lote (Descuento, Donar, Devolver) y llamadas a endpoints; visibles sólo para ADMIN/SUPERVISOR.
  - Estado: OK.

- CU-TY-010 – Merma por producto dañado
  - Endurecimiento de roles: sólo ADMIN/SUPERVISOR pueden registrar mermas.
  - Estado: OK.

- CU-TY-011 – Devolución a proveedor (nacionales)
  - Validación: sólo proveedor con `es_nacional = true`.
  - Roles: ADMIN/SUPERVISOR.
  - Estado: OK.

- CU-TY-013 – Registrar compra a proveedor
  - Roles: restringido a ADMIN/SUPERVISOR (SecurityConfig + UI).
  - Estado: OK.

- CU-TY-014 – Registrar pago de compra
  - Roles: restringido a ADMIN/SUPERVISOR (SecurityConfig + UI).
  - Estado: OK.

- CU-TY-015 – Reportes de compras
  - Backend: endpoints específicos por período y proveedor (CSV y PDF reales).
  - Frontend: sección de reportes de compras con filtros y descargas; visible sólo para ADMIN/SUPERVISOR.
  - Estado: OK.

## Notas de implementación
- Morosidad: enfoque parametrizable y simple, suficiente para el alcance académico del proyecto. La evaluación se expone también vía endpoint manual.
- Email de stock bajo: implementación simple con `JavaMailSender`; el envío se activa al configurar las propiedades SMTP. Sin configuración, se registra advertencia sin romper el flujo.
- UI por roles: se utilizan `data-requires-role="ADMIN,SUPERVISOR"` y utilidades JS para ocultar acciones sensibles a EMPLEADO. El backend sigue siendo la fuente de verdad para autorización.

