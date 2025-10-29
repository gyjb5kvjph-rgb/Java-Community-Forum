package com.example.demo;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId; // ★ JST変換のために追加
import java.time.format.DateTimeFormatter; // ★ JST変換のために追加
import java.util.Set; // ★ Likesのために追加

@Entity
@Table(name = "posts") // ★ テーブル名を "posts" に変更 (schema.sqlと合わせる)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT") // ★ contentカラムの型をTEXTに変更
    private String content;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY) // ★ パフォーマンスのためLAZYに変更
    @JoinColumn(name = "user_id")
    private User user;

    // --- ▼▼▼ 【Likesの関連付けを追加】 ▼▼▼ ---
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY) // ★ LAZYに変更
    private Set<Like> likes;
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

    // --- ▼▼▼ 【Likesのゲッター/セッターを追加】 ▼▼▼ ---
    public Set<Like> getLikes() {
        return likes;
    }

    public void setLikes(Set<Like> likes) {
        this.likes = likes;
    }
    // --- ▲▲▲ ここまで追加 ▲▲▲ ---


    // --- ▼▼▼ 【JST変換用メソッドの追加】 ▼▼▼ ---
    /**
     * Thymeleaf (list.html) で日本時間(JST)を表示するためのヘルパーメソッド。
     * T(...) のセキュリティエラーを回避するために、Java側で変換処理を行う。
     * @return "yyyy/MM/dd HH:mm" 形式の日本時間の文字列
     */
    public String getFormattedCreatedAt() {
        if (this.createdAt == null) {
            return "";
        }
        // 1. DBから取得した時刻(UTC)を "UTC" タイムゾーンとして定義
        ZoneId utcZone = ZoneId.of("UTC");
        // 2. 変換したいタイムゾーン "Asia/Tokyo" (JST) を定義
        ZoneId jstZone = ZoneId.of("Asia/Tokyo");
        // 3. JSTに変換
        LocalDateTime jstDateTime = this.createdAt.atZone(utcZone)
                .withZoneSameInstant(jstZone)
                .toLocalDateTime();
        // 4. フォーマット
        return jstDateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
    }
    // --- ▲▲▲ ここまで追加 ▲▲▲ ---

    @PrePersist
    protected void onCreate() {
        // 投稿日時を（UTCで）自動設定
        this.createdAt = LocalDateTime.now(ZoneId.of("UTC"));
    }
}

