package edu.ies63.rcc2026_backend.security.auth;

import edu.ies63.rcc2026_backend.security.dto.AuthRequest;
import edu.ies63.rcc2026_backend.security.dto.AuthResponse;
import edu.ies63.rcc2026_backend.security.dto.CambiarPasswordRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;


    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión y obtener JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }



}
