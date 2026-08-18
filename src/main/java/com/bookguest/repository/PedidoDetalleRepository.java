package com.bookguest.repository;

import com.bookguest.domain.PedidoDetalle;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoDetalleRepository extends JpaRepository<PedidoDetalle, Long> {

    @EntityGraph(attributePaths = {"libro"})
    List<PedidoDetalle> findByPedidoIdPedidoInOrderByIdPedidoDetalleAsc(Collection<Long> idsPedido);

    @EntityGraph(attributePaths = {"libro"})
    List<PedidoDetalle> findByPedidoIdPedidoOrderByIdPedidoDetalleAsc(Long idPedido);
}
