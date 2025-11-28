package com.tienda.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "clientes")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(length = 50)
    private String telefono;

    @Column(name = "es_mayorista")
    private boolean esMayorista;

    @Column(name = "limite_credito")
    private BigDecimal limiteCredito;

    @Column(name = "saldo_credito")
    private BigDecimal saldoCredito = BigDecimal.ZERO;

    @Column(name = "esta_bloqueado")
    private boolean estaBloqueado = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public boolean isEsMayorista() { return esMayorista; }
    public void setEsMayorista(boolean esMayorista) { this.esMayorista = esMayorista; }
    public BigDecimal getLimiteCredito() { return limiteCredito; }
    public void setLimiteCredito(BigDecimal limiteCredito) { this.limiteCredito = limiteCredito; }
    public BigDecimal getSaldoCredito() { return saldoCredito; }
    public void setSaldoCredito(BigDecimal saldoCredito) { this.saldoCredito = saldoCredito; }
    public boolean isEstaBloqueado() { return estaBloqueado; }
    public void setEstaBloqueado(boolean estaBloqueado) { this.estaBloqueado = estaBloqueado; }
}

