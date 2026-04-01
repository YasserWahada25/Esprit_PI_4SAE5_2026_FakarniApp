package com.alzheimer.post_service.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reactions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"post_id", "user_id"})
})
public class Reaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReactionType type;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum ReactionType {
        LIKE, HEART, SUPPORT
    }

    public Reaction() {
    }

    public Reaction(Post post, Long userId, ReactionType type) {
        this.post = post;
        this.userId = userId;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public ReactionType getType() {
        return type;
    }

    public void setType(ReactionType type) {
        this.type = type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
