package com.alzheimer.Chat_Service.controllers;

import com.alzheimer.Chat_Service.dto.MessageRequestDTO;
import com.alzheimer.Chat_Service.dto.MessageResponseDTO;
import com.alzheimer.Chat_Service.services.MessageService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    private final MessageService messageService;

    public ChatController(MessageService messageService) {
        this.messageService = messageService;
    }

    @MessageMapping("/sendMessage")
    @SendTo("/topic/private")
    public MessageResponseDTO sendMessage(MessageRequestDTO messageRequest) {
        return messageService.saveMessage(messageRequest);
    }
}
