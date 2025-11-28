package com.tienda.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ProductoDTO {
    private Long id;

    @NotBlank(message = "nombre es requerido")
    private String nombre;

    @NotBlank(message = "codigoBarras es requerido")
    private String codigoBarras;

    @NotNull(message = "precioUnitario es requerido")
    @DecimalMin(value = "0.01", message = "precioUnitario debe ser > 0")
    private BigDecimal precioUnitario;

    @NotNull(message = "stockActual es requerido")
    @Min(value = 0, message = "stockActual no puede ser negativo")
    private Integer stockActual;

    @NotNull(message = "stockMinimo es requerido")
    @Min(value = 0, message = "stockMinimo no puede ser negativo")
    private Integer stockMinimo;

    private Long idProveedor;
    private boolean activo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    public Integer getStockActual() { return stockActual; }
    public void setStockActual(Integer stockActual) { this.stockActual = stockActual; }
    public Integer getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(Integer stockMinimo) { this.stockMinimo = stockMinimo; }
    public Long getIdProveedor() { return idProveedor; }
    public void setIdProveedor(Long idProveedor) { this.idProveedor = idProveedor; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
