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
     * ページネーション（N+1問題対策）のためのクエリ
     *
     * ステップ1: まず、ページネーションを適用して「投稿のIDだけ」を取得します。
     * (このクエリはDB側で正しくページングされます)
     */
    @Query("SELECT p.id FROM Post p ORDER BY p.createdAt DESC")
    Page<Long> findPostIdsByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * ページネーション（N+1問題対策）のためのクエリ
     *
     * ステップ2: ステップ1で取得したIDのリスト（postIds）を使って、
     * 必要な情報（Post, User, Likes）をすべてJOIN FETCHで一括取得します。
     * (これにより、投稿ごとのユーザー情報や「いいね」のループクエリ（N+1）を防ぎます)
     *
     * @param postIds 取得対象の投稿IDのリスト
     * @return 関連情報がフェッチされたPostのリスト
     */
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.user " +        // 投稿者情報(User)をフェッチ
            "LEFT JOIN FETCH p.likes l " +     // いいね情報(Like)をフェッチ
            "LEFT JOIN FETCH l.user " +        // いいねしたユーザー情報もフェッチ
            "WHERE p.id IN :postIds " +
            "ORDER BY p.createdAt DESC")
    List<Post> findAllPostsWithUserAndLikes(@Param("postIds") List<Long> postIds);
}

