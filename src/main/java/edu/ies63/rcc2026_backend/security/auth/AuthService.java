package edu.ies63.rcc2026_backend.security.auth;

import edu.ies63.rcc2026_backend.dto.GrupoCreateDTO;
import edu.ies63.rcc2026_backend.dto.OrganizadorCreateDTO;
import edu.ies63.rcc2026_backend.dto.UsuarioCreateDTO;
import edu.ies63.rcc2026_backend.enums.Role;
import edu.ies63.rcc2026_backend.exception.EmailYaRegistradoException;
import edu.ies63.rcc2026_backend.exception.PasswordInvalidaException;
import edu.ies63.rcc2026_backend.model.Localidad;
import edu.ies63.rcc2026_backend.model.PasswordResetToken;
import edu.ies63.rcc2026_backend.model.Usuario;
import edu.ies63.rcc2026_backend.repository.LocalidadRepository;
import edu.ies63.rcc2026_backend.repository.PasswordResetTokenRepository;
import edu.ies63.rcc2026_backend.repository.UsuarioRepository;
import edu.ies63.rcc2026_backend.security.dto.*;
import edu.ies63.rcc2026_backend.security.jwt.JwtService;
import edu.ies63.rcc2026_backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UsuarioRepository userRepository;
  private final LocalidadRepository localidadRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  private final PasswordResetTokenRepository resetTokenRepository;
  private final EmailService emailService;


  public AuthResponse authenticate(AuthRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.username(),
            request.password()
        )
    );

    Usuario user = userRepository.findByUsername(request.username())
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.UNAUTHORIZED,
            "Usuario no encontrado: " + request.username()));

/*        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", user.getRole().name());

        return new AuthResponse(jwtService.generateToken(claims, user));*/
    return guardarYGenerarToken(user);

  }

  @Transactional
  public AuthResponse registerUsuario(UsuarioCreateDTO request) {
    validarUsernameDisponible(request.username());
    Localidad localidad = resolverLocalidad(request.localidadId());

    Usuario user = Usuario.builder()
        .nombre(request.nombre())
        .username(request.username())
        .password(passwordEncoder.encode(request.password()))
        .biografia(request.biografia())
        .localidad(localidad)
        .role(Role.USUARIO)          // ← rol FIJO, no viene del cliente
        .debaja(false)
        .build();

    return guardarYGenerarToken(user);
  }

  @Transactional
  public AuthResponse registerOrganizador(OrganizadorCreateDTO request) {
    validarUsernameDisponible(request.username());

    Localidad localidad = resolverLocalidad(request.localidadId());

    Usuario user = Usuario.builder()
        .nombre(request.nombre())
        .username(request.username())
        .password(passwordEncoder.encode(request.password()))
        .biografia(request.biografia())
        .director(request.director())
        .localidad(localidad)
        .role(Role.ORGANIZADOR)      // ← rol FIJO
        .debaja(false)
        .build();

    return guardarYGenerarToken(user);
  }

  @Transactional
  public AuthResponse registerGrupo(GrupoCreateDTO request) {
    validarUsernameDisponible(request.username());

    Localidad localidad = resolverLocalidad(request.localidadId());

    Usuario user = Usuario.builder()
        .nombre(request.nombre())
        .username(request.username())
        .password(passwordEncoder.encode(request.password()))
        .biografia(request.biografia())
        .director(request.director())
        .localidad(localidad)
        .role(Role.GRUPO)            // ← rol FIJO
        .debaja(false)
        .build();

    return guardarYGenerarToken(user);
  }

  // métodos privados para reutilizar

  private AuthResponse guardarYGenerarToken(Usuario user) {
    userRepository.save(user);

    Map<String, Object> claims = new HashMap<>();
    claims.put("rol", user.getRole().name());
    claims.put("id", user.getId());
    claims.put("name", user.getNombre());

    return new AuthResponse(jwtService.generateToken(claims, user));
  }

  private void validarUsernameDisponible(String username) {
    if (userRepository.findByUsername(username).isPresent()) {
      throw new EmailYaRegistradoException("El usuario ya existe: " + username);
    }
  }

  private Localidad resolverLocalidad(Long localidadId) {
    return Optional.ofNullable(localidadId)
        .map(id -> localidadRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Localidad no encontrada con ID: " + id)))
        .orElse(null);
  }

  @Transactional
  public void requestPasswordReset(ForgotPasswordRequest request) {
    Usuario user = userRepository.findByUsername(request.email())
            .or(() -> userRepository.findByUsername(request.email()))
            .orElse(null);

    if (user == null) {
      return;
    }

    // 1. Limpiar tokens anteriores asignados a este usuario
    resetTokenRepository.deleteByUsuario(user);

    // 2. Generar token único con expiración de 15 minutos
    String token = UUID.randomUUID().toString();
    PasswordResetToken resetToken = PasswordResetToken.builder()
            .token(token)
            .usuario(user)
            .expiryDate(LocalDateTime.now().plusMinutes(15))
            .build();

    resetTokenRepository.save(resetToken);

    // 3. Enviar correo electrónico
    emailService.sendPasswordResetEmail(user.getUsername(), token);
  }

  @Transactional
  public void resetPassword(ResetPasswordRequest request) {
    if (!request.newPassword().equals(request.confirmationPassword())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las contraseñas no coinciden");
    }

    PasswordResetToken resetToken = resetTokenRepository.findByToken(request.token())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token de recuperación inválido o inexistente"));

    if (resetToken.isExpired()) {
      resetTokenRepository.delete(resetToken);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El token de recuperación ha expirado");
    }

    // Actualizar contraseña con hash BCrypt
    Usuario user = resetToken.getUsuario();
    user.setPassword(passwordEncoder.encode(request.newPassword()));
    userRepository.save(user);

    // Eliminar token para que no pueda reutilizarse
    resetTokenRepository.delete(resetToken);
  }
}
