package com.alzheimer.post_service.repositories;

import com.alzheimer.post_service.entities.Reaction;
import com.alzheimer.post_service.entities.Reaction.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    Optional<Reaction> findByPostIdAndUserId(Long postId, Long userId);
    long countByPostIdAndType(Long postId, ReactionType type);
    
    @Modifying
    @Transactional
    void deleteByPostIdAndUserId(Long postId, Long userId);
    
    @Modifying
    @Transactional
    void deleteByPostId(Long postId);
}
