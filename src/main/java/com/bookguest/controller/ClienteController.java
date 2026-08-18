package com.bookguest.controller;

import com.bookguest.domain.Libro;
import com.bookguest.service.FavoritoService;
import com.bookguest.service.LibroService;
import com.bookguest.service.OfertaService;
import java.security.Principal;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ClienteController {

    private final LibroService libroService;
    private final OfertaService ofertaService;
    private final FavoritoService favoritoService;

    public ClienteController(LibroService libroService,
            OfertaService ofertaService,
            FavoritoService favoritoService) {
        this.libroService = libroService;
        this.ofertaService = ofertaService;
        this.favoritoService = favoritoService;
    }

    @GetMapping("/cliente/inicio")
    public String inicio(Model model, Principal principal) {
        List<Libro> libros = libroService.getLibrosInicioCliente();
        var ofertas = ofertaService.getOfertasVigentesCliente();

        model.addAttribute("libros", libros);
        model.addAttribute("ofertas", ofertas);
        model.addAttribute("ofertasPorLibro", ofertaService.getOfertasVigentesPorLibros(libros));
        model.addAttribute("finOfertaEpoch", ofertaService.getFinOfertaMasProxima(ofertas));
        model.addAttribute("categorias", libroService.getCategoriasCliente());
        cargarFavoritos(model, principal);

        return "cliente/inicio";
    }

    @GetMapping("/cliente/libros")
    public String libros(@RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String origen,
            Model model,
            Principal principal) {
        boolean busquedaDesdeBarra = "busqueda".equals(origen)
                && busqueda != null
                && !busqueda.isBlank();

        List<Libro> libros = libroService.getLibrosCliente(busqueda);

        model.addAttribute("libros", libros);
        model.addAttribute("ofertasPorLibro", ofertaService.getOfertasVigentesPorLibros(libros));
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("mostrarCategorias", !busquedaDesdeBarra);

        if (!busquedaDesdeBarra) {
            model.addAttribute("categorias", libroService.getCategoriasCliente());
        }

        cargarFavoritos(model, principal);

        return "cliente/libros";
    }

    @GetMapping("/cliente/libro/{idLibro}")
    public String detalleLibro(@PathVariable Long idLibro,
            Model model,
            Principal principal) {
        Libro libro = libroService.getLibroActivo(idLibro);

        if (libro == null) {
            return "redirect:/cliente/inicio";
        }

        model.addAttribute("libro", libro);
        var ofertas = ofertaService.getOfertasVigentesCliente();

        model.addAttribute("ofertaLibro", ofertaService.getOfertaVigente(idLibro));
        model.addAttribute("ofertas", ofertas);
        model.addAttribute("finOfertaEpoch", ofertaService.getFinOfertaMasProxima(ofertas));
        model.addAttribute("categorias", libroService.getCategoriasCliente());
        cargarFavoritos(model, principal);

        return "cliente/detalleLibro";
    }

    private void cargarFavoritos(Model model, Principal principal) {
        model.addAttribute(
                "idsFavoritos",
                favoritoService.getIdsLibrosFavoritos(principal.getName())
        );
    }
}
