package com.bookguest.controller;

import com.bookguest.domain.Libro;
import com.bookguest.service.LibroService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ClienteController {

    private final LibroService libroService;

    public ClienteController(LibroService libroService) {
        this.libroService = libroService;
    }

    @GetMapping("/cliente/inicio")
    public String inicio(Model model) {
        model.addAttribute("libros", libroService.getLibrosInicioCliente());
        model.addAttribute("ofertas", libroService.getOfertasCliente());
        model.addAttribute("categorias", libroService.getCategoriasCliente());

        return "cliente/inicio";
    }

    @GetMapping("/cliente/libros")
    public String libros(@RequestParam(required = false) String busqueda, Model model) {
        model.addAttribute("libros", libroService.getLibrosCliente(busqueda));
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("categorias", libroService.getCategoriasCliente());

        return "cliente/libros";
    }

    @GetMapping("/cliente/libro/{idLibro}")
    public String detalleLibro(@PathVariable Long idLibro, Model model) {
        Libro libro = libroService.getLibroActivo(idLibro);

        if (libro == null) {
            return "redirect:/cliente/inicio";
        }

        model.addAttribute("libro", libro);
        model.addAttribute("ofertas", libroService.getOfertasCliente());
        model.addAttribute("categorias", libroService.getCategoriasCliente());

        return "cliente/detalleLibro";
    }

    @GetMapping("/cliente/factura")
    public String factura() {
        return "cliente/factura";
    }

    @GetMapping("/cliente/compra-confirmada")
    public String compraConfirmada() {
        return "cliente/compraConfirmada";
    }

    @GetMapping("/cliente/pedidos")
    public String pedidos() {
        return "cliente/pedidos";
    }

    @GetMapping("/cliente/perfil")
    public String perfil() {
        return "cliente/perfil";
    }

    @GetMapping("/cliente/contactos")
    public String contactos() {
        return "cliente/contactos";
    }

    @GetMapping("/cliente/acerca")
    public String acerca() {
        return "cliente/acerca";
    }
}
