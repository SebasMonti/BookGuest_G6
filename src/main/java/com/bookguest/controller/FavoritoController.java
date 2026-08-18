package com.bookguest.controller;

import com.bookguest.domain.Libro;
import com.bookguest.service.FavoritoService;
import com.bookguest.service.OfertaService;
import java.security.Principal;
import java.util.List;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class FavoritoController {

    private final FavoritoService favoritoService;
    private final OfertaService ofertaService;
    private final MessageSource messageSource;

    public FavoritoController(FavoritoService favoritoService,
            OfertaService ofertaService,
            MessageSource messageSource) {
        this.favoritoService = favoritoService;
        this.ofertaService = ofertaService;
        this.messageSource = messageSource;
    }

    @GetMapping("/cliente/favoritos")
    public String favoritos(Principal principal, Model model) {
        List<Libro> libros = favoritoService.getLibrosFavoritos(principal.getName());

        model.addAttribute("librosFavoritos", libros);
        model.addAttribute("idsFavoritos", favoritoService.getIdsLibrosFavoritos(principal.getName()));
        model.addAttribute("ofertasPorLibro", ofertaService.getOfertasVigentesPorLibros(libros));
        return "cliente/favoritos";
    }

    @PostMapping("/cliente/favoritos/alternar")
    public String alternar(@RequestParam Long idLibro,
            @RequestParam(defaultValue = "inicio") String retorno,
            Principal principal,
            RedirectAttributes redirectAttributes,
            Locale locale) {
        try {
            boolean agregado = favoritoService.alternarFavorito(principal.getName(), idLibro);
            redirectAttributes.addFlashAttribute(
                    "favoritoAgregado",
                    agregado
            );
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", getMensaje(e.getMessage(), locale));
        }

        return redirigir(retorno, idLibro);
    }

    private String redirigir(String retorno, Long idLibro) {
        return switch (retorno) {
            case "favoritos" -> "redirect:/cliente/favoritos";
            case "libros" -> "redirect:/cliente/libros";
            case "detalle" -> "redirect:/cliente/libro/" + idLibro;
            default -> "redirect:/cliente/inicio";
        };
    }

    private String getMensaje(String clave, Locale locale) {
        try {
            return messageSource.getMessage(clave, null, locale);
        } catch (NoSuchMessageException e) {
            return messageSource.getMessage("mensaje.error.general", null, locale);
        }
    }
}
