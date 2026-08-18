package com.bookguest.controller;

import com.bookguest.domain.Usuario;
import com.bookguest.service.UsuarioService;
import java.io.IOException;
import java.security.Principal;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final MessageSource messageSource;

    public UsuarioController(UsuarioService usuarioService,
            MessageSource messageSource) {
        this.usuarioService = usuarioService;
        this.messageSource = messageSource;
    }

    @GetMapping
    public String usuarios(@RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String rol,
            @RequestParam(defaultValue = "false") boolean mostrarFiltros,
            Model model) {

        cargarModeloUsuarios(model, page, busqueda, estado, rol, mostrarFiltros, "lista", null);
        return "admin/usuarios";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        cargarModeloUsuarios(model, 0, null, null, null, false, "nuevo", null);
        return "admin/usuarios";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam String nombre,
            @RequestParam String apellidos,
            @RequestParam String email,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) String rol,
            @RequestParam String password,
            @RequestParam String confirmarPassword,
            @RequestParam(value = "imagenFile", required = false) MultipartFile imagenFile,
            RedirectAttributes redirectAttributes,
            Locale locale) {

        try {
            Usuario usuario = usuarioService.crearUsuarioDesdeAdministracion(
                    nombre,
                    apellidos,
                    email,
                    telefono,
                    password,
                    confirmarPassword,
                    rol,
                    imagenFile
            );

            return "redirect:/admin/usuarios/confirmacion?idUsuario=" + usuario.getIdUsuario();

        } catch (IllegalArgumentException | IOException e) {
            redirectAttributes.addFlashAttribute("mensajeError", getMensaje(e.getMessage(), locale));
            return "redirect:/admin/usuarios/nuevo";
        }
    }

    @GetMapping("/confirmacion")
    public String confirmacion(@RequestParam Long idUsuario, Model model) {
        int page = usuarioService.getPaginaDeUsuario(idUsuario);
        cargarModeloUsuarios(model, page, null, null, null, false, "confirmacionAgregar", idUsuario);
        return "admin/usuarios";
    }

    @GetMapping("/seleccionar")
    public String seleccionar(@RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String rol,
            @RequestParam(defaultValue = "false") boolean mostrarFiltros,
            Model model) {

        cargarModeloUsuarios(model, page, busqueda, estado, rol, mostrarFiltros, "seleccionar", null);
        return "admin/usuarios";
    }

    @GetMapping("/modificar")
    public String modificar(@RequestParam Long idUsuario,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String rol,
            Model model) {

        cargarModeloUsuarios(model, page, busqueda, estado, rol, false, "modificar", idUsuario);
        return "admin/usuarios";
    }

    @PostMapping("/actualizar")
    public String actualizar(@RequestParam Long idUsuario,
            @RequestParam String nombre,
            @RequestParam String apellidos,
            @RequestParam String email,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String confirmarPassword,
            @RequestParam(value = "imagenFile", required = false) MultipartFile imagenFile,
            Principal principal,
            RedirectAttributes redirectAttributes,
            Locale locale) {

        try {
            Usuario usuario = usuarioService.actualizarUsuarioDesdeAdministracion(
                    idUsuario,
                    nombre,
                    apellidos,
                    email,
                    telefono,
                    password,
                    confirmarPassword,
                    rol,
                    imagenFile,
                    principal.getName()
            );

            return "redirect:/admin/usuarios/modificado?idUsuario=" + usuario.getIdUsuario();

        } catch (IllegalArgumentException | IOException e) {
            redirectAttributes.addFlashAttribute("mensajeError", getMensaje(e.getMessage(), locale));
            return "redirect:/admin/usuarios/modificar?idUsuario=" + idUsuario;
        }
    }

    @GetMapping("/modificado")
    public String modificado(@RequestParam Long idUsuario, Model model) {
        int page = usuarioService.getPaginaDeUsuario(idUsuario);
        cargarModeloUsuarios(model, page, null, null, null, false, "confirmacionModificar", idUsuario);
        return "admin/usuarios";
    }

    private void cargarModeloUsuarios(Model model,
            int page,
            String busqueda,
            String estado,
            String rol,
            boolean mostrarFiltros,
            String modo,
            Long idUsuario) {

        Page<Usuario> paginaUsuarios = usuarioService.getUsuariosAdministracion(page, busqueda, estado, rol);

        model.addAttribute("paginaUsuarios", paginaUsuarios);
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("estado", estado);
        model.addAttribute("rolFiltro", rol);
        model.addAttribute("mostrarFiltros", mostrarFiltros);
        model.addAttribute("modo", modo);
        model.addAttribute("totalUsuarios", usuarioService.getTotalUsuarios());
        model.addAttribute("totalUsuariosActivos", usuarioService.getTotalUsuariosActivos());
        model.addAttribute("usuariosUltimosSieteDias", usuarioService.getUsuariosRegistradosUltimosSieteDias());
        model.addAttribute("totalAdministradores", usuarioService.getTotalAdministradores());

        if (idUsuario != null) {
            Usuario usuario = usuarioService.getUsuario(idUsuario);
            model.addAttribute("usuarioSeleccionado", usuario);
            model.addAttribute("rolUsuarioSeleccionado", usuarioService.getRolPrincipal(usuario));
        }
    }

    private String getMensaje(String clave, Locale locale) {
        return messageSource.getMessage(clave, null, clave, locale);
    }
}
