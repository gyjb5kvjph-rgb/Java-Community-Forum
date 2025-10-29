package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime; // ★ 1. import を追加
import java.util.Map;
import java.util.Optional;

@RestController // これは @Controller ではなく、@RestController です
@RequestMapping("/api/posts") // このコントローラーは /api/posts でアクセス
public class LikeController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LikeRepository likeRepository;

    /**
     * 「いいね」の状態を切り替えるAPI
     * (いいね！されていなければ追加、されていれば削除)
     * @param postId いいね！する投稿のID
     * @return 更新後のいいね！数
     */
    @PostMapping("/{postId}/toggle-like")
    public ResponseEntity<?> toggleLike(@PathVariable Long postId) {

        // 1. ログイン中のユーザー情報を取得
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElse(null); // .orElseThrow はセキュリティリスクがあるので null に変更

        if (currentUser == null) {
            // ログインしていない、またはセッションが切れている
            return ResponseEntity.status(401).body(Map.of("error", "認証が必要です。"));
        }

        // 2. 投稿が存在するか確認
        Post post = postRepository.findById(postId)
                .orElse(null);

        if (post == null) {
            return ResponseEntity.status(404).body(Map.of("error", "投稿が見つかりません。"));
        }

        // 3. 既に「いいね」しているか確認
        Optional<Like> existingLike = likeRepository.findByPostIdAndUserId(postId, currentUser.getId());

        int likeCount;

        if (existingLike.isPresent()) {
            // --- 既に「いいね」している場合：削除 ---
            likeRepository.delete(existingLike.get());

            // 削除後のいいね！数をDBから再取得（より正確性のため）
            likeCount = likeRepository.countByPostId(postId);

        } else {
            // --- まだ「いいね」していない場合：追加 ---
            Like newLike = new Like(new LikeId(currentUser.getId(), post.getId())); // ★ 2. LikeIdを使ってインスタンス化
            newLike.setUser(currentUser);
            newLike.setPost(post);
            newLike.setCreatedAt(LocalDateTime.now()); // ★ 3. 作成日時をセット
            likeRepository.save(newLike);

            // 追加後のいいね！数をDBから再取得
            likeCount = likeRepository.countByPostId(postId);
        }

        // 4. JavaScript側（フロントエンド）に、更新後のいいね！数を返す
        return ResponseEntity.ok(Map.of("likeCount", likeCount, "userLiked", !existingLike.isPresent()));
    }
}

