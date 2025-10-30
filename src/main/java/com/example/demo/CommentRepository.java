package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 特定の投稿ID (postId) に紐づくコメントをすべて検索し、
     * 作成日時 (createdAt) の昇順で返すメソッド。
     * (メソッド命名規則により、Spring Data JPAが自動でSQLを生成)
     */
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);

    // 必要に応じて、他の検索メソッド（例：特定のユーザーによるコメント検索）を追加できます。
}