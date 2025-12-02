package com.tienda.service;

import com.tienda.dto.FiadoDtos;
import com.tienda.entity.Cliente;
import com.tienda.entity.CuentaCorrienteCliente;
import com.tienda.entity.MovimientoCuentaCorriente;
import com.tienda.repository.ClienteRepository;
import com.tienda.repository.CuentaCorrienteRepository;
import com.tienda.repository.MovimientoCuentaCorrienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FiadoService {
    private final ClienteRepository clienteRepository;
    private final CuentaCorrienteRepository cuentaRepository;
    private final MovimientoCuentaCorrienteRepository movRepository;

    public FiadoService(ClienteRepository clienteRepository, CuentaCorrienteRepository cuentaRepository, MovimientoCuentaCorrienteRepository movRepository) {
        this.clienteRepository = clienteRepository;
        this.cuentaRepository = cuentaRepository;
        this.movRepository = movRepository;
    }

    public List<FiadoDtos.ClienteSaldo> listarSaldos(){
        return clienteRepository.findAll().stream().map(c -> {
            CuentaCorrienteCliente cuenta = cuentaRepository.findByCliente(c).orElse(null);
            FiadoDtos.ClienteSaldo dto = new FiadoDtos.ClienteSaldo();
            dto.setIdCliente(c.getId());
            dto.setNombre(c.getNombre());
            dto.setBloqueado(c.isEstaBloqueado());
            dto.setSaldo(cuenta == null ? BigDecimal.ZERO : cuenta.getSaldoActual());
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public FiadoDtos.ClienteSaldo abonar(FiadoDtos.AbonoRequest req){
        Cliente c = clienteRepository.findById(req.getIdCliente())
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        CuentaCorrienteCliente cuenta = cuentaRepository.findByCliente(c)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada"));
        BigDecimal nuevo = cuenta.getSaldoActual().subtract(req.getMonto());
        if (nuevo.compareTo(BigDecimal.ZERO) < 0) nuevo = BigDecimal.ZERO;
        cuenta.setSaldoActual(nuevo);
        cuentaRepository.save(cuenta);

        MovimientoCuentaCorriente mov = new MovimientoCuentaCorriente();
        mov.setCuenta(cuenta);
        mov.setTipo("ABONO");
        mov.setMonto(req.getMonto());
        mov.setDescripcion("Abono a cuenta");
        movRepository.save(mov);

        FiadoDtos.ClienteSaldo dto = new FiadoDtos.ClienteSaldo();
        dto.setIdCliente(c.getId());
        dto.setNombre(c.getNombre());
        dto.setBloqueado(c.isEstaBloqueado());
        dto.setSaldo(nuevo);
        return dto;
    }

    // Marca clientes bloqueados si saldo > límite o sin abonos por N días con saldo pendiente
    @org.springframework.transaction.annotation.Transactional
    public int evaluarMorosidad(int dias) {
        java.time.LocalDateTime limite = java.time.LocalDateTime.now().minusDays(Math.max(dias, 0));
        int afectados = 0;
        for (Cliente c : clienteRepository.findAll()) {
            CuentaCorrienteCliente cuenta = cuentaRepository.findByCliente(c).orElse(null);
            java.math.BigDecimal saldo = cuenta == null ? java.math.BigDecimal.ZERO : cuenta.getSaldoActual();
            boolean excedeLimite = c.getLimiteCredito() != null && saldo.compareTo(c.getLimiteCredito()) > 0;
            boolean diasSinAbono = false;
            if (saldo.compareTo(java.math.BigDecimal.ZERO) > 0) {
                // último abono
                java.util.Optional<MovimientoCuentaCorriente> ult = movRepository.findAll().stream()
                        .filter(m -> m.getCuenta()!=null && cuenta!=null && m.getCuenta().getId().equals(cuenta.getId()))
                        .filter(m -> "ABONO".equalsIgnoreCase(m.getTipo()))
                        .max(java.util.Comparator.comparing(MovimientoCuentaCorriente::getFecha));
                diasSinAbono = ult.map(m -> m.getFecha().isBefore(limite)).orElse(true);
            }
            boolean bloquear = excedeLimite || diasSinAbono;
            if (bloquear != c.isEstaBloqueado()) {
                c.setEstaBloqueado(bloquear); clienteRepository.save(c); afectados++;
            }
        }
        return afectados;
    }
}

