package edu.ies63.rcc2026_backend.config;

import edu.ies63.rcc2026_backend.tools.RccAppTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
@Configuration
public class ChatConfig {
    @Value("classpath:prompts/system-rcc.st")
    private Resource systemPrompt;

    @Value("classpath:prompts/rag-rcc.st")
    private Resource ragPrompt;

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder,
                                 RccAppTools rccAppTools,
                                 VectorStore vectorStore,
                                 ChatMemory chatMemory) {
        return builder
                .defaultSystem(s->s.text(systemPrompt)
                .param("sitio", "Red Cultural Conecta 2026" )
                .param("maxOraciones",4))
                .defaultTools(rccAppTools)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        QuestionAnswerAdvisor.builder(vectorStore)       // 5
                                .searchRequest(SearchRequest.builder()
                                        .similarityThreshold(0.6).topK(3).build())
                                .promptTemplate(new PromptTemplate(ragPrompt))
                                .build(),
                        new SimpleLoggerAdvisor()) // NUEVO

                .build();

    }

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(10)
                .build();
    }

}
