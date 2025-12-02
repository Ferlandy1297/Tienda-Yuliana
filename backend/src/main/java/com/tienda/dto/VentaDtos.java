package com.tienda.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public class VentaDtos {
    public static class SaleItem {
        @NotNull(message = "idProducto es requerido")
        private Long idProducto;

        @NotNull(message = "cantidad es requerida")
        @Min(value = 1, message = "cantidad debe ser >= 1")
        private Integer cantidad;
        public Long getIdProducto() { return idProducto; }
        public void setIdProducto(Long idProducto) { this.idProducto = idProducto; }
        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    }

    public static class PagoInfo {
        @NotBlank(message = "metodo es requerido")
        private String metodo; // EFECTIVO, TRANSFERENCIA
        @DecimalMin(value = "0.00", message = "monto debe ser >= 0")
        private BigDecimal monto;
        private BigDecimal denominacion;
        public String getMetodo() { return metodo; }
        public void setMetodo(String metodo) { this.metodo = metodo; }
        public BigDecimal getMonto() { return monto; }
        public void setMonto(BigDecimal monto) { this.monto = monto; }
        public BigDecimal getDenominacion() { return denominacion; }
        public void setDenominacion(BigDecimal denominacion) { this.denominacion = denominacion; }
    }

    public static class VentaRequest {
        @NotBlank(message = "tipo es requerido")
        @Pattern(regexp = "MOSTRADOR|MAYOREO|FIADO", message = "tipo inválido")
        private String tipo; // MOSTRADOR, MAYOREO, FIADO
        private Long idCliente; // opcional
        @NotEmpty(message = "items no puede estar vacío")
        @Valid
        private List<SaleItem> items;
        @Valid
        private PagoInfo pago;
        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
        public Long getIdCliente() { return idCliente; }
        public void setIdCliente(Long idCliente) { this.idCliente = idCliente; }
        public List<SaleItem> getItems() { return items; }
        public void setItems(List<SaleItem> items) { this.items = items; }
        public PagoInfo getPago() { return pago; }
        public void setPago(PagoInfo pago) { this.pago = pago; }
    }

    public static class VentaResponse {
        private Long idVenta;
        private BigDecimal total;
        private BigDecimal cambio;
        public Long getIdVenta() { return idVenta; }
        public void setIdVenta(Long idVenta) { this.idVenta = idVenta; }
        public BigDecimal getTotal() { return total; }
        public void setTotal(BigDecimal total) { this.total = total; }
        public BigDecimal getCambio() { return cambio; }
        public void setCambio(BigDecimal cambio) { this.cambio = cambio; }
    }

    // Ticket de venta para CU-TY-003
    public static class TicketItem {
        private String producto;
        private Integer cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;
        public String getProducto() { return producto; }
        public void setProducto(String producto) { this.producto = producto; }
        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
        public BigDecimal getPrecioUnitario() { return precioUnitario; }
        public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    }

    public static class VentaTicket {
        private Long idVenta;
        private java.time.LocalDateTime fechaHora;
        private String cliente;
        private String atendio;
        private java.util.List<TicketItem> items;
        private BigDecimal total;
        private String metodoPago;
        private BigDecimal montoPagado;
        private BigDecimal cambio;
        public Long getIdVenta() { return idVenta; }
        public void setIdVenta(Long idVenta) { this.idVenta = idVenta; }
        public java.time.LocalDateTime getFechaHora() { return fechaHora; }
        public void setFechaHora(java.time.LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
        public String getCliente() { return cliente; }
        public void setCliente(String cliente) { this.cliente = cliente; }
        public String getAtendio() { return atendio; }
        public void setAtendio(String atendio) { this.atendio = atendio; }
        public java.util.List<TicketItem> getItems() { return items; }
        public void setItems(java.util.List<TicketItem> items) { this.items = items; }
        public BigDecimal getTotal() { return total; }
        public void setTotal(BigDecimal total) { this.total = total; }
        public String getMetodoPago() { return metodoPago; }
        public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
        public BigDecimal getMontoPagado() { return montoPagado; }
        public void setMontoPagado(BigDecimal montoPagado) { this.montoPagado = montoPagado; }
        public BigDecimal getCambio() { return cambio; }
        public void setCambio(BigDecimal cambio) { this.cambio = cambio; }
    }
}
