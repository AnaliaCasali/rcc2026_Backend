package edu.ies63.rcc2026_backend.service;

import edu.ies63.rcc2026_backend.dto.LikeResponseDTO;

public interface PostLikeService {
    LikeResponseDTO toggleLike(Long postId, String username);
}