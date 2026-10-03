package edu.ies63.rcc2026_backend.controller;

import edu.ies63.rcc2026_backend.dto.LikeResponseDTO;
import edu.ies63.rcc2026_backend.service.PostLikeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostLikeControllerTest {

    @Mock
    private PostLikeService postLikeService;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private PostLikeController postLikeController;

    @Test
    @DisplayName("Llama al servicio con postId y username del UserDetails autenticado")
    void toggleLike_LlamaServicioYRetorna200() {
        Long postId = 15L;
        String username = "usuario@test.com";

        when(userDetails.getUsername()).thenReturn(username);
        when(postLikeService.toggleLike(postId, username))
                .thenReturn(new LikeResponseDTO(postId, true, 5L));

        ResponseEntity<LikeResponseDTO> response = postLikeController.toggleLike(postId, userDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().postId()).isEqualTo(15L);
        assertThat(response.getBody().liked()).isTrue();
        assertThat(response.getBody().totalLikes()).isEqualTo(5L);

        verify(postLikeService).toggleLike(postId, username);
    }
}