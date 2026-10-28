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
import edu.ies63.rcc2026_backend.model.VerificationToken;
import edu.ies63.rcc2026_backend.repository.LocalidadRepository;
import edu.ies63.rcc2026_backend.repository.PasswordResetTokenRepository;
import edu.ies63.rcc2026_backend.repository.UsuarioRepository;
import edu.ies63.rcc2026_backend.repository.VerificationTokenRepository;
import edu.ies63.rcc2026_backend.security.dto.*;
import edu.ies63.rcc2026_backend.security.jwt.JwtService;
import edu.ies63.rcc2026_backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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
  private final VerificationTokenRepository verificationTokenRepository;


  public AuthResponse authenticate(AuthRequest request) {
    Usuario user = userRepository.findByUsername(request.username())
        .orElseThrow(() -> new BadCredentialsException("Credenciales incorrectas"));

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new BadCredentialsException("Credenciales incorrectas");
    }

    if (!user.getVerificado()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cuenta no verificada. Por favor revisa tu correo.");
    }

    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.username(), request.password())
    );

    Map<String, Object> extraClaims = new HashMap<>();
    extraClaims.put("rol", user.getRole().name());
    extraClaims.put("id", user.getId());
    extraClaims.put("name", user.getNombre());
    String token = jwtService.generateToken(extraClaims, user);

    return new AuthResponse(token);
  }

  @Transactional
  public RegistroResponseDTO registerUsuario(UsuarioCreateDTO request) {
    validarUsernameDisponible(request.username());
    Localidad localidad = resolverLocalidad(request.localidadId());

    Usuario user = Usuario.builder()
        .nombre(request.nombre())
        .username(request.username())
        .password(passwordEncoder.encode(request.password()))
        .biografia(request.biografia())
        .localidad(localidad)
        .role(Role.USUARIO)
        .debaja(false)
        .verificado(false)
        .fechaAceptacionTerminos(LocalDateTime.now()) // NUEVO: aceptó los términos al registrarse
        .build();
    userRepository.save(user);

    generarYEnviarTokenVerificacion(user);

    return new RegistroResponseDTO(
        "Usuario registrado con éxito. Por favor, revisá tu correo para activar la cuenta.",
        user.getUsername()
    );
  }

  @Transactional
  public RegistroResponseDTO registerOrganizador(OrganizadorCreateDTO request) {
    validarUsernameDisponible(request.username());

    Localidad localidad = resolverLocalidad(request.localidadId());

    Usuario user = Usuario.builder()
        .nombre(request.nombre())
        .username(request.username())
        .password(passwordEncoder.encode(request.password()))
        .biografia(request.biografia())
        .director(request.director())
        .localidad(localidad)
        .role(Role.ORGANIZADOR)
        .debaja(false)
        .verificado(false)
        .fechaAceptacionTerminos(LocalDateTime.now()) // NUEVO: aceptó los términos al registrarse
        .build();

    userRepository.save(user);

    generarYEnviarTokenVerificacion(user);

    return new RegistroResponseDTO(
        "Usuario registrado con éxito. Por favor, revisá tu correo para activar la cuenta.",
        user.getUsername()
    );
  }

  @Transactional
  public RegistroResponseDTO registerGrupo(GrupoCreateDTO request) {
    validarUsernameDisponible(request.username());

    Localidad localidad = resolverLocalidad(request.localidadId());

    Usuario user = Usuario.builder()
        .nombre(request.nombre())
        .username(request.username())
        .password(passwordEncoder.encode(request.password()))
        .biografia(request.biografia())
        .director(request.director())
        .localidad(localidad)
        .role(Role.GRUPO)
        .debaja(false)
        .verificado(false)
        .fechaAceptacionTerminos(LocalDateTime.now()) // NUEVO: aceptó los términos al registrarse
        .build();

    userRepository.save(user);

    generarYEnviarTokenVerificacion(user);

    return new RegistroResponseDTO(
        "Usuario registrado con éxito. Por favor, revisá tu correo para activar la cuenta.",
        user.getUsername()
    );
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
            .orElse(null);

    if (user == null) {
      return;
    }

    Optional<PasswordResetToken> tokenPrevio = resetTokenRepository.findByUsuario(user);
    if (tokenPrevio.isPresent()) {
      LocalDateTime fechaCreacionEstimada = tokenPrevio.get().getExpiryDate().minusMinutes(15);
      if (fechaCreacionEstimada.plusMinutes(2).isAfter(LocalDateTime.now())) {
        throw new ResponseStatusException(
                HttpStatus.TOO_MANY_REQUESTS,
                "Ya enviamos un enlace a tu correo. Por favor, esperá al menos 2 minutos antes de solicitar otro."
        );
      }
    }

    resetTokenRepository.deleteByUsuario(user);
    resetTokenRepository.flush();

    String token = UUID.randomUUID().toString();
    PasswordResetToken resetToken = PasswordResetToken.builder()
            .token(token)
            .usuario(user)
            .expiryDate(LocalDateTime.now().plusMinutes(15))
            .build();

    resetTokenRepository.save(resetToken);

    try {
      emailService.sendPasswordResetEmail(user.getUsername(), token);
    } catch (Exception e) {
      throw new ResponseStatusException(
              HttpStatus.TOO_MANY_REQUESTS,
              "Ya enviamos un correo recientemente. Por favor, revisá tu casilla o aguardá unos minutos antes de solicitar otro enlace."
      );
    }
  }

  @Transactional
  public void resetPassword(ResetPasswordRequest request) {
    if (request.newPassword() == null || request.newPassword().length() < 8) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La nueva contraseña debe tener al menos 8 caracteres");
    }

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

  // Verificacion de email para registro
  @Transactional
  public void verifyAccount(String token) {
    VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token inválido o no existe"));

    if (verificationToken.isExpired()) {
      verificationTokenRepository.delete(verificationToken);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El token de verificación ha expirado");
    }

    Usuario user = verificationToken.getUsuario();
    user.setVerificado(true);
    userRepository.save(user);

    // Se elimina el token una vez utilizado con éxito
    verificationTokenRepository.delete(verificationToken);
  }

  private void generarYEnviarTokenVerificacion(Usuario user) {
    String token = UUID.randomUUID().toString();
    VerificationToken verificationToken = VerificationToken.builder()
        .token(token)
        .usuario(user)
        .expiryDate(LocalDateTime.now().plusHours(24))
        .build();
    verificationTokenRepository.save(verificationToken);
    emailService.sendVerificationEmail(user.getUsername(), token);
  }

  @Transactional
  public void resendVerificationEmail(String email) {
    Usuario user = userRepository.findByUsername(email).orElse(null);

    if (user == null || user.getVerificado()) {
      return;
    }

    // Control de Spam (Rate Limiting)
    Optional<VerificationToken> tokenPrevio = verificationTokenRepository.findByUsuario(user);
    if (tokenPrevio.isPresent()) {
      LocalDateTime fechaCreacionEstimada = tokenPrevio.get().getExpiryDate().minusHours(24);

      if (fechaCreacionEstimada.plusMinutes(2).isAfter(LocalDateTime.now())) {
        throw new ResponseStatusException(
            HttpStatus.TOO_MANY_REQUESTS,
            "Ya enviamos un enlace a tu correo. Por favor, esperá al menos 2 minutos antes de solicitar otro."
        );
      }
    }

    verificationTokenRepository.deleteByUsuario(user);
    verificationTokenRepository.flush();

    generarYEnviarTokenVerificacion(user);
  }
}
