package com.tienda.controller;

import com.tienda.dto.ProductoDTO;
import com.tienda.entity.Producto;
import com.tienda.service.ProductoService;
import com.tienda.service.StockAlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/productos")
public class ProductoController {
    private final ProductoService productoService;
    private final StockAlertService stockAlertService;

    public ProductoController(ProductoService productoService, StockAlertService stockAlertService) {
        this.productoService = productoService;
        this.stockAlertService = stockAlertService;
    }

    @GetMapping
    public List<Producto> listar() { return productoService.listar(); }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtener(@PathVariable Long id) {
        return productoService.obtener(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Producto crear(@jakarta.validation.Valid @RequestBody ProductoDTO dto) { return productoService.crear(dto); }

    @PutMapping("/{id}")
    public Producto actualizar(@PathVariable Long id, @jakarta.validation.Valid @RequestBody ProductoDTO dto) { return productoService.actualizar(id, dto); }

    @DeleteMapping("/{id}")
    public Producto toggle(@PathVariable Long id) { return productoService.toggleActivo(id); }

    @GetMapping("/stock-bajo")
    public List<Producto> stockBajo() { return productoService.stockBajo(); }

    // Opcional: disparar correo de alerta stock bajo (ADMIN/SUPERVISOR)
    @PostMapping("/stock-bajo/alerta-email")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public org.springframework.http.ResponseEntity<Void> enviarAlerta(@RequestParam(required = false) String to) {
        List<Producto> low = productoService.stockBajo();
        stockAlertService.enviarAlertaStockBajo(low, to);
        return org.springframework.http.ResponseEntity.accepted().build();
    }
}
