package com.bookguest.service;

import com.bookguest.domain.Rol;
import com.bookguest.domain.Usuario;
import com.bookguest.domain.EstadoPedido;
import com.bookguest.repository.PedidoRepository;
import com.bookguest.repository.RolRepository;
import com.bookguest.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UsuarioService {

    private static final String ROL_CLIENTE = "ROLE_CLIENTE";
    private static final String ROL_ADMIN = "ROLE_ADMIN";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PedidoRepository pedidoRepository;
    private final PasswordEncoder passwordEncoder;
    private final FirebaseStorageService firebaseStorageService;

    public UsuarioService(UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PedidoRepository pedidoRepository,
            PasswordEncoder passwordEncoder,
            FirebaseStorageService firebaseStorageService) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.pedidoRepository = pedidoRepository;
        this.passwordEncoder = passwordEncoder;
        this.firebaseStorageService = firebaseStorageService;
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

    public Page<Usuario> getUsuariosAdministracion(int page,
            String busqueda,
            String estado,
            String rol) {

        int pagina = Math.max(page, 0);
        String busquedaNormalizada = normalizarFiltro(busqueda);
        Boolean activo = convertirEstado(estado);
        String rolNormalizado = convertirRolFiltro(rol);

        Page<Usuario> paginaUsuarios = usuarioRepository.buscarParaAdministracion(
                busquedaNormalizada,
                activo,
                rolNormalizado,
                PageRequest.of(pagina, 9, Sort.by("idUsuario").ascending())
        );

        List<Long> idsUsuarios = paginaUsuarios.getContent()
                .stream()
                .map(Usuario::getIdUsuario)
                .toList();

        if (!idsUsuarios.isEmpty()) {
            Map<Long, Long> comprasPorUsuario = pedidoRepository
                    .contarComprasNoCanceladasPorUsuarios(idsUsuarios, EstadoPedido.Cancelado)
                    .stream()
                    .collect(Collectors.toMap(
                            resumen -> resumen.getIdUsuario(),
                            resumen -> resumen.getTotalCompras()
                    ));

            paginaUsuarios.forEach(usuario -> usuario.setTotalCompras(
                    comprasPorUsuario.getOrDefault(usuario.getIdUsuario(), 0L)
            ));
        }

        return paginaUsuarios;
    }

    public Usuario getUsuario(Long idUsuario) {
        if (idUsuario == null) {
            return null;
        }

        return usuarioRepository.findByIdUsuario(idUsuario).orElse(null);
    }

    public Usuario getUsuarioPorEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        return usuarioRepository.findByEmail(email.trim().toLowerCase()).orElse(null);
    }

    public long getTotalUsuarios() {
        return usuarioRepository.count();
    }

    public long getTotalUsuariosActivos() {
        return usuarioRepository.countByActivoTrue();
    }

    public long getUsuariosRegistradosUltimosSieteDias() {
        return usuarioRepository.countByFechaCreacionAfter(LocalDateTime.now().minusDays(7));
    }

    public long getTotalAdministradores() {
        return usuarioRepository.countByRolesNombre(ROL_ADMIN);
    }

    public int getPaginaDeUsuario(Long idUsuario) {
        if (idUsuario == null) {
            return 0;
        }

        return (int) (usuarioRepository.countByIdUsuarioLessThan(idUsuario) / 9);
    }

    @Transactional
    public Usuario crearUsuarioDesdeAdministracion(String nombre,
            String apellidos,
            String email,
            String telefono,
            String password,
            String confirmarPassword,
            String rolSolicitado,
            MultipartFile imagenFile) throws IOException {

        validarDatosUsuario(nombre, apellidos, email);
        validarPasswordsObligatorias(password, confirmarPassword);

        String emailNormalizado = normalizarEmail(email);

        if (usuarioRepository.existsByEmailIgnoreCase(emailNormalizado)) {
            throw new IllegalArgumentException("usuario.registro.emailDuplicado");
        }

        Rol rol = obtenerRolPermitido(rolSolicitado);

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre.trim());
        usuario.setApellidos(apellidos.trim());
        usuario.setEmail(emailNormalizado);
        usuario.setTelefono(normalizarTextoOpcional(telefono));
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setActivo(true);
        usuario.setRoles(new java.util.ArrayList<>(List.of(rol)));

        String rutaImagen = firebaseStorageService.subirImagen(imagenFile, "usuarios");
        usuario.setRutaImagen(rutaImagen);

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario actualizarUsuarioDesdeAdministracion(Long idUsuario,
            String nombre,
            String apellidos,
            String email,
            String telefono,
            String password,
            String confirmarPassword,
            String rolSolicitado,
            MultipartFile imagenFile,
            String emailAdministradorActual) throws IOException {

        Usuario usuario = usuarioRepository.findByIdUsuario(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("usuarios.error.noExiste"));

        validarDatosUsuario(nombre, apellidos, email);
        validarPasswordsOpcionales(password, confirmarPassword);

        String emailNormalizado = normalizarEmail(email);

        if (usuarioRepository.existsByEmailIgnoreCaseAndIdUsuarioNot(emailNormalizado, idUsuario)) {
            throw new IllegalArgumentException("perfil.emailDuplicado");
        }

        Rol rolNuevo = obtenerRolPermitido(rolSolicitado);
        boolean eraAdministrador = tieneRol(usuario, ROL_ADMIN);
        boolean seguiraComoAdministrador = ROL_ADMIN.equals(rolNuevo.getNombre());
        boolean esCuentaActual = usuario.getEmail().equalsIgnoreCase(emailAdministradorActual);

        if (esCuentaActual && !seguiraComoAdministrador) {
            throw new IllegalArgumentException("usuarios.error.propioRolAdmin");
        }

        if (eraAdministrador
                && !seguiraComoAdministrador
                && usuarioRepository.countByRolesNombre(ROL_ADMIN) <= 1) {
            throw new IllegalArgumentException("usuarios.error.ultimoAdmin");
        }

        usuario.setNombre(nombre.trim());
        usuario.setApellidos(apellidos.trim());
        usuario.setEmail(emailNormalizado);
        usuario.setTelefono(normalizarTextoOpcional(telefono));
        usuario.setRoles(new java.util.ArrayList<>(List.of(rolNuevo)));

        if (password != null && !password.isBlank()) {
            usuario.setPassword(passwordEncoder.encode(password));
        }

        String nuevaRutaImagen = firebaseStorageService.subirImagen(imagenFile, "usuarios");

        if (nuevaRutaImagen != null) {
            usuario.setRutaImagen(nuevaRutaImagen);
        }

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario actualizarPerfilCuenta(String emailActual,
            String nombre,
            String apellidos,
            String email,
            String telefono,
            String direccion,
            String password,
            String confirmarPassword,
            MultipartFile imagenFile) throws IOException {

        Usuario usuario = usuarioRepository.findByEmail(normalizarEmail(emailActual))
                .orElseThrow(() -> new IllegalArgumentException("perfil.usuarioNoExiste"));

        validarDatosUsuario(nombre, apellidos, email);
        validarPasswordsOpcionales(password, confirmarPassword);

        String emailNormalizado = normalizarEmail(email);

        if (usuarioRepository.existsByEmailIgnoreCaseAndIdUsuarioNot(
                emailNormalizado,
                usuario.getIdUsuario())) {
            throw new IllegalArgumentException("perfil.emailDuplicado");
        }

        usuario.setNombre(nombre.trim());
        usuario.setApellidos(apellidos.trim());
        usuario.setEmail(emailNormalizado);
        usuario.setTelefono(normalizarTextoOpcional(telefono));
        usuario.setDireccion(normalizarTextoOpcional(direccion));

        if (password != null && !password.isBlank()) {
            usuario.setPassword(passwordEncoder.encode(password));
        }

        String nuevaRutaImagen = firebaseStorageService.subirImagen(imagenFile, "usuarios");

        if (nuevaRutaImagen != null) {
            usuario.setRutaImagen(nuevaRutaImagen);
        }

        return usuarioRepository.save(usuario);
    }

    public String getRolPrincipal(Usuario usuario) {
        if (usuario == null || usuario.getRoles() == null) {
            return ROL_CLIENTE;
        }

        if (tieneRol(usuario, ROL_ADMIN)) {
            return ROL_ADMIN;
        }

        return ROL_CLIENTE;
    }

    private boolean tieneRol(Usuario usuario, String nombreRol) {
        return usuario.getRoles() != null
                && usuario.getRoles()
                        .stream()
                        .anyMatch(rol -> nombreRol.equals(rol.getNombre()));
    }

    private void validarDatosUsuario(String nombre, String apellidos, String email) {
        validarTexto(nombre, "usuario.validacion.nombreRequerido");
        validarTexto(apellidos, "usuario.validacion.apellidosRequeridos");
        validarTexto(email, "usuario.validacion.emailRequerido");

        if (!normalizarEmail(email).matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("usuario.validacion.emailInvalido");
        }
    }

    private void validarPasswordsObligatorias(String password, String confirmarPassword) {
        validarTexto(password, "usuario.validacion.passwordRequerida");
        validarTexto(confirmarPassword, "usuario.validacion.confirmacionRequerida");
        validarCoincidenciaYLongitudPassword(password, confirmarPassword);
    }

    private void validarPasswordsOpcionales(String password, String confirmarPassword) {
        boolean passwordVacio = password == null || password.isBlank();
        boolean confirmacionVacia = confirmarPassword == null || confirmarPassword.isBlank();

        if (passwordVacio && confirmacionVacia) {
            return;
        }

        if (passwordVacio || confirmacionVacia) {
            throw new IllegalArgumentException("perfil.passwordCampos");
        }

        validarCoincidenciaYLongitudPassword(password, confirmarPassword);
    }

    private void validarCoincidenciaYLongitudPassword(String password, String confirmarPassword) {
        if (password.length() < 8) {
            throw new IllegalArgumentException("usuario.validacion.passwordMinimo");
        }

        if (!password.equals(confirmarPassword)) {
            throw new IllegalArgumentException("usuario.validacion.passwordNoCoincide");
        }
    }

    private Rol obtenerRolPermitido(String rolSolicitado) {
        String nombreRol = ROL_ADMIN.equalsIgnoreCase(rolSolicitado) ? ROL_ADMIN : ROL_CLIENTE;

        return rolRepository.findByNombre(nombreRol)
                .orElseThrow(() -> new IllegalArgumentException("usuarios.error.rolNoExiste"));
    }

    private String normalizarEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private String normalizarFiltro(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }

    private Boolean convertirEstado(String estado) {
        if ("activo".equalsIgnoreCase(estado)) {
            return Boolean.TRUE;
        }

        if ("inactivo".equalsIgnoreCase(estado)) {
            return Boolean.FALSE;
        }

        return null;
    }

    private String convertirRolFiltro(String rol) {
        if ("admin".equalsIgnoreCase(rol) || ROL_ADMIN.equalsIgnoreCase(rol)) {
            return ROL_ADMIN;
        }

        if ("cliente".equalsIgnoreCase(rol) || ROL_CLIENTE.equalsIgnoreCase(rol)) {
            return ROL_CLIENTE;
        }

        return null;
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
