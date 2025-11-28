package com.tienda.repository;

import com.tienda.entity.Compra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CompraRepository extends JpaRepository<Compra, Long> {
    List<Compra> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);
}

