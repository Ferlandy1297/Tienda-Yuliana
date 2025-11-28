package com.tienda.controller;

import com.tienda.dto.CompraDtos;
import com.tienda.entity.Compra;
import com.tienda.repository.CompraRepository;
import com.tienda.service.CompraService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/compras")
public class ComprasController {
    private final CompraService compraService;
    private final CompraRepository compraRepository;
    public ComprasController(CompraService compraService, CompraRepository compraRepository) {
        this.compraService = compraService;
        this.compraRepository = compraRepository;
    }

    @PostMapping
    public Compra registrar(@jakarta.validation.Valid @RequestBody CompraDtos.CompraRequest req){
        return compraService.registrarCompra(req);
    }

    @GetMapping
    public List<Compra> listar(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin){
        if (inicio != null && fin != null) {
            return compraRepository.findByFechaHoraBetween(inicio.atStartOfDay(), fin.atTime(LocalTime.MAX));
        }
        return compraRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Compra> obtener(@PathVariable Long id){
        return compraRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
