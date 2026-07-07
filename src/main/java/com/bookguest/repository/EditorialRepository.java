package com.bookguest.repository;

import com.bookguest.domain.Editorial;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EditorialRepository extends JpaRepository<Editorial, Long> {

    Optional<Editorial> findByNombreIgnoreCase(String nombre);
}
