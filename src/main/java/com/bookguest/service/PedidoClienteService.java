package com.bookguest.service;

import com.bookguest.domain.EstadoPedido;
import com.bookguest.domain.MetodoPago;
import com.bookguest.domain.Pedido;
import com.bookguest.domain.PedidoDetalle;
import com.bookguest.repository.PedidoDetalleRepository;
import com.bookguest.repository.PedidoRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoClienteService {

    private final PedidoRepository pedidoRepository;
    private final PedidoDetalleRepository pedidoDetalleRepository;
    private final MessageSource messageSource;

    public PedidoClienteService(PedidoRepository pedidoRepository,
            PedidoDetalleRepository pedidoDetalleRepository,
            MessageSource messageSource) {
        this.pedidoRepository = pedidoRepository;
        this.pedidoDetalleRepository = pedidoDetalleRepository;
        this.messageSource = messageSource;
    }

    @Transactional(readOnly = true)
    public List<PedidoClienteVista> getPedidosCliente(String email) {
        List<Pedido> pedidos = pedidoRepository
                .findByUsuarioEmailOrderByFechaDescIdPedidoDesc(email);

        if (pedidos.isEmpty()) {
            return List.of();
        }

        List<Long> ids = pedidos.stream().map(Pedido::getIdPedido).toList();
        Map<Long, List<PedidoDetalle>> detallesPorPedido = new LinkedHashMap<>();

        pedidoDetalleRepository.findByPedidoIdPedidoInOrderByIdPedidoDetalleAsc(ids)
                .forEach(detalle -> detallesPorPedido
                .computeIfAbsent(detalle.getPedido().getIdPedido(), clave -> new ArrayList<>())
                .add(detalle));

        return pedidos.stream()
                .map(pedido -> convertirPedido(
                pedido,
                detallesPorPedido.getOrDefault(pedido.getIdPedido(), List.of())
        ))
                .toList();
    }

    private PedidoClienteVista convertirPedido(Pedido pedido, List<PedidoDetalle> detalles) {
        List<CompraProductoVista> productos = detalles.stream()
                .map(detalle -> new CompraProductoVista(
                detalle.getLibro().getTitulo(),
                detalle.getLibro().getRutaImagen(),
                detalle.getCantidad(),
                detalle.getPrecioHistorico(),
                detalle.getSubtotal()
        ))
                .toList();

        int cantidadTotal = detalles.stream()
                .mapToInt(PedidoDetalle::getCantidad)
                .sum();
        String estadoCodigo = convertirCodigoEstado(pedido.getEstado());

        return new PedidoClienteVista(
                pedido.getIdPedido(),
                formatearNumeroOrden(pedido.getIdPedido()),
                pedido.getFecha(),
                productos,
                cantidadTotal,
                pedido.getTotal(),
                convertirMetodoPago(pedido.getMetodoPago()),
                pedido.getDireccionEnvio(),
                convertirEtiquetaEstado(estadoCodigo),
                estadoCodigo
        );
    }

    private String formatearNumeroOrden(Long idPedido) {
        long consecutivo = idPedido == null ? 1000 : 1000 + idPedido;
        return "ORD-" + String.format(Locale.ROOT, "%04d", consecutivo);
    }

    private String convertirCodigoEstado(EstadoPedido estado) {
        if (estado == null) {
            return "abierto";
        }

        return switch (estado) {
            case Pendiente, Pagado -> "abierto";
            case Enviado -> "en-entrega";
            case Entregado -> "entregado";
            case Cancelado -> "cancelado";
        };
    }

    private String convertirEtiquetaEstado(String codigo) {
        String clave = switch (codigo) {
            case "en-entrega" -> "pedidos.estado.enEntrega";
            case "entregado" -> "pedidos.estado.entregado";
            case "cancelado" -> "pedidos.estado.cancelado";
            default -> "pedidos.estado.abierto";
        };
        return traducir(clave);
    }

    private String convertirMetodoPago(MetodoPago metodoPago) {
        if (metodoPago == null) {
            return "-";
        }

        String clave = switch (metodoPago) {
            case Tarjeta -> "cliente.pedidos.pago.tarjeta";
            case Efectivo -> "cliente.pedidos.pago.efectivo";
            case Sinpe -> "cliente.pedidos.pago.sinpe";
            case Transferencia -> "cliente.pedidos.pago.transferencia";
        };
        return traducir(clave);
    }

    private String traducir(String clave) {
        return messageSource.getMessage(clave, null, LocaleContextHolder.getLocale());
    }
}
