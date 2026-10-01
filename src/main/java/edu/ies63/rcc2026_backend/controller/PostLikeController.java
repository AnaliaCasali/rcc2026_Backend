package edu.ies63.rcc2026_backend.controller;

import edu.ies63.rcc2026_backend.dto.LikeResponseDTO;
import edu.ies63.rcc2026_backend.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts/{postId}/likes")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService likeService;

    @PostMapping("/toggle")
    public ResponseEntity<LikeResponseDTO> toggleLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {

        LikeResponseDTO resultado = likeService.toggleLike(postId, userDetails.getUsername());
        return ResponseEntity.ok(resultado);
    }
}