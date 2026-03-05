package com.alzheimer.Chat_Service.controllers;

import com.alzheimer.Chat_Service.dto.MessageRequestDTO;
import com.alzheimer.Chat_Service.dto.MessageResponseDTO;
import com.alzheimer.Chat_Service.services.MessageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/send")
    public MessageResponseDTO sendMessage(@RequestBody MessageRequestDTO request) {
        return messageService.saveMessage(request);
    }

    @GetMapping("/conversation")
    public List<MessageResponseDTO> getConversation(
            @RequestParam String user1,
            @RequestParam String user2) {
        return messageService.getConversation(user1, user2);
    }

    @GetMapping("/all")
    public List<MessageResponseDTO> getAllMessages() {
        return messageService.getAllMessages();
    }

    @PutMapping("/{messageId}")
    public MessageResponseDTO updateMessage(
            @PathVariable String messageId,
            @RequestBody String newContent) {
        return messageService.updateMessage(messageId, newContent);
    }

    @DeleteMapping("/{messageId}")
    public void deleteMessage(@PathVariable String messageId) {
        messageService.deleteMessage(messageId);
    }

    @DeleteMapping("/conversation")
    public void deleteConversation(
            @RequestParam String user1,
            @RequestParam String user2) {
        messageService.deleteConversation(user1, user2);
    }
}
