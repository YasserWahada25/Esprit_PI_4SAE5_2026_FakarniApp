package com.alzheimer.post_service.services;

import com.alzheimer.post_service.dto.CommentRequest;
import com.alzheimer.post_service.dto.CommentResponse;
import com.alzheimer.post_service.entities.Comment;
import com.alzheimer.post_service.entities.Post;
import com.alzheimer.post_service.repositories.CommentRepository;
import com.alzheimer.post_service.repositories.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Transactional
    public CommentResponse addComment(Long postId, CommentRequest request) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        Comment parentComment = null;
        if (request.getParentCommentId() != null) {
            parentComment = commentRepository.findById(request.getParentCommentId())
                .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            
            // Validate only one level of nesting
            if (parentComment.getParentComment() != null) {
                throw new RuntimeException("Only one level of comment nesting is allowed");
            }
        }

        Comment comment = new Comment(post, request.getUserId(), request.getContent(), parentComment);
        Comment savedComment = commentRepository.save(comment);
        
        return toResponse(savedComment);
    }

    public Page<CommentResponse> getCommentsByPost(Long postId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Comment> comments = commentRepository.findByPostIdAndParentCommentIsNull(postId, pageable);
        
        return comments.map(comment -> {
            CommentResponse response = toResponse(comment);
            // Load replies for each parent comment
            List<CommentResponse> replies = getReplies(comment.getId());
            response.setReplies(replies);
            return response;
        });
    }

    public List<CommentResponse> getReplies(Long parentCommentId) {
        Pageable pageable = PageRequest.of(0, 100, Sort.by("createdAt").ascending());
        Page<Comment> replies = commentRepository.findByParentCommentId(parentCommentId, pageable);
        return replies.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("You can only delete your own comments");
        }

        commentRepository.delete(comment);
    }

    private CommentResponse toResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setPostId(comment.getPost().getId());
        response.setUserId(comment.getUserId());
        response.setContent(comment.getContent());
        response.setParentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null);
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        response.setReplies(new ArrayList<>());
        return response;
    }
}
