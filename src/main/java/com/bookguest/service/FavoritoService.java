package com.bookguest.service;

import com.bookguest.domain.Favorito;
import com.bookguest.domain.Libro;
import com.bookguest.domain.Usuario;
import com.bookguest.repository.FavoritoRepository;
import com.bookguest.repository.LibroRepository;
import com.bookguest.repository.UsuarioRepository;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LibroRepository libroRepository;

    public FavoritoService(FavoritoRepository favoritoRepository,
            UsuarioRepository usuarioRepository,
            LibroRepository libroRepository) {
        this.favoritoRepository = favoritoRepository;
        this.usuarioRepository = usuarioRepository;
        this.libroRepository = libroRepository;
    }

    @Transactional(readOnly = true)
    public List<Libro> getLibrosFavoritos(String email) {
        if (email == null || email.isBlank()) {
            return List.of();
        }

        return favoritoRepository
                .findByUsuarioEmailAndLibroActivoTrueOrderByFechaCreacionDesc(email)
                .stream()
                .map(Favorito::getLibro)
                .toList();
    }

    @Transactional(readOnly = true)
    public Set<Long> getIdsLibrosFavoritos(String email) {
        if (email == null || email.isBlank()) {
            return Set.of();
        }

        return favoritoRepository.buscarIdsLibrosPorUsuario(email);
    }

    @Transactional
    public boolean alternarFavorito(String email, Long idLibro) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("favoritos.error.usuarioNoExiste"));

        Libro libro = libroRepository.findByIdLibroAndActivoTrue(idLibro)
                .orElseThrow(() -> new IllegalArgumentException("favoritos.error.libroNoExiste"));

        Favorito favorito = favoritoRepository
                .findByUsuarioIdUsuarioAndLibroIdLibro(usuario.getIdUsuario(), libro.getIdLibro())
                .orElse(null);

        if (favorito != null) {
            favoritoRepository.delete(favorito);
            return false;
        }

        Favorito nuevoFavorito = new Favorito();
        nuevoFavorito.setUsuario(usuario);
        nuevoFavorito.setLibro(libro);
        favoritoRepository.save(nuevoFavorito);
        return true;
    }
}
