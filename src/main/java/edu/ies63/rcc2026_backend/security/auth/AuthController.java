package edu.ies63.rcc2026_backend.security.auth;

import edu.ies63.rcc2026_backend.dto.*;
import edu.ies63.rcc2026_backend.security.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

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
  public ResponseEntity<AuthResponse> registerUsuario(
      @Valid @RequestBody UsuarioCreateDTO request) {
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(authService.registerUsuario(request));
  }

  @PostMapping("/register/organizador")
  @Operation(summary = "Registrar organizador")
  public ResponseEntity<AuthResponse> registerOrganizador(
      @Valid @RequestBody OrganizadorCreateDTO request) {
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(authService.registerOrganizador(request));
  }

  @PostMapping("/register/grupo")
  @Operation(summary = "Registrar grupo")
  public ResponseEntity<AuthResponse> registerGrupo(
      @Valid @RequestBody GrupoCreateDTO request) {
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(authService.registerGrupo(request));
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
}
