package com.alzheimer.Post_Service.repositories;

import com.alzheimer.Post_Service.entities.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUserId(String userId);
    List<Post> findByUserIdOrderByCreatedAtDesc(String userId);
}

