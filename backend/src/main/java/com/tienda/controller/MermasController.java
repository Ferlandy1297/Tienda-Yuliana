package com.tienda.controller;

import com.tienda.entity.Merma;
import com.tienda.service.MermaService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mermas")
public class MermasController {
    private final MermaService mermaService;
    public MermasController(MermaService mermaService) { this.mermaService = mermaService; }

    public static class MermaRequest {
        @jakarta.validation.constraints.NotNull(message = "idProducto es requerido")
        public Long idProducto;
        public Long idLote;
        @jakarta.validation.constraints.NotNull(message = "cantidad es requerida")
        @jakarta.validation.constraints.Min(value = 1, message = "cantidad debe ser >= 1")
        public Integer cantidad;
        public String motivo;
    }

    @PostMapping
    public Merma registrar(@jakarta.validation.Valid @RequestBody MermaRequest req){
        return mermaService.registrarMerma(req.idProducto, req.idLote, req.cantidad, req.motivo);
    }
}
