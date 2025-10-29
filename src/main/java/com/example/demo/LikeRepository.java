package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; // ★ 1. import を追加

@Repository
public interface LikeRepository extends JpaRepository<Like, LikeId> {

    // --- ▼▼▼ 【ステップ2-Bで追加】 ▼▼▼ ---

    /**
     * 投稿ID(postId)とユーザーID(userId)を使って、
     * 既に「いいね」が存在するかどうかを検索する
     * @param postId 投稿ID
     * @param userId ユーザーID
     * @return Likeエンティティ（存在する場合）
     */
    Optional<Like> findByPostIdAndUserId(Long postId, Long userId);

    /**
     * 投稿ID(postId)に紐づく「いいね」の総数をカウントする
     * @param postId 投稿ID
     * @return いいねの総数
     */
    int countByPostId(Long postId);

    // --- ▲▲▲ ここまで追加 ▲▲▲ ---
}

