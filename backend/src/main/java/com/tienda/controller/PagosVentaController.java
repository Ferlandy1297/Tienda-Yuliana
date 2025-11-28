package com.tienda.controller;

import com.tienda.dto.PagoVentaRequest;
import com.tienda.entity.Venta;
import com.tienda.service.PagoVentaService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pagos-venta")
public class PagosVentaController {
    private final PagoVentaService pagoVentaService;
    public PagosVentaController(PagoVentaService pagoVentaService) { this.pagoVentaService = pagoVentaService; }

    @PostMapping
    public Venta pagar(@jakarta.validation.Valid @RequestBody PagoVentaRequest req){
        return pagoVentaService.registrarPago(req);
    }
}
