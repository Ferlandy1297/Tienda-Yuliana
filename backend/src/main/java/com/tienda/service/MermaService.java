package com.tienda.service;

import com.tienda.entity.LoteProducto;
import com.tienda.entity.Merma;
import com.tienda.entity.Producto;
import com.tienda.repository.LoteProductoRepository;
import com.tienda.repository.MermaRepository;
import com.tienda.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MermaService {
    private final MermaRepository mermaRepository;
    private final ProductoRepository productoRepository;
    private final LoteProductoRepository loteProductoRepository;

    public MermaService(MermaRepository mermaRepository, ProductoRepository productoRepository, LoteProductoRepository loteProductoRepository) {
        this.mermaRepository = mermaRepository;
        this.productoRepository = productoRepository;
        this.loteProductoRepository = loteProductoRepository;
    }

    @Transactional
    public Merma registrarMerma(Long idProducto, Long idLote, Integer cantidad, String motivo){
        Producto p = productoRepository.findById(idProducto)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        if (p.getStockActual() < cantidad) {
            throw new IllegalArgumentException("Stock insuficiente para merma");
        }
        p.setStockActual(p.getStockActual() - cantidad);
        productoRepository.save(p);

        LoteProducto lote = null;
        if (idLote != null) {
            lote = loteProductoRepository.findById(idLote).orElseThrow(() -> new IllegalArgumentException("Lote no encontrado"));
            if (lote.getCantidad() < cantidad) {
                throw new IllegalArgumentException("Cantidad excede el lote");
            }
            lote.setCantidad(lote.getCantidad() - cantidad);
            loteProductoRepository.save(lote);
        }

        Merma m = new Merma();
        m.setProducto(p);
        m.setLote(lote);
        m.setCantidad(cantidad);
        m.setMotivo(motivo);
        return mermaRepository.save(m);
    }
}

