package com.alzheimer.post_service.services;

import com.alzheimer.post_service.dto.CommentRequest;
import com.alzheimer.post_service.dto.CommentResponse;
import com.alzheimer.post_service.entities.Comment;
import com.alzheimer.post_service.entities.Post;
import com.alzheimer.post_service.repositories.CommentRepository;
import com.alzheimer.post_service.repositories.PostRepository;
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
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private CommentService commentService;

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
    void addComment_ShouldCreateComment_WhenValidRequest() {
        // Arrange
        CommentRequest request = new CommentRequest(userId, "Test comment", null);
        Comment savedComment = new Comment(testPost, userId, "Test comment", null);
        savedComment.setId(1L);

        when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        // Act
        CommentResponse response = commentService.addComment(postId, request);

        // Assert
        assertNotNull(response);
        assertEquals(userId, response.getUserId());
        assertEquals("Test comment", response.getContent());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void addComment_ShouldCreateReply_WhenParentCommentExists() {
        // Arrange
        Comment parentComment = new Comment(testPost, 2L, "Parent comment", null);
        parentComment.setId(1L);
        
        CommentRequest request = new CommentRequest(userId, "Reply comment", 1L);
        Comment savedReply = new Comment(testPost, userId, "Reply comment", parentComment);
        savedReply.setId(2L);

        when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(parentComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(savedReply);

        // Act
        CommentResponse response = commentService.addComment(postId, request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getParentCommentId());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void addComment_ShouldThrowException_WhenNestedReply() {
        // Arrange
        Comment grandParent = new Comment(testPost, 2L, "Grandparent", null);
        grandParent.setId(1L);
        
        Comment parent = new Comment(testPost, 3L, "Parent", grandParent);
        parent.setId(2L);
        
        CommentRequest request = new CommentRequest(userId, "Nested reply", 2L);

        when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
        when(commentRepository.findById(2L)).thenReturn(Optional.of(parent));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> commentService.addComment(postId, request));
        assertTrue(exception.getMessage().contains("one level"));
    }

    @Test
    void deleteComment_ShouldDeleteComment_WhenUserIsOwner() {
        // Arrange
        Comment comment = new Comment(testPost, userId, "Test comment", null);
        comment.setId(1L);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        // Act
        commentService.deleteComment(1L, userId);

        // Assert
        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_ShouldThrowException_WhenUserIsNotOwner() {
        // Arrange
        Comment comment = new Comment(testPost, 2L, "Test comment", null);
        comment.setId(1L);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> commentService.deleteComment(1L, userId));
        assertTrue(exception.getMessage().contains("your own"));
    }
}
