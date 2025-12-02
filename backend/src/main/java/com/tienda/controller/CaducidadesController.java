package com.tienda.controller;

import com.tienda.entity.LoteProducto;
import com.tienda.entity.Promocion;
import com.tienda.repository.LoteProductoRepository;
import com.tienda.repository.PromocionRepository;
import com.tienda.service.MermaService;
import com.tienda.service.DevolucionService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/caducidades")
public class CaducidadesController {
    private final LoteProductoRepository repo;
    private final PromocionRepository promoRepo;
    private final MermaService mermaService;
    private final DevolucionService devolucionService;
    public CaducidadesController(LoteProductoRepository repo, PromocionRepository promoRepo,
                                 MermaService mermaService, DevolucionService devolucionService) {
        this.repo = repo; this.promoRepo = promoRepo; this.mermaService = mermaService; this.devolucionService = devolucionService;
    }

    @GetMapping("/por-vencer")
    public List<LoteProducto> porVencer(@jakarta.validation.constraints.Min(value = 0, message = "dias debe ser >= 0") @RequestParam(defaultValue = "30") int dias){
        LocalDate hoy = LocalDate.now();
        LocalDate fin = hoy.plusDays(dias);
        return repo.findByFechaVencimientoBetween(hoy, fin);
    }

    // Acciones: aplicar descuento (promoción temporal) sobre el producto del lote
    public static class DescuentoRequest {
        @jakarta.validation.constraints.NotBlank public String tipo; // PORCENTAJE o MONTO_FIJO
        @jakarta.validation.constraints.NotNull public java.math.BigDecimal valor;
        @jakarta.validation.constraints.Min(1) public int diasVigencia = 7;
    }
    @PostMapping("/{idLote}/descuento")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public Promocion aplicarDescuento(@PathVariable Long idLote, @jakarta.validation.Valid @RequestBody DescuentoRequest req) {
        LoteProducto lote = repo.findById(idLote).orElseThrow(() -> new IllegalArgumentException("Lote no encontrado"));
        Promocion p = new Promocion();
        p.setProducto(lote.getProducto());
        p.setTipo(req.tipo);
        p.setValor(req.valor);
        p.setFechaInicio(LocalDate.now());
        p.setFechaFin(LocalDate.now().plusDays(req.diasVigencia));
        p.setActiva(true);
        return promoRepo.save(p);
    }

    // Donación = merma con motivo DONACIÓN
    public static class DonacionRequest {
        @jakarta.validation.constraints.NotNull public Integer cantidad;
        public String motivo;
    }
    @PostMapping("/{idLote}/donacion")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public com.tienda.entity.Merma donar(@PathVariable Long idLote, @jakarta.validation.Valid @RequestBody DonacionRequest req) {
        LoteProducto lote = repo.findById(idLote).orElseThrow(() -> new IllegalArgumentException("Lote no encontrado"));
        String motivo = (req.motivo == null || req.motivo.isBlank()) ? "DONACIÓN" : req.motivo;
        return mermaService.registrarMerma(lote.getProducto().getId(), idLote, req.cantidad, motivo);
    }

    // Devolver a proveedor (requiere proveedor nacional)
    public static class DevRequest {
        @jakarta.validation.constraints.NotNull public Long idProveedor;
        @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.Min(1) public Integer cantidad;
        public String motivo;
    }
    @PostMapping("/{idLote}/devolver")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public com.tienda.entity.DevolucionProveedor devolver(@PathVariable Long idLote, @jakarta.validation.Valid @RequestBody DevRequest req) {
        LoteProducto lote = repo.findById(idLote).orElseThrow(() -> new IllegalArgumentException("Lote no encontrado"));
        java.util.List<com.tienda.service.DevolucionService.ItemDevolucion> items = java.util.List.of(item(lote.getProducto().getId(), req.cantidad, idLote));
        return devolucionService.devolver(req.idProveedor, req.motivo, items);
    }

    private static DevolucionService.ItemDevolucion item(Long idProducto, Integer cantidad, Long idLote) {
        DevolucionService.ItemDevolucion it = new DevolucionService.ItemDevolucion();
        it.idProducto = idProducto; it.cantidad = cantidad; it.idLote = idLote; return it;
    }
}
