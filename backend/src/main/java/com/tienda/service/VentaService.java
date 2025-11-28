package com.tienda.service;

import com.tienda.dto.VentaDtos;
import com.tienda.entity.*;
import com.tienda.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class VentaService {
    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final LoteProductoRepository loteProductoRepository;
    private final PromocionRepository promocionRepository;
    private final CuentaCorrienteRepository cuentaCorrienteRepository;

    public VentaService(ProductoRepository productoRepository, VentaRepository ventaRepository,
                        ClienteRepository clienteRepository, UsuarioRepository usuarioRepository,
                        LoteProductoRepository loteProductoRepository, PromocionRepository promocionRepository,
                        CuentaCorrienteRepository cuentaCorrienteRepository) {
        this.productoRepository = productoRepository;
        this.ventaRepository = ventaRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.loteProductoRepository = loteProductoRepository;
        this.promocionRepository = promocionRepository;
        this.cuentaCorrienteRepository = cuentaCorrienteRepository;
    }

    @Transactional
    public VentaDtos.VentaResponse registrarVenta(String username, VentaDtos.VentaRequest req) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        final Cliente cliente = (req.getIdCliente() != null)
                ? clienteRepository.findById(req.getIdCliente())
                    .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"))
                : null;

        boolean esMayorista = "MAYOREO".equalsIgnoreCase(req.getTipo()) || (cliente != null && cliente.isEsMayorista());

        Venta venta = new Venta();
        venta.setUsuario(usuario);
        venta.setCliente(cliente);
        venta.setTipo(req.getTipo());
        venta.setEstado("ABIERTA");

        BigDecimal total = BigDecimal.ZERO;

        for (VentaDtos.SaleItem item : req.getItems()) {
            Producto p = productoRepository.findById(item.getIdProducto())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
            if (p.getStockActual() < item.getCantidad()) {
                throw new IllegalArgumentException("Stock insuficiente para producto: " + p.getNombre());
            }

            BigDecimal precio = p.getPrecioUnitario();
            if (esMayorista && item.getCantidad() >= 6) { // simple regla mayoreo
                precio = precio.multiply(new BigDecimal("0.9")); // 10% descuento por mayoreo
            }
            // promociones activas
            List<Promocion> promos = promocionRepository
                    .findByProductoAndActivaIsTrueAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
                            p, LocalDate.now(), LocalDate.now());
            for (Promocion promo : promos) {
                if ("PORCENTAJE".equalsIgnoreCase(promo.getTipo())) {
                    precio = precio.multiply(BigDecimal.ONE.subtract(promo.getValor().divide(new BigDecimal("100"))));
                } else if ("MONTO_FIJO".equalsIgnoreCase(promo.getTipo())) {
                    precio = precio.subtract(promo.getValor());
                }
            }
            if (precio.compareTo(BigDecimal.ZERO) < 0) precio = BigDecimal.ZERO;

            VentaDetalle det = new VentaDetalle();
            det.setVenta(venta);
            det.setProducto(p);
            det.setCantidad(item.getCantidad());
            det.setPrecioUnitario(precio);
            det.setSubtotal(precio.multiply(new BigDecimal(item.getCantidad())));
            venta.getDetalles().add(det);

            total = total.add(det.getSubtotal());

            // descontar stock general
            p.setStockActual(p.getStockActual() - item.getCantidad());
            productoRepository.save(p);
            // descontar lotes por FIFO
            int porDescontar = item.getCantidad();
            for (LoteProducto lote : loteProductoRepository.findByProductoOrderByFechaVencimientoAsc(p)) {
                if (porDescontar <= 0) break;
                int usar = Math.min(lote.getCantidad(), porDescontar);
                lote.setCantidad(lote.getCantidad() - usar);
                loteProductoRepository.save(lote);
                porDescontar -= usar;
            }
        }

        venta.setTotal(total);

        // Pago
        if (req.getPago() != null) {
            PagoVenta pago = new PagoVenta();
            pago.setVenta(venta);
            pago.setMetodo(req.getPago().getMetodo());
            pago.setMontoPagado(req.getPago().getMonto());
            pago.setDenominacion(req.getPago().getDenominacion());
            BigDecimal cambio = req.getPago().getMonto() == null ? BigDecimal.ZERO : req.getPago().getMonto().subtract(total);
            if (cambio.compareTo(BigDecimal.ZERO) < 0) cambio = BigDecimal.ZERO;
            pago.setCambio(cambio);
            venta.getPagos().add(pago);
            venta.setEstado(pago.getMontoPagado() != null && pago.getMontoPagado().compareTo(total) >= 0 ? "PAGADA" : "ABIERTA");
        }

        // FIADO
        if ("FIADO".equalsIgnoreCase(req.getTipo())) {
            if (cliente == null) throw new IllegalArgumentException("Cliente requerido para fiado");
            if (cliente.isEstaBloqueado()) throw new IllegalArgumentException("Cliente bloqueado por morosidad");
            CuentaCorrienteCliente cuenta = cuentaCorrienteRepository.findByCliente(cliente)
                    .orElseGet(() -> {
                        CuentaCorrienteCliente c = new CuentaCorrienteCliente();
                        c.setCliente(cliente);
                        return c;
                    });
            BigDecimal nuevoSaldo = cuenta.getSaldoActual().add(total);
            if (cliente.getLimiteCredito() != null && nuevoSaldo.compareTo(cliente.getLimiteCredito()) > 0) {
                throw new IllegalArgumentException("Excede límite de crédito");
            }
            cuenta.setSaldoActual(nuevoSaldo);
            cuentaCorrienteRepository.save(cuenta);
            venta.setEstado("ABIERTA");
        }

        venta = ventaRepository.save(venta);

        VentaDtos.VentaResponse resp = new VentaDtos.VentaResponse();
        resp.setIdVenta(venta.getId());
        resp.setTotal(total);
        BigDecimal cambio = BigDecimal.ZERO;
        if (!venta.getPagos().isEmpty() && venta.getPagos().get(0).getCambio() != null) {
            cambio = venta.getPagos().get(0).getCambio();
        }
        resp.setCambio(cambio);
        return resp;
    }
}
