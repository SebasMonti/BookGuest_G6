package com.bookguest.controller;

import com.bookguest.domain.Usuario;
import com.bookguest.service.CarritoService;
import com.bookguest.service.CheckoutForm;
import com.bookguest.service.CompraService;
import com.bookguest.service.CompraVista;
import com.bookguest.service.UsuarioService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CheckoutController {

    private final CarritoService carritoService;
    private final CompraService compraService;
    private final UsuarioService usuarioService;

    public CheckoutController(CarritoService carritoService,
            CompraService compraService,
            UsuarioService usuarioService) {
        this.carritoService = carritoService;
        this.compraService = compraService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/cliente/factura")
    public String factura(Model model, Principal principal) {
        if (carritoService.getDetallesCarrito(principal.getName()).isEmpty()) {
            return "redirect:/cliente/carrito";
        }

        if (!model.containsAttribute("checkoutForm")) {
            Usuario usuario = usuarioService.getUsuarioPorEmail(principal.getName());
            CheckoutForm formulario = new CheckoutForm();
            formulario.setNombre((usuario.getNombre() + " " + usuario.getApellidos()).trim());
            precargarDireccion(formulario, usuario.getDireccion());
            formulario.setTelefono(usuario.getTelefono());
            formulario.setMetodoPago("tarjeta");
            formulario.setGuardarInformacion(true);
            model.addAttribute("checkoutForm", formulario);
        }

        cargarFactura(model, principal.getName());
        return "cliente/factura";
    }

    @PostMapping("/cliente/factura/confirmar")
    public String confirmar(@Valid @ModelAttribute("checkoutForm") CheckoutForm formulario,
            BindingResult bindingResult,
            Model model,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            cargarFactura(model, principal.getName());
            return "cliente/factura";
        }

        CompraVista compra;

        try {
            compra = compraService.confirmarCompra(principal.getName(), formulario);
        } catch (IllegalArgumentException e) {
            bindingResult.reject(e.getMessage());
            cargarFactura(model, principal.getName());
            return "cliente/factura";
        }

        redirectAttributes.addAttribute("idPedido", compra.getIdPedido());
        return "redirect:/cliente/compra-confirmada";
    }

    @GetMapping("/cliente/compra-confirmada")
    public String compraConfirmada(@RequestParam Long idPedido,
            Principal principal,
            Model model) {
        try {
            model.addAttribute("compra", compraService.getCompraCliente(principal.getName(), idPedido));
            return "cliente/compraConfirmada";
        } catch (IllegalArgumentException e) {
            return "redirect:/cliente/carrito";
        }
    }

    private void cargarFactura(Model model, String email) {
        model.addAttribute("detallesCarrito", carritoService.getDetallesCarrito(email));
        model.addAttribute("subtotalCarrito", carritoService.getSubtotalCarrito(email));
        model.addAttribute("totalCarrito", carritoService.getTotalCarrito(email));
        model.addAttribute("emailCliente", email);
    }

    private void precargarDireccion(CheckoutForm formulario, String direccionGuardada) {
        if (direccionGuardada == null || direccionGuardada.isBlank()) {
            return;
        }

        String[] partes = direccionGuardada.trim().split("\\s*,\\s*");
        boolean terminaEnCostaRica = partes.length >= 2
                && "Costa Rica".equalsIgnoreCase(partes[partes.length - 1]);

        if (!terminaEnCostaRica) {
            formulario.setDireccion(direccionGuardada.trim());
            return;
        }

        formulario.setCiudad(partes[partes.length - 2]);

        if (partes.length == 2) {
            formulario.setDireccion(partes[0]);
            return;
        }

        int ultimoSegmentoDireccion = partes.length - 2;

        if (partes.length >= 4) {
            formulario.setDireccionSecundaria(partes[partes.length - 3]);
            ultimoSegmentoDireccion = partes.length - 3;
        }

        formulario.setDireccion(String.join(", ",
                java.util.Arrays.copyOfRange(partes, 0, ultimoSegmentoDireccion)));
    }
}
