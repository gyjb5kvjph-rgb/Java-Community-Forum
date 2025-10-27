package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Securityがログイン時に使う
    // ユーザー名 (username) を元にユーザー情報を検索するためのメソッド
    Optional<User> findByUsername(String username);
}