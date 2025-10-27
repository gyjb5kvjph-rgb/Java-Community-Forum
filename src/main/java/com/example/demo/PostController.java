package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository; // これがあることを確認

    /**
     * トップページ（投稿一覧）
     */
    @GetMapping("/")
    public String index(Model model) {
        List<Post> posts = postRepository.findAll();
        model.addAttribute("posts", posts);
        return "list";
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
     * 投稿処理 (★ 安全なコードに修正済み)
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
     * 更新処理 (★ 安全なコードに修正済み)
     */
    @PostMapping("/update")
    public String updatePost(@ModelAttribute Post post) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Post existingPost = postRepository.findById(post.getId()).orElse(null);

        // (修正) existingPost.getUser() != null のチェックを追加
        if (existingPost != null && existingPost.getUser() != null && existingPost.getUser().getUsername().equals(currentUsername)) {
            existingPost.setTitle(post.getTitle());
            existingPost.setContent(post.getContent());
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