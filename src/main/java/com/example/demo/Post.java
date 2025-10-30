package com.example.demo;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Set;
// ▼▼▼ 【ステップ3】 java.util.List をインポート ▼▼▼
import java.util.List;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // --- Likesの関連付け ---
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Like> likes;

    // --- ▼▼▼ 【ステップ3】 Comments の関連付けを追加 ▼▼▼ ---
    /**
     * この投稿に紐づくコメントのリスト
     * コメントは作成日時の昇順（古い順）で並び替えられます
     */
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC") // コメントを古い順にソート
    private List<Comment> comments; // 順序を保持するために List を使用
    // --- ▲▲▲ ここまで追加 ▲▲▲ ---


    // --- ゲッターとセッター ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    // --- Likesのゲッター/セッター ---
    public Set<Like> getLikes() {
        return likes;
    }

    public void setLikes(Set<Like> likes) {
        this.likes = likes;
    }

    // --- ▼▼▼ 【ステップ3】 Comments のゲッター/セッターを追加 (エラー解消に必須) ▼▼▼ ---
    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
    // --- ▲▲▲ ここまで追加 ▲▲▲ ---


    // --- JST変換用メソッド ---
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
        this.createdAt = LocalDateTime.now(ZoneId.of("UTC"));
    }
}