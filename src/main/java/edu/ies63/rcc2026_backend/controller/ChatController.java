package edu.ies63.rcc2026_backend.controller;

import edu.ies63.rcc2026_backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/ai")
public class ChatController {
    private final ChatService chatService;
    @GetMapping("preguntar")
    public String preguntar(@RequestParam String pregunta){
        return chatService.preguntar(pregunta);
    }
}
