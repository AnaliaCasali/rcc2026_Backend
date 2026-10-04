package edu.ies63.rcc2026_backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.List;

@Configuration
@Slf4j
public class RagConfig {

    @Value("classpath:docs/rcc-info.txt")
    private Resource infoSitio;

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {

        var store = SimpleVectorStore.builder(embeddingModel).build();

        // 1) leer  2) dividir  3) vectorizar y guardar
        List<Document> docs = new TextReader(infoSitio).get();
        // Fragmentos chicos (≈ una oración)
        List<Document> partes = TokenTextSplitter.builder()
                .withChunkSize(40)
                .withMinChunkSizeChars(20)
                .build()
                .apply(docs);
        store.add(partes);

        log.info("RAG listo: {} fragmentos", partes.size());
        return store;
    }
}
