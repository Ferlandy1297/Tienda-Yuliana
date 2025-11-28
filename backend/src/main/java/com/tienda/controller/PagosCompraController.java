package com.tienda.controller;

import com.tienda.dto.CompraDtos;
import com.tienda.entity.Compra;
import com.tienda.service.PagoCompraService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pagos-compra")
public class PagosCompraController {
    private final PagoCompraService service;
    public PagosCompraController(PagoCompraService service) { this.service = service; }

    @PostMapping
    public Compra pagar(@jakarta.validation.Valid @RequestBody CompraDtos.PagoRequest req){
        return service.registrarPago(req);
    }
}
