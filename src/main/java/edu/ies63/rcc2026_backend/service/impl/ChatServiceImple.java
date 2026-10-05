package edu.ies63.rcc2026_backend.service.impl;

import edu.ies63.rcc2026_backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class ChatServiceImple implements ChatService {

    private final OllamaChatModel ollamaChatModel;
    @Override
    public String preguntar(String pregunta) {
        log.info("pregunta:" + pregunta );
        return  ollamaChatModel.call(pregunta);
    }
}
