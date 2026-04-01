package com.alzheimer.post_service.services;

import com.alzheimer.post_service.dto.ReactionCountResponse;
import com.alzheimer.post_service.dto.ReactionRequest;
import com.alzheimer.post_service.entities.Post;
import com.alzheimer.post_service.entities.Reaction;
import com.alzheimer.post_service.entities.Reaction.ReactionType;
import com.alzheimer.post_service.repositories.PostRepository;
import com.alzheimer.post_service.repositories.ReactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReactionServiceTest {

    @Mock
    private ReactionRepository reactionRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private ReactionService reactionService;

    private Post testPost;
    private Long userId;
    private Long postId;

    @BeforeEach
    void setUp() {
        postId = 1L;
        userId = 1L;
        testPost = new Post();
        testPost.setId(postId);
        testPost.setContent("Test post");
    }

    @Test
    void toggleReaction_ShouldCreateNewReaction_WhenNoExistingReaction() {
        // Arrange
        ReactionRequest request = new ReactionRequest(userId, ReactionType.LIKE);
        when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
        when(reactionRepository.findByPostIdAndUserId(postId, userId)).thenReturn(Optional.empty());
        when(reactionRepository.countByPostIdAndType(postId, ReactionType.LIKE)).thenReturn(1L);
        when(reactionRepository.countByPostIdAndType(postId, ReactionType.HEART)).thenReturn(0L);
        when(reactionRepository.countByPostIdAndType(postId, ReactionType.SUPPORT)).thenReturn(0L);

        // Act
        ReactionCountResponse response = reactionService.toggleReaction(postId, request);

        // Assert
        verify(reactionRepository).save(any(Reaction.class));
        assertEquals(1L, response.getCounts().get("LIKE"));
        assertEquals("LIKE", response.getUserReaction());
    }

    @Test
    void toggleReaction_ShouldRemoveReaction_WhenSameTypeExists() {
        // Arrange
        Reaction existingReaction = new Reaction(testPost, userId, ReactionType.LIKE);
        ReactionRequest request = new ReactionRequest(userId, ReactionType.LIKE);
        
        when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
        when(reactionRepository.findByPostIdAndUserId(postId, userId)).thenReturn(Optional.of(existingReaction));
        when(reactionRepository.countByPostIdAndType(postId, ReactionType.LIKE)).thenReturn(0L);
        when(reactionRepository.countByPostIdAndType(postId, ReactionType.HEART)).thenReturn(0L);
        when(reactionRepository.countByPostIdAndType(postId, ReactionType.SUPPORT)).thenReturn(0L);

        // Act
        ReactionCountResponse response = reactionService.toggleReaction(postId, request);

        // Assert
        verify(reactionRepository).delete(existingReaction);
        assertNull(response.getUserReaction());
    }

    @Test
    void toggleReaction_ShouldUpdateReaction_WhenDifferentTypeExists() {
        // Arrange
        Reaction existingReaction = new Reaction(testPost, userId, ReactionType.LIKE);
        ReactionRequest request = new ReactionRequest(userId, ReactionType.HEART);
        
        when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
        when(reactionRepository.findByPostIdAndUserId(postId, userId)).thenReturn(Optional.of(existingReaction));
        when(reactionRepository.countByPostIdAndType(postId, ReactionType.LIKE)).thenReturn(0L);
        when(reactionRepository.countByPostIdAndType(postId, ReactionType.HEART)).thenReturn(1L);
        when(reactionRepository.countByPostIdAndType(postId, ReactionType.SUPPORT)).thenReturn(0L);

        // Act
        ReactionCountResponse response = reactionService.toggleReaction(postId, request);

        // Assert
        verify(reactionRepository).save(existingReaction);
        assertEquals(ReactionType.HEART, existingReaction.getType());
        assertEquals("HEART", response.getUserReaction());
    }

    @Test
    void toggleReaction_ShouldThrowException_WhenPostNotFound() {
        // Arrange
        ReactionRequest request = new ReactionRequest(userId, ReactionType.LIKE);
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> reactionService.toggleReaction(postId, request));
    }
}
