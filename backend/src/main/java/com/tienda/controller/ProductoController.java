package com.tienda.controller;

import com.tienda.dto.ProductoDTO;
import com.tienda.entity.Producto;
import com.tienda.service.ProductoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/productos")
public class ProductoController {
    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
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
}
