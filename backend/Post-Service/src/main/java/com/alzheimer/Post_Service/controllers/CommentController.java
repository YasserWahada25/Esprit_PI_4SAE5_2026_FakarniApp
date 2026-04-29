package com.alzheimer.post_service.controllers;

import com.alzheimer.post_service.dto.CommentRequest;
import com.alzheimer.post_service.dto.CommentResponse;
import com.alzheimer.post_service.services.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@CrossOrigin(origins = "*", allowCredentials = "false")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping
    public ResponseEntity<?> addComment(
            @PathVariable Long postId,
            @RequestBody CommentRequest request) {
        try {
            if (request.getUserId() == null) {
                return ResponseEntity.badRequest().body("User ID is required");
            }
            if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Content is required");
            }
            if (request.getContent().length() > 1000) {
                return ResponseEntity.badRequest().body("Content must not exceed 1000 characters");
            }

            CommentResponse response = commentService.addComment(postId, request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error adding comment: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<CommentResponse> comments = commentService.getCommentsByPost(postId, page, size);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error getting comments: " + e.getMessage());
        }
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestParam Long userId) {
        try {
            if (userId == null) {
                return ResponseEntity.badRequest().body("User ID is required");
            }

            commentService.deleteComment(commentId, userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error deleting comment: " + e.getMessage());
        }
    }
}
