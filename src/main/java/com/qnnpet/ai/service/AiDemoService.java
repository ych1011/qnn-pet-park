package com.qnnpet.ai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiDemoService {

    private final ChatClient chatClient;


    public AiDemoService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public String chat(String prompt) {
        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}
