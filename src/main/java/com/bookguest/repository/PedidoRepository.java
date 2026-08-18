package com.bookguest.repository;

import com.bookguest.domain.EstadoPedido;
import com.bookguest.domain.Pedido;
import java.time.LocalDateTime;
import java.util.Collection;
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

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @EntityGraph(attributePaths = {"usuario"})
    @Query(value = """
            SELECT p
            FROM Pedido p
            WHERE (
                :busqueda IS NULL
                OR (:idPedido IS NOT NULL AND p.idPedido = :idPedido)
                OR LOWER(p.usuario.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                OR LOWER(p.usuario.apellidos) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                OR LOWER(p.usuario.email) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                OR EXISTS (
                    SELECT pd.idPedidoDetalle
                    FROM PedidoDetalle pd
                    WHERE pd.pedido = p
                    AND LOWER(pd.libro.titulo) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                )
            )
            AND p.estado IN :estados
            """,
            countQuery = """
            SELECT COUNT(p)
            FROM Pedido p
            WHERE (
                :busqueda IS NULL
                OR (:idPedido IS NOT NULL AND p.idPedido = :idPedido)
                OR LOWER(p.usuario.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                OR LOWER(p.usuario.apellidos) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                OR LOWER(p.usuario.email) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                OR EXISTS (
                    SELECT pd.idPedidoDetalle
                    FROM PedidoDetalle pd
                    WHERE pd.pedido = p
                    AND LOWER(pd.libro.titulo) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                )
            )
            AND p.estado IN :estados
            """)
    Page<Pedido> buscarParaAdministracion(@Param("busqueda") String busqueda,
            @Param("idPedido") Long idPedido,
            @Param("estados") Collection<EstadoPedido> estados,
            Pageable pageable);

    @EntityGraph(attributePaths = {"usuario"})
    Optional<Pedido> findByIdPedido(Long idPedido);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Pedido p WHERE p.idPedido = :idPedido")
    Optional<Pedido> findByIdPedidoForUpdate(@Param("idPedido") Long idPedido);

    @EntityGraph(attributePaths = {"usuario"})
    Optional<Pedido> findByIdPedidoAndUsuarioEmail(Long idPedido, String email);

    @EntityGraph(attributePaths = {"usuario"})
    List<Pedido> findByUsuarioEmailOrderByFechaDescIdPedidoDesc(String email);

    @Query("""
            SELECT p.usuario.idUsuario AS idUsuario,
                   COUNT(p) AS totalCompras
            FROM Pedido p
            WHERE p.usuario.idUsuario IN :idsUsuarios
              AND p.estado <> :estadoCancelado
            GROUP BY p.usuario.idUsuario
            """)
    List<UsuarioTotalCompras> contarComprasNoCanceladasPorUsuarios(
            @Param("idsUsuarios") Collection<Long> idsUsuarios,
            @Param("estadoCancelado") EstadoPedido estadoCancelado);

    long countByFechaAfter(LocalDateTime fecha);

    long countByEstado(EstadoPedido estado);

    long countByEstadoAndFechaModificacionAfter(EstadoPedido estado, LocalDateTime fecha);

    long countByIdPedidoLessThan(Long idPedido);
}
