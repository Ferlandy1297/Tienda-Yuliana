package com.tienda.controller;

import com.tienda.entity.DevolucionProveedor;
import com.tienda.service.DevolucionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/devoluciones-proveedor")
public class DevolucionesProveedorController {
    private final DevolucionService service;
    public DevolucionesProveedorController(DevolucionService service) { this.service = service; }

    public static class DevolucionRequest {
        @jakarta.validation.constraints.NotNull(message = "idProveedor es requerido")
        public Long idProveedor; 
        public String motivo; 
        @jakarta.validation.Valid
        @jakarta.validation.constraints.NotEmpty(message = "items no puede estar vacío")
        public List<DevolucionService.ItemDevolucion> items;
    }

    @PostMapping
    public DevolucionProveedor registrar(@jakarta.validation.Valid @RequestBody DevolucionRequest req){
        return service.devolver(req.idProveedor, req.motivo, req.items);
    }
}
