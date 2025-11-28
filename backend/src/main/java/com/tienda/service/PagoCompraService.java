package com.tienda.service;

import com.tienda.dto.CompraDtos;
import com.tienda.entity.Compra;
import com.tienda.entity.PagoCompra;
import com.tienda.repository.CompraRepository;
import com.tienda.repository.PagoCompraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PagoCompraService {
    private final CompraRepository compraRepository;
    private final PagoCompraRepository pagoCompraRepository;

    public PagoCompraService(CompraRepository compraRepository, PagoCompraRepository pagoCompraRepository) {
        this.compraRepository = compraRepository;
        this.pagoCompraRepository = pagoCompraRepository;
    }

    @Transactional
    public Compra registrarPago(CompraDtos.PagoRequest req){
        Compra compra = compraRepository.findById(req.getIdCompra())
                .orElseThrow(() -> new IllegalArgumentException("Compra no encontrada"));
        BigDecimal pagado = compra.getDetalles() == null ? BigDecimal.ZERO : compra.getDetalles().stream()
                .map(d -> BigDecimal.ZERO) // no hay pagos en detalles; sólo para compilar
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // calcular pagado real por pagos
        // no hay relación en la entidad; guardamos y recalculamos sumando pagos persistidos
        BigDecimal pagosPrevios = pagoCompraRepository.findAll().stream()
                .filter(p -> p.getCompra().getId().equals(compra.getId()))
                .map(PagoCompra::getMontoPagado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPagos = pagosPrevios.add(req.getMonto());
        PagoCompra pago = new PagoCompra();
        pago.setCompra(compra);
        pago.setMetodo(req.getMetodo());
        pago.setMontoPagado(req.getMonto());
        pagoCompraRepository.save(pago);

        if (totalPagos.compareTo(compra.getTotal()) >= 0) {
            compra.setEstado("PAGADA");
        }
        return compraRepository.save(compra);
    }
}

