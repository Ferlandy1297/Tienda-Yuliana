package com.tienda.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class FiadoDtos {
    public static class ClienteSaldo {
        private Long idCliente;
        private String nombre;
        private BigDecimal saldo;
        private boolean bloqueado;
        public Long getIdCliente() { return idCliente; }
        public void setIdCliente(Long idCliente) { this.idCliente = idCliente; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public BigDecimal getSaldo() { return saldo; }
        public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }
        public boolean isBloqueado() { return bloqueado; }
        public void setBloqueado(boolean bloqueado) { this.bloqueado = bloqueado; }
    }

    public static class AbonoRequest {
        @NotNull(message = "idCliente es requerido")
        private Long idCliente;
        @NotNull(message = "monto es requerido")
        @DecimalMin(value = "0.01", message = "monto debe ser > 0")
        private BigDecimal monto;
        public Long getIdCliente() { return idCliente; }
        public void setIdCliente(Long idCliente) { this.idCliente = idCliente; }
        public BigDecimal getMonto() { return monto; }
        public void setMonto(BigDecimal monto) { this.monto = monto; }
    }
}
