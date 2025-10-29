package com.example.demo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // ★ 1. @Query をインポート
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // --- ▼▼▼ 【ステップ2-Dで追加】 ▼▼▼ ---

    /**
     * ページネーション対応の投稿一覧を取得する (N+1問題対策版)
     *
     * JPQL (Java Persistence Query Language) を使用して、
     * 投稿(p)を取得する際に、関連するユーザー(u)といいね(l)も
     * 同時に「JOIN FETCH」で取得する。
     * これにより、投稿ごとに個別のクエリが発行されるのを防ぐ。
     *
     * @param pageable ページ情報 (ページ番号, サイズ, ソート順)
     * @return 関連情報（User, Likes）を含む投稿のページ
     */
    @Query(value = "SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.user u " +
            "LEFT JOIN FETCH p.likes l " +
            "ORDER BY p.createdAt DESC", // JPQLではPageableのSortがJOIN FETCHと競合しやすいため、クエリ内で明示的にソート
            countQuery = "SELECT COUNT(p) FROM Post p") // ページ総数計算用のクエリ
    Page<Post> findAllPostsWithUserAndLikes(Pageable pageable);

    // --- ▲▲▲ ここまで追加 ▲▲▲ ---
}

