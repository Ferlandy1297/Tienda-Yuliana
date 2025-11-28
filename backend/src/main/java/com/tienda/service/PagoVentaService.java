package com.tienda.service;

import com.tienda.dto.PagoVentaRequest;
import com.tienda.entity.PagoVenta;
import com.tienda.entity.Venta;
import com.tienda.repository.PagoVentaRepository;
import com.tienda.repository.VentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PagoVentaService {
    private final VentaRepository ventaRepository;
    private final PagoVentaRepository pagoVentaRepository;

    public PagoVentaService(VentaRepository ventaRepository, PagoVentaRepository pagoVentaRepository) {
        this.ventaRepository = ventaRepository;
        this.pagoVentaRepository = pagoVentaRepository;
    }

    @Transactional
    public Venta registrarPago(PagoVentaRequest req) {
        Venta venta = ventaRepository.findById(req.getIdVenta())
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada"));
        BigDecimal pagado = venta.getPagos().stream()
                .map(p -> p.getMontoPagado() == null ? BigDecimal.ZERO : p.getMontoPagado())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal saldo = venta.getTotal().subtract(pagado);
        BigDecimal pagoMonto = req.getMonto() == null ? BigDecimal.ZERO : req.getMonto();

        PagoVenta pago = new PagoVenta();
        pago.setVenta(venta);
        pago.setMetodo(req.getMetodo());
        pago.setMontoPagado(pagoMonto);
        pago.setDenominacion(req.getDenominacion());
        BigDecimal cambio = pagoMonto.subtract(saldo);
        if (cambio.compareTo(BigDecimal.ZERO) < 0) cambio = BigDecimal.ZERO;
        pago.setCambio(cambio);
        pagoVentaRepository.save(pago);
        venta.getPagos().add(pago);

        BigDecimal totalPagado = pagado.add(pagoMonto);
        if (totalPagado.compareTo(venta.getTotal()) >= 0) {
            venta.setEstado("PAGADA");
        }
        return ventaRepository.save(venta);
    }
}

