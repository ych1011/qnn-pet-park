package com.qnnpet.controller;

import com.qnnpet.ai.service.AiDemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/demo")
@RequiredArgsConstructor
public class AiDemoController {
    private final AiDemoService aiDemoService;

    @GetMapping("/chat")
    public String chat(@RequestParam String prompt) {
        return aiDemoService.chat(prompt);
    }


}
