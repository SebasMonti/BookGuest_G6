package com.bookguest.service;

import com.bookguest.domain.Autor;
import com.bookguest.domain.Categoria;
import com.bookguest.domain.Editorial;
import com.bookguest.domain.Libro;
import com.bookguest.repository.AutorRepository;
import com.bookguest.repository.CategoriaRepository;
import com.bookguest.repository.EditorialRepository;
import com.bookguest.repository.LibroRepository;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LibroService {

    private final LibroRepository libroRepository;
    private final AutorRepository autorRepository;
    private final EditorialRepository editorialRepository;
    private final CategoriaRepository categoriaRepository;
    private final FirebaseStorageService firebaseStorageService;

    public LibroService(LibroRepository libroRepository,
            AutorRepository autorRepository,
            EditorialRepository editorialRepository,
            CategoriaRepository categoriaRepository,
            FirebaseStorageService firebaseStorageService) {
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
        this.editorialRepository = editorialRepository;
        this.categoriaRepository = categoriaRepository;
        this.firebaseStorageService = firebaseStorageService;
    }

    public Page<Libro> getLibrosInventario(String estado, int page) {
        int pagina = Math.max(page, 0);
        var pageable = PageRequest.of(pagina, 10, Sort.by("titulo").ascending());

        if ("disponibles".equals(estado)) {
            return libroRepository.findByActivoTrueAndExistenciasGreaterThan(5, pageable);
        }

        if ("pocas".equals(estado)) {
            return libroRepository.findByActivoTrueAndExistenciasBetween(1, 5, pageable);
        }

        if ("sin_stock".equals(estado)) {
            return libroRepository.findByActivoTrueAndExistencias(0, pageable);
        }

        return libroRepository.findByActivoTrue(pageable);
    }

    public Libro getLibro(Long idLibro) {
        return libroRepository.findById(idLibro).orElse(null);
    }

    public long getTotalLibrosActivos() {
        return libroRepository.countByActivoTrue();
    }

    public long getTotalSinStock() {
        return libroRepository.countByActivoTrueAndExistencias(0);
    }

    public long getTotalPocasUnidades() {
        return libroRepository.countByActivoTrueAndExistenciasBetween(1, 5);
    }

    public long getTotalCategorias() {
        return categoriaRepository.count();
    }

    public List<Libro> getLibrosInicioCliente() {
        return getLibrosCliente()
                .stream()
                .limit(8)
                .toList();
    }

    public List<Libro> getOfertasCliente() {
        return getLibrosCliente()
                .stream()
                .sorted((l1, l2) -> l1.getPrecio().compareTo(l2.getPrecio()))
                .limit(4)
                .toList();
    }

    public List<Libro> getLibrosCliente() {
        return libroRepository.findByActivoTrue()
                .stream()
                .filter(libro -> libro.getExistencias() > 0)
                .toList();
    }

    public List<Libro> getLibrosCliente(String busqueda) {
        if (busqueda == null || busqueda.isBlank()) {
            return getLibrosCliente();
        }

        String filtro = busqueda.toLowerCase().trim();

        return getLibrosCliente()
                .stream()
                .filter(libro -> libro.getTitulo().toLowerCase().contains(filtro)
                || (libro.getAutor() != null && libro.getAutor().getNombre().toLowerCase().contains(filtro))
                || (libro.getCategoria() != null && libro.getCategoria().getDescripcion().toLowerCase().contains(filtro))
                || (libro.getEditorial() != null && libro.getEditorial().getNombre().toLowerCase().contains(filtro)))
                .toList();
    }

    public Libro getLibroActivo(Long idLibro) {
        return libroRepository.findById(idLibro)
                .filter(Libro::isActivo)
                .orElse(null);
    }
    
    public List<Categoria> getCategoriasCliente() {
        return categoriaRepository.findAll();
    }

    @Transactional
    public Libro crearDesdeInventario(String titulo,
            String autorNombre,
            String precioTexto,
            String editorialNombre,
            String isbn,
            String ubicacionFisica,
            String categoriaNombre,
            int existencias,
            MultipartFile imagenFile) throws IOException {

        validarTextoObligatorio(titulo, "El título es obligatorio.");
        validarTextoObligatorio(autorNombre, "El autor es obligatorio.");
        validarTextoObligatorio(precioTexto, "El precio es obligatorio.");
        validarTextoObligatorio(editorialNombre, "La editorial es obligatoria.");
        validarTextoObligatorio(isbn, "El ISBN es obligatorio.");
        validarTextoObligatorio(ubicacionFisica, "La ubicación física es obligatoria.");
        validarTextoObligatorio(categoriaNombre, "La categoría es obligatoria.");
        validarImagenObligatoria(imagenFile);

        BigDecimal precio = convertirPrecio(precioTexto);
        validarPrecio(precio);

        if (existencias < 0) {
            throw new IllegalArgumentException("Las existencias no pueden ser negativas.");
        }

        String isbnNormalizado = normalizarIsbn(isbn);
        validarIsbn13(isbnNormalizado);

        if (libroRepository.existsByIsbn(isbnNormalizado)) {
            throw new IllegalArgumentException("Ya existe un libro registrado con ese ISBN.");
        }

        Autor autor = obtenerOCrearAutor(autorNombre);
        Editorial editorial = obtenerOCrearEditorial(editorialNombre);
        Categoria categoria = obtenerOCrearCategoria(categoriaNombre);

        Libro libro = new Libro();
        libro.setTitulo(titulo.trim());
        libro.setAutor(autor);
        libro.setPrecio(precio);
        libro.setEditorial(editorial);
        libro.setIsbn(isbnNormalizado);
        libro.setUbicacionFisica(ubicacionFisica.trim());
        libro.setCategoria(categoria);
        libro.setExistencias(existencias);
        libro.setActivo(true);
        libro.setDescripcion("Libro registrado desde inventario.");

        String rutaImagen = firebaseStorageService.subirImagen(imagenFile, "libros");
        libro.setRutaImagen(rutaImagen);

        return libroRepository.save(libro);
    }


    @Transactional
    public Libro actualizarDesdeInventario(Long idLibro,
            String titulo,
            String autorNombre,
            String precioTexto,
            String editorialNombre,
            String isbn,
            String ubicacionFisica,
            String categoriaNombre,
            int existencias,
            MultipartFile imagenFile) throws IOException {

        Libro libro = getLibro(idLibro);

        if (libro == null) {
            return null;
        }

        validarTextoObligatorio(titulo, "El título es obligatorio.");
        validarTextoObligatorio(autorNombre, "El autor es obligatorio.");
        validarTextoObligatorio(precioTexto, "El precio es obligatorio.");
        validarTextoObligatorio(editorialNombre, "La editorial es obligatoria.");
        validarTextoObligatorio(isbn, "El ISBN es obligatorio.");
        validarTextoObligatorio(ubicacionFisica, "La ubicación física es obligatoria.");
        validarTextoObligatorio(categoriaNombre, "La categoría es obligatoria.");

        BigDecimal precio = convertirPrecio(precioTexto);
        validarPrecio(precio);

        if (existencias < 0) {
            throw new IllegalArgumentException("Las existencias no pueden ser negativas.");
        }

        String isbnNormalizado = normalizarIsbn(isbn);
        validarIsbn13(isbnNormalizado);

        if (libroRepository.existsByIsbnAndIdLibroNot(isbnNormalizado, idLibro)) {
            throw new IllegalArgumentException("Ya existe otro libro registrado con ese ISBN.");
        }

        Autor autor = obtenerOCrearAutor(autorNombre);
        Editorial editorial = obtenerOCrearEditorial(editorialNombre);
        Categoria categoria = obtenerOCrearCategoria(categoriaNombre);

        libro.setTitulo(titulo.trim());
        libro.setAutor(autor);
        libro.setPrecio(precio);
        libro.setEditorial(editorial);
        libro.setIsbn(isbnNormalizado);
        libro.setUbicacionFisica(ubicacionFisica.trim());
        libro.setCategoria(categoria);
        libro.setExistencias(existencias);
        libro.setActivo(true);

        if (imagenFile != null && !imagenFile.isEmpty()) {
            if (libro.getRutaImagen() != null && !libro.getRutaImagen().isBlank()) {
                firebaseStorageService.eliminarImagen(libro.getRutaImagen());
            }

            String rutaImagen = firebaseStorageService.subirImagen(imagenFile, "libros");
            libro.setRutaImagen(rutaImagen);
        }

        return libroRepository.save(libro);
    }

    private Autor obtenerOCrearAutor(String nombre) {
        String valor = limpiarTexto(nombre);

        return autorRepository.findByNombreIgnoreCase(valor)
                .orElseGet(() -> {
                    Autor autor = new Autor();
                    autor.setNombre(valor);
                    return autorRepository.save(autor);
                });
    }

    private Editorial obtenerOCrearEditorial(String nombre) {
        String valor = limpiarTexto(nombre);

        return editorialRepository.findByNombreIgnoreCase(valor)
                .orElseGet(() -> {
                    Editorial editorial = new Editorial();
                    editorial.setNombre(valor);
                    return editorialRepository.save(editorial);
                });
    }

    private Categoria obtenerOCrearCategoria(String descripcion) {
        String valor = limpiarTexto(descripcion);

        return categoriaRepository.findByDescripcionIgnoreCase(valor)
                .orElseGet(() -> {
                    Categoria categoria = new Categoria();
                    categoria.setDescripcion(valor);
                    return categoriaRepository.save(categoria);
                });
    }

    private String limpiarTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return "Sin especificar";
        }

        return texto.trim();
    }

    private BigDecimal convertirPrecio(String precioTexto) {
        if (precioTexto == null || precioTexto.isBlank()) {
            return BigDecimal.ZERO;
        }

        String valor = precioTexto
                .replace("₡", "")
                .replace("¢", "")
                .replace(" ", "")
                .replace(".", "")
                .replace(",", ".");

        return new BigDecimal(valor);
    }

    private void validarTextoObligatorio(String valor, String mensaje) {
        if (valor == null || valor.trim().isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    private void validarImagenObligatoria(MultipartFile imagenFile) {
        if (imagenFile == null || imagenFile.isEmpty()) {
            throw new IllegalArgumentException("La imagen del libro es obligatoria.");
        }
    }

    private void validarPrecio(BigDecimal precio) {
        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a cero.");
        }
    }

    private String normalizarIsbn(String isbn) {
        return isbn.replace("-", "")
                .replace(" ", "")
                .trim();
    }

    private void validarIsbn13(String isbn) {
        if (!isbn.matches("97[89][0-9]{10}")) {
            throw new IllegalArgumentException("El ISBN debe tener 13 dígitos y comenzar con 978 o 979.");
        }

        int suma = 0;

        for (int i = 0; i < 12; i++) {
            int digito = Character.getNumericValue(isbn.charAt(i));
            suma += (i % 2 == 0) ? digito : digito * 3;
        }

        int digitoControl = (10 - (suma % 10)) % 10;
        int ultimoDigito = Character.getNumericValue(isbn.charAt(12));

        if (digitoControl != ultimoDigito) {
            throw new IllegalArgumentException("El ISBN ingresado no es válido.");
        }
    }

    public int getTotalExistenciasActivas() {
        return libroRepository.findByActivoTrue()
                .stream()
                .mapToInt(Libro::getExistencias)
                .sum();
    }

    public BigDecimal getValorTotalInventario() {
        return libroRepository.findByActivoTrue()
                .stream()
                .map(libro -> libro.getPrecio().multiply(BigDecimal.valueOf(libro.getExistencias())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Libro> getUltimosLibrosAgregados() {
        return libroRepository.findTop5ByActivoTrueOrderByIdLibroDesc();
    }

    public List<Libro> getLibrosBajoStock() {
        return libroRepository.findTop5ByActivoTrueAndExistenciasBetweenOrderByExistenciasAsc(1, 5);
    }

    public List<Libro> getLibrosSinStockListado() {
        return libroRepository.findTop5ByActivoTrueAndExistenciasOrderByTituloAsc(0);
    }
}
