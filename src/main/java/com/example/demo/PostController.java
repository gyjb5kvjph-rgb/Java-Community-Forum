package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page; // ★ 1. Page をインポート
import org.springframework.data.domain.PageRequest; // ★ 2. PageRequest をインポート
import org.springframework.data.domain.Pageable; // ★ 3. Pageable をインポート
import org.springframework.data.domain.Sort; // ★ 4. Sort をインポート
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam; // ★ 5. RequestParam をインポート

import java.util.List;
import java.util.stream.Collectors; // ★ 6. Collectors をインポート
import java.util.stream.IntStream; // ★ 7. IntStream をインポート

@Controller // Webのリクエストを受け取るクラス
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LikeRepository likeRepository; // ★ 8. LikeRepository をインポート

    /**
     * トップページ（投稿一覧） (★ ページネーション対応に大幅変更)
     * http://localhost:8080/ でアクセスされる
     * @param page 表示するページ番号 (デフォルトは0)
     * @param size 1ページあたりの投稿数 (デフォルトは5)
     */
    @GetMapping("/")
    public String index(Model model,
                        @RequestParam(name = "page", defaultValue = "0") int page,
                        @RequestParam(name = "size", defaultValue = "5") int size) {

        // ★ 9. ページネーションのリクエストを作成 (作成日の降順 = 新しい順)
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // ★ 10. 【ステップ2-Dで作成する】性能改善版のメソッドを呼び出す
        Page<Post> postPage = postRepository.findAllPostsWithUserAndLikes(pageable);

        // ★ 11. ページネーションのリンク番号リストを作成
        int totalPages = postPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .map(i -> i - 1) // 0-indexed に変換
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        // ★ 12. ログイン中のユーザーIDをモデルに追加（いいね状態の判定用）
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            User currentUser = userRepository.findByUsername(auth.getName()).orElse(null);
            if (currentUser != null) {
                model.addAttribute("currentUserId", currentUser.getId());
            }
        }

        // ★ 13. Modelに "postPage" という名前で、取得したページ情報を渡す
        model.addAttribute("postPage", postPage);
        return "list"; // "list.html" を表示
    }

    /**
     * 新規投稿フォーム (変更なし)
     */
    @GetMapping("/new")
    public String newPostForm(Model model) {
        model.addAttribute("post", new Post());
        return "form";
    }

    /**
     * 投稿処理 (変更なし・安全なコード)
     */
    @PostMapping("/create")
    public String createPost(@ModelAttribute Post post) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User currentUser = userRepository.findByUsername(username)
                .orElse(null); // orElseThrow の代わりに orElse(null) を使う

        if (currentUser == null) {
            // DBにユーザーがいなければ (セッションだけ残っている場合)
            return "redirect:/logout";
        }

        post.setUser(currentUser);
        postRepository.save(post);
        return "redirect:/";
    }

    /**
     * 編集フォームの表示 (変更なし・安全なコード)
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Post post = postRepository.findById(id).orElse(null);
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        if (post == null || post.getUser() == null || !post.getUser().getUsername().equals(currentUsername)) {
            return "redirect:/";
        }

        model.addAttribute("post", post);
        return "edit_form";
    }

    /**
     * 更新処理 (変更なし・安全なコード)
     */
    @PostMapping("/update")
    public String updatePost(@ModelAttribute Post post) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Post existingPost = postRepository.findById(post.getId()).orElse(null);

        if (existingPost != null && existingPost.getUser() != null && existingPost.getUser().getUsername().equals(currentUsername)) {
            existingPost.setTitle(post.getTitle());
            existingPost.setContent(post.getContent());
            postRepository.save(existingPost);
        }

        return "redirect:/";
    }

    /**
     * 削除処理 (変更なし・安全なコード)
     */
    @GetMapping("/delete/{id}")
    public String deletePost(@PathVariable Long id) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Post post = postRepository.findById(id).orElse(null);

        if (post != null && post.getUser() != null && post.getUser().getUsername().equals(currentUsername)) {
            postRepository.deleteById(id);
        }

        return "redirect:/";
    }
}

