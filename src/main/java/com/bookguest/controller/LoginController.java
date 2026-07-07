package com.bookguest.controller;

import com.bookguest.service.UsuarioService;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {

    private final UsuarioService usuarioService;
    private final MessageSource messageSource;

    public LoginController(UsuarioService usuarioService, MessageSource messageSource) {
        this.usuarioService = usuarioService;
        this.messageSource = messageSource;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/registro")
    public String registro() {
        return "registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@RequestParam String nombre,
            @RequestParam String apellidos,
            @RequestParam String email,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) String direccion,
            @RequestParam String password,
            @RequestParam String confirmarPassword,
            RedirectAttributes redirectAttributes,
            Locale locale) {

        try {
            usuarioService.registrarCliente(nombre, apellidos, email, telefono, direccion, password, confirmarPassword);
            redirectAttributes.addFlashAttribute("mensajeOk", getMensaje("usuario.registro.ok", locale));
            return "redirect:/registro/confirmacion";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", getMensaje(e.getMessage(), locale));
            return "redirect:/registro";
        }
    }

    @GetMapping("/registro/confirmacion")
    public String registroConfirmacion() {
        return "registroConfirmacion";
    }

    @GetMapping("/restablecer")
    public String restablecer() {
        return "restablecer";
    }

    @PostMapping("/restablecer")
    public String procesarRestablecer(@RequestParam String email,
            @RequestParam String nuevaPassword,
            @RequestParam String confirmarPassword,
            RedirectAttributes redirectAttributes,
            Locale locale) {

        try {
            usuarioService.restablecerPassword(email, nuevaPassword, confirmarPassword);
            redirectAttributes.addFlashAttribute("mensajeOk", getMensaje("usuario.restablecer.ok", locale));
            return "redirect:/restablecer/confirmacion";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", getMensaje(e.getMessage(), locale));
            return "redirect:/restablecer";
        }
    }

    @GetMapping("/restablecer/confirmacion")
    public String restablecerConfirmacion() {
        return "restablecerConfirmacion";
    }

    @GetMapping("/post-login")
    public String postLogin(Authentication authentication) {
        boolean esAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(rol -> rol.getAuthority().equals("ROLE_ADMIN"));

        if (esAdmin) {
            return "redirect:/admin/dashboard";
        }

        return "redirect:/cliente/inicio";
    }

    private String getMensaje(String clave, Locale locale) {
        try {
            return messageSource.getMessage(clave, null, locale);
        } catch (NoSuchMessageException e) {
            return messageSource.getMessage("mensaje.error.general", null, locale);
        }
    }
}