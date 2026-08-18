package com.bookguest.controller;

import com.bookguest.domain.Usuario;
import com.bookguest.service.UsuarioService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class UsuarioSesionControllerAdvice {

    private final UsuarioService usuarioService;

    public UsuarioSesionControllerAdvice(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @ModelAttribute
    public void agregarUsuarioSesion(Authentication authentication, Model model) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return;
        }

        Usuario usuario = usuarioService.getUsuarioPorEmail(authentication.getName());

        if (usuario != null) {
            model.addAttribute("usuarioSesion", usuario);
        }
    }
}
