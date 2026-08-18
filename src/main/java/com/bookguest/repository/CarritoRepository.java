package com.bookguest.repository;

import com.bookguest.domain.Carrito;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    Optional<Carrito> findByUsuarioEmailAndActivoTrue(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Carrito c WHERE c.usuario.email = :email AND c.activo = true")
    Optional<Carrito> findActivoByEmailForUpdate(@Param("email") String email);
}
