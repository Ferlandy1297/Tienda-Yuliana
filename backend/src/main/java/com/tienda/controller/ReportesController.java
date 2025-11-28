package com.tienda.controller;

import com.tienda.entity.Compra;
import com.tienda.entity.CompraDetalle;
import com.tienda.entity.Venta;
import com.tienda.entity.VentaDetalle;
import com.tienda.repository.CompraRepository;
import com.tienda.repository.VentaRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reportes")
public class ReportesController {
    private final VentaRepository ventaRepository;
    private final CompraRepository compraRepository;
    public ReportesController(VentaRepository ventaRepository, CompraRepository compraRepository) {
        this.ventaRepository = ventaRepository;
        this.compraRepository = compraRepository;
    }

    @GetMapping("/ventas")
    public Map<String, Object> reporteVentas(@RequestParam String tipo,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        LocalDate inicio; LocalDate fin; var range = rango(tipo, fecha); inicio = range[0]; fin = range[1];
        List<Venta> ventas = ventaRepository.findByFechaHoraBetween(inicio.atStartOfDay(), fin.atTime(LocalTime.MAX));
        BigDecimal total = ventas.stream().map(Venta::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String,Object> res = new HashMap<>();
        res.put("tipo", tipo);
        res.put("inicio", inicio);
        res.put("fin", fin);
        res.put("total", total);
        res.put("transacciones", ventas.size());
        return res;
    }

    @GetMapping("/export/excel")
    public org.springframework.http.ResponseEntity<byte[]> exportCsv(@RequestParam String tipo,
                                                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        LocalDate[] range = rango(tipo, fecha);
        List<Venta> ventas = ventaRepository.findByFechaHoraBetween(range[0].atStartOfDay(), range[1].atTime(LocalTime.MAX));
        StringBuilder csv = new StringBuilder("id,fecha,total\n");
        ventas.forEach(v -> csv.append(v.getId()).append(',').append(v.getFechaHora()).append(',').append(v.getTotal()).append('\n'));
        byte[] bytes = csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return org.springframework.http.ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=ventas.csv")
                .header("Content-Type", "text/csv; charset=utf-8")
                .body(bytes);
    }

    @GetMapping("/export/pdf")
    public org.springframework.http.ResponseEntity<byte[]> exportPdfMock(@RequestParam String tipo,
                                                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        LocalDate[] range = rango(tipo, fecha);
        String txt = "Reporte de ventas (mock PDF)\\n" + "Tipo: " + tipo + "\\n" + "Desde: " + range[0] + " Hasta: " + range[1] + "\\n";
        byte[] bytes = txt.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return org.springframework.http.ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=reporte.pdf")
                .header("Content-Type", "application/octet-stream")
                .body(bytes);
    }

    @GetMapping("/mas-vendidos")
    public java.util.List<java.util.Map<String, Object>> masVendidos(@RequestParam String tipo,
                                                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        LocalDate[] range = rango(tipo, fecha);
        List<Venta> ventas = ventaRepository.findByFechaHoraBetween(range[0].atStartOfDay(), range[1].atTime(LocalTime.MAX));
        java.util.Map<Long, java.util.Map<String, Object>> acc = new java.util.HashMap<>();
        for (Venta v : ventas) {
            for (VentaDetalle d : v.getDetalles()) {
                Long pid = d.getProducto().getId();
                java.util.Map<String, Object> m = acc.computeIfAbsent(pid, k -> {
                    java.util.Map<String,Object> x = new java.util.HashMap<>();
                    x.put("idProducto", pid);
                    x.put("nombre", d.getProducto().getNombre());
                    x.put("cantidad", 0L);
                    x.put("total", java.math.BigDecimal.ZERO);
                    return x;
                });
                long cantidad = ((Number) m.get("cantidad")).longValue() + (d.getCantidad() == null ? 0 : d.getCantidad());
                m.put("cantidad", cantidad);
                java.math.BigDecimal total = (java.math.BigDecimal) m.get("total");
                java.math.BigDecimal sub = d.getSubtotal() != null ? d.getSubtotal() : java.math.BigDecimal.ZERO;
                m.put("total", total.add(sub));
            }
        }
        java.util.List<java.util.Map<String, Object>> list = new java.util.ArrayList<>(acc.values());
        list.sort((a,b) -> Long.compare(((Number)b.get("cantidad")).longValue(), ((Number)a.get("cantidad")).longValue()));
        if(list.size() > 10) list = list.subList(0,10);
        return list;
    }

    @GetMapping("/utilidades")
    public java.util.Map<String, Object> utilidades(@RequestParam String tipo,
                                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        LocalDate[] range = rango(tipo, fecha);
        List<Venta> ventas = ventaRepository.findByFechaHoraBetween(range[0].atStartOfDay(), range[1].atTime(LocalTime.MAX));
        java.math.BigDecimal totalVentas = ventas.stream()
                .map(Venta::getTotal)
                .filter(java.util.Objects::nonNull)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        // Costos estimados: promedio de costo de compra del período; si no hay, último promedio histórico
        java.util.Map<Long, java.math.BigDecimal> costoPeriodo = new java.util.HashMap<>();
        List<Compra> comprasPeriodo = compraRepository.findByFechaHoraBetween(range[0].atStartOfDay(), range[1].atTime(LocalTime.MAX));
        java.util.Map<Long, java.util.List<java.math.BigDecimal>> tmp = new java.util.HashMap<>();
        for (Compra c : comprasPeriodo) {
            for (CompraDetalle d : c.getDetalles()) {
                if (d.getProducto() == null || d.getCostoUnitario() == null) continue;
                tmp.computeIfAbsent(d.getProducto().getId(), k -> new java.util.ArrayList<>()).add(d.getCostoUnitario());
            }
        }
        tmp.forEach((pid, costs) -> {
            java.math.BigDecimal sum = java.math.BigDecimal.ZERO;
            for (java.math.BigDecimal cc : costs) sum = sum.add(cc);
            java.math.BigDecimal avg = costs.isEmpty()? java.math.BigDecimal.ZERO : sum.divide(java.math.BigDecimal.valueOf(costs.size()), java.math.RoundingMode.HALF_UP);
            costoPeriodo.put(pid, avg);
        });

        // Si algún producto vendido no tiene costo en el período, intenta con costo histórico global simple
        // (promedio de todas las compras registradas para ese producto)
        java.util.function.Function<Long, java.math.BigDecimal> costoProducto = (pid) -> {
            java.math.BigDecimal cp = costoPeriodo.get(pid);
            if (cp != null) return cp;
            // buscar costo histórico (simple)
            java.math.BigDecimal sum = java.math.BigDecimal.ZERO; long cnt = 0;
            for (Compra c : compraRepository.findAll()) {
                for (CompraDetalle d : c.getDetalles()) {
                    if (d.getProducto()!=null && d.getProducto().getId().equals(pid) && d.getCostoUnitario()!=null) {
                        sum = sum.add(d.getCostoUnitario()); cnt++;
                    }
                }
            }
            return cnt==0? java.math.BigDecimal.ZERO : sum.divide(java.math.BigDecimal.valueOf(cnt), java.math.RoundingMode.HALF_UP);
        };

        java.math.BigDecimal costoEstimado = java.math.BigDecimal.ZERO;
        for (Venta v : ventas) {
            for (VentaDetalle d : v.getDetalles()) {
                if (d.getProducto()==null || d.getCantidad()==null) continue;
                java.math.BigDecimal c = costoProducto.apply(d.getProducto().getId());
                if (c==null) c = java.math.BigDecimal.ZERO;
                java.math.BigDecimal cant = java.math.BigDecimal.valueOf(d.getCantidad());
                costoEstimado = costoEstimado.add(c.multiply(cant));
            }
        }

        java.util.Map<String,Object> res = new java.util.HashMap<>();
        res.put("inicio", range[0]);
        res.put("fin", range[1]);
        res.put("totalVentas", totalVentas);
        res.put("costoEstimado", costoEstimado);
        res.put("utilidad", totalVentas.subtract(costoEstimado));
        return res;
    }

    private LocalDate[] rango(String tipo, LocalDate fecha){
        LocalDate inicio; LocalDate fin;
        switch (tipo.toLowerCase()) {
            case "diario" -> { inicio = fecha; fin = fecha; }
            case "quincenal" -> { inicio = fecha.withDayOfMonth((fecha.getDayOfMonth() <= 15) ? 1 : 16); fin = inicio.plusDays(14); }
            case "mensual" -> { inicio = fecha.withDayOfMonth(1); fin = fecha.withDayOfMonth(fecha.lengthOfMonth()); }
            default -> throw new IllegalArgumentException("Tipo no soportado");
        }
        return new LocalDate[]{inicio, fin};
    }
}
