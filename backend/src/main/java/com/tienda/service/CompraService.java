package com.tienda.service;

import com.tienda.dto.CompraDtos;
import com.tienda.entity.*;
import com.tienda.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CompraService {
    private final ProveedorRepository proveedorRepository;
    private final ProductoRepository productoRepository;
    private final CompraRepository compraRepository;
    private final LoteProductoRepository loteProductoRepository;

    public CompraService(ProveedorRepository proveedorRepository, ProductoRepository productoRepository,
                         CompraRepository compraRepository, LoteProductoRepository loteProductoRepository) {
        this.proveedorRepository = proveedorRepository;
        this.productoRepository = productoRepository;
        this.compraRepository = compraRepository;
        this.loteProductoRepository = loteProductoRepository;
    }

    @Transactional
    public Compra registrarCompra(CompraDtos.CompraRequest req){
        Proveedor proveedor = proveedorRepository.findById(req.getIdProveedor())
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
        Compra compra = new Compra();
        compra.setProveedor(proveedor);
        compra.setEstado(req.getEstado() == null ? "ABIERTA" : req.getEstado());
        BigDecimal total = BigDecimal.ZERO;
        for (CompraDtos.Item it : req.getItems()) {
            Producto p = productoRepository.findById(it.getIdProducto())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
            CompraDetalle det = new CompraDetalle();
            det.setCompra(compra);
            det.setProducto(p);
            det.setCantidad(it.getCantidad());
            det.setCostoUnitario(it.getCostoUnitario());
            det.setFechaVencimiento(it.getFechaVencimiento());
            det.setSubtotal(it.getCostoUnitario().multiply(new BigDecimal(it.getCantidad())));
            compra.getDetalles().add(det);

            // aumentar stock
            p.setStockActual(p.getStockActual() + it.getCantidad());
            productoRepository.save(p);
            // crear lote
            LoteProducto lote = new LoteProducto();
            lote.setProducto(p);
            lote.setCantidad(it.getCantidad());
            lote.setCostoUnitario(it.getCostoUnitario());
            lote.setFechaVencimiento(it.getFechaVencimiento());
            lote.setEstado("NORMAL");
            loteProductoRepository.save(lote);

            total = total.add(det.getSubtotal());
        }
        compra.setTotal(total);
        return compraRepository.save(compra);
    }
}

