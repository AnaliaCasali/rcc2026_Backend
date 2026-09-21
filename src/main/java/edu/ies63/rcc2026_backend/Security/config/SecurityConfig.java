package edu.ies63.rcc2026_backend.Security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        // En APIs stateless no usamos cookies/sesión → se desactiva (lo trae ACTIVO)
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            // PÚBLICAS: cualquiera, sin login
            .requestMatchers("/api/v1/auth/").permitAll()
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
            // POR ROL: la autorización en acción
            // .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
            // RESTO: solo usuarios logueados
            .anyRequest().authenticated()
        )
        //se habilita HTTP Basic usando la configuración predeterminada
        // de Spring Security
        .httpBasic(Customizer.withDefaults());
    return http.build();
  }
}
