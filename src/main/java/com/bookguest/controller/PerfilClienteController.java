package com.bookguest.controller;

import com.bookguest.domain.Usuario;
import com.bookguest.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cliente/perfil")
public class PerfilClienteController {

    private final UsuarioService usuarioService;
    private final MessageSource messageSource;

    public PerfilClienteController(UsuarioService usuarioService, MessageSource messageSource) {
        this.usuarioService = usuarioService;
        this.messageSource = messageSource;
    }

    @GetMapping
    public String perfil(Authentication authentication, Model model) {
        return cargarPerfil(authentication, model, "vista");
    }

    @GetMapping("/editar")
    public String editar(Authentication authentication, Model model) {
        return cargarPerfil(authentication, model, "editar");
    }

    @PostMapping("/actualizar")
    public String actualizar(@RequestParam String nombre,
            @RequestParam String apellidos,
            @RequestParam String email,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) String direccion,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String confirmarPassword,
            @RequestParam(value = "imagenFile", required = false) MultipartFile imagenFile,
            Authentication authentication,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes,
            Locale locale) {

        String emailAnterior = authentication.getName();

        try {
            Usuario usuario = usuarioService.actualizarPerfilCuenta(
                    emailAnterior,
                    nombre,
                    apellidos,
                    email,
                    telefono,
                    direccion,
                    password,
                    confirmarPassword,
                    imagenFile
            );

            actualizarIdentidadDeSesion(emailAnterior, usuario, authentication, request);
            return "redirect:/cliente/perfil/confirmacion";

        } catch (IllegalArgumentException | IOException e) {
            redirectAttributes.addFlashAttribute("mensajeError", resolverMensaje(e.getMessage(), locale));
            redirectAttributes.addFlashAttribute("nombreIngresado", nombre);
            redirectAttributes.addFlashAttribute("apellidosIngresados", apellidos);
            redirectAttributes.addFlashAttribute("emailIngresado", email);
            redirectAttributes.addFlashAttribute("telefonoIngresado", telefono);
            redirectAttributes.addFlashAttribute("direccionIngresada", direccion);
            return "redirect:/cliente/perfil/editar";
        }
    }

    @GetMapping("/confirmacion")
    public String confirmacion(Authentication authentication, Model model) {
        return cargarPerfil(authentication, model, "confirmacion");
    }

    private String cargarPerfil(Authentication authentication, Model model, String modo) {
        Usuario usuario = usuarioService.getUsuarioPorEmail(authentication.getName());

        if (usuario == null) {
            return "redirect:/logout";
        }

        model.addAttribute("perfilUsuario", usuario);
        model.addAttribute("modo", modo);
        return "cliente/perfil";
    }

    private void actualizarIdentidadDeSesion(String emailAnterior,
            Usuario usuario,
            Authentication authentication,
            HttpServletRequest request) {

        if (emailAnterior.equalsIgnoreCase(usuario.getEmail())) {
            return;
        }

        UsernamePasswordAuthenticationToken nuevaAutenticacion
                = UsernamePasswordAuthenticationToken.authenticated(
                        usuario.getEmail(),
                        authentication.getCredentials(),
                        authentication.getAuthorities()
                );
        nuevaAutenticacion.setDetails(authentication.getDetails());

        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(nuevaAutenticacion);
        SecurityContextHolder.setContext(contexto);
        request.getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                contexto
        );
    }

    private String resolverMensaje(String claveOMensaje, Locale locale) {
        if (claveOMensaje == null || claveOMensaje.isBlank()) {
            return messageSource.getMessage("mensaje.error.general", null, locale);
        }

        try {
            return messageSource.getMessage(claveOMensaje, null, locale);
        } catch (NoSuchMessageException e) {
            return claveOMensaje;
        }
    }
}
