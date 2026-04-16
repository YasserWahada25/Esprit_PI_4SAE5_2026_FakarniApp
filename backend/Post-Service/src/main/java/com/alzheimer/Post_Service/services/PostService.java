package com.alzheimer.post_service.services;

import com.alzheimer.post_service.client.UserClient;
import com.alzheimer.post_service.dto.PostRequest;
import com.alzheimer.post_service.dto.PostResponse;
import com.alzheimer.post_service.dto.UserDTO;
import com.alzheimer.post_service.entities.Post;
import com.alzheimer.post_service.repositories.CommentRepository;
import com.alzheimer.post_service.repositories.PostRepository;
import com.alzheimer.post_service.repositories.ReactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ReactionRepository reactionRepository;

    @Autowired
    private UserClient userClient;

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        return authentication.getName();
    }

    public PostResponse createPost(PostRequest postRequest) {
        String userId = getCurrentUserId();
        
        Post post = new Post();
        post.setContent(postRequest.getContent());
        post.setImageUrl(postRequest.getImageUrl());
        post.setUserId(userId);

        Post savedPost = postRepository.save(post);
        return toResponse(savedPost);
    }

    public PostResponse getPostById(Long id) {
        Post post = postRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
        return toResponse(post);
    }

    public List<PostResponse> getAllPosts() {
        List<Post> posts = postRepository.findAll();
        return posts.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public List<PostResponse> getPostsByUserId(String userId) {
        List<Post> posts = postRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return posts.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public List<PostResponse> getCurrentUserPosts() {
        String userId = getCurrentUserId();
        return getPostsByUserId(userId);
    }

    public PostResponse updatePost(Long id, PostRequest postRequest) {
        Post post = postRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

        post.setContent(postRequest.getContent());
        post.setImageUrl(postRequest.getImageUrl());

        Post updatedPost = postRepository.save(post);
        return toResponse(updatedPost);
    }

    @Transactional
    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

        commentRepository.deleteByPostId(id);
        reactionRepository.deleteByPostId(id);

        postRepository.delete(post);
    }

    private PostResponse toResponse(Post post) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setContent(post.getContent());
        response.setImageUrl(post.getImageUrl());
        response.setUserId(post.getUserId());
        response.setCreatedAt(post.getCreatedAt());
        response.setUpdatedAt(post.getUpdatedAt());
        
        // Fetch user details from User-Service
        try {
            UserDTO user = userClient.getUserById(post.getUserId());
            response.setUser(user);
        } catch (Exception e) {
            // Log error but don't fail the request
            System.err.println("Failed to fetch user details: " + e.getMessage());
        }
        
        return response;
    }
}
