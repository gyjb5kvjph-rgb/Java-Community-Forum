package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set; // ★ import を追加

@Repository
// ★ LikeId(複合主キー) を JpaRepository の2番目の型引数に指定
public interface LikeRepository extends JpaRepository<Like, LikeId> {

    // --- LikeController で使用 ---

    /**
     * 投稿IDとユーザーIDに基づいて、特定の「いいね！」を検索する
     * (メソッド命名規則により、Spring Data JPAが自動でSQLを生成)
     */
    Optional<Like> findByPostIdAndUserId(Long postId, Long userId);

    /**
     * 特定の投稿IDに関連する「いいね！」の総数を数える
     * (メソッド命名規則により、Spring Data JPAが自動でSQLを生成)
     */
    int countByPostId(Long postId);


    // --- PostController (一覧表示) で使用 ---

    /**
     * 特定のユーザーIDが「いいね！」したすべての投稿IDのセットを取得する
     * (これはJPQLクエリ)
     */
    @Query("SELECT l.id.postId FROM Like l WHERE l.id.userId = :userId")
    Set<Long> findLikedPostIdsByUserId(@Param("userId") Long userId);
}

