package com.ceqb.SistemaVentaDispositivos2025.config;

import com.ceqb.SistemaVentaDispositivos2025.dto.UsuarioDTO;
import com.ceqb.SistemaVentaDispositivos2025.service.UsuarioService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    private static final Set<String> PUBLIC_PREFIXES = Set.of(
            "/productos/img", "/uploads"
    );

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/",
            "/tienda",
            "/tienda/**",
            "/login",
            "/registrar",
            "/desbloquearUsuario",
            "/verificar",
            "/reenviar-verificacion",
            "/reenviar-codigo",
            "/password/forgot",
            "/error",
            "/carrito",
            "/carrito/agregar",
            "/carrito/aumentar",
            "/carrito/disminuir",
            "/carrito/eliminar",
            "/carrito/vaciar",
            "/mercadopago/webhook",
            "/productos/registrar-click",
            "/pedidos/createPagoContraEntrega",
            "/cambiar-clave",
            "/favoritos",
            "/guardar-nueva-clave",
            "/pedidos/test-notificacion",
            "/topic/notificaciones",
            "/topic/entregas-completadas",
            "/AdminLTE/**",
            "/user/notificaciones",
            "/ws",
            "/ws/**"

    );

    private static final Set<String> ADMIN_PATHS = Set.of(
            "/admin","/Administrador"

    );

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String uri = request.getRequestURI();
        String pathSinContexto = uri.substring(request.getContextPath().length());
        HttpSession session = request.getSession(false);

        boolean esRutaPublica = PUBLIC_PREFIXES.stream().anyMatch(pathSinContexto::startsWith) ||
                PUBLIC_PATHS.contains(pathSinContexto);

        if (esRutaPublica) {

            return true;
        }

        if (session != null && session.getAttribute("usuarioLogueado") != null) {
            UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuarioLogueado");

            boolean esRutaAdmin = ADMIN_PATHS.stream().anyMatch(pathSinContexto::startsWith);
            if (esRutaAdmin && !"Administrador".equalsIgnoreCase(usuario.getNombreCargo())) {

                response.sendRedirect(request.getContextPath() + "/tienda");
                return false;
            }

            return true;
        }

        String acceptHeader = request.getHeader("Accept");
        boolean esLlamadaApi = acceptHeader != null && acceptHeader.contains("application/json");

        if (esLlamadaApi) {
            // Guardamos la URL de la página que hace la llamada a la API
            String referrer = request.getHeader("referer");
            if (referrer != null && referrer.contains("/carrito")) {
                HttpSession sesionActiva = request.getSession(true);
                sesionActiva.setAttribute("redirectAfterLogin", "/carrito");
            }
            // Se envía el 401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"No autorizado. Inicia sesión.\"}");
            return false;
        } else {
            // Si es una petición de página normal, redirigimos directamente
            HttpSession sesionActiva = request.getSession(true);
            // 🚀 Si estaba intentando pagar, redirigimos al carrito con el parámetro adecuado
            if (pathSinContexto.equals("/pedidos/createPagoContraEntrega")) {
                sesionActiva.setAttribute("redirectAfterLogin", "/carrito?finalizar=pagoContraentrega");
            } else {
                sesionActiva.setAttribute("redirectAfterLogin", pathSinContexto);
            }

            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
    }
}
