package edu.ies63.rcc2026_backend.exception;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("handleValidationExceptions: mapea errores de campos con 400 Bad Request")
    void handleValidationExceptions_MapeaErroresDeCampos() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError = new FieldError("objectName", "email", "Formato de correo no válido");
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleValidationExceptions(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(400);
        assertThat(response.getBody().get("message")).isEqualTo("Error de validación");

        @SuppressWarnings("unchecked")
        Map<String, String> details = (Map<String, String>) response.getBody().get("details");
        assertThat(details).containsEntry("email", "Formato de correo no válido");
    }

    @Test
    @DisplayName("handleEntityNotFound: retorna 404 con el mensaje de la excepción")
    void handleEntityNotFound_Retorna404() {
        EntityNotFoundException ex = new EntityNotFoundException("Entidad no hallada");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleEntityNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(404);
        assertThat(response.getBody().get("message")).isEqualTo("Entidad no hallada");
    }

    @Test
    @DisplayName("handleResponseStatusException: respeta el código HTTP y motivo definido")
    void handleResponseStatusException_RespetaStatusYReason() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleResponseStatusException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(401);
        assertThat(response.getBody().get("message")).isEqualTo("Credenciales incorrectas");
    }

    @Test
    @DisplayName("handleUsuarioNoEncontrado: retorna 404")
    void handleUsuarioNoEncontrado_Retorna404() {
        UsuarioNoEncontradoException ex = new UsuarioNoEncontradoException("Usuario no existe");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleUsuarioNoEncontrado(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(404);
    }

    @Test
    @DisplayName("handleGrupoNoEncontrado: retorna 404 recibiendo Long")
    void handleGrupoNoEncontrado_Retorna404() {
        GrupoNoEncontradoException ex = new GrupoNoEncontradoException(1L);

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGrupoNoEncontrado(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(404);
    }

    @Test
    @DisplayName("handleNoEsGrupo: retorna 400 Bad Request recibiendo Long")
    void handleNoEsGrupo_Retorna400() {
        NoEsGrupoException ex = new NoEsGrupoException(1L);

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleNoEsGrupo(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(400);
    }

    @Test
    @DisplayName("handleAutoSeguimiento: retorna 400 Bad Request con constructor sin argumentos")
    void handleAutoSeguimiento_Retorna400() {
        AutoSeguimientoException ex = new AutoSeguimientoException();

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleAutoSeguimiento(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(400);
        assertThat(response.getBody().get("message")).isEqualTo("Un usuario no puede seguirse a sí mismo");
    }

    @Test
    @DisplayName("handleEmailAlreadyRegistered: retorna 409 Conflict")
    void handleEmailAlreadyRegistered_Retorna409() {
        EmailYaRegistradoException ex = new EmailYaRegistradoException("El email ya está en uso");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleEmailAlreadyRegistered(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(409);
        assertThat(response.getBody().get("details")).isEqualTo("El email ya está en uso");
    }

    @Test
    @DisplayName("handleInvalidPassword: retorna 400 Bad Request")
    void handleInvalidPassword_Retorna400() {
        PasswordInvalidaException ex = new PasswordInvalidaException("Password muy corta");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleInvalidPassword(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(400);
    }

    @Test
    @DisplayName("handleImageProcessing: retorna 500 Internal Server Error con mensaje y causa")
    void handleImageProcessing_Retorna500() {
        ProcesamientoImagenException ex = new ProcesamientoImagenException(
                "Error al procesar el archivo multimedia",
                new RuntimeException("Error de E/S")
        );

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleImageProcessing(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(500);
        assertThat(response.getBody().get("details")).isEqualTo("Error al procesar el archivo multimedia");
    }

    @Test
    @DisplayName("handleGenericException: retorna 500 para errores no contemplados")
    void handleGenericException_Retorna500() {
        Exception ex = new RuntimeException("Error inesperado");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGenericException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(500);
    }

    @Test
    @DisplayName("handleBusquedaInvalidaException: retorna 400 Bad Request")
    void handleBusquedaInvalidaException_Retorna400() {
        BusquedaInvalidaException ex = new BusquedaInvalidaException("Coordenadas incompletas");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleBusquedaInvalidaException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(400);
        assertThat(response.getBody().get("message")).isEqualTo("Coordenadas incompletas");
    }

    @Test
    @DisplayName("handlePostNotFoundException: retorna 404 Not Found")
    void handlePostNotFoundException_Retorna404() {
        PostNotFoundException ex = new PostNotFoundException("Post 123 no existe");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handlePostNotFoundException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(404);
        assertThat(response.getBody().get("message")).isEqualTo("Post 123 no existe");
    }

    @Test
    @DisplayName("handleBadCredentials: retorna 401 Unauthorized")
    void handleBadCredentials_Retorna401() {
        org.springframework.security.authentication.BadCredentialsException ex =
            new org.springframework.security.authentication.BadCredentialsException("Credenciales inválidas");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleBadCredentials(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(401);
        assertThat(response.getBody().get("message")).isEqualTo("Credenciales incorrectas");
    }

    @Test
    @DisplayName("handleEventoConflict: retorna 409 Conflict")
    void handleEventoConflict_Retorna409() {
        EventoConflictException ex = new EventoConflictException("CODIGO_TEST", "Mensaje de conflicto");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleEventoConflict(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(409);
        assertThat(response.getBody().get("message")).isEqualTo("Mensaje de conflicto");
        assertThat(response.getBody().get("details")).isNotNull();
    }

    @Test
    @DisplayName("handleEventoNoPermitido: retorna 403 Forbidden")
    void handleEventoNoPermitido_Retorna403() {
        EventoNoPermitidoException ex = new EventoNoPermitidoException("Acción no permitida");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleEventoNoPermitido(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(403);
        assertThat(response.getBody().get("message")).isEqualTo("Acción no permitida");
    }
}