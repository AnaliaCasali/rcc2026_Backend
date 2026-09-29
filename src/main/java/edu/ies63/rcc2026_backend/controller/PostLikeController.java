package edu.ies63.rcc2026_backend.controller;

import edu.ies63.rcc2026_backend.dto.LikeResponseDTO;
import edu.ies63.rcc2026_backend.service.PostLikeService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping("/{postId}/like")
    @Operation(summary = "Dar Like")
    public ResponseEntity<LikeResponseDTO> toggleLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {

        LikeResponseDTO response = postLikeService.toggleLike(postId, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}