package com.bookguest.service;

import com.bookguest.domain.Rol;
import com.bookguest.domain.Usuario;
import com.bookguest.repository.RolRepository;
import com.bookguest.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private static final String ROL_CLIENTE = "ROLE_CLIENTE";
    private static final String ROL_ADMIN = "ROLE_ADMIN";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registrarCliente(String nombre,
            String apellidos,
            String email,
            String telefono,
            String direccion,
            String password,
            String confirmarPassword) {

        validarTexto(nombre, "usuario.validacion.nombreRequerido");
        validarTexto(apellidos, "usuario.validacion.apellidosRequeridos");
        validarTexto(email, "usuario.validacion.emailRequerido");
        validarTexto(password, "usuario.validacion.passwordRequerida");
        validarTexto(confirmarPassword, "usuario.validacion.confirmacionRequerida");

        email = email.trim().toLowerCase();

        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("usuario.validacion.emailInvalido");
        }

        if (!password.equals(confirmarPassword)) {
            throw new IllegalArgumentException("usuario.validacion.passwordNoCoincide");
        }

        if (password.length() < 8) {
            throw new IllegalArgumentException("usuario.validacion.passwordMinimo");
        }

        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("usuario.registro.emailDuplicado");
        }

        Rol rolCliente = rolRepository.findByNombre(ROL_CLIENTE)
                .orElseThrow(() -> new IllegalArgumentException("usuario.rol.clienteNoExiste"));

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre.trim());
        usuario.setApellidos(apellidos.trim());
        usuario.setEmail(email);
        usuario.setTelefono(normalizarTextoOpcional(telefono));
        usuario.setDireccion(normalizarTextoOpcional(direccion));
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setActivo(true);
        usuario.getRoles().add(rolCliente);

        usuarioRepository.save(usuario);
    }

    @Transactional
    public void restablecerPassword(String email, String nuevaPassword, String confirmarPassword) {

        validarTexto(email, "usuario.validacion.emailRequerido");
        validarTexto(nuevaPassword, "usuario.validacion.passwordNuevaRequerida");
        validarTexto(confirmarPassword, "usuario.validacion.confirmacionRequerida");

        email = email.trim().toLowerCase();

        if (!nuevaPassword.equals(confirmarPassword)) {
            throw new IllegalArgumentException("usuario.validacion.passwordNoCoincide");
        }

        if (nuevaPassword.length() < 8) {
            throw new IllegalArgumentException("usuario.validacion.passwordMinimo");
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("usuario.restablecer.noExiste"));

        if (esAdministrador(usuario)) {
            throw new IllegalArgumentException("usuario.restablecer.adminBloqueado");
        }

        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
    }

    private boolean esAdministrador(Usuario usuario) {
        if (usuario.getRoles() == null) {
            return false;
        }

        return usuario.getRoles()
                .stream()
                .anyMatch(rol -> ROL_ADMIN.equals(rol.getNombre()));
    }

    private void validarTexto(String valor, String claveMensaje) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(claveMensaje);
        }
    }

    private String normalizarTextoOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }
}