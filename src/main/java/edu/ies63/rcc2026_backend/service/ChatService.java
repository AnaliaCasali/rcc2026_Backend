package edu.ies63.rcc2026_backend.service;

import edu.ies63.rcc2026_backend.dto.ChatRequestDTO;
import edu.ies63.rcc2026_backend.dto.ChatResponseDTO;
import reactor.core.publisher.Flux;

public interface ChatService {
    ChatResponseDTO chatear(ChatRequestDTO pregunta);
    Flux<String> chatearStream(ChatRequestDTO request); // en vivo

}
