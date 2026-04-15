package com.alzheimer.post_service.controllers;

import com.alzheimer.post_service.dto.ReactionCountResponse;
import com.alzheimer.post_service.dto.ReactionRequest;
import com.alzheimer.post_service.services.ReactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts/{postId}/reactions")
@CrossOrigin(origins = "*", allowCredentials = "false")
public class ReactionController {

    @Autowired
    private ReactionService reactionService;

    @PostMapping("/toggle")
    public ResponseEntity<?> toggleReaction(
            @PathVariable Long postId,
            @RequestBody ReactionRequest request) {
        try {
            if (request.getUserId() == null) {
                return ResponseEntity.badRequest().body("User ID is required");
            }
            if (request.getType() == null) {
                return ResponseEntity.badRequest().body("Reaction type is required");
            }

            ReactionCountResponse response = reactionService.toggleReaction(postId, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error toggling reaction: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getReactionCounts(
            @PathVariable Long postId,
            @RequestParam(required = false) Long userId) {
        try {
            ReactionCountResponse response = reactionService.getReactionCounts(postId, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error getting reaction counts: " + e.getMessage());
        }
    }
}
