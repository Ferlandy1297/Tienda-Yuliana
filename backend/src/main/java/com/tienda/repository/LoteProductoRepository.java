package com.tienda.repository;

import com.tienda.entity.LoteProducto;
import com.tienda.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LoteProductoRepository extends JpaRepository<LoteProducto, Long> {
    List<LoteProducto> findByProductoOrderByFechaVencimientoAsc(Producto producto);
    List<LoteProducto> findByFechaVencimientoBetween(LocalDate inicio, LocalDate fin);
}

