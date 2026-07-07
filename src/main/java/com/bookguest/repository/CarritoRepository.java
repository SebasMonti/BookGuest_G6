package com.bookguest.repository;

import com.bookguest.domain.Carrito;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    Optional<Carrito> findByUsuarioEmailAndActivoTrue(String email);
}
