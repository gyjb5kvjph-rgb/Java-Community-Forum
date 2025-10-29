package com.example.demo;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set; // ★ 1. Set をインポート

@Entity
@Table(name = "posts") // "post" は予約語の可能性があるため "posts" とします
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY) // ユーザー情報は必要な時だけ取得
    @JoinColumn(name = "user_id")
    private User user;

    // --- ▼▼▼ ここから2行を追加 ▼▼▼ ---
    // この投稿(One)は、たくさんの「いいね(Many)」を持つ
    // cascade = CascadeType.ALL: 投稿が削除されたら、関連する「いいね」もすべて削除する
    // orphanRemoval = true: リストから「いいね」を外したら、DBからも削除する
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Like> likes;
    // --- ▲▲▲ ここまで追加 ▲▲▲ ---


    // --- ゲッターとセッター (user, title, content, createdAt は既存) ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // --- ▼▼▼ likes のゲッターとセッターを追加 ▼▼▼ ---
    public Set<Like> getLikes() {
        return likes;
    }

    public void setLikes(Set<Like> likes) {
        this.likes = likes;
    }
    // --- ▲▲▲ ここまで追加 ▲▲▲ ---
}

