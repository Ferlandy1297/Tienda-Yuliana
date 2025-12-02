package com.tienda.service;

import com.tienda.entity.*;
import com.tienda.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DevolucionService {
    private final ProveedorRepository proveedorRepository;
    private final ProductoRepository productoRepository;
    private final LoteProductoRepository loteProductoRepository;
    private final DevolucionProveedorRepository devolucionProveedorRepository;

    public DevolucionService(ProveedorRepository proveedorRepository, ProductoRepository productoRepository,
                             LoteProductoRepository loteProductoRepository, DevolucionProveedorRepository devolucionProveedorRepository) {
        this.proveedorRepository = proveedorRepository;
        this.productoRepository = productoRepository;
        this.loteProductoRepository = loteProductoRepository;
        this.devolucionProveedorRepository = devolucionProveedorRepository;
    }

    @Transactional
    public DevolucionProveedor devolver(Long idProveedor, String motivo, List<ItemDevolucion> items){
        Proveedor prov = proveedorRepository.findById(idProveedor).orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
        if (!prov.isEsNacional()) {
            throw new IllegalArgumentException("Devolución permitida solo a proveedores nacionales");
        }
        DevolucionProveedor dev = new DevolucionProveedor();
        dev.setProveedor(prov);
        dev.setMotivo(motivo);
        for (ItemDevolucion it : items) {
            Producto p = productoRepository.findById(it.idProducto)
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
            if (p.getStockActual() < it.cantidad) {
                throw new IllegalArgumentException("Stock insuficiente para devolución");
            }
            p.setStockActual(p.getStockActual() - it.cantidad);
            productoRepository.save(p);
            if (it.idLote != null) {
                LoteProducto lote = loteProductoRepository.findById(it.idLote)
                        .orElseThrow(() -> new IllegalArgumentException("Lote no encontrado"));
                if (lote.getCantidad() < it.cantidad) throw new IllegalArgumentException("Cantidad excede lote");
                lote.setCantidad(lote.getCantidad() - it.cantidad);
                loteProductoRepository.save(lote);
            }
            DevolucionDetalle det = new DevolucionDetalle();
            det.setDevolucion(dev);
            det.setProducto(p);
            det.setCantidad(it.cantidad);
            if (it.idLote != null) det.setLote(loteProductoRepository.findById(it.idLote).orElse(null));
            dev.getDetalles().add(det);
        }
        return devolucionProveedorRepository.save(dev);
    }

    public static class ItemDevolucion {
        @jakarta.validation.constraints.NotNull(message = "idProducto es requerido")
        public Long idProducto;
        @jakarta.validation.constraints.NotNull(message = "cantidad es requerida")
        @jakarta.validation.constraints.Min(value = 1, message = "cantidad debe ser >= 1")
        public Integer cantidad;
        public Long idLote;
    }
}
