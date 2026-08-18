package com.bookguest.controller;

import com.bookguest.service.CarritoService;
import java.security.Principal;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
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
    private final MessageSource messageSource;

    public CarritoController(CarritoService carritoService, MessageSource messageSource) {
        this.carritoService = carritoService;
        this.messageSource = messageSource;
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
            RedirectAttributes redirectAttributes,
            Locale locale) {

        try {
            carritoService.agregarLibro(principal.getName(), idLibro, cantidad);
            redirectAttributes.addFlashAttribute("mensajeOk", getMensaje("carrito.mensaje.agregado", locale));
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", getMensaje(e.getMessage(), locale));
        }

        return "redirect:/cliente/carrito";
    }

    @PostMapping("/actualizar")
    public String actualizar(@RequestParam Long idLibro,
            @RequestParam int cantidad,
            Principal principal,
            RedirectAttributes redirectAttributes,
            Locale locale) {

        try {
            carritoService.actualizarCantidad(principal.getName(), idLibro, cantidad);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", getMensaje(e.getMessage(), locale));
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

    private String getMensaje(String clave, Locale locale) {
        try {
            return messageSource.getMessage(clave, null, locale);
        } catch (NoSuchMessageException e) {
            return messageSource.getMessage("mensaje.error.general", null, locale);
        }
    }
}
