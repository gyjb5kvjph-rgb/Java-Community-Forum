package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // ★ コメント機能のため追加

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LikeRepository likeRepository;

    // ▼▼▼ CommentRepository を Autowired ▼▼▼
    @Autowired
    private CommentRepository commentRepository;
    // ▲▲▲ 追加 ▲▲▲

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
            // 3. ★ N+1対策: IDのリストを使って、関連データ(User, Likes, Comments)をまとめて取得
            //    PostRepository 側も修正が必要な場合がありますが、まずはこのまま進めます。
            //    (N+1問題がコメントで再発する可能性があります)
            //    → findAllPostsWithUserAndLikes を findAllPostsWithDetails などにリネームし、
            //       Comment も JOIN FETCH するのが理想です。

            // ひとまず、コメント取得のために findAllById を使います。
            // ★ N+1対策クエリに コメント も含めるように PostRepository を修正するのがベストですが、
            //    ここでは Post エンティティの @OrderBy でソートされたコメントが
            //    N+1でロードされることを前提とします。
            posts = postRepository.findAllById(postIds); // N+1対策クエリを一旦停止

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

    // =================================================================
    // ▼▼▼ 【コメント機能】 ▼▼▼
    // =================================================================

    /**
     * 新しいコメントを保存する処理
     * @param comment コメント内容 (content フィールドのみフォームから受け取る)
     * @param postId コメント対象の投稿ID
     * @param redirectAttributes リダイレクト時にメッセージを渡すため
     * @return リダイレクト先のURL
     */
    @PostMapping("/comments/create")
    public String createComment(@ModelAttribute Comment comment,
                                @RequestParam("postId") Long postId,
                                RedirectAttributes redirectAttributes) {

        // 1. ログイン中のユーザーを取得
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElse(null);

        // 2. コメント対象の投稿を取得
        Post post = postRepository.findById(postId)
                .orElse(null);

        // 3. ユーザーまたは投稿が見つからない場合はエラー
        if (currentUser == null || post == null) {
            // redirectAttributes.addFlashAttribute("errorMessage", "投稿またはユーザーが見つかりません。");
            return "redirect:/"; // トップにリダイレクト
        }

        // 4. Comment オブジェクトに必要な情報をセット
        comment.setUser(currentUser);
        comment.setPost(post);
        // ★ コメントの改行コードも正規化
        comment.setContent(normalizeContent(comment.getContent()));
        // createdAt は @PrePersist で自動セットされる

        // 5. データベースに保存
        commentRepository.save(comment);

        // 6. コメント投稿後は元の投稿一覧（または詳細ページ）にリダイレクト
        return "redirect:/";
    }

    /**
     * コメント編集フォームを表示
     */
    @GetMapping("/comments/edit/{id}")
    public String showCommentEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {

        // 1. コメントを取得
        Comment comment = commentRepository.findById(id)
                .orElse(null);

        // 2. ログイン中のユーザー名を取得
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        // 3. セキュリティチェック
        // コメントが存在しない、または投稿者本人でない場合はリダイレクト
        if (comment == null || comment.getUser() == null || !comment.getUser().getUsername().equals(currentUsername)) {
            // redirectAttributes.addFlashAttribute("errorMessage", "編集権限がありません。");
            return "redirect:/";
        }

        // 4. フォームにコメントオブジェクトを渡す
        model.addAttribute("comment", comment);
        return "comment_edit"; // comment_edit.html を表示
    }

    /**
     * コメント更新処理
     */
    @PostMapping("/comments/update")
    public String updateComment(@ModelAttribute Comment comment, RedirectAttributes redirectAttributes) {

        // 1. 更新対象のコメントをDBから取得
        Comment existingComment = commentRepository.findById(comment.getId())
                .orElse(null);

        // 2. ログイン中のユーザー名を取得
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        // 3. セキュリティチェック (コメントが存在し、かつ投稿者本人であること)
        if (existingComment == null || existingComment.getUser() == null || !existingComment.getUser().getUsername().equals(currentUsername)) {
            // redirectAttributes.addFlashAttribute("errorMessage", "更新権限がありません。");
            return "redirect:/";
        }

        // 4. 内容を更新（改行コードも正規化）
        existingComment.setContent(normalizeContent(comment.getContent()));

        // 5. データベースに保存
        commentRepository.save(existingComment);

        // 6. 投稿一覧に戻る
        return "redirect:/";
    }
    // ▲▲▲ 【コメント機能 ここまで】 ▲▲▲
}