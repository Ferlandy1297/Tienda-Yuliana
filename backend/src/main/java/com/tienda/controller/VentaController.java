package com.tienda.controller;

import com.tienda.dto.VentaDtos;
import com.tienda.entity.Venta;
import com.tienda.repository.VentaRepository;
import com.tienda.service.VentaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/ventas")
public class VentaController {
    private final VentaService ventaService;
    private final VentaRepository ventaRepository;

    public VentaController(VentaService ventaService, VentaRepository ventaRepository) {
        this.ventaService = ventaService;
        this.ventaRepository = ventaRepository;
    }

    @PostMapping
    public VentaDtos.VentaResponse registrarVenta(@AuthenticationPrincipal UserDetails user,
                                                  @jakarta.validation.Valid @RequestBody VentaDtos.VentaRequest request) {
        return ventaService.registrarVenta(user.getUsername(), request);
    }

    @GetMapping
    public List<Venta> listar(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        if (inicio != null && fin != null) {
            return ventaRepository.findByFechaHoraBetween(inicio.atStartOfDay(), fin.atTime(LocalTime.MAX));
        }
        return ventaRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Venta> obtener(@PathVariable Long id) {
        return ventaRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // CU-TY-003: Ticket de venta
    @GetMapping("/{id}/ticket")
    public ResponseEntity<VentaDtos.VentaTicket> ticket(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ventaService.obtenerTicket(id));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
