package com.tienda.controller;

import com.tienda.dto.FiadoDtos;
import com.tienda.service.FiadoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fiados")
public class FiadosController {
    private final FiadoService service;
    public FiadosController(FiadoService service) { this.service = service; }

    @GetMapping("/clientes")
    public List<FiadoDtos.ClienteSaldo> clientes(){
        return service.listarSaldos();
    }

    @PostMapping("/abono")
    public FiadoDtos.ClienteSaldo abonar(@jakarta.validation.Valid @RequestBody FiadoDtos.AbonoRequest req){
        return service.abonar(req);
    }
}
