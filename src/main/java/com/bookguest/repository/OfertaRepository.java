package com.bookguest.repository;

import com.bookguest.domain.Oferta;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OfertaRepository extends JpaRepository<Oferta, Long> {

    @EntityGraph(attributePaths = {"libro", "libro.autor", "libro.categoria", "libro.editorial"})
    Optional<Oferta> findFirstByLibroIdLibroOrderByIdOfertaDesc(Long idLibro);

    @EntityGraph(attributePaths = {"libro", "libro.autor", "libro.categoria", "libro.editorial"})
    List<Oferta> findByLibroIdLibroInOrderByIdOfertaDesc(Collection<Long> idsLibro);

    @EntityGraph(attributePaths = {"libro", "libro.autor", "libro.categoria", "libro.editorial"})
    @Query("""
            SELECT o
            FROM Oferta o
            WHERE o.activo = true
              AND o.fechaInicio <= :ahora
              AND o.fechaFin >= :ahora
              AND o.libro.activo = true
              AND o.libro.existencias > 0
            ORDER BY o.fechaFin ASC, o.idOferta DESC
            """)
    List<Oferta> buscarOfertasVigentes(@Param("ahora") LocalDate ahora);

    @EntityGraph(attributePaths = {"libro", "libro.autor", "libro.categoria", "libro.editorial"})
    @Query("""
            SELECT o
            FROM Oferta o
            WHERE o.activo = true
              AND o.fechaInicio <= :ahora
              AND o.fechaFin >= :ahora
              AND o.libro.idLibro IN :idsLibro
            ORDER BY o.fechaFin ASC, o.idOferta DESC
            """)
    List<Oferta> buscarOfertasVigentesPorLibros(
            @Param("idsLibro") Collection<Long> idsLibro,
            @Param("ahora") LocalDate ahora);

    @EntityGraph(attributePaths = {"libro", "libro.autor", "libro.categoria", "libro.editorial"})
    @Query("""
            SELECT o
            FROM Oferta o
            WHERE o.activo = true
              AND o.fechaInicio <= :ahora
              AND o.fechaFin >= :ahora
              AND o.libro.idLibro = :idLibro
            ORDER BY o.fechaFin ASC, o.idOferta DESC
            """)
    List<Oferta> buscarOfertaVigentePorLibro(
            @Param("idLibro") Long idLibro,
            @Param("ahora") LocalDate ahora);
}
