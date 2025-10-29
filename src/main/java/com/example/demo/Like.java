package com.example.demo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 「いいね」エンティティ（中間テーブル）
 */
@Entity
@Table(name = "likes") // データベースのテーブル名を "likes" にする
public class Like {

    // 複合主キー (LikeIdクラス) を使用
    @EmbeddedId
    private LikeId id;

    // ユーザー(User)への関連付け (多対一)
    @ManyToOne(fetch = FetchType.LAZY) // パフォーマンスのためLAZYフェッチ推奨
    @MapsId("userId") // LikeIdの "userId" フィールドとマッピング
    @JoinColumn(name = "user_id")
    private User user;

    // 投稿(Post)への関連付け (多対一)
    @ManyToOne(fetch = FetchType.LAZY) // パフォーマンスのためLAZYフェッチ推奨
    @MapsId("postId") // LikeIdの "postId" フィールドとマッピング
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(nullable = false) // ★ createdAt に @Column を追加
    private LocalDateTime createdAt;

    // --- コンストラクタ ---
    public Like() {
        // デフォルトコンストラクタ (JPAに必要)
    }

    // LikeControllerで使うためのコンストラクタ
    public Like(LikeId id) {
        this.id = id;
    }

    // --- ゲッターとセッター ---

    public LikeId getId() {
        return id;
    }

    public void setId(LikeId id) {
        this.id = id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

