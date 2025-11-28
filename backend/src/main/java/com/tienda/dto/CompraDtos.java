package com.tienda.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class CompraDtos {
    public static class Item {
        @NotNull(message = "idProducto es requerido")
        private Long idProducto;
        @NotNull(message = "cantidad es requerida")
        @Min(value = 1, message = "cantidad debe ser >= 1")
        private Integer cantidad;
        @NotNull(message = "costoUnitario es requerido")
        @DecimalMin(value = "0.01", message = "costoUnitario debe ser > 0")
        private BigDecimal costoUnitario;
        private LocalDate fechaVencimiento;
        public Long getIdProducto() { return idProducto; }
        public void setIdProducto(Long idProducto) { this.idProducto = idProducto; }
        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
        public BigDecimal getCostoUnitario() { return costoUnitario; }
        public void setCostoUnitario(BigDecimal costoUnitario) { this.costoUnitario = costoUnitario; }
        public LocalDate getFechaVencimiento() { return fechaVencimiento; }
        public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    }

    public static class CompraRequest {
        @NotNull(message = "idProveedor es requerido")
        private Long idProveedor;
        @Pattern(regexp = "ABIERTA|PAGADA", message = "estado inválido")
        private String estado; // ABIERTA/PAGADA
        @NotEmpty(message = "items no puede estar vacío")
        @Valid
        private List<Item> items;
        public Long getIdProveedor() { return idProveedor; }
        public void setIdProveedor(Long idProveedor) { this.idProveedor = idProveedor; }
        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }
        public List<Item> getItems() { return items; }
        public void setItems(List<Item> items) { this.items = items; }
    }

    public static class PagoRequest {
        @NotNull(message = "idCompra es requerido")
        private Long idCompra;
        @NotBlank(message = "metodo es requerido")
        private String metodo;
        @NotNull(message = "monto es requerido")
        @DecimalMin(value = "0.01", message = "monto debe ser > 0")
        private BigDecimal monto;
        public Long getIdCompra() { return idCompra; }
        public void setIdCompra(Long idCompra) { this.idCompra = idCompra; }
        public String getMetodo() { return metodo; }
        public void setMetodo(String metodo) { this.metodo = metodo; }
        public BigDecimal getMonto() { return monto; }
        public void setMonto(BigDecimal monto) { this.monto = monto; }
    }
}
