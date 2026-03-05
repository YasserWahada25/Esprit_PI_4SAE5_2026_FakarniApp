package com.alzheimer.Chat_Service.services;

import com.alzheimer.Chat_Service.dto.MessageRequestDTO;
import com.alzheimer.Chat_Service.dto.MessageResponseDTO;
import com.alzheimer.Chat_Service.entities.Message;
import com.alzheimer.Chat_Service.repositories.MessageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public MessageResponseDTO saveMessage(MessageRequestDTO dto) {
        String conversationId = generateConversationId(dto.getSenderId(), dto.getReceiverId());
        
        Message message = new Message();
        message.setSenderId(dto.getSenderId());
        message.setReceiverId(dto.getReceiverId());
        message.setContent(dto.getContent());
        message.setConversationId(conversationId);
        message.setTimestamp(LocalDateTime.now());
        
        Message savedMessage = messageRepository.save(message);
        
        return mapToResponseDTO(savedMessage);
    }

    public List<MessageResponseDTO> getConversation(String user1, String user2) {
        String conversationId = generateConversationId(user1, user2);
        List<Message> messages = messageRepository.findByConversationIdOrderByTimestampAsc(conversationId);
        
        return messages.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<MessageResponseDTO> getAllMessages() {
        return messageRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public MessageResponseDTO updateMessage(String messageId, String newContent) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found with id: " + messageId));
        
        message.setContent(newContent);
        Message updatedMessage = messageRepository.save(message);
        
        return mapToResponseDTO(updatedMessage);
    }

    public void deleteMessage(String messageId) {
        if (!messageRepository.existsById(messageId)) {
            throw new RuntimeException("Message not found with id: " + messageId);
        }
        messageRepository.deleteById(messageId);
    }

    public void deleteConversation(String user1, String user2) {
        String conversationId = generateConversationId(user1, user2);
        List<Message> messages = messageRepository.findByConversationIdOrderByTimestampAsc(conversationId);
        messageRepository.deleteAll(messages);
    }

    private String generateConversationId(String user1, String user2) {
        int id1 = Integer.parseInt(user1);
        int id2 = Integer.parseInt(user2);
        
        if (id1 < id2) {
            return user1 + "_" + user2;
        } else {
            return user2 + "_" + user1;
        }
    }

    private MessageResponseDTO mapToResponseDTO(Message message) {
        return new MessageResponseDTO(
                message.getId(),
                message.getSenderId(),
                message.getReceiverId(),
                message.getContent(),
                message.getConversationId(),
                message.getTimestamp()
        );
    }
}
