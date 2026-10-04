package edu.ies63.rcc2026_backend.service.impl;

import edu.ies63.rcc2026_backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.resource.ResourceTransformer;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final OllamaChatModel chatModel;
    private final ResourceTransformer resourceTransformer;

    @Override
    public String preguntar(String pregunta) {
        log.info("pregunta", pregunta);
        return chatModel.call(pregunta);
    }
}
