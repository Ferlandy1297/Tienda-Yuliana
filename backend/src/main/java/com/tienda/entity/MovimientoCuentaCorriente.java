package com.tienda.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_cuenta_corriente")
public class MovimientoCuentaCorriente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_cuenta")
    private CuentaCorrienteCliente cuenta;

    @Column(length = 10)
    private String tipo; // CARGO, ABONO

    private BigDecimal monto;
    private LocalDateTime fecha = LocalDateTime.now();
    private String descripcion;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public CuentaCorrienteCliente getCuenta() { return cuenta; }
    public void setCuenta(CuentaCorrienteCliente cuenta) { this.cuenta = cuenta; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}

