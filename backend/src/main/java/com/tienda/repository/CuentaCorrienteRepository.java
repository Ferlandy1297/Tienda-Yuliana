package com.tienda.repository;

import com.tienda.entity.Cliente;
import com.tienda.entity.CuentaCorrienteCliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CuentaCorrienteRepository extends JpaRepository<CuentaCorrienteCliente, Long> {
    Optional<CuentaCorrienteCliente> findByCliente(Cliente cliente);
}

