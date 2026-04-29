package com.alzheimer.post_service.controllers;

import com.alzheimer.post_service.dto.PostRequest;
import com.alzheimer.post_service.dto.PostResponse;
import com.alzheimer.post_service.services.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "*", allowCredentials = "false")
public class PostController {

    @Autowired
    private PostService postService;

    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody PostRequest postRequest) {
        try {
            // Validate content
            if (postRequest == null || postRequest.getContent() == null || postRequest.getContent().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Content is required");
            }
            
            // Validate content length
            if (postRequest.getContent().length() < 10) {
                return ResponseEntity.badRequest().body("Content must be at least 10 characters");
            }
            
            if (postRequest.getContent().length() > 2000) {
                return ResponseEntity.badRequest().body("Content must not exceed 2000 characters");
            }
            
            // Validate image size if present (Base64 encoded)
            if (postRequest.getImageUrl() != null && !postRequest.getImageUrl().isEmpty()) {
                // Base64 string size check (approximately 4MB limit)
                if (postRequest.getImageUrl().length() > 5 * 1024 * 1024) {
                    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body("Image is too large. Maximum size is 4MB.");
                }
            }
            
            PostResponse response = postService.createPost(postRequest);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error creating post: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long id) {
        PostResponse response = postService.getPostById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        List<PostResponse> responses = postService.getAllPosts();
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostResponse>> getPostsByUserId(@PathVariable String userId) {
        List<PostResponse> responses = postService.getPostsByUserId(userId);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @GetMapping("/my-posts")
    public ResponseEntity<List<PostResponse>> getCurrentUserPosts() {
        List<PostResponse> responses = postService.getCurrentUserPosts();
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
        @PathVariable Long id,
        @RequestBody PostRequest postRequest) {
        PostResponse response = postService.updatePost(id, postRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
