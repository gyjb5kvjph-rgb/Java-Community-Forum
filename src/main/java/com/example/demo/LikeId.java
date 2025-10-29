package com.example.demo;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

// 複合主キーとして埋め込まれるクラス
@Embeddable
public class LikeId implements Serializable {

    private Long userId;

    private Long postId;

    // --- コンストラクタ ---
    public LikeId() {}

    public LikeId(Long userId, Long postId) {
        this.userId = userId;
        this.postId = postId;
    }

    // --- ゲッター ---
    public Long getUserId() {
        return userId;
    }

    public Long getPostId() {
        return postId;
    }

    // --- equals() と hashCode() (複合主キーに必須) ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LikeId likeId = (LikeId) o;
        return Objects.equals(userId, likeId.userId) &&
                Objects.equals(postId, likeId.postId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, postId);
    }
}
