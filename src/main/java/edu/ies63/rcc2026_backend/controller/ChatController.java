package edu.ies63.rcc2026_backend.controller;

import edu.ies63.rcc2026_backend.dto.ChatRequestDTO;
import edu.ies63.rcc2026_backend.dto.ChatResponseDTO;
import edu.ies63.rcc2026_backend.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/ai")
public class ChatController {
    private final ChatService chatService;
    /*@GetMapping("preguntar")
    public String preguntar(@RequestParam String pregunta){
        return chatService.preguntar(pregunta);
    }*/

    @PostMapping("/chat") // Respuesta completa en JSON
    public ResponseEntity<ChatResponseDTO> chat(
            @Valid @RequestBody ChatRequestDTO request) {
        return ResponseEntity.ok(chatService.chatear(request));}

    // Respuesta en vivo
    @PostMapping(value = "/chat/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@Valid @RequestBody ChatRequestDTO request) {        return chatService.chatearStream(request);
    }

}
