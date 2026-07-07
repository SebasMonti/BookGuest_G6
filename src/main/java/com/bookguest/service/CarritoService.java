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

    public CarritoService(CarritoRepository carritoRepository,
            CarritoDetalleRepository carritoDetalleRepository,
            UsuarioRepository usuarioRepository,
            LibroRepository libroRepository) {
        this.carritoRepository = carritoRepository;
        this.carritoDetalleRepository = carritoDetalleRepository;
        this.usuarioRepository = usuarioRepository;
        this.libroRepository = libroRepository;
    }

    @Transactional
    public Carrito getCarritoActivo(String email) {
        return carritoRepository.findByUsuarioEmailAndActivoTrue(email)
                .orElseGet(() -> crearCarrito(email));
    }

    public List<CarritoDetalle> getDetallesCarrito(String email) {
        Carrito carrito = getCarritoActivo(email);
        return carritoDetalleRepository.findByCarritoOrderByIdCarritoDetalleAsc(carrito);
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

        if (detalle == null) {
            validarCantidadDisponible(libro, cantidadSolicitada);

            detalle = new CarritoDetalle();
            detalle.setCarrito(carrito);
            detalle.setLibro(libro);
            detalle.setCantidad(cantidadSolicitada);
            detalle.setPrecioUnitario(libro.getPrecio());
        } else {
            int nuevaCantidad = detalle.getCantidad() + cantidadSolicitada;
            validarCantidadDisponible(libro, nuevaCantidad);
            detalle.setCantidad(nuevaCantidad);
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

        CarritoDetalle detalle = carritoDetalleRepository
                .findByCarritoAndLibroIdLibro(carrito, idLibro)
                .orElseThrow(() -> new IllegalArgumentException("El libro no existe en el carrito."));

        detalle.setCantidad(cantidad);
        carritoDetalleRepository.save(detalle);
    }

    @Transactional
    public void eliminarLibro(String email, Long idLibro) {
        Carrito carrito = getCarritoActivo(email);
        carritoDetalleRepository.deleteByCarritoAndLibroIdLibro(carrito, idLibro);
    }

    @Transactional
    public void vaciarCarrito(String email) {
        Carrito carrito = getCarritoActivo(email);
        List<CarritoDetalle> detalles = carritoDetalleRepository.findByCarritoOrderByIdCarritoDetalleAsc(carrito);
        carritoDetalleRepository.deleteAll(detalles);
    }

    private Carrito crearCarrito(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        Carrito carrito = new Carrito();
        carrito.setUsuario(usuario);
        carrito.setActivo(true);

        return carritoRepository.save(carrito);
    }

    private Libro obtenerLibroValido(Long idLibro) {
        Libro libro = libroRepository.findById(idLibro)
                .orElseThrow(() -> new IllegalArgumentException("Libro no encontrado."));

        if (!libro.isActivo()) {
            throw new IllegalArgumentException("El libro no se encuentra activo.");
        }

        if (libro.getExistencias() <= 0) {
            throw new IllegalArgumentException("El libro no tiene existencias disponibles.");
        }

        return libro;
    }

    private void validarCantidadDisponible(Libro libro, int cantidad) {
        if (cantidad < 1) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }

        if (cantidad > libro.getExistencias()) {
            throw new IllegalArgumentException("La cantidad solicitada supera las existencias disponibles.");
        }
    }
}
