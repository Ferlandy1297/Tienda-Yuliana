package com.tienda.controller;

import com.tienda.entity.Cliente;
import com.tienda.repository.ClienteRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clientes")
public class ClienteController {
    private final ClienteRepository repo;
    public ClienteController(ClienteRepository repo) { this.repo = repo; }

    @GetMapping public List<Cliente> listar() { return repo.findAll(); }
    @GetMapping("/{id}") public ResponseEntity<Cliente> obtener(@PathVariable Long id) { return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build()); }
    @PostMapping public Cliente crear(@RequestBody Cliente c) { return repo.save(c); }
    @PutMapping("/{id}") public Cliente actualizar(@PathVariable Long id, @RequestBody Cliente c) { c.setId(id); return repo.save(c); }
    @DeleteMapping("/{id}") public void eliminar(@PathVariable Long id) { repo.deleteById(id); }
}

