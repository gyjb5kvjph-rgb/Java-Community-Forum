package com.example.demo;

import org.springframework.data.domain.Page; // ★ 1. importを追加
import org.springframework.data.domain.Pageable; // ★ 2. importを追加
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // --- ▼▼▼ この1行を追記 ▼▼▼ ---

    /**
     * 作成日時の降順（新しい順）で、ページネーション付きで投稿を取得
     */
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // --- ▲▲▲ ここまで ▲▲▲ ---
}

