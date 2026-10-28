package edu.ies63.rcc2026_backend.security.auth;

import edu.ies63.rcc2026_backend.dto.*;
import edu.ies63.rcc2026_backend.security.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthService authService;

  AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  @Operation(summary = "Iniciar sesión y obtener JWT")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
    return ResponseEntity.ok(authService.authenticate(request));
  }

  /*
      @PostMapping("/register")
      @Operation(summary = "Registrar nuevo usuario")
      public ResponseEntity<AuthResponse> register(
              @Valid @RequestBody UsuarioCreateDTO request) {

          return ResponseEntity
                  .status(HttpStatus.CREATED)
                  .body(authService.register(request));
      }*/
  @PostMapping("/register/usuario")
  @Operation(summary = "Registrar usuario estándar")
  public ResponseEntity<RegistroResponseDTO> registerUsuario(
      @RequestBody @Valid UsuarioCreateDTO request) {
    RegistroResponseDTO response = authService.registerUsuario(request);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(response);
  }

  @PostMapping("/register/organizador")
  @Operation(summary = "Registrar organizador")
  public ResponseEntity<RegistroResponseDTO> registerOrganizador(
      @RequestBody @Valid OrganizadorCreateDTO request) {
    RegistroResponseDTO response = authService.registerOrganizador(request);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(response);
  }

  @PostMapping("/register/grupo")
  @Operation(summary = "Registrar grupo")
  public ResponseEntity<RegistroResponseDTO> registerGrupo(
      @RequestBody @Valid GrupoCreateDTO request) {
    RegistroResponseDTO response = authService.registerGrupo(request);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(response);
  }

  @PostMapping("/forgot-password")
  @Operation(summary = "Olvide Contraseña")
  public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
    authService.requestPasswordReset(request);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/reset-password")
  @Operation(summary = "Reiniciar Contraseña Olvidada")
  public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    authService.resetPassword(request);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/verify-account")
  @Operation(summary = "Verificar registro de cuenta mediante token enviado al correo")
  public ResponseEntity<Map<String, String>> verifyAccount(@RequestParam String token) {
    authService.verifyAccount(token);
    return ResponseEntity.ok(Map.of("message", "Cuenta verificada con éxito. Ya puedes iniciar sesión."));
  }

  @PostMapping("/resend-verification")
  public ResponseEntity<Map<String, String>> resendVerification(@RequestParam String email) {
    authService.resendVerificationEmail(email);
    // Siempre devolvemos el mismo mensaje para no revelar si el correo existe o no
    return ResponseEntity.ok(Map.of("message", "Si el correo está registrado y sin verificar, se enviará un nuevo enlace."));
  }
}
