package com.bookguest.service;

import com.bookguest.domain.Carrito;
import com.bookguest.domain.CarritoDetalle;
import com.bookguest.domain.Libro;
import com.bookguest.domain.Usuario;
import com.bookguest.repository.CarritoDetalleRepository;
import com.bookguest.repository.CarritoRepository;
import com.bookguest.repository.LibroRepository;
import com.bookguest.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final CarritoDetalleRepository carritoDetalleRepository;
    private final UsuarioRepository usuarioRepository;
    private final LibroRepository libroRepository;
    private final OfertaService ofertaService;

    public CarritoService(CarritoRepository carritoRepository,
            CarritoDetalleRepository carritoDetalleRepository,
            UsuarioRepository usuarioRepository,
            LibroRepository libroRepository,
            OfertaService ofertaService) {
        this.carritoRepository = carritoRepository;
        this.carritoDetalleRepository = carritoDetalleRepository;
        this.usuarioRepository = usuarioRepository;
        this.libroRepository = libroRepository;
        this.ofertaService = ofertaService;
    }

    private Carrito getCarritoActivo(String email) {
        return carritoRepository.findByUsuarioEmailAndActivoTrue(email)
                .orElseGet(() -> crearCarrito(email));
    }

    @Transactional
    public List<CarritoDetalle> getDetallesCarrito(String email) {
        Carrito carrito = getCarritoActivo(email);
        List<CarritoDetalle> detalles = carritoDetalleRepository
                .findByCarritoOrderByIdCarritoDetalleAsc(carrito);
        boolean precioActualizado = false;

        for (CarritoDetalle detalle : detalles) {
            BigDecimal precioVenta = ofertaService.getPrecioVenta(detalle.getLibro());

            if (precioVenta != null
                    && (detalle.getPrecioUnitario() == null
                    || detalle.getPrecioUnitario().compareTo(precioVenta) != 0)) {
                detalle.setPrecioUnitario(precioVenta);
                precioActualizado = true;
            }
        }

        if (precioActualizado) {
            carritoDetalleRepository.saveAll(detalles);
        }

        return detalles;
    }

    public BigDecimal getSubtotalCarrito(String email) {
        return getDetallesCarrito(email)
                .stream()
                .map(CarritoDetalle::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalCarrito(String email) {
        return getSubtotalCarrito(email);
    }

    @Transactional
    public void agregarLibro(String email, Long idLibro, int cantidad) {
        Carrito carrito = getCarritoActivo(email);
        Libro libro = obtenerLibroValido(idLibro);

        int cantidadSolicitada = Math.max(cantidad, 1);

        CarritoDetalle detalle = carritoDetalleRepository
                .findByCarritoAndLibroIdLibro(carrito, idLibro)
                .orElse(null);
        BigDecimal precioVenta = ofertaService.getPrecioVenta(libro);

        if (detalle == null) {
            validarCantidadDisponible(libro, cantidadSolicitada);

            detalle = new CarritoDetalle();
            detalle.setCarrito(carrito);
            detalle.setLibro(libro);
            detalle.setCantidad(cantidadSolicitada);
            detalle.setPrecioUnitario(precioVenta);
        } else {
            int nuevaCantidad = detalle.getCantidad() + cantidadSolicitada;
            validarCantidadDisponible(libro, nuevaCantidad);
            detalle.setCantidad(nuevaCantidad);
            detalle.setPrecioUnitario(precioVenta);
        }

        carritoDetalleRepository.save(detalle);
    }

    @Transactional
    public void actualizarCantidad(String email, Long idLibro, int cantidad) {
        Carrito carrito = getCarritoActivo(email);

        if (cantidad <= 0) {
            carritoDetalleRepository.deleteByCarritoAndLibroIdLibro(carrito, idLibro);
            return;
        }

        Libro libro = obtenerLibroValido(idLibro);
        validarCantidadDisponible(libro, cantidad);
        BigDecimal precioVenta = ofertaService.getPrecioVenta(libro);

        CarritoDetalle detalle = carritoDetalleRepository
                .findByCarritoAndLibroIdLibro(carrito, idLibro)
                .orElseThrow(() -> new IllegalArgumentException("carrito.error.libroNoCarrito"));

        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(precioVenta);
        carritoDetalleRepository.save(detalle);
    }

    @Transactional
    public void eliminarLibro(String email, Long idLibro) {
        Carrito carrito = getCarritoActivo(email);
        carritoDetalleRepository.deleteByCarritoAndLibroIdLibro(carrito, idLibro);
    }

    private Carrito crearCarrito(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("carrito.error.usuarioNoExiste"));

        Carrito carrito = new Carrito();
        carrito.setUsuario(usuario);
        carrito.setActivo(true);

        return carritoRepository.save(carrito);
    }

    private Libro obtenerLibroValido(Long idLibro) {
        Libro libro = libroRepository.findById(idLibro)
                .orElseThrow(() -> new IllegalArgumentException("carrito.error.libroNoExiste"));

        if (!libro.isActivo()) {
            throw new IllegalArgumentException("carrito.error.libroInactivo");
        }

        if (libro.getExistencias() <= 0) {
            throw new IllegalArgumentException("carrito.error.sinExistencias");
        }

        return libro;
    }

    private void validarCantidadDisponible(Libro libro, int cantidad) {
        if (cantidad < 1) {
            throw new IllegalArgumentException("carrito.error.cantidadPositiva");
        }

        if (cantidad > libro.getExistencias()) {
            throw new IllegalArgumentException("carrito.error.stockInsuficiente");
        }
    }
}
