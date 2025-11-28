package com.tienda.service;

import com.tienda.dto.ProductoDTO;
import com.tienda.entity.Producto;
import com.tienda.entity.Proveedor;
import com.tienda.repository.ProductoRepository;
import com.tienda.repository.ProveedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {
    private final ProductoRepository productoRepository;
    private final ProveedorRepository proveedorRepository;

    public ProductoService(ProductoRepository productoRepository, ProveedorRepository proveedorRepository) {
        this.productoRepository = productoRepository;
        this.proveedorRepository = proveedorRepository;
    }

    public List<Producto> listar() { return productoRepository.findAll(); }

    public Optional<Producto> obtener(Long id) { return productoRepository.findById(id); }

    @Transactional
    public Producto crear(ProductoDTO dto) {
        productoRepository.findByCodigoBarras(dto.getCodigoBarras()).ifPresent(p -> {
            throw new IllegalArgumentException("Codigo de barras ya existe");
        });
        Producto p = new Producto();
        mapear(dto, p);
        return productoRepository.save(p);
    }

    @Transactional
    public Producto actualizar(Long id, ProductoDTO dto) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        if (dto.getCodigoBarras() != null && !dto.getCodigoBarras().equals(p.getCodigoBarras())) {
            productoRepository.findByCodigoBarras(dto.getCodigoBarras()).ifPresent(ex -> {
                throw new IllegalArgumentException("Codigo de barras ya existe");
            });
        }
        mapear(dto, p);
        return productoRepository.save(p);
    }

    @Transactional
    public Producto toggleActivo(Long id) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        p.setActivo(!p.isActivo());
        return productoRepository.save(p);
    }

    public List<Producto> stockBajo() {
        return productoRepository.findAll().stream()
                .filter(p -> p.getStockActual() != null && p.getStockMinimo() != null && p.getStockActual() <= p.getStockMinimo())
                .toList();
    }

    private void mapear(ProductoDTO dto, Producto p) {
        if (dto.getNombre() != null) p.setNombre(dto.getNombre());
        if (dto.getCodigoBarras() != null) p.setCodigoBarras(dto.getCodigoBarras());
        if (dto.getPrecioUnitario() != null) p.setPrecioUnitario(dto.getPrecioUnitario());
        if (dto.getStockActual() != null) p.setStockActual(dto.getStockActual());
        if (dto.getStockMinimo() != null) p.setStockMinimo(dto.getStockMinimo());
        if (dto.getIdProveedor() != null) {
            Proveedor prov = proveedorRepository.findById(dto.getIdProveedor())
                    .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
            p.setProveedor(prov);
        }
        p.setActivo(dto.isActivo() || p.isActivo());
    }
}

