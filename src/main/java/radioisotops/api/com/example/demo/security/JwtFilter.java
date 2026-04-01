package radioisotops.api.com.example.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
        response.setHeader("Access-Control-Max-Age", "3600");

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // 1. Extraer la cabecera "Authorization"
        String authHeader = request.getHeader("Authorization");

        // 2. Si viene el Token (formato: "Bearer texto_del_token")
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            // 3. Validar el Token
            if (jwtUtil.validateToken(token)) {
                // Si es válido, guardamos el email en los atributos para que el Controller lo use
                String email = jwtUtil.extractEmail(token);
                request.setAttribute("userEmail", email);
            } else {
                // Si es falso o caducado, cortamos la conexión
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido o expirado");
                return;
            }
        } else if (!request.getRequestURI().contains("/api/auth/")) {
            // Si intenta entrar a /patients/ sin token, fuera
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Falta el token de autorización");
            return;
        }

        filterChain.doFilter(request, response);
    }
}