package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // ★ 1. HttpMethod をインポート
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.security.web.util.matcher.AntPathRequestMatcher; // ★ 不要になったため削除

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // ★ 2. CSSとJSファイルへのアクセスを全員に許可 (GETリクエスト)
                        .requestMatchers(HttpMethod.GET, "/css/**", "/js/**").permitAll()
                        // ★ 3. 「いいね！」API (POSTリクエスト) へのアクセスを認証済みのユーザーに許可
                        .requestMatchers(HttpMethod.POST, "/api/posts/**").authenticated()
                        // 「/register」（新規登録）と「/login」（ログイン）、「/」（一覧）ページは全員アクセス許可
                        .requestMatchers("/", "/register", "/login").permitAll()
                        // その他のリクエストはすべて認証が必要
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        // ログインページのURLを指定
                        .loginPage("/login")
                        // ログイン成功時のリダイレクト先
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        // ログアウト処理のURLを指定 (非推奨の AntPathRequestMatcher を .logoutUrl に変更)
                        .logoutUrl("/logout")
                        // ログアウト成功時のリダイレクト先
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // パスワードをハッシュ化するためのエンコーダー
        return new BCryptPasswordEncoder();
    }
}

