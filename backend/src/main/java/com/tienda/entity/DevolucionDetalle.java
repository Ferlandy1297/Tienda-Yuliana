package com.tienda.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "devoluciones_detalle")
public class DevolucionDetalle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_devolucion")
    private DevolucionProveedor devolucion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_producto")
    private Producto producto;

    private Integer cantidad;

    @ManyToOne
    @JoinColumn(name = "id_lote")
    private LoteProducto lote;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public DevolucionProveedor getDevolucion() { return devolucion; }
    public void setDevolucion(DevolucionProveedor devolucion) { this.devolucion = devolucion; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public LoteProducto getLote() { return lote; }
    public void setLote(LoteProducto lote) { this.lote = lote; }
}

