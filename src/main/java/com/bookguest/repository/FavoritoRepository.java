package com.bookguest.repository;

import com.bookguest.domain.Favorito;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FavoritoRepository extends JpaRepository<Favorito, Long> {

    @EntityGraph(attributePaths = {
        "libro",
        "libro.autor",
        "libro.categoria",
        "libro.editorial"
    })
    List<Favorito> findByUsuarioEmailAndLibroActivoTrueOrderByFechaCreacionDesc(String email);

    Optional<Favorito> findByUsuarioIdUsuarioAndLibroIdLibro(Long idUsuario, Long idLibro);

    @Query("""
            SELECT f.libro.idLibro
            FROM Favorito f
            WHERE f.usuario.email = :email
              AND f.libro.activo = true
            """)
    Set<Long> buscarIdsLibrosPorUsuario(@Param("email") String email);
}
