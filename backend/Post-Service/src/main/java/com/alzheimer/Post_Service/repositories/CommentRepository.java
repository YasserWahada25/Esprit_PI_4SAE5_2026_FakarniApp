package com.alzheimer.post_service.repositories;

import com.alzheimer.post_service.entities.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByPostIdAndParentCommentIsNull(Long postId, Pageable pageable);
    Page<Comment> findByParentCommentId(Long parentCommentId, Pageable pageable);
    long countByPostId(Long postId);
    
    @Modifying
    @Transactional
    void deleteByPostId(Long postId);
}
