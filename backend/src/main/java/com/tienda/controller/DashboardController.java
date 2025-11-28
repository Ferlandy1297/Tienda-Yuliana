package com.tienda.controller;

import com.tienda.entity.Producto;
import com.tienda.entity.Venta;
import com.tienda.repository.ProductoRepository;
import com.tienda.repository.VentaRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;

    public DashboardController(VentaRepository ventaRepository, ProductoRepository productoRepository) {
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        LocalDate today = LocalDate.now();
        List<Venta> ventas = ventaRepository.findByFechaHoraBetween(today.atStartOfDay(), today.atTime(LocalTime.MAX));
        BigDecimal ventasHoy = ventas.stream()
                .map(Venta::getTotal)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Producto> productos = productoRepository.findAll();
        long low = productos.stream()
                .filter(p -> p.getStockActual() != null && p.getStockMinimo() != null && p.getStockActual() <= p.getStockMinimo())
                .count();

        Map<String, Object> res = new HashMap<>();
        res.put("ventasHoy", ventasHoy);
        res.put("lowStock", low);
        res.put("productos", productos.size());
        return res;
    }
}

