package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // SecurityConfig で @Bean にしたものが注入される

    /**
     * ログインフォームを表示
     */
    @GetMapping("/login")
    public String login() {
        return "login"; // login.html を表示
    }

    /**
     * ユーザー登録フォームを表示
     */
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register"; // register.html を表示
    }

    /**
     * ユーザー登録処理
     */
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user) {
        // ★★★ 重要 ★★★
        // パスワードをそのまま保存せず、必ずハッシュ化 (暗号化) する
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // デフォルトの権限を設定
        user.setRole("USER");

        // データベースに保存
        userRepository.save(user);

        // 登録後はログインページにリダイレクト
        return "redirect:/login";
    }

    // ▼▼▼ 【フッターリンク用メソッドを追加】 ▼▼▼
    /**
     * 利用規約ページを表示
     */
    @GetMapping("/terms")
    public String showTerms() {
        return "terms"; // terms.html を表示
    }

    /**
     * プライバシーポリシーページを表示
     */
    @GetMapping("/privacy")
    public String showPrivacy() {
        return "privacy"; // privacy.html を表示
    }
    // ▲▲▲ 追加ここまで ▲▲▲
}