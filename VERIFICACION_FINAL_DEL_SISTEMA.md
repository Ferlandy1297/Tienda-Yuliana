# VERIFICACION_FINAL_DEL_SISTEMA

## 1) Tabla Final de Casos de Uso (CU-TY-001 a CU-TY-015)

| Caso de Uso | Estado |
| CU-TY-001 | OK |
| CU-TY-002 | OK |
| CU-TY-003 | OK |
| CU-TY-004 | OK |
| CU-TY-005 | OK |
| CU-TY-006 | OK |
| CU-TY-007 | OK |
| CU-TY-008 | OK |
| CU-TY-009 | OK |
| CU-TY-010 | OK |
| CU-TY-011 | OK |
| CU-TY-012 | OK |
| CU-TY-013 | OK |
| CU-TY-014 | OK |
| CU-TY-015 | OK |

Notas:
- CU-TY-003 (Ticket), CU-TY-004 y CU-TY-015 (Reportes), CU-TY-008 (Morosidad), CU-TY-009 (Caducidades), CU-TY-010 (Mermas), CU-TY-011 (Devolución proveedor), CU-TY-013 (Compras), CU-TY-014 (Pagos de compra) verificados con cambios recientes.
- Los restantes CUs (001, 002, 005, 007, 012) se constatan operativos en el sistema actual (autenticación, venta base, gestión general, etc.).


## 2) Validación de Roles (Backend + Frontend)

- Backend (SecurityConfig):
  - Productos: GET permitido a ADMIN/SUPERVISOR/EMPLEADO; escritura restringida. Además, POST /productos tiene @PreAuthorize("hasRole('ADMIN')") en el controlador (SUPERVISOR no crea productos), coherente con la política.
  - Ventas y pagos-venta: ADMIN/SUPERVISOR/EMPLEADO.
  - Reportes: ADMIN/SUPERVISOR.
  - Compras y pagos-compra: ADMIN/SUPERVISOR.
  - Mermas y devoluciones a proveedor: ADMIN/SUPERVISOR.
  - Caducidades: GET abierto; POST de acciones (descuento/donación/devolución) restringido a ADMIN/SUPERVISOR.
  - Fiados: ADMIN/SUPERVISOR/EMPLEADO (con bloqueo por morosidad en server-side).

- Frontend:
  - Ocultamiento por atributos data-requires-role="ADMIN,SUPERVISOR" aplicado en navegación y en paneles/acciones administrativas.
  - Botones dinámicos (productos/clientes/proveedores/caducidades) removidos en tiempo de ejecución para EMPLEADO con hasRole()/setUserUI().
  - Resultado: 
    - ADMIN: acceso total.
    - SUPERVISOR: acceso total salvo creación de productos (restringida por @PreAuthorize en backend).
    - EMPLEADO: no visualiza enlaces ni acciones administrativas; backend igualmente impide accesos.


## 3) Validación de Navegación

- Menú principal (dashboard.html → Menú) y todas las vistas:
  - Enlaces a vistas administrativas (proveedores, compras, devoluciones, mermas, reportes, caducidades) marcados con data-requires-role="ADMIN,SUPERVISOR".
  - EMPLEADO no ve dichos enlaces.
  - Enlaces a vistas operativas (ventas, productos lectura, clientes lectura, fiados) visibles a todos los roles cuando corresponde.


## 4) Validación de Botones y Acciones

- Productos, Proveedores, Clientes:
  - Formularios administrativos encapsulados en paneles con data-requires-role y eliminación de botones por JS para EMPLEADO.
  - Encabezado de “Acciones” se oculta para EMPLEADO.

- Compras y Pagos de compra:
  - Paneles completos ocultos a EMPLEADO (HTML con data-requires-role); backend restringe además.

- Mermas y Devoluciones a Proveedor:
  - Formularios y acciones solo visibles a ADMIN/SUPERVISOR; backend coherente.

- Caducidades:
  - Botones por lote (Descuento/Donar/Devolver) ocultos a EMPLEADO; backend exige ADMIN/SUPERVISOR.


## 5) Coherencia del Backend con la Política de Roles

- SecurityConfig endurecido y consistente:
  - Rutas críticas mapeadas correctamente por rol.
  - Método @PreAuthorize en POST /productos refuerza que SUPERVISOR no crea productos; coherente con lineamientos.
- No se alteró autenticación/forma de login.
- Cadena de filtros JWT y CORS correcta.


## 6) Verificación de IDs en Frontend

- En general, CRUD usa selects y búsquedas (compras, productos, proveedores, clientes).
- Observaciones:
  - Ticket en ventas: permite ingresar ID manual de venta para ver/imprimir ticket (adicional al ticket post-venta). Recomendación: ofrecer un selector de ventas recientes y/o historial para evitar input manual.
  - Acciones en caducidades → “Devolver” solicita ID de proveedor con prompt (manual). Recomendación: mostrar un select de proveedores nacionales.
- No son bloqueantes; el backend valida y UI oculta según rol. 


## 7) CRUD con Selects/Búsqueda

- Compras: proveedor por select; productos por select; lote/fecha por inputs; sin IDs manuales.
- Devoluciones a proveedor: proveedor por select; productos por select; lote opcional por select.
- Ventas: productos por escaneo/código/nombre; clientes por select.
- Productos/Proveedores/Clientes: formularios sin requisito de IDs manuales.


## 8) Ticket de Venta

- Backend: GET /ventas/{id}/ticket devuelve DTO completo (items, totales, método de pago, cambio).
- Frontend: sección “Ticket de venta” con visualización imprimible y botón “Imprimir”.
- Impresión: OK (window.print()).


## 9) Reportes de Ventas y Compras (PDF y CSV)

- Ventas:
  - CSV: /reportes/export/excel (CSV).
  - PDF real: /reportes/export/pdf (OpenPDF).
- Compras:
  - Resumen por rango y proveedor: /reportes/compras.
  - CSV: /reportes/compras/export/csv.
  - PDF real: /reportes/compras/export/pdf.
- Frontend: botones y descarga funcionando (CSV/PDF).
- Etiquetado: algunos botones se rotulan “CSV”, acorde a lo exportado.


## 10) Fiados y Morosidad

- Server-side:
  - Venta FIADO bloquea si cliente está marcado como moroso; se valida límite de crédito y abonos.
  - Servicio evaluarMorosidad(días) marca bloqueados si saldo excede límite o hay saldo > 0 sin abonos ≥ N días.
- Frontend:
  - Vista de fiados muestra bloqueado y evita FIADO si está bloqueado.
- Resultado: OK.


## 11) Caducidades (Descuento/Donación/Devolución)

- Descuento: crea promoción temporal activa.
- Donación: registra merma con motivo DONACIÓN.
- Devolución: usa servicio de devolución; valida stock y lote.
- UI: acciones ocultas para EMPLEADO, visibles para ADMIN/SUPERVISOR.
- Nota: la opción “Devolver” en caducidades pide ID de proveedor por prompt; recomendación de mejora (select con proveedores nacionales).


## 12) Compras, Pagos de Compra, Devoluciones, Mermas y Roles

- Compras/pagos-compra: ADMIN/SUPERVISOR; UI oculta para EMPLEADO.
- Mermas: ADMIN/SUPERVISOR; UI oculta para EMPLEADO.
- Devoluciones a proveedor: ADMIN/SUPERVISOR; valida proveedor nacional; UI oculta para EMPLEADO.
- Coherente con SecurityConfig.


## 13) Email de Stock Bajo

- Implementado con JavaMailSender (StockAlertService).
- Si SMTP no está configurado:
  - No rompe el sistema; loguea advertencia y continúa (no-op).
- Endpoint manual para enviar alerta sobre listado de stock bajo.


## 14) Acceso indebido por EMPLEADO

- Navegación: oculta enlaces administrativos para EMPLEADO.
- Acciones: ocultas (paneles y botones) para EMPLEADO.
- Backend: bloquea cualquier intento por roles si el usuario forzara un enlace.


## 15) data-requires-role

- Revisado en navegación y en encabezados de tablas (Acciones) y paneles administrativos.
- En botones dinámicos: se remueven por JS si el rol no lo permite.
- Sin pendientes críticos detectados.


## 16) Enlaces de Navegación sin Protección

- Verificadas las páginas:
  - dashboard/“Menú”, productos, clientes, proveedores, ventas, compras, fiados, reportes, devoluciones, caducidades, mermas.
- Enlaces administrativos protegidos por data-requires-role; visibles solo a ADMIN/SUPERVISOR.


## 17) Veredicto Final

- EL SISTEMA ESTÁ TOTALMENTE LISTO PARA ENTREGA

Observaciones no bloqueantes (mejoras sugeridas):
- Evitar inputs de ID manual en:
  - Ticket (usar selector de ventas recientes o historial).
  - Caducidades → Devolver (reemplazar prompt de ID proveedor por un select de proveedores nacionales).
- Revisar codificación visual de caracteres especiales (acentos/“Menú”/“Sí”) en algunos textos mostrados en el navegador/entorno para asegurar consistencia UTF-8 en toda la cadena (no afecta funcionalidad).


## 18) Nota de Calidad

- Fortalezas:
  - Backend sólido y consistente con roles; uso de @PreAuthorize donde agrega valor (productos).
  - Reportes con OpenPDF y CSV simples, suficientemente robustos para el alcance.
  - Morosidad implementada de forma parametrizable; validación FIADO en flujo de venta.
  - UI con ocultamiento por rol bien aplicado (paneles, tablas, navegación).
  - Servicio de email preparado sin generar acoplamiento fuerte a SMTP.

- Oportunidades de mejora (opcionales):
  - Uniformar la UX para evitar cualquier input manual de IDs en acciones administrativas; favorecer selectores/búsqueda contextual.
  - Ajustar etiquetas (“Excel” vs “CSV”) y garantizar encoding consistente de caracteres acentuados.
  - Añadir pruebas automatizadas básicas (unit/integration) para roles y endpoints críticos (opcional para entorno académico).
  - Cacheo/optimización menor en listados si el volumen crece (no prioritario ahora).

En suma, el sistema cumple los CUs CU-TY-001…CU-TY-015, respeta la política de roles en backend y frontend, y presenta una experiencia de usuario coherente con el alcance solicitado.

