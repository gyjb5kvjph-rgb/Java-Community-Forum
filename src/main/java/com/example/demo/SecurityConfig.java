package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * パスワードを暗号化するための Bean (部品)
     * BCrypt という強力な暗号化方式を使います
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * セキュリティ設定 (URLごとのアクセス制御)
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // ルート("/")、"/css/**"、"/register" は誰でもアクセス可能
                        .requestMatchers("/", "/css/**", "/register").permitAll()
                        // "/new", "/edit/**", "/delete/**" は認証されたユーザー (ログインした人) のみアクセス可能
                        .requestMatchers("/new", "/edit/**", "/delete/**", "/create", "/update").authenticated()
                        // その他のリクエストはすべて許可 (今回は上記以外は特にないが念のため)
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        // ログインフォームのURLを指定
                        .loginPage("/login")
                        // ログイン成功時のリダイレクト先 (デフォルトはトップページ "/")
                        .defaultSuccessUrl("/", true)
                        // 誰でもログインページにはアクセス可能
                        .permitAll()
                )
                .logout(logout -> logout
                        // ログアウト処理のURL (デフォルトは /logout)
                        // ログアウト成功時のリダイレクト先
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }
}