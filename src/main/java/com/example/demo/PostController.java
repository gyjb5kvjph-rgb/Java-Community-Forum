package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page; // ★ import
import org.springframework.data.domain.PageImpl; // ★ import
import org.springframework.data.domain.PageRequest; // ★ import
import org.springframework.data.domain.Pageable; // ★ import
import org.springframework.data.domain.Sort; // ★ import
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam; // ★ import

import java.util.Collections; // ★ import
import java.util.List; // ★ import
import java.util.Set; // ★ import
import java.util.stream.Collectors; // ★ import

@Controller
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LikeRepository likeRepository; // ★ LikeRepository をAutowired

    // --- ヘルパーメソッド ---
    /**
     * テキストエリアから送信された改行コード (\r\n) を \n に正規化する
     */
    private String normalizeContent(String content) {
        if (content != null) {
            // WindowsのCRLF(\r\n)をLF(\n)に統一
            return content.replaceAll("\r\n", "\n");
        }
        return null;
    }

    /**
     * トップページ（投稿一覧） (★ N+1問題対策済みに変更)
     * @param model ビューに渡すモデル
     * @param page リクエストされたページ番号 (デフォルトは0)
     * @return テンプレート名
     */
    @GetMapping("/")
    public String index(Model model, @RequestParam(name = "page", defaultValue = "0") int page) {

        // 1. ページネーションの設定 (1ページあたり5件、作成日時の降順)
        Pageable pageable = PageRequest.of(page, 5, Sort.by("createdAt").descending());

        // 2. ★ N+1対策: まずIDのリストだけをページネーションで取得
        Page<Long> postIdsPage = postRepository.findPostIdsByOrderByCreatedAtDesc(pageable);
        List<Long> postIds = postIdsPage.getContent();

        List<Post> posts;
        if (postIds.isEmpty()) {
            posts = Collections.emptyList();
        } else {
            // 3. ★ N+1対策: IDのリストを使って、関連データ(User, Likes)をまとめて取得
            posts = postRepository.findAllPostsWithUserAndLikes(postIds);

            // 4. (重要) DBから取得したリストはID順になっているため、元のcreatedAt順（postIdsの順）に並び替える
            posts.sort((p1, p2) -> Long.compare(postIds.indexOf(p1.getId()), postIds.indexOf(p2.getId())));
        }

        // 5. Page<Post> オブジェクトを再構築
        Page<Post> postPage = new PageImpl<>(posts, pageable, postIdsPage.getTotalElements());

        model.addAttribute("postPage", postPage);

        // 6. ログイン中のユーザーがいいねした投稿IDのセットを渡す
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String)) {
            String username = auth.getName();
            User currentUser = userRepository.findByUsername(username).orElse(null);
            if (currentUser != null) {
                Set<Long> likedPostIds = likeRepository.findLikedPostIdsByUserId(currentUser.getId());
                model.addAttribute("currentUserLikePostIds", likedPostIds);
            } else {
                model.addAttribute("currentUserLikePostIds", Collections.emptySet());
            }
        } else {
            model.addAttribute("currentUserLikePostIds", Collections.emptySet());
        }

        return "list"; // list.html を表示
    }


    /**
     * 新規投稿フォーム
     */
    @GetMapping("/new")
    public String newPostForm(Model model) {
        model.addAttribute("post", new Post());
        return "form";
    }

    /**
     * 投稿処理 (★ 改行コード正規化を追加)
     * ログイン中のユーザー情報を取得し、投稿に紐づける
     */
    @PostMapping("/create")
    public String createPost(@ModelAttribute Post post) {
        // 現在ログイン中のユーザー名を取得
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // ユーザー名を使って、データベースから User オブジェクトを取得
        User currentUser = userRepository.findByUsername(username)
                .orElse(null); // orElseThrow の代わりに orElse(null) を使う

        // もしDBにユーザーがいなければ (セッションだけ残っている場合)
        if (currentUser == null) {
            // ログアウトさせてセッションをクリアし、ログインページに戻す
            return "redirect:/logout";
        }

        // 投稿(Post)に、見つけたユーザー(User)をセットする
        post.setUser(currentUser);

        // ★ 改行コードを正規化してセット
        post.setContent(normalizeContent(post.getContent()));

        postRepository.save(post);
        return "redirect:/";
    }

    /**
     * 編集フォームの表示 (★ 安全なコードに修正済み)
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Post post = postRepository.findById(id).orElse(null);
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        // (修正) post.getUser() != null のチェックを追加
        if (post == null || post.getUser() == null || !post.getUser().getUsername().equals(currentUsername)) {
            return "redirect:/";
        }

        model.addAttribute("post", post);
        return "edit_form";
    }

    /**
     * 更新処理 (★ 改行コード正規化を追加)
     */
    @PostMapping("/update")
    public String updatePost(@ModelAttribute Post post) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Post existingPost = postRepository.findById(post.getId()).orElse(null);

        // (修正) existingPost.getUser() != null のチェックを追加
        if (existingPost != null && existingPost.getUser() != null && existingPost.getUser().getUsername().equals(currentUsername)) {
            existingPost.setTitle(post.getTitle());

            // ★ 改行コードを正規化してセット
            existingPost.setContent(normalizeContent(post.getContent()));

            postRepository.save(existingPost);
        }

        return "redirect:/";
    }

    /**
     * 削除処理 (★ 安全なコードに修正済み)
     */
    @GetMapping("/delete/{id}")
    public String deletePost(@PathVariable Long id) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Post post = postRepository.findById(id).orElse(null);

        // (修正) post.getUser() != null のチェックを追加
        if (post != null && post.getUser() != null && post.getUser().getUsername().equals(currentUsername)) {
            postRepository.deleteById(id);
        }

        return "redirect:/";
    }
}