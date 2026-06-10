package com.ceqb.SistemaVentaDispositivos2025.controller;

import com.ceqb.SistemaVentaDispositivos2025.API_AUDITORIA.AuditoriaAccesoDTO;
import com.ceqb.SistemaVentaDispositivos2025.API_AUDITORIA.AuditoriaAccesoService;
import com.ceqb.SistemaVentaDispositivos2025.config.LoginInterceptor;
import com.ceqb.SistemaVentaDispositivos2025.config.UsuarioNoVerificadoException;
import com.ceqb.SistemaVentaDispositivos2025.dto.CarritoDTO;
import com.ceqb.SistemaVentaDispositivos2025.dto.UsuarioDTO;
import com.ceqb.SistemaVentaDispositivos2025.dto.UsuarioLoginDTO;
import com.ceqb.SistemaVentaDispositivos2025.model.Usuario;
import com.ceqb.SistemaVentaDispositivos2025.service.CarritoService;
import com.ceqb.SistemaVentaDispositivos2025.service.UsuarioService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class UsuarioController {
    private static final Logger logger = LoggerFactory.getLogger(LoginInterceptor.class);

    private final UsuarioService usuarioService;
    private final CarritoService carritoService;
    private final AuditoriaAccesoService auditoriaAccesoService;

    //-------------------------REGISTRO----------------------------//

    @GetMapping("/registrar")
    public String mostrarFormularioDeRegistro(Model model) {
        model.addAttribute("usuarioDTO", new UsuarioDTO());
        return "admin/NuevoUsuario";
    }

    @PostMapping("/registrar")
    public String procesarRegistro(@Valid @ModelAttribute("usuarioDTO") UsuarioDTO usuarioDTO,
                                   BindingResult result,
                                   Model model,
                                   RedirectAttributes redirectAttributes,
                                   HttpServletRequest request) {

        // 1️⃣ Validaciones automáticas con @Valid (campos vacíos, formato email, etc.)
        if (result.hasErrors()) {
            return "admin/NuevoUsuario"; // vuelve al formulario mostrando los errores
        }

        /*// 2️⃣ Verificar coincidencia de contraseñas
        if (!usuarioDTO.getClave().equals(confirmarClave)) {
            model.addAttribute("errorRegistro", "Las contraseñas no coinciden.");
            return "admin/NuevoUsuario";
        }*/

        // 3️⃣ Verificar si el correo ya está registrado
        if (usuarioService.existeCorreo(usuarioDTO.getCorreo())) {
            model.addAttribute("errorRegistro", "El correo electrónico ya está registrado.");
            return "admin/NuevoUsuario";
        }

        // 4️⃣ Verificar si el nombre de usuario ya está en uso
        if (usuarioService.existeUsuario(usuarioDTO.getUsuario())) {
            model.addAttribute("errorRegistro", "El nombre de usuario ya está en uso.");
            return "admin/NuevoUsuario";
        }
        // 5️⃣ Guardar usuario si esta bien
        try {
            usuarioService.registrarNuevoUsuario(usuarioDTO, request);

            redirectAttributes.addFlashAttribute("registroExitoso", "¡Tu cuenta ha sido creada con éxito! Por favor, verificala con el link que te hemos enviado a tu correo.");
            return "redirect:/login";

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorRegistro", e.getMessage());
            return "redirect:/registrar";
        }
    }

    @PostMapping("/reenviar-verificacion")
    public String reenviarVerificacion(
            @RequestParam String correo,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {
        try {
            usuarioService.reenviarCorreoVerificacion(correo, request);
            redirectAttributes.addFlashAttribute("registroExitoso",
                    "Se envió un nuevo correo de verificación.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorRegistro", ex.getMessage());
        }

        return "redirect:/login";
    }

    @GetMapping("/verificar")
    public String verificarCuenta(@RequestParam("token") String token,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        try {
            boolean verificado = usuarioService.verificarCuenta(token);

            if (verificado) {
                model.addAttribute("success", "¡Tu cuenta ha sido verificada exitosamente!");
                return "/login/verificacionExitosa"; // ESTA ES LA VISTA QUE TE FALTA
            } else {
                model.addAttribute("error", "El enlace de verificación es inválido o ha expirado.");
                return "/login/verificacionFallida";
            }

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorRegistro", e.getMessage());
        }


        return "redirect:/login";
    }
    //-------------------------LOGIN----------------------------//

    @GetMapping("/login")
    public String mostrarLogin(Model model) {
        return "login/login";
    }

    /*ORIGINAL*/
    @PostMapping("/login")
    public String processLogin(@ModelAttribute UsuarioLoginDTO loginRequest,
                               HttpSession session,
                               HttpServletResponse response,
                               RedirectAttributes redirectAttributes,
                               HttpServletRequest request) {
        try {
            Optional<UsuarioDTO> userOptional = usuarioService.login(loginRequest.getNombre(), loginRequest.getClave());

            if (userOptional.isPresent()) {
                UsuarioDTO loggedInUser = userOptional.get();

                // ===================== AUDITORÍA LOGIN EXITOSO =====================
                String ip = request.getRemoteAddr();
                String navegador = request.getHeader("User-Agent");

                AuditoriaAccesoDTO auditoria = AuditoriaAccesoDTO.builder()
                        .usuario(loggedInUser.getUsuario())
                        .ip(ip)
                        .navegador(navegador)
                        .fechaHora(LocalDateTime.now())
                        .exitoso(true)
                        .observacion("Inicio de sesión exitoso")
                        .build();

                auditoriaAccesoService.registrarAuditoriaAcceso(auditoria);
                // ===============================================================

                // ✅ Fusionar carrito anónimo con el persistente SOLO una vez en el login
                @SuppressWarnings("unchecked")
                List<CarritoDTO> anonymousCart = (List<CarritoDTO>) session.getAttribute("carritoAnonimo");
                if (anonymousCart != null && !anonymousCart.isEmpty()) {
                    carritoService.fusionarCarrito(anonymousCart, loggedInUser);
                    session.removeAttribute("carritoAnonimo");
                }

                // Guardar usuario en sesión
                session.setAttribute("usuarioLogueado", loggedInUser);

                // Cookie rememberMe (30 días)
                String rememberMeToken = UUID.randomUUID().toString();
                Cookie rememberCookie = new Cookie("rememberMe", rememberMeToken);
                rememberCookie.setHttpOnly(true);
                rememberCookie.setPath("/");
                rememberCookie.setMaxAge(60 * 60 * 24 * 30);
                response.addCookie(rememberCookie);

                // 👇 Solo clientes usan redirectAfterLogin
                String redirectUrl = (String) session.getAttribute("redirectAfterLogin");
                session.removeAttribute("redirectAfterLogin");

                if (redirectUrl != null && !redirectUrl.isEmpty()
                        && !redirectUrl.contains("pedidos/create-payment-preference")) {
                    return "redirect:" + redirectUrl;
                }


                // 🔑 Redirección por cargo (roles tienen prioridad sobre redirectAfterLogin)
                if ("Administrador".equalsIgnoreCase(loggedInUser.getNombreCargo())) {
                    return "redirect:/admin/index";
                } else if ("Repartidor".equalsIgnoreCase(loggedInUser.getNombreCargo())) {
                    return "redirect:/pedidosRepartidor/pedidos";
                }

                return "redirect:/tienda";

            } else {

                // ===================== AUDITORÍA LOGIN FALLIDO =====================
                String ip = request.getRemoteAddr();
                String navegador = request.getHeader("User-Agent");

                AuditoriaAccesoDTO auditoria = AuditoriaAccesoDTO.builder()
                        .usuario(loginRequest.getNombre())
                        .ip(ip)
                        .navegador(navegador)
                        .fechaHora(LocalDateTime.now())
                        .exitoso(false)
                        .observacion("Login fallido: credenciales incorrectas")
                        .build();

                auditoriaAccesoService.registrarAuditoriaAcceso(auditoria);
                // ===============================================================


                redirectAttributes.addFlashAttribute("loginError", "Usuario o contraseña incorrectos.");
                return "redirect:/login";
            }
        } catch (UsuarioNoVerificadoException ex) {
            redirectAttributes.addFlashAttribute("loginError", ex.getMessage());
            return "redirect:/login"; // 👈 NO ENVIAR A DESBLOQUEO


        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("loginError", ex.getMessage());
            return "redirect:/desbloquearUsuario";
        }
    }

    //-------------------------LOGOUT----------------------------//

    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse response) {
        // ❌ Ya no copiamos carritoUsuario → carritoAnonimo
        // Esto causaba la duplicación en cada login

        // Eliminar cookie rememberMe
        Cookie deleteCookie = new Cookie("rememberMe", null);
        deleteCookie.setPath("/");
        deleteCookie.setMaxAge(0);
        response.addCookie(deleteCookie);

        // Limpiar sesión por completo
        session.invalidate();

        return "redirect:/tienda";
    }

    //-------------------------PRELOADER----------------------------//

    @GetMapping("/preloader")
    public String mostrarPreloader(Model model, HttpSession session) {
        UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        String redirectUrl;

        if ("Administrador".equalsIgnoreCase(usuario.getNombreCargo())) {
            redirectUrl = "/admin/index";
        } else if ("Repartidor".equalsIgnoreCase(usuario.getNombreCargo())) {
            redirectUrl = "/pedidosRepartidor/pedidos";
        } else {
            // Cliente → respetar redirectAfterLogin si existe
            String savedRedirect = (String) session.getAttribute("redirectAfterLogin");
            session.removeAttribute("redirectAfterLogin");

            if (savedRedirect != null && !savedRedirect.isEmpty()
                    && !savedRedirect.contains("pedidos/create-payment-preference")) {
                redirectUrl = savedRedirect;
            } else {
                redirectUrl = "/tienda";
            }
        }

        model.addAttribute("redirectUrl", redirectUrl);
        return "preloader";
    }

    //-------------------------DESBLOQUEO----------------------------//

    @GetMapping("/desbloquearUsuario")
    public String mostrarFormularioDesbloqueo() {
        return "/login/desbloquearUsuario";
    }

    @PostMapping("/desbloquearUsuario")
    public String procesarDesbloqueo(@RequestParam String usuario,
                                     @RequestParam String codigo,
                                     Model model,
                                     HttpServletRequest request) {
        try {
            usuarioService.desbloquearUsuario(usuario, codigo, request);
            model.addAttribute("success", "Cuenta desbloqueada correctamente.");
            return "redirect:/cambiar-clave?usuario=" + usuario;
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("usuario", usuario);
            return "/login/desbloquearUsuario";
        }
    }

    //-------------------------REENVIO DE CODIGO----------------------------//

    @PostMapping("/reenviar-codigo")
    public String reenviarCodigoDesbloqueo(@RequestParam String usuario,
                                           Model model) {
        try {
            usuarioService.reenviarCodigoDesbloqueo(usuario);
            model.addAttribute("success", "✅ Código reenviado correctamente al correo.");
            model.addAttribute("usuario", usuario);
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("usuario", usuario);
        }

        return "/login/desbloquearUsuario";
    }

    @GetMapping("/cambiar-clave")
    public String mostrarFormularioCambioClave(
            @RequestParam(required = false) String usuario,
            HttpSession session,
            Model model) {

        // 1. 🛑 ESCENARIO DE DESBLOQUEO / RESETEO (Prioridad alta)
        if (usuario != null && !usuario.isEmpty()) {

            // Invalida cualquier sesión antigua para asegurar que el usuario no pueda navegar sin cambiar la clave
            session.invalidate();

            // Pasamos el usuario recibido por URL
            model.addAttribute("usuario", usuario);
            return "/login/cambiar-clave";
        }

        // 2. ✅ ESCENARIO DE CAMBIO DE CLAVE ESTÁNDAR (Usuario logueado)
        UsuarioDTO loggedInUser = (UsuarioDTO) session.getAttribute("usuarioLogueado");

        if (loggedInUser != null) {
            // Aquí es vital usar getUsuario() porque ese es tu identificador
            model.addAttribute("usuario", loggedInUser.getUsuario());
            return "/login/cambiar-clave";
        }

        // 3. ❌ Si no hay parámetro ni sesión → Redirigimos al login
        return "redirect:/login";
    }

    @PostMapping("/guardar-nueva-clave")
    public String guardarNuevaClave(
            @RequestParam String usuario,
            @RequestParam String clave,
            @RequestParam String confirmar,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Validación: Las claves deben coincidir
        if (!clave.equals(confirmar)) {
            model.addAttribute("error", "Las contraseñas no coinciden.");
            model.addAttribute("usuario", usuario);
            return "/login/cambiar-clave";
        }

        try {
            boolean actualizado = usuarioService.cambiarClave(usuario, clave);

            if (!actualizado) {
                model.addAttribute("error", "No se pudo actualizar la contraseña.");
                model.addAttribute("usuario", usuario);
                return "/login/cambiar-clave";
            }

            // ✅ Si la clave se cambió exitosamente, forzamos cierre de sesión
            session.invalidate();

            // ✅ Mensaje para que aparezca en el login
            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "Tu clave se actualizó correctamente. Ahora inicia sesión."
            );

            return "redirect:/login";

        } catch (Exception ex) {

            model.addAttribute("error", ex.getMessage());
            model.addAttribute("usuario", usuario);

            return "/login/cambiar-clave";
        }
    }


    /*OLVIDE MI CONTRASEÑA*/

    @GetMapping("/password/forgot")
    public String mostrarForgotPassword(Model model) {
        return "/login/forgot-password"; // página con input email
    }

    @PostMapping("/password/forgot")
    public String procesarForgotPassword(@RequestParam("correo") String correo,
                                         HttpServletRequest request,
                                         Model model) {

        usuarioService.enviarTokenResetPassword(correo, request);

        model.addAttribute("mensaje", "Si el correo existe, enviamos un enlace para restablecer tu contraseña.");
        return "/login/forgot-password";
    }

    @GetMapping("/password/reset")
    public String mostrarFormularioReset(@RequestParam("token") String token,
                                         Model model) {

        Optional<Usuario> opt = usuarioService.findByTokenResetPassword(token);

        if (opt.isEmpty() || opt.get().getExpiracionResetPass().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "El enlace es inválido o ha expirado.");
            return "error";
        }

        model.addAttribute("token", token);
        return "/login/reset-password-form";
    }

    @PostMapping("/password/reset")
    public String procesarResetPassword(@RequestParam("token") String token,
                                        @RequestParam("password") String password,
                                        // Cambiamos Model por RedirectAttributes para post-redirect-get
                                        RedirectAttributes redirectAttributes) {

        try {
            // El servicio maneja TODA la lógica, validaciones, codificación y guardado.
            usuarioService.restablecerContrasena(token, password);

            // Mensaje de éxito que aparece después de la redirección
            redirectAttributes.addFlashAttribute(
                    "registroExitoso", // o 'mensaje'
                    "Tu contraseña ha sido restablecida con éxito. Puedes iniciar sesión."
            );
            return "redirect:/login";

        } catch (RuntimeException e) {
            // Mensaje de error que aparece después de la redirección
            redirectAttributes.addFlashAttribute("errorRegistro", e.getMessage());

            // Lo más limpio es redirigir al login o a la página de error
            // para que el usuario pueda intentar reenviar el enlace o desbloquear.
            return "redirect:/login";
        }
    }
}
