package com.bookguest.repository;

import com.bookguest.domain.Autor;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AutorRepository extends JpaRepository<Autor, Long> {

    Optional<Autor> findByNombreIgnoreCase(String nombre);
}
