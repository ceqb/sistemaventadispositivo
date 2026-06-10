package com.ceqb.SistemaVentaDispositivos2025.service.serviceImpl;

import com.ceqb.SistemaVentaDispositivos2025.config.UsuarioNoVerificadoException;
import com.ceqb.SistemaVentaDispositivos2025.dto.UsuarioDTO;
import com.ceqb.SistemaVentaDispositivos2025.mapper.UsuarioMapper;
import com.ceqb.SistemaVentaDispositivos2025.model.Cargo;
import com.ceqb.SistemaVentaDispositivos2025.model.Usuario;
import com.ceqb.SistemaVentaDispositivos2025.repository.CargoRepository;
import com.ceqb.SistemaVentaDispositivos2025.repository.UsuarioRepository;
import com.ceqb.SistemaVentaDispositivos2025.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {
    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final CargoRepository cargoRepository;
    private final JavaMailSender mailSender;

    // Generar código de desbloqueo aleatorio
    private String generarCodigoDesbloqueo() {
        return String.valueOf((int) (Math.random() * 900000 + 100000)); // 6 dígitos
    }

    // Enviar correo con el código
    private void enviarCorreoDesbloqueo(String correo, String codigo) {
        System.out.println("✅ Intentando enviar correo a: " + correo);
        System.out.println("✅ Código generado: " + codigo);
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(correo);
        mensaje.setSubject("🔐 Código de desbloqueo");
        mensaje.setText("Tu cuenta ha sido bloqueada por 3 intentos fallidos.\n\n" +
                "Código para desbloquear: " + codigo + "\n" +
                "Este código expirará en 10 minutos.");
        mensaje.setFrom("crearveint@gmail.com"); // Cambiar por el remitente configurado
        mailSender.send(mensaje);
    }

    @Override
    public Optional<UsuarioDTO> login(String usuario, String clave) {
        Optional<Usuario> userOpt = usuarioRepository.findByUsuario(usuario);

        if (userOpt.isEmpty()) return Optional.empty();

        Usuario user = userOpt.get();
// 🚫 NO PERMITIR LOGIN SI NO ESTÁ VERIFICADO
        if (!user.getVerificado()) {
            throw new UsuarioNoVerificadoException("Debes verificar tu correo antes de iniciar sesión. Revisa tu correo.");
        }
        if (user.getBloqueado() != null && user.getBloqueado()) {
            throw new RuntimeException("Tu cuenta está bloqueada. Se ha enviado un código a tu correo.");
        }

        if (passwordEncoder.matches(clave, user.getClave())) {
            user.setIntentosFallidos(0);
            usuarioRepository.save(user);
            return Optional.of(UsuarioMapper.toDto(user));
        } else {
            user.setIntentosFallidos(user.getIntentosFallidos() + 1);

            if (user.getIntentosFallidos() >= 3) {
                user.setBloqueado(true);
                String codigo = generarCodigoDesbloqueo();
                user.setCodigoDesbloqueo(codigo);
                user.setExpiracionCodigo(LocalDateTime.now().plusMinutes(10));
                enviarCorreoDesbloqueo(user.getCorreo(), codigo);
            }

            usuarioRepository.save(user);
            return Optional.empty();
        }
    }

    @Override
    public List<UsuarioDTO> listarTodos() {
        return usuarioRepository.findAll()
                .stream()
                .map(UsuarioMapper::toDto)
                .collect(Collectors.toList());
    }

    public void registrarNuevoUsuario(UsuarioDTO usuarioDTO, HttpServletRequest request) {

        /*Usuario usuario = UsuarioMapper.toEntity(usuarioDTO);

        // Validar que la contraseña no sea nula o vacía
        if (usuario.getClave() == null || usuario.getClave().trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede ser nula o vacía.");
        }

        // ✅ 1. Busca el rol 'USER'
        Optional<Cargo> cargoUsuarioOptional = cargoRepository.findByNombrecargo("USER");
        if (!cargoUsuarioOptional.isPresent()) {
            throw new RuntimeException("El rol 'USER' no se encontró en la base de datos.");
        }

        // ✅ 2. Asigna valores por defecto
        usuario.setBloqueado(false); // ✅ Nuevo: El usuario no está bloqueado al registrarse
        usuario.setIntentosFallidos(0); // ✅ Nuevo: Los intentos fallidos son 0 al inicio
        usuario.setCargo(cargoUsuarioOptional.get());

        // ✅ 3. Encripta la contraseña
        usuario.setClave(passwordEncoder.encode(usuario.getClave()));

        // ✅ 4. Guarda el usuario
        usuarioRepository.save(usuario);

        //logger.info("Usuario registrado exitosamente: {}", usuarioDTO.getCorreo());*/

        if (existeCorreo(usuarioDTO.getCorreo())) {
            throw new RuntimeException("Ya existe una cuenta registrada con este correo.");
        }
        if (existeUsuario(usuarioDTO.getUsuario())) {
            throw new RuntimeException("El nombre de usuario ya está en uso.");
        }

        Usuario usuario = UsuarioMapper.toEntity(usuarioDTO);
        usuario.setClave(passwordEncoder.encode(usuario.getClave()));
        usuario.setBloqueado(false);
        usuario.setIntentosFallidos(0);

        // Rol por defecto
        Cargo cargoUsuario = cargoRepository.findByNombrecargo("USER")
                .orElseThrow(() -> new RuntimeException("El rol 'USER' no se encontró."));
        usuario.setCargo(cargoUsuario);

        // Generar token
        String token = UUID.randomUUID().toString();
        usuario.setTokenVerificacion(token);
        usuario.setExpiracionCodigo(LocalDateTime.now().plusHours(24));
        usuario.setVerificado(false);

        usuarioRepository.save(usuario);

        // Enviar correo
        enviarCorreoVerificacion(usuario.getCorreo(), token, request);
    }

    @Override
    public UsuarioDTO buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return UsuarioMapper.toDto(usuario);
    }

    @Override
    public UsuarioDTO buscarPorUsuario(String usuario) {
        Usuario entity = usuarioRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con nombre: " + usuario));
        return UsuarioMapper.toDto(entity);
    }

    @Override
    public UsuarioDTO desbloquearUsuario(String usuario, String codigo, HttpServletRequest request) {
        Usuario user = usuarioRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        if (!user.getBloqueado()) {
            throw new RuntimeException("Este usuario no está bloqueado.");
        }

        if (user.getCodigoDesbloqueo() == null || !user.getCodigoDesbloqueo().equals(codigo)) {
            throw new RuntimeException("Código incorrecto.");
        }

        if (user.getExpiracionCodigo() == null || user.getExpiracionCodigo().isBefore(LocalDateTime.now())) {
            // 🔁 Código expirado: generar uno nuevo
            String nuevoCodigo = generarCodigoDesbloqueo();
            user.setCodigoDesbloqueo(nuevoCodigo);
            user.setExpiracionCodigo(LocalDateTime.now().plusMinutes(10));
            usuarioRepository.save(user);

            // 📧 Reenviar correo
            enviarCorreoDesbloqueo(user.getCorreo(), nuevoCodigo);

            throw new RuntimeException("El código ha expirado. Se ha enviado uno nuevo al correo.");
        }

        // 🔓 Desbloqueo y limpieza
        user.setBloqueado(false);
        user.setIntentosFallidos(0);
        user.setCodigoDesbloqueo(null);
        user.setExpiracionCodigo(null);

        Usuario actualizado = usuarioRepository.save(user);


        return UsuarioMapper.toDto(actualizado);
    }

    @Override
    public boolean cambiarClave(String nombreUsuario, String nuevaClave) {

        // 1. Buscar al usuario en la base de datos
        Usuario user = usuarioRepository.findByUsuario(nombreUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        // 2. Hashear la nueva clave antes de guardarla
        String claveHasheada = passwordEncoder.encode(nuevaClave);

        // 3. Actualizar y guardar la nueva clave
        user.setClave(claveHasheada);
        usuarioRepository.save(user);
        return true;
    }


    @Override
    public UsuarioDTO reenviarCodigoDesbloqueo(String usuario) {
        Optional<Usuario> userOpt = usuarioRepository.findByUsuario(usuario);

        if (userOpt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado.");
        }

        Usuario user = userOpt.get();

        if (!user.getBloqueado()) {
            throw new RuntimeException("Este usuario no está bloqueado.");
        }

        String nuevoCodigo = generarCodigoDesbloqueo();
        user.setCodigoDesbloqueo(nuevoCodigo);
        user.setExpiracionCodigo(LocalDateTime.now().plusMinutes(10));
        Usuario codigoReenviado = usuarioRepository.save(user);

        enviarCorreoDesbloqueo(user.getCorreo(), nuevoCodigo);

        return UsuarioMapper.toDto(codigoReenviado); // ✅ Solo este return

    }

    @Override
    public void eliminar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("No existe el usuario con ID: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    /**REPARTIDORES**/
    @Override
    public List<UsuarioDTO> obtenerRepartidores() {
        return usuarioRepository.findByCargoNombrecargoIgnoreCase("Repartidor")
                .stream()
                .map(UsuarioMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existeCorreo(String correo) {
        return usuarioRepository.existsByCorreo(correo);
    }

    @Override
    public boolean existeUsuario(String usuario) {
        return usuarioRepository.existsByUsuario(usuario);
    }
    @Override
    public void enviarCorreoVerificacion(String correo, String token, HttpServletRequest request) {
        String urlVerificacion = request.getRequestURL().toString().replace(request.getServletPath(), "")
                + "/verificar?token=" + token;

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(correo);
        mensaje.setSubject("✅ Verifica tu cuenta");
        mensaje.setText("Bienvenido a nuestro sistema.\n\n" +
                "Haz clic en el siguiente enlace para activar tu cuenta:\n" + urlVerificacion +
                "\n\nEste enlace expirará en 24 horas.");
        mensaje.setFrom("crearveint@gmail.com");

        mailSender.send(mensaje);
    }
    @Override
    public void reenviarCorreoVerificacion(String correo, HttpServletRequest request) {
        Usuario user = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("No existe un usuario con este correo."));

        if (user.getVerificado()) {
            throw new RuntimeException("Esta cuenta ya está verificada.");
        }

        // generar nuevo token
        String token = UUID.randomUUID().toString();
        user.setTokenVerificacion(token);
        user.setExpiracionCodigo(LocalDateTime.now().plusHours(24));

        usuarioRepository.save(user);

        enviarCorreoVerificacion(correo, token, request);
    }

    @Override
    public void enviarTokenResetPassword(String correo, HttpServletRequest request) {
        Optional<Usuario> opt = usuarioRepository.findByCorreo(correo);
        if (opt.isEmpty()) return;

        Usuario user = opt.get();

        String token = UUID.randomUUID().toString();

        user.setTokenResetPassword(token);
        user.setExpiracionResetPass(LocalDateTime.now().plusHours(1));

        usuarioRepository.save(user);

        String urlReset = request.getRequestURL().toString().replace(request.getServletPath(), "")
                + "/password/reset?token=" + token;

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(correo);
        mensaje.setSubject("Restablecer contraseña");
        mensaje.setText("Haz clic en el siguiente enlace:\n" + urlReset + "\n\nVálido por 1 hora.");
        mensaje.setFrom("crearveint@gmail.com");

        mailSender.send(mensaje);

    }

    @Override
    public boolean restablecerContrasena(String token, String nuevaClave) {
        Optional<Usuario> opt = usuarioRepository.findByTokenResetPassword(token);

        if (opt.isEmpty()) {
            throw new RuntimeException("El token es inválido o el enlace ya fue usado.");
        }

        Usuario user = opt.get();

        // 1. Validar expiración contra la columna correcta (ExpiracionResetPass)
        if (user.getExpiracionResetPass() == null || user.getExpiracionResetPass().isBefore(LocalDateTime.now())) {
            // Opcional: limpiar el token expirado
            user.setTokenResetPassword(null);
            user.setExpiracionResetPass(null);
            usuarioRepository.save(user);
            throw new RuntimeException("El enlace ha expirado. Por favor, solicita un nuevo reseteo.");
        }

        // 2. Codificar y asignar la nueva clave
        user.setClave(passwordEncoder.encode(nuevaClave));

        // 3. Limpiar los campos de reseteo
        user.setTokenResetPassword(null);
        user.setExpiracionResetPass(null);

        usuarioRepository.save(user);
        return true;
    }


    @Override
    public boolean verificarCuenta(String token) {

        Usuario usuario = usuarioRepository.findByTokenVerificacion(token)
                .orElseThrow(() -> new RuntimeException("Token inválido."));

        if (usuario.getExpiracionCodigo().isBefore(LocalDateTime.now())) {
            return false; // token expirado
        }

        usuario.setVerificado(true);
        usuario.setTokenVerificacion(null);
        usuario.setExpiracionCodigo(null);
        usuarioRepository.save(usuario);

        return true;
    }
    @Override
    public Usuario save(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    @Override
    public Optional<Usuario> findByTokenResetPassword(String token) {
        return usuarioRepository.findByTokenResetPassword(token);
    }
}
