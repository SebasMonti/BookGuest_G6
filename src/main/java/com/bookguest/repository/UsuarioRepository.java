package com.bookguest.repository;

import com.bookguest.domain.Usuario;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @EntityGraph(attributePaths = {"roles"})
    Optional<Usuario> findByEmail(String email);

    @EntityGraph(attributePaths = {"roles"})
    Optional<Usuario> findByIdUsuario(Long idUsuario);

    @Query(value = """
            SELECT u
            FROM Usuario u
            WHERE (
                :busqueda IS NULL
                OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                OR LOWER(u.apellidos) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                OR LOWER(u.email) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                OR LOWER(COALESCE(u.telefono, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
            )
            AND (:activo IS NULL OR u.activo = :activo)
            AND (
                :rol IS NULL
                OR EXISTS (
                    SELECT r
                    FROM u.roles r
                    WHERE r.nombre = :rol
                )
            )
            """,
            countQuery = """
            SELECT COUNT(u)
            FROM Usuario u
            WHERE (
                :busqueda IS NULL
                OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                OR LOWER(u.apellidos) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                OR LOWER(u.email) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                OR LOWER(COALESCE(u.telefono, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
            )
            AND (:activo IS NULL OR u.activo = :activo)
            AND (
                :rol IS NULL
                OR EXISTS (
                    SELECT r
                    FROM u.roles r
                    WHERE r.nombre = :rol
                )
            )
            """)
    Page<Usuario> buscarParaAdministracion(@Param("busqueda") String busqueda,
            @Param("activo") Boolean activo,
            @Param("rol") String rol,
            Pageable pageable);

    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdUsuarioNot(String email, Long idUsuario);

    long countByActivoTrue();

    long countByFechaCreacionAfter(java.time.LocalDateTime fecha);

    long countByRolesNombre(String nombre);

    long countByIdUsuarioLessThan(Long idUsuario);
}
