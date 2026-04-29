package com.alzheimer.post_service.services;

import com.alzheimer.post_service.dto.ReactionCountResponse;
import com.alzheimer.post_service.dto.ReactionRequest;
import com.alzheimer.post_service.entities.Post;
import com.alzheimer.post_service.entities.Reaction;
import com.alzheimer.post_service.entities.Reaction.ReactionType;
import com.alzheimer.post_service.repositories.PostRepository;
import com.alzheimer.post_service.repositories.ReactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ReactionService {

    @Autowired
    private ReactionRepository reactionRepository;

    @Autowired
    private PostRepository postRepository;

    @Transactional
    public ReactionCountResponse toggleReaction(Long postId, ReactionRequest request) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        Optional<Reaction> existingReaction = reactionRepository.findByPostIdAndUserId(postId, request.getUserId());

        if (existingReaction.isPresent()) {
            Reaction reaction = existingReaction.get();
            if (reaction.getType() == request.getType()) {
                // Remove reaction if same type
                reactionRepository.delete(reaction);
            } else {
                // Update to new type
                reaction.setType(request.getType());
                reactionRepository.save(reaction);
            }
        } else {
            // Create new reaction
            Reaction newReaction = new Reaction(post, request.getUserId(), request.getType());
            reactionRepository.save(newReaction);
        }

        return getReactionCounts(postId, request.getUserId());
    }

    public ReactionCountResponse getReactionCounts(Long postId, Long userId) {
        Map<String, Long> counts = new HashMap<>();
        counts.put("LIKE", reactionRepository.countByPostIdAndType(postId, ReactionType.LIKE));
        counts.put("HEART", reactionRepository.countByPostIdAndType(postId, ReactionType.HEART));
        counts.put("SUPPORT", reactionRepository.countByPostIdAndType(postId, ReactionType.SUPPORT));

        String userReaction = null;
        if (userId != null) {
            Optional<Reaction> reaction = reactionRepository.findByPostIdAndUserId(postId, userId);
            if (reaction.isPresent()) {
                userReaction = reaction.get().getType().name();
            }
        }

        return new ReactionCountResponse(counts, userReaction);
    }
}
