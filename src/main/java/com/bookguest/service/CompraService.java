package com.bookguest.service;

import com.bookguest.domain.Carrito;
import com.bookguest.domain.CarritoDetalle;
import com.bookguest.domain.EstadoPedido;
import com.bookguest.domain.Libro;
import com.bookguest.domain.MetodoPago;
import com.bookguest.domain.Pedido;
import com.bookguest.domain.PedidoDetalle;
import com.bookguest.domain.Usuario;
import com.bookguest.repository.CarritoDetalleRepository;
import com.bookguest.repository.CarritoRepository;
import com.bookguest.repository.LibroRepository;
import com.bookguest.repository.PedidoDetalleRepository;
import com.bookguest.repository.PedidoRepository;
import com.bookguest.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompraService {

    private final CarritoRepository carritoRepository;
    private final CarritoDetalleRepository carritoDetalleRepository;
    private final LibroRepository libroRepository;
    private final PedidoRepository pedidoRepository;
    private final PedidoDetalleRepository pedidoDetalleRepository;
    private final UsuarioRepository usuarioRepository;
    private final OfertaService ofertaService;

    public CompraService(CarritoRepository carritoRepository,
            CarritoDetalleRepository carritoDetalleRepository,
            LibroRepository libroRepository,
            PedidoRepository pedidoRepository,
            PedidoDetalleRepository pedidoDetalleRepository,
            UsuarioRepository usuarioRepository,
            OfertaService ofertaService) {
        this.carritoRepository = carritoRepository;
        this.carritoDetalleRepository = carritoDetalleRepository;
        this.libroRepository = libroRepository;
        this.pedidoRepository = pedidoRepository;
        this.pedidoDetalleRepository = pedidoDetalleRepository;
        this.usuarioRepository = usuarioRepository;
        this.ofertaService = ofertaService;
    }

    @Transactional
    public CompraVista confirmarCompra(String email, CheckoutForm formulario) {
        Carrito carrito = carritoRepository.findActivoByEmailForUpdate(email)
                .orElseThrow(() -> new IllegalArgumentException("checkout.error.carritoNoExiste"));

        List<CarritoDetalle> detallesCarrito = carritoDetalleRepository
                .findByCarritoOrderByIdCarritoDetalleAsc(carrito);

        if (detallesCarrito.isEmpty()) {
            throw new IllegalArgumentException("checkout.error.carritoVacio");
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("checkout.error.usuarioNoExiste"));
        MetodoPago metodoPago = convertirMetodoPago(formulario.getMetodoPago());
        String direccionEntrega = construirDireccion(formulario);
        List<DetalleCompraTemporal> productos = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CarritoDetalle detalleCarrito : detallesCarrito) {
            Libro libro = libroRepository.findByIdLibroForUpdate(detalleCarrito.getLibro().getIdLibro())
                    .orElseThrow(() -> new IllegalArgumentException("checkout.error.libroNoExiste"));

            if (!libro.isActivo()) {
                throw new IllegalArgumentException("checkout.error.libroInactivo");
            }

            if (detalleCarrito.getCantidad() > libro.getExistencias()) {
                throw new IllegalArgumentException("checkout.error.stockInsuficiente");
            }

            BigDecimal precioUnitario = ofertaService.getPrecioVenta(libro);
            BigDecimal subtotalProducto = precioUnitario
                    .multiply(BigDecimal.valueOf(detalleCarrito.getCantidad()));

            productos.add(new DetalleCompraTemporal(
                    libro,
                    detalleCarrito.getCantidad(),
                    precioUnitario,
                    subtotalProducto
            ));
            subtotal = subtotal.add(subtotalProducto);
        }

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setFecha(LocalDateTime.now());
        pedido.setSubtotal(subtotal);
        pedido.setImpuesto(BigDecimal.ZERO);
        pedido.setTotal(subtotal);
        pedido.setEstado(EstadoPedido.Pendiente);
        pedido.setMetodoPago(metodoPago);
        pedido.setDireccionEnvio(direccionEntrega);
        pedido = pedidoRepository.saveAndFlush(pedido);

        List<PedidoDetalle> detallesPedido = new ArrayList<>();

        for (DetalleCompraTemporal producto : productos) {
            PedidoDetalle detallePedido = new PedidoDetalle();
            detallePedido.setPedido(pedido);
            detallePedido.setLibro(producto.libro());
            detallePedido.setPrecioHistorico(producto.precioUnitario());
            detallePedido.setCantidad(producto.cantidad());
            detallePedido.setSubtotal(producto.subtotal());
            detallesPedido.add(detallePedido);

            producto.libro().setExistencias(
                    producto.libro().getExistencias() - producto.cantidad()
            );
        }

        pedidoDetalleRepository.saveAll(detallesPedido);
        libroRepository.saveAll(productos.stream().map(DetalleCompraTemporal::libro).toList());

        carrito.setActivo(false);
        carritoRepository.save(carrito);

        if (formulario.isGuardarInformacion()) {
            usuario.setDireccion(direccionEntrega);
            usuario.setTelefono(formulario.getTelefono().trim());
            usuarioRepository.save(usuario);
        }

        return construirVista(
                pedido,
                formulario.getNombre().trim(),
                formulario.getTelefono().trim(),
                detallesPedido
        );
    }

    @Transactional(readOnly = true)
    public CompraVista getCompraCliente(String email, Long idPedido) {
        Pedido pedido = pedidoRepository.findByIdPedidoAndUsuarioEmail(idPedido, email)
                .orElseThrow(() -> new IllegalArgumentException("checkout.error.pedidoNoExiste"));
        List<PedidoDetalle> detalles = pedidoDetalleRepository
                .findByPedidoIdPedidoOrderByIdPedidoDetalleAsc(idPedido);
        Usuario usuario = pedido.getUsuario();
        String nombre = (usuario.getNombre() + " " + usuario.getApellidos()).trim();

        return construirVista(pedido, nombre, usuario.getTelefono(), detalles);
    }

    public String formatearNumeroOrden(Long idPedido) {
        long consecutivo = idPedido == null ? 1000 : 1000 + idPedido;
        return "ORD-" + String.format(Locale.ROOT, "%04d", consecutivo);
    }

    private CompraVista construirVista(Pedido pedido,
            String nombreCliente,
            String telefono,
            List<PedidoDetalle> detalles) {
        List<CompraProductoVista> productos = detalles.stream()
                .map(detalle -> new CompraProductoVista(
                detalle.getLibro().getTitulo(),
                detalle.getLibro().getRutaImagen(),
                detalle.getCantidad(),
                detalle.getPrecioHistorico(),
                detalle.getSubtotal()
        ))
                .toList();

        return new CompraVista(
                pedido.getIdPedido(),
                formatearNumeroOrden(pedido.getIdPedido()),
                nombreCliente,
                pedido.getUsuario().getEmail(),
                telefono,
                pedido.getDireccionEnvio(),
                pedido.getMetodoPago(),
                pedido.getFecha(),
                productos,
                pedido.getSubtotal(),
                pedido.getTotal()
        );
    }

    private MetodoPago convertirMetodoPago(String valor) {
        if (valor == null) {
            throw new IllegalArgumentException("checkout.validacion.metodoRequerido");
        }

        return switch (valor.trim().toLowerCase(Locale.ROOT)) {
            case "tarjeta" -> MetodoPago.Tarjeta;
            case "efectivo" -> MetodoPago.Efectivo;
            default -> throw new IllegalArgumentException("checkout.error.metodoInvalido");
        };
    }

    private String construirDireccion(CheckoutForm formulario) {
        List<String> partes = new ArrayList<>();
        partes.add(formulario.getDireccion().trim());

        if (formulario.getDireccionSecundaria() != null
                && !formulario.getDireccionSecundaria().isBlank()) {
            partes.add(formulario.getDireccionSecundaria().trim());
        }

        partes.add(formulario.getCiudad().trim());
        partes.add("Costa Rica");
        String direccion = String.join(", ", partes);

        if (direccion.length() > 255) {
            throw new IllegalArgumentException("checkout.error.direccionCompletaLarga");
        }

        return direccion;
    }

    private record DetalleCompraTemporal(
            Libro libro,
            int cantidad,
            BigDecimal precioUnitario,
            BigDecimal subtotal) {
    }
}
