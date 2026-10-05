package edu.ies63.rcc2026_backend.controller;


import edu.ies63.rcc2026_backend.service.ChatService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/auth/ai")
public class ChatController {
    private final ChatService chatService;
    @GetMapping
    public String preguntar(String pregunta) {
       return chatService.preguntar(pregunta);
    }
}

