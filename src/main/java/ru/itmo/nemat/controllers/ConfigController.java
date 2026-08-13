package ru.itmo.nemat.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.nemat.data.MessageRepository;
import ru.itmo.nemat.dto.ChatConfigDTO;

@RestController
@RequestMapping(path = "api/v1/config")
@RequiredArgsConstructor
public class ConfigController {

    @Value("${app.chat.owner}")
    private String defaultChatOwner;

    private final MessageRepository messageRepository;

    @GetMapping
    public ChatConfigDTO getChatConfig() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        if (messageRepository.existsByAuthor(currentUsername)) {
            return new ChatConfigDTO(currentUsername);
        }

        return new ChatConfigDTO(defaultChatOwner);
    }
}
