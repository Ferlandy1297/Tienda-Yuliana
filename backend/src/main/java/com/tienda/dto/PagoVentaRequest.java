package com.tienda.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class PagoVentaRequest {
    @NotNull(message = "idVenta es requerido")
    private Long idVenta;
    @NotBlank(message = "metodo es requerido")
    private String metodo; // EFECTIVO, TRANSFERENCIA
    @NotNull(message = "monto es requerido")
    @DecimalMin(value = "0.01", message = "monto debe ser > 0")
    private BigDecimal monto;
    private BigDecimal denominacion;
    public Long getIdVenta() { return idVenta; }
    public void setIdVenta(Long idVenta) { this.idVenta = idVenta; }
    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public BigDecimal getDenominacion() { return denominacion; }
    public void setDenominacion(BigDecimal denominacion) { this.denominacion = denominacion; }
}
