package com.bookguest.repository;

import com.bookguest.domain.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @EntityGraph(attributePaths = {"roles"})
    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);
}