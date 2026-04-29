package com.alzheimer.Chat_Service.controllers;

import com.alzheimer.Chat_Service.dto.MessageRequestDTO;
import com.alzheimer.Chat_Service.dto.MessageResponseDTO;
import com.alzheimer.Chat_Service.services.MessageService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketMessageController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketMessageController(MessageService messageService, SimpMessagingTemplate messagingTemplate) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload MessageRequestDTO message) {
        // Sauvegarder le message dans la base de données
        MessageResponseDTO savedMessage = messageService.saveMessage(message);

        // Envoyer le message au destinataire via WebSocket
        messagingTemplate.convertAndSend(
                "/queue/messages/" + message.getReceiverId(),
                savedMessage
        );

        // Envoyer aussi à l'expéditeur pour confirmation
        messagingTemplate.convertAndSend(
                "/queue/messages/" + message.getSenderId(),
                savedMessage
        );
    }

    @MessageMapping("/chat.typing")
    public void userTyping(@Payload TypingNotification notification) {
        // Notifier que l'utilisateur est en train d'écrire
        messagingTemplate.convertAndSend(
                "/queue/typing/" + notification.getReceiverId(),
                notification
        );
    }

    // Classe interne pour les notifications de frappe
    public static class TypingNotification {
        private String senderId;
        private String receiverId;
        private boolean typing;

        public TypingNotification() {
        }

        public String getSenderId() {
            return senderId;
        }

        public void setSenderId(String senderId) {
            this.senderId = senderId;
        }

        public String getReceiverId() {
            return receiverId;
        }

        public void setReceiverId(String receiverId) {
            this.receiverId = receiverId;
        }

        public boolean isTyping() {
            return typing;
        }

        public void setTyping(boolean typing) {
            this.typing = typing;
        }
    }
}
