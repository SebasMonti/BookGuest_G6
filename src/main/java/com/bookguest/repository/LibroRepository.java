package com.bookguest.repository;

import com.bookguest.domain.Libro;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LibroRepository extends JpaRepository<Libro, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Libro l WHERE l.idLibro = :idLibro")
    Optional<Libro> findByIdLibroForUpdate(@Param("idLibro") Long idLibro);

    @EntityGraph(attributePaths = {"autor", "categoria", "editorial"})
    Page<Libro> findByActivoTrue(Pageable pageable);

    @EntityGraph(attributePaths = {"autor", "categoria", "editorial"})
    @Query(value = """
            SELECT l
            FROM Libro l
            WHERE l.activo = true
              AND (
                  :busqueda IS NULL
                  OR LOWER(l.titulo) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                  OR LOWER(l.isbn) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                  OR LOWER(l.autor.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                  OR LOWER(l.categoria.descripcion) LIKE LOWER(CONCAT('%', :busqueda, '%'))
              )
            """,
            countQuery = """
            SELECT COUNT(l)
            FROM Libro l
            WHERE l.activo = true
              AND (
                  :busqueda IS NULL
                  OR LOWER(l.titulo) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                  OR LOWER(l.isbn) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                  OR LOWER(l.autor.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                  OR LOWER(l.categoria.descripcion) LIKE LOWER(CONCAT('%', :busqueda, '%'))
              )
            """)
    Page<Libro> buscarProductosAdministracion(
            @Param("busqueda") String busqueda,
            Pageable pageable);

    @EntityGraph(attributePaths = {"autor", "categoria", "editorial"})
    Page<Libro> findByActivoTrueAndExistencias(int existencias, Pageable pageable);

    @EntityGraph(attributePaths = {"autor", "categoria", "editorial"})
    Page<Libro> findByActivoTrueAndExistenciasBetween(int minimo, int maximo, Pageable pageable);

    @EntityGraph(attributePaths = {"autor", "categoria", "editorial"})
    Page<Libro> findByActivoTrueAndExistenciasGreaterThan(int existencias, Pageable pageable);

    @EntityGraph(attributePaths = {"autor", "categoria", "editorial"})
    List<Libro> findByActivoTrue();

    @EntityGraph(attributePaths = {"autor", "categoria", "editorial"})
    Optional<Libro> findByIdLibroAndActivoTrue(Long idLibro);

    @EntityGraph(attributePaths = {"autor", "categoria", "editorial"})
    List<Libro> findTop5ByActivoTrueOrderByIdLibroDesc();

    @EntityGraph(attributePaths = {"autor", "categoria", "editorial"})
    List<Libro> findTop5ByActivoTrueAndExistenciasBetweenOrderByExistenciasAsc(int minimo, int maximo);

    @EntityGraph(attributePaths = {"autor", "categoria", "editorial"})
    List<Libro> findTop5ByActivoTrueAndExistenciasOrderByTituloAsc(int existencias);

    long countByActivoTrue();

    long countByActivoTrueAndExistencias(int existencias);

    long countByActivoTrueAndExistenciasBetween(int minimo, int maximo);

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndIdLibroNot(String isbn, Long idLibro);
}
