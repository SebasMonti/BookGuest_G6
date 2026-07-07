package com.bookguest.repository;

import com.bookguest.domain.Libro;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibroRepository extends JpaRepository<Libro, Long> {

    @EntityGraph(attributePaths = {"autor", "categoria", "editorial"})
    Page<Libro> findByActivoTrue(Pageable pageable);

    @EntityGraph(attributePaths = {"autor", "categoria", "editorial"})
    Page<Libro> findByActivoTrueAndExistencias(int existencias, Pageable pageable);

    @EntityGraph(attributePaths = {"autor", "categoria", "editorial"})
    Page<Libro> findByActivoTrueAndExistenciasBetween(int minimo, int maximo, Pageable pageable);

    @EntityGraph(attributePaths = {"autor", "categoria", "editorial"})
    Page<Libro> findByActivoTrueAndExistenciasGreaterThan(int existencias, Pageable pageable);

    @EntityGraph(attributePaths = {"autor", "categoria", "editorial"})
    List<Libro> findByActivoTrue();

    @EntityGraph(attributePaths = {"autor", "categoria", "editorial"})
    List<Libro> findByActivoTrueAndExistenciasGreaterThanOrderByTituloAsc(int existencias);

    @EntityGraph(attributePaths = {"autor", "categoria", "editorial"})
    List<Libro> findTop8ByActivoTrueAndExistenciasGreaterThanOrderByIdLibroDesc(int existencias);

    @EntityGraph(attributePaths = {"autor", "categoria", "editorial"})
    List<Libro> findTop8ByActivoTrueAndExistenciasGreaterThanOrderByPrecioAsc(int existencias);

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
