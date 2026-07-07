package com.bookguest.repository;

import com.bookguest.domain.Categoria;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findByDescripcionIgnoreCase(String descripcion);

    List<Categoria> findByActivoTrueOrderByDescripcionAsc();
}
