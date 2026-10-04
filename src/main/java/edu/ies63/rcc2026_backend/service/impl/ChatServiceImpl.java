package edu.ies63.rcc2026_backend.service.impl;

import edu.ies63.rcc2026_backend.dto.ChatRequestDTO;
import edu.ies63.rcc2026_backend.dto.ChatResponseDTO;
import edu.ies63.rcc2026_backend.exception.ChatIAException;
import edu.ies63.rcc2026_backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.resource.ResourceTransformer;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

//    private final OllamaChatModel chatModel;
//    private final ResourceTransformer resourceTransformer;
    private final ChatClient chatClient;   // @Bean de ChatConfig (nivel 2)

    @Override
    public ChatResponseDTO chatear(ChatRequestDTO request) {
        String id = resolverId(request.conversacionId());
        try {
            String respuesta = chatClient.prompt()
                    .user(request.mensaje())
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, id))
                    .call()
                    .content();
            return new ChatResponseDTO(id, respuesta, LocalDateTime.now());
        } catch (Exception ex) {
            log.error("Error al consultar el modelo", ex);
            throw new ChatIAException("Sin respuesta del modelo", ex);
        }    }
    @Override
    public Flux<String> chatearStream(ChatRequestDTO request) {
        String id = resolverId(request.conversacionId());
        return chatClient.prompt()
                .user(request.mensaje())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, id))
                .stream()                  // en vez de call()
                .content()                 // Flux<String>
                .onErrorResume(ex -> {
                    log.error("Error en streaming", ex);
                    return Flux.just("[ERROR] Asistente no disponible");
                });
    }

    private String resolverId(String id) {
        return (id == null || id.isBlank()) ? UUID.randomUUID().toString() : id;
    }

}
