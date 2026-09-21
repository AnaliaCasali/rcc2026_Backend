package edu.ies63.rcc2026_backend.security.jwt;

import edu.ies63.rcc2026_backend.service.impl.UsuarioServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioServiceImpl userDetailsService;

    JwtAuthFilter(JwtService jwtService,
                  UsuarioServiceImpl userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    // AntPathMatcher permite patrones tipo /swagger-ui/** en lugar de startsWith
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private static final List<String> WHITE_LIST = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/actuator/health"
    );

    // Spring Security 7: shouldNotFilter reemplaza la lógica manual de whitelist
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest req) {
        String path = req.getRequestURI();
        return WHITE_LIST.stream()
                .anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest req,
                                    @NonNull HttpServletResponse res,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {

        final String token = extractToken(req);

        if (token == null) {
            chain.doFilter(req, res);
            return;
        }

        try {
            final String username = jwtService.extractUsername(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails user = userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(token, user)) {
                    setAuthentication(user, req);
                }
            }
            chain.doFilter(req, res);

        } catch (ExpiredJwtException ex) {
            log.warn("Token expirado para request: {}", req.getRequestURI());
            sendUnauthorized(res, "Token expirado");

        } catch (JwtException e) {
            log.warn("Token JWT inválido: {}", e.getMessage());
            sendUnauthorized(res, "Token inválido");

        } catch (Exception e) {
            log.error("Error inesperado en JwtAuthFilter", e);
            sendUnauthorized(res, "Error de autenticación");
        }
    }

    private void setAuthentication(UserDetails user, HttpServletRequest req) {
        var authToken = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities()
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private String extractToken(HttpServletRequest req) {
        // HttpHeaders.AUTHORIZATION en lugar del String "Authorization" hardcodeado
        final String header = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) return null;
        return header.substring(7);
    }

    private void sendUnauthorized(HttpServletResponse res, String message) throws IOException {
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write(
                "{\"error\":\"Unauthorized\",\"message\":\"%s\"}".formatted(message)
        );
    }
}
