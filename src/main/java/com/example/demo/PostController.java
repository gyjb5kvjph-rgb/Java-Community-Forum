package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam; // ★ importが追加されます

import java.util.List;
import java.util.stream.Collectors; // ★ importが追加されます
import java.util.stream.IntStream; // ★ importが追加されます

@Controller
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * トップページ（投稿一覧） (★ ページネーション対応に修正)
     *
     * @param model Spring MVCのモデル
     * @param page  現在のページ番号 (デフォルトは0から開始)
     * @param size  1ページあたりの表示件数 (デフォルトは5件)
     * @return テンプレート名
     */
    @GetMapping("/")
    public String index(Model model,
                        @RequestParam(name = "page", defaultValue = "0") int page,
                        @RequestParam(name = "size", defaultValue = "5") int size) { // 1ページの件数を5件に設定

        // ページネーションのリクエスト情報を作成 (現在のページ, 1ページの件数)
        Pageable pageable = PageRequest.of(page, size);

        // データベースから新しい順で、指定されたページ（例: 0ページ目の5件）だけを取得
        Page<Post> postPage = postRepository.findAllByOrderByCreatedAtDesc(pageable);

        // 取得したページオブジェクトをHTML（Thymeleaf）に渡す
        model.addAttribute("postPage", postPage);

        // --- ページネーションの番号（1, 2, 3...）を生成するための処理 ---
        int totalPages = postPage.getTotalPages();
        if (totalPages > 0) {
            // 0から始まるページ番号のリストを作成 (例: 5ページなら 0, 1, 2, 3, 4)
            List<Integer> pageNumbers = IntStream.rangeClosed(0, totalPages - 1)
                    .boxed()
                    .collect(Collectors.toList());
            // ページ番号のリストをHTMLに渡す
            model.addAttribute("pageNumbers", pageNumbers);
        }
        // --- ここまで ---

        return "list"; // list.html を表示
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
     * 投稿処理 (変更なし - 以前の安全なコードのまま)
     */
    @PostMapping("/create")
    public String createPost(@ModelAttribute Post post) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username).orElse(null);

        if (currentUser == null) {
            return "redirect:/logout"; // DBにユーザーがいない場合
        }

        post.setUser(currentUser);
        postRepository.save(post);
        return "redirect:/";
    }

    /**
     * 編集フォームの表示 (変更なし - 以前の安全なコードのまま)
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
     * 更新処理 (変更なし - 以前の安全なコードのまま)
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
     * 削除処理 (変更なし - 以前の安全なコードのまま)
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

