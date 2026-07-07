package com.bookguest.repository;

import com.bookguest.domain.Carrito;
import com.bookguest.domain.CarritoDetalle;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarritoDetalleRepository extends JpaRepository<CarritoDetalle, Long> {

    @EntityGraph(attributePaths = {"libro", "libro.autor", "libro.categoria", "libro.editorial"})
    List<CarritoDetalle> findByCarritoOrderByIdCarritoDetalleAsc(Carrito carrito);

    Optional<CarritoDetalle> findByCarritoAndLibroIdLibro(Carrito carrito, Long idLibro);

    void deleteByCarritoAndLibroIdLibro(Carrito carrito, Long idLibro);
}
