package com.finassist.ai.controller;

import com.finassist.ai.service.TransactionToolService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/chat")
public class TransactionChatController {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public TransactionChatController(ChatClient.Builder chatClientBuilder,
                                      TransactionToolService transactionToolService) {

        this.chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(20)
                .build();

        this.chatClient = chatClientBuilder
                .defaultSystem("""
                        You are a banking assistant that answers questions about the
                        user's transaction history. Always use the available tools to
                        get exact figures — never estimate or calculate numbers yourself.

                        Today's date is %s. When the user gives a month without a year
                        (e.g. "September"), assume the most recent occurrence of that
                        month relative to today's date.

                        Treat each question independently unless it clearly refers back
                        to something specific from the prior turn (e.g. "that", "it",
                        "the same period"). A general question like "how much did I
                        spend" should use the total-spending tool, not a category filter
                        from a previous turn, unless the user explicitly asks about that
                        category again.
                        """.formatted(LocalDate.now()))
                .defaultTools(transactionToolService)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    @GetMapping("/transactions")
public String chat(@RequestParam String message,
                    @RequestParam(defaultValue = "default-session") String conversationId) {

    if (message == null || message.isBlank()) {
        throw new IllegalArgumentException("message parameter cannot be empty.");
    }

    return chatClient
            .prompt()
            .user(message)
            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
            .call()
            .content();
}
}