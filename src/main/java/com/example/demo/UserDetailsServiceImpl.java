package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. UserRepositoryを使って、DBからユーザー名で検索
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // 2. DBから見つかったユーザー情報 (私たちの User) を、
        //    Spring Securityが理解できる UserDetails 形式に変換する
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                // ユーザーの権限 (Role) を設定
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
        );
    }
}