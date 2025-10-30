package com.example.demo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 1. まず、ページング指定で「投稿のID」だけを取得する
     * (作成日の降順)
     */
    @Query("SELECT p.id FROM Post p ORDER BY p.createdAt DESC")
    Page<Long> findPostIdsByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 2. 取得したIDのリストに基づき、「いいね！」「ユーザー」【コメント】の情報を
     * JOIN FETCH（一括取得）でまとめて取得する
     */
    // ▼▼▼ このクエリを修正 ▼▼▼
    @Query("SELECT DISTINCT p FROM Post p " +      // ★ DISTINCT を追加
            "LEFT JOIN FETCH p.user " +          // 投稿者(User)の情報を一括取得
            "LEFT JOIN FETCH p.likes " +         // いいね(Likes)の情報を一括取得
            "LEFT JOIN FETCH p.comments " +      // ★ コメント(Comments)の情報を一括取得
            "WHERE p.id IN :ids " +              // 1. で取得したIDリストに絞り込む
            "ORDER BY p.createdAt DESC")         // 順序を保持
    List<Post> findAllPostsWithUserAndLikes(@Param("ids") List<Long> ids);
    // ▲▲▲ 修正ここまで ▲▲▲

    /**
     * (ページネーション機能で追加した、古いメソッド。PostControllerからは使われなくなる)
     */
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
}