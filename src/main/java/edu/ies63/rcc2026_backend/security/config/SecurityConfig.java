package edu.ies63.rcc2026_backend.security.config;

import edu.ies63.rcc2026_backend.security.jwt.JwtAuthFilter;
import edu.ies63.rcc2026_backend.service.impl.UsuarioServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

  // para usar seguridad de JWT inyecto service.
  private final JwtAuthFilter jwtAuthFilter;
  private final UsuarioServiceImpl usuarioServiceImpl;

  @Value("${app.cors.allowed-origins}")
  private List<String> allowedOrigins;

  //constructor
  SecurityConfig(JwtAuthFilter jwtAuthFilter, UsuarioServiceImpl usuarioServiceImpl){
    this.jwtAuthFilter = jwtAuthFilter;
    this.usuarioServiceImpl = usuarioServiceImpl;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http

            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            .csrf(csrf -> csrf.disable())

            // Configurar manejo de sesiones como STATELESS
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .securityMatcher("/api/**")
            // Reglas de autorización
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                .requestMatchers("/api/v1/auth/login").permitAll()
                .requestMatchers("/api/v1/auth/register/**").permitAll()
                .requestMatchers("/api/v1/auth/forgot-password").permitAll()
                .requestMatchers("/api/v1/auth/reset-password").permitAll()

                .requestMatchers(HttpMethod.GET, "/api/v1/auth/usuarios/{id}/perfil").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/auth/usuarios/lista-usuarios").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/localidades").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/posts").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/auth/usuarios/{id}/posts").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/auth/usuarios/{id}/premios").permitAll()

                .requestMatchers(HttpMethod.GET, "/api/v1/posts/premios").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/eventos").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/eventos/proximos").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/eventos/{id:[0-9]+}").permitAll()

                .requestMatchers("/api/v1/auth/usuarios/cambiar-password").authenticated()
                .requestMatchers("/api/v1/auth/usuarios/perfil/mi-perfil").authenticated()

                .requestMatchers(HttpMethod.POST, "/api/v1/posts/crear").authenticated()
                .requestMatchers(HttpMethod.PATCH, "/api/v1/posts/editar/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/v1/posts/eliminar/**").authenticated()

                .requestMatchers("/api/v1/eventos/mis-invitaciones").authenticated()
                .requestMatchers("/api/v1/eventos/invitaciones/**").authenticated()
                .requestMatchers("/api/v1/eventos/crear").authenticated()
                .requestMatchers("/api/v1/eventos/editar/**").authenticated()

                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
            ).authenticationProvider(authenticationProvider())

            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .headers(h -> h
                    .frameOptions(frame -> frame.disable()))
            .build();}


  @Bean
  AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider provider = new
            DaoAuthenticationProvider(usuarioServiceImpl);
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
  }

  @Bean
  public AuthenticationManager authenticationManager(
          AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // Orígenes permitidos (URLs donde corre el frontend)
    configuration.setAllowedOrigins(allowedOrigins);

    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

    configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));

    configuration.setExposedHeaders(List.of("Authorization"));

    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
