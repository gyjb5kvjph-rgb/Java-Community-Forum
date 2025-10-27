package com.example.demo; // 修正箇所

// ▼▼▼ これら import が必要です ▼▼▼
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;   // ★ これ
import jakarta.persistence.JoinColumn; // ★ これ
import java.time.LocalDateTime;
// ▲▲▲ ここまで ▲▲▲

@Entity // これがデータベースのテーブルになる印
public class Post {

    @Id // 主キー（識別ID）
    @GeneratedValue(strategy = GenerationType.IDENTITY) // IDを自動で連番にする
    private Long id;

    private String title; // 記事のタイトル
    private String content; // 記事の本文
    private LocalDateTime createdAt; // 投稿日時
// --- ▼▼▼ ここから下を追加 ▼▼▼ ---

    @ManyToOne // 投稿(Many) 対 ユーザー(One) の関係
    @JoinColumn(name = "user_id") // データベース上では "user_id" というカラム名で紐づく
    private User user; // この投稿の所有者

    // user のゲッターとセッター
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // --- ゲッターとセッター ---
    // (IntelliJの Alt + Insert キー (Windows) や Command + N (Mac) の
    //  "Getter and Setter" メニューを使うと自動生成できます)

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // 投稿日時を自動セットする
    @jakarta.persistence.PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}