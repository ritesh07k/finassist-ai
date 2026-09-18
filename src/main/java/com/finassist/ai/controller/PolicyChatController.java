package com.finassist.ai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
public class PolicyChatController {

    private final ChatClient chatClient;

    public PolicyChatController(
            ChatClient.Builder chatClientBuilder,
            VectorStore vectorStore) {

        // Create the ChatClient that talks to our Qwen model.
        this.chatClient = chatClientBuilder
        .defaultAdvisors(
                QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(SearchRequest.builder()
                                .topK(2)
                                .similarityThreshold(0.55)
                                .build())
                        .build()
        )
        .build();
    }

    @GetMapping("/policy")
    public String policyChat(@RequestParam String message) {

        // Send the user's question to the LLM.
        //
        // QuestionAnswerAdvisor automatically:
        // 1. Searches the vector store.
        // 2. Retrieves relevant documents.
        // 3. Adds them to the LLM context.
        // 4. Sends the augmented prompt to Qwen.
        // 5. Returns the generated response.
        return chatClient
                .prompt()
                .user(message)
                .call()
                .content();
    }
}