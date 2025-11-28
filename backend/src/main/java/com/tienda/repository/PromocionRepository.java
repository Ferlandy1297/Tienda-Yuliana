package com.tienda.repository;

import com.tienda.entity.Producto;
import com.tienda.entity.Promocion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PromocionRepository extends JpaRepository<Promocion, Long> {
    List<Promocion> findByProductoAndActivaIsTrueAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
            Producto producto, LocalDate inicio, LocalDate fin);
}

