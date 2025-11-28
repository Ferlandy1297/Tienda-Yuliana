package com.tienda.controller;

import com.tienda.entity.Proveedor;
import com.tienda.repository.ProveedorRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/proveedores")
public class ProveedorController {
    private final ProveedorRepository repo;
    public ProveedorController(ProveedorRepository repo) { this.repo = repo; }

    @GetMapping public List<Proveedor> listar() { return repo.findAll(); }
    @GetMapping("/{id}") public ResponseEntity<Proveedor> obtener(@PathVariable Long id) { return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build()); }
    @PostMapping public Proveedor crear(@RequestBody Proveedor p) { return repo.save(p); }
    @PutMapping("/{id}") public Proveedor actualizar(@PathVariable Long id, @RequestBody Proveedor p) { p.setId(id); return repo.save(p); }
    @DeleteMapping("/{id}") public void eliminar(@PathVariable Long id) { repo.deleteById(id); }
}

