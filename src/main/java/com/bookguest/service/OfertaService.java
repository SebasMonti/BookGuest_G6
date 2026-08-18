package com.bookguest.service;

import com.bookguest.domain.Libro;
import com.bookguest.domain.Oferta;
import com.bookguest.repository.LibroRepository;
import com.bookguest.repository.OfertaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OfertaService {

    private static final ZoneId ZONA_COSTA_RICA = ZoneId.of("America/Costa_Rica");
    private static final int PRODUCTOS_POR_PAGINA = 8;

    private final OfertaRepository ofertaRepository;
    private final LibroRepository libroRepository;

    public OfertaService(OfertaRepository ofertaRepository,
            LibroRepository libroRepository) {
        this.ofertaRepository = ofertaRepository;
        this.libroRepository = libroRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProductoAdminVista> getProductosAdministracion(
            int page,
            String busqueda) {

        int pagina = Math.max(page, 0);
        String filtro = normalizarFiltro(busqueda);
        Page<Libro> paginaLibros = libroRepository.buscarProductosAdministracion(
                filtro,
                PageRequest.of(
                        pagina,
                        PRODUCTOS_POR_PAGINA,
                        Sort.by("titulo").ascending()
                )
        );

        List<Long> idsLibros = paginaLibros.getContent()
                .stream()
                .map(Libro::getIdLibro)
                .toList();
        Map<Long, Oferta> ofertasPorLibro = getUltimasOfertasPorLibro(idsLibros);
        LocalDate ahora = hoyCostaRica();

        List<ProductoAdminVista> productos = paginaLibros.getContent()
                .stream()
                .map(libro -> {
                    Oferta oferta = ofertasPorLibro.get(libro.getIdLibro());
                    return new ProductoAdminVista(
                            libro,
                            oferta,
                            getEstadoOferta(oferta, ahora)
                    );
                })
                .toList();

        return new PageImpl<>(productos, paginaLibros.getPageable(), paginaLibros.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Libro getLibroActivo(Long idLibro) {
        if (idLibro == null) {
            return null;
        }

        return libroRepository.findByIdLibroAndActivoTrue(idLibro).orElse(null);
    }

    @Transactional(readOnly = true)
    public Oferta getOfertaConfigurada(Long idLibro) {
        if (idLibro == null) {
            return null;
        }

        return ofertaRepository
                .findFirstByLibroIdLibroOrderByIdOfertaDesc(idLibro)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Oferta> getOfertasVigentesCliente() {
        Map<Long, Oferta> ofertasUnicas = new LinkedHashMap<>();

        ofertaRepository.buscarOfertasVigentes(hoyCostaRica())
                .forEach(oferta -> ofertasUnicas.putIfAbsent(
                oferta.getLibro().getIdLibro(),
                oferta
        ));

        return ofertasUnicas.values().stream().limit(8).toList();
    }

    @Transactional(readOnly = true)
    public Map<Long, Oferta> getOfertasVigentesPorLibros(List<Libro> libros) {
        if (libros == null || libros.isEmpty()) {
            return Map.of();
        }

        List<Long> idsLibro = libros.stream().map(Libro::getIdLibro).toList();
        Map<Long, Oferta> ofertasPorLibro = new LinkedHashMap<>();

        ofertaRepository.buscarOfertasVigentesPorLibros(idsLibro, hoyCostaRica())
                .forEach(oferta -> ofertasPorLibro.putIfAbsent(
                oferta.getLibro().getIdLibro(),
                oferta
        ));

        return ofertasPorLibro;
    }

    @Transactional(readOnly = true)
    public Oferta getOfertaVigente(Long idLibro) {
        if (idLibro == null) {
            return null;
        }

        return ofertaRepository
                .buscarOfertaVigentePorLibro(idLibro, hoyCostaRica())
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public BigDecimal getPrecioVenta(Libro libro) {
        if (libro == null || libro.getPrecio() == null) {
            return BigDecimal.ZERO;
        }

        Oferta oferta = getOfertaVigente(libro.getIdLibro());
        return oferta == null ? libro.getPrecio() : oferta.getPrecioOferta();
    }

    @Transactional
    public Oferta guardarOferta(Long idLibro,
            String descripcion,
            BigDecimal porcentajeDescuento,
            LocalDate fechaInicio,
            LocalDate fechaFin) {

        Libro libro = libroRepository.findByIdLibroAndActivoTrue(idLibro)
                .orElseThrow(() -> new IllegalArgumentException(
                "admin.productos.error.productoInactivo"
        ));

        validarOferta(descripcion, porcentajeDescuento, fechaInicio, fechaFin);

        Oferta oferta = ofertaRepository
                .findFirstByLibroIdLibroOrderByIdOfertaDesc(idLibro)
                .orElseGet(Oferta::new);

        oferta.setLibro(libro);
        oferta.setDescripcion(descripcion.trim());
        oferta.setPorcentajeDescuento(porcentajeDescuento);
        oferta.setFechaInicio(fechaInicio);
        oferta.setFechaFin(fechaFin);
        oferta.setActivo(true);

        return ofertaRepository.saveAndFlush(oferta);
    }

    @Transactional
    public void finalizarOferta(Long idLibro) {
        Oferta oferta = ofertaRepository
                .findFirstByLibroIdLibroOrderByIdOfertaDesc(idLibro)
                .orElseThrow(() -> new IllegalArgumentException(
                "admin.productos.error.sinOferta"
        ));

        oferta.setActivo(false);

        if (oferta.getFechaFin() != null && oferta.getFechaFin().isAfter(hoyCostaRica())) {
            oferta.setFechaFin(hoyCostaRica());
        }

        ofertaRepository.saveAndFlush(oferta);
    }

    public LocalDate hoyCostaRica() {
        return LocalDate.now(ZONA_COSTA_RICA);
    }

    public long getFinOfertaMasProxima(List<Oferta> ofertas) {
        if (ofertas == null || ofertas.isEmpty()) {
            return 0L;
        }

        return ofertas.stream()
                .mapToLong(Oferta::getFechaFinEpochMillis)
                .filter(valor -> valor > 0)
                .min()
                .orElse(0L);
    }

    private Map<Long, Oferta> getUltimasOfertasPorLibro(List<Long> idsLibros) {
        if (idsLibros.isEmpty()) {
            return Map.of();
        }

        Map<Long, Oferta> ofertasPorLibro = new LinkedHashMap<>();
        ofertaRepository.findByLibroIdLibroInOrderByIdOfertaDesc(idsLibros)
                .forEach(oferta -> ofertasPorLibro.putIfAbsent(
                oferta.getLibro().getIdLibro(),
                oferta
        ));

        return ofertasPorLibro;
    }

    private String getEstadoOferta(Oferta oferta, LocalDate ahora) {
        if (oferta == null) {
            return "Sin oferta";
        }

        if (!oferta.isActivo()) {
            return "Inactiva";
        }

        if (oferta.getFechaFin() == null || oferta.getFechaFin().isBefore(ahora)) {
            return "Finalizada";
        }

        if (oferta.getFechaInicio() != null && oferta.getFechaInicio().isAfter(ahora)) {
            return "Programada";
        }

        return "Activa";
    }

    private void validarOferta(String descripcion,
            BigDecimal porcentajeDescuento,
            LocalDate fechaInicio,
            LocalDate fechaFin) {

        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("admin.productos.error.descripcionRequerida");
        }

        if (descripcion.trim().length() > 150) {
            throw new IllegalArgumentException("admin.productos.error.descripcionLarga");
        }

        if (porcentajeDescuento == null
                || porcentajeDescuento.compareTo(BigDecimal.ZERO) <= 0
                || porcentajeDescuento.compareTo(BigDecimal.valueOf(100)) >= 0) {
            throw new IllegalArgumentException("admin.productos.error.descuento");
        }

        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("admin.productos.error.fechasRequeridas");
        }

        if (fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("admin.productos.error.fechaOrden");
        }

        if (fechaFin.isBefore(hoyCostaRica())) {
            throw new IllegalArgumentException("admin.productos.error.fechaPasado");
        }
    }

    private String normalizarFiltro(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
