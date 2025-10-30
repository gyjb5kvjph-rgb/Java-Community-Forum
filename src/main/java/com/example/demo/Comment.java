package com.example.demo;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId; // JST変換用に保持
import java.time.format.DateTimeFormatter; // JST変換用に保持

@Entity
@Table(name = "comments") // テーブル名を "comments" にします
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false) // 本文は必須
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // コメント投稿者 (User) への関連付け (多対一)
    @ManyToOne(fetch = FetchType.LAZY) // パフォーマンスのためLAZYフェッチ
    @JoinColumn(name = "user_id", nullable = false) // 投稿者は必須
    private User user;

    // 対象の投稿 (Post) への関連付け (多対一)
    @ManyToOne(fetch = FetchType.LAZY) // パフォーマンスのためLAZYフェッチ
    @JoinColumn(name = "post_id", nullable = false) // 対象投稿は必須
    private Post post;

    // --- ゲッターとセッター ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    // --- JST変換用ヘルパーメソッド (Post.java と同様) ---
    /**
     * Thymeleaf で日本時間(JST)を表示するためのヘルパーメソッド。
     * @return "yyyy/MM/dd HH:mm" 形式の日本時間の文字列
     */
    public String getFormattedCreatedAt() {
        if (this.createdAt == null) {
            return "";
        }
        ZoneId utcZone = ZoneId.of("UTC");
        ZoneId jstZone = ZoneId.of("Asia/Tokyo");
        LocalDateTime jstDateTime = this.createdAt.atZone(utcZone)
                .withZoneSameInstant(jstZone)
                .toLocalDateTime();
        return jstDateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
    }

    // --- コールバックメソッド ---
    @PrePersist
    protected void onCreate() {
        // コメント日時を（UTCで）自動設定
        this.createdAt = LocalDateTime.now(ZoneId.of("UTC"));
    }
}