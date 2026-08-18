package com.bookguest.service;

import com.bookguest.domain.EstadoPedido;
import com.bookguest.domain.Libro;
import com.bookguest.domain.Pedido;
import com.bookguest.domain.PedidoDetalle;
import com.bookguest.domain.Usuario;
import com.bookguest.repository.PedidoDetalleRepository;
import com.bookguest.repository.PedidoRepository;
import com.bookguest.repository.LibroRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoService {

    private static final int PEDIDOS_POR_PAGINA = 9;

    private final PedidoRepository pedidoRepository;
    private final PedidoDetalleRepository pedidoDetalleRepository;
    private final LibroRepository libroRepository;

    public PedidoService(PedidoRepository pedidoRepository,
            PedidoDetalleRepository pedidoDetalleRepository,
            LibroRepository libroRepository) {
        this.pedidoRepository = pedidoRepository;
        this.pedidoDetalleRepository = pedidoDetalleRepository;
        this.libroRepository = libroRepository;
    }

    @Transactional(readOnly = true)
    public Page<PedidoAdminVista> getPedidosAdministracion(int page,
            String busqueda,
            String estado) {

        int pagina = Math.max(page, 0);
        String busquedaNormalizada = normalizarFiltro(busqueda);
        Long idPedido = extraerIdPedido(busquedaNormalizada);
        List<EstadoPedido> estados = convertirFiltroEstado(estado);

        Page<Pedido> paginaPedidos = pedidoRepository.buscarParaAdministracion(
                busquedaNormalizada,
                idPedido,
                estados,
                PageRequest.of(pagina, PEDIDOS_POR_PAGINA, Sort.by("idPedido").ascending())
        );

        List<PedidoAdminVista> pedidos = convertirPedidos(paginaPedidos.getContent());

        return new PageImpl<>(pedidos, paginaPedidos.getPageable(), paginaPedidos.getTotalElements());
    }

    @Transactional(readOnly = true)
    public PedidoAdminVista getPedidoAdministracion(Long idPedido) {
        if (idPedido == null) {
            return null;
        }

        Pedido pedido = pedidoRepository.findByIdPedido(idPedido).orElse(null);

        if (pedido == null) {
            return null;
        }

        List<PedidoDetalle> detalles = pedidoDetalleRepository
                .findByPedidoIdPedidoOrderByIdPedidoDetalleAsc(idPedido);

        return convertirPedido(pedido, detalles);
    }

    @Transactional
    public PedidoAdminVista actualizarEstado(Long idPedido, String nuevoEstado) {
        Pedido pedido = pedidoRepository.findByIdPedidoForUpdate(idPedido)
                .orElseThrow(() -> new IllegalArgumentException("pedidos.error.noExiste"));

        String codigoActual = convertirCodigoEstado(pedido.getEstado());
        String codigoNuevo = normalizarCodigoEstado(nuevoEstado);

        if (codigoNuevo == null) {
            throw new IllegalArgumentException("pedidos.error.estadoRequerido");
        }

        if (codigoActual.equals(codigoNuevo)) {
            throw new IllegalArgumentException("pedidos.error.estadoIgual");
        }

        EstadoPedido estadoActual = pedido.getEstado();
        EstadoPedido estadoNuevo = convertirEstadoInterno(codigoNuevo);
        List<PedidoDetalle> detalles = pedidoDetalleRepository
                .findByPedidoIdPedidoOrderByIdPedidoDetalleAsc(idPedido);

        if (estadoActual != EstadoPedido.Cancelado && estadoNuevo == EstadoPedido.Cancelado) {
            ajustarExistencias(detalles, true);
        } else if (estadoActual == EstadoPedido.Cancelado && estadoNuevo != EstadoPedido.Cancelado) {
            ajustarExistencias(detalles, false);
        }

        pedido.setEstado(estadoNuevo);
        pedidoRepository.saveAndFlush(pedido);

        return convertirPedido(pedido, detalles);
    }

    private void ajustarExistencias(List<PedidoDetalle> detalles, boolean devolver) {
        Map<Long, Integer> cantidadesPorLibro = new TreeMap<>();

        detalles.forEach(detalle -> cantidadesPorLibro.merge(
                detalle.getLibro().getIdLibro(),
                detalle.getCantidad(),
                Integer::sum
        ));

        for (Map.Entry<Long, Integer> entrada : cantidadesPorLibro.entrySet()) {
            Libro libro = libroRepository.findByIdLibroForUpdate(entrada.getKey())
                    .orElseThrow(() -> new IllegalArgumentException(
                    "pedidos.error.productoNoExiste"
            ));
            int cantidad = entrada.getValue();

            if (devolver) {
                long nuevasExistencias = (long) libro.getExistencias() + cantidad;

                if (nuevasExistencias > Integer.MAX_VALUE) {
                    throw new IllegalArgumentException(
                            "pedidos.error.devolucionStock"
                    );
                }

                libro.setExistencias((int) nuevasExistencias);
            } else {
                if (libro.getExistencias() < cantidad) {
                    throw new IllegalArgumentException(
                            "pedidos.error.reactivacionStock"
                    );
                }

                libro.setExistencias(libro.getExistencias() - cantidad);
            }

            libroRepository.save(libro);
        }
    }

    public long getTotalPedidos() {
        return pedidoRepository.count();
    }

    public long getPedidosRecibidosUltimosSieteDias() {
        return pedidoRepository.countByFechaAfter(LocalDateTime.now().minusDays(7));
    }

    public long getPedidosEntregadosUltimosSieteDias() {
        return pedidoRepository.countByEstadoAndFechaModificacionAfter(
                EstadoPedido.Entregado,
                LocalDateTime.now().minusDays(7)
        );
    }

    public long getPedidosEnEntrega() {
        return pedidoRepository.countByEstado(EstadoPedido.Enviado);
    }

    public int getPaginaDePedido(Long idPedido) {
        if (idPedido == null) {
            return 0;
        }

        return (int) (pedidoRepository.countByIdPedidoLessThan(idPedido) / PEDIDOS_POR_PAGINA);
    }

    private List<PedidoAdminVista> convertirPedidos(List<Pedido> pedidos) {
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

    private PedidoAdminVista convertirPedido(Pedido pedido, List<PedidoDetalle> detalles) {
        Usuario usuario = pedido.getUsuario();
        String nombreCliente = usuario == null
                ? "-"
                : (usuario.getNombre() + " " + usuario.getApellidos()).trim();

        String emailCliente = usuario == null ? "-" : usuario.getEmail();
        Long idCliente = usuario == null ? null : usuario.getIdUsuario();

        int cantidad = detalles.stream().mapToInt(PedidoDetalle::getCantidad).sum();
        BigDecimal precioUnitario = detalles.isEmpty()
                ? BigDecimal.ZERO
                : detalles.get(0).getPrecioHistorico();

        List<PedidoProductoVista> productos = detalles.stream()
                .map(detalle -> new PedidoProductoVista(
                detalle.getLibro().getTitulo(),
                detalle.getCantidad()
        ))
                .toList();

        String libros = productos.isEmpty()
                ? "-"
                : productos.stream()
                        .map(PedidoProductoVista::getTitulo)
                        .collect(Collectors.joining(", "));

        String estadoCodigo = convertirCodigoEstado(pedido.getEstado());

        return new PedidoAdminVista(
                pedido.getIdPedido(),
                formatearNumeroOrden(pedido.getIdPedido()),
                idCliente,
                nombreCliente,
                emailCliente,
                libros,
                productos,
                productos.size(),
                precioUnitario,
                detalles.size() > 1,
                cantidad,
                pedido.getTotal(),
                pedido.getFecha() == null ? null : pedido.getFecha().toLocalDate().plusDays(3),
                convertirEtiquetaEstado(estadoCodigo),
                estadoCodigo
        );
    }

    private String formatearNumeroOrden(Long idPedido) {
        long consecutivo = idPedido == null ? 1000 : 1000 + idPedido;
        return "ORD-" + String.format(Locale.ROOT, "%04d", consecutivo);
    }

    private String normalizarFiltro(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private Long extraerIdPedido(String busqueda) {
        if (busqueda == null) {
            return null;
        }

        String numero = busqueda.trim().toUpperCase(Locale.ROOT);

        if (numero.startsWith("ORD-")) {
            numero = numero.substring(4);
        }

        try {
            long valor = Long.parseLong(numero);
            return valor >= 1001 ? valor - 1000 : valor;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private List<EstadoPedido> convertirFiltroEstado(String estado) {
        String codigo = normalizarCodigoEstado(estado);

        if (codigo == null) {
            return Arrays.asList(EstadoPedido.values());
        }

        return switch (codigo) {
            case "abierto" -> List.of(EstadoPedido.Pendiente, EstadoPedido.Pagado);
            case "en-entrega" -> List.of(EstadoPedido.Enviado);
            case "entregado" -> List.of(EstadoPedido.Entregado);
            case "cancelado" -> List.of(EstadoPedido.Cancelado);
            default -> Arrays.asList(EstadoPedido.values());
        };
    }

    private EstadoPedido convertirEstadoInterno(String codigo) {
        return switch (codigo) {
            case "abierto" -> EstadoPedido.Pagado;
            case "en-entrega" -> EstadoPedido.Enviado;
            case "entregado" -> EstadoPedido.Entregado;
            case "cancelado" -> EstadoPedido.Cancelado;
            default -> throw new IllegalArgumentException("pedidos.error.estadoInvalido");
        };
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
        return switch (codigo) {
            case "en-entrega" -> "pedidos.estado.enEntrega";
            case "entregado" -> "pedidos.estado.entregado";
            case "cancelado" -> "pedidos.estado.cancelado";
            default -> "pedidos.estado.abierto";
        };
    }

    private String normalizarCodigoEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            return null;
        }

        String codigo = estado.trim()
                .toLowerCase(Locale.ROOT)
                .replace('_', '-')
                .replace(' ', '-');

        return switch (codigo) {
            case "abierto", "pendiente", "pagado" -> "abierto";
            case "en-entrega", "enviado" -> "en-entrega";
            case "entregado" -> "entregado";
            case "cancelado" -> "cancelado";
            default -> null;
        };
    }
}
