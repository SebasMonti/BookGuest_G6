package com.bookguest.controller;

import com.bookguest.service.CarritoService;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cliente/carrito")
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @GetMapping
    public String carrito(Model model, Principal principal) {
        cargarModeloCarrito(model, principal.getName());
        return "cliente/carrito";
    }

    @PostMapping("/agregar")
    public String agregar(@RequestParam Long idLibro,
            @RequestParam(defaultValue = "1") int cantidad,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            carritoService.agregarLibro(principal.getName(), idLibro, cantidad);
            redirectAttributes.addFlashAttribute("mensajeOk", "Libro agregado al carrito.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }

        return "redirect:/cliente/carrito";
    }

    @PostMapping("/actualizar")
    public String actualizar(@RequestParam Long idLibro,
            @RequestParam int cantidad,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            carritoService.actualizarCantidad(principal.getName(), idLibro, cantidad);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }

        return "redirect:/cliente/carrito";
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam Long idLibro,
            Principal principal) {

        carritoService.eliminarLibro(principal.getName(), idLibro);
        return "redirect:/cliente/carrito";
    }

    private void cargarModeloCarrito(Model model, String email) {
        model.addAttribute("detallesCarrito", carritoService.getDetallesCarrito(email));
        model.addAttribute("subtotalCarrito", carritoService.getSubtotalCarrito(email));
        model.addAttribute("totalCarrito", carritoService.getTotalCarrito(email));
    }
}
