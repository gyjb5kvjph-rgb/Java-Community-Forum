-- 既存のテーブルがあれば、関連する制約ごと削除 (CASCADE)
-- (データ永続化のため、DROP TABLEはコメントアウトのまま)
--DROP TABLE IF EXISTS likes CASCADE;
--DROP TABLE IF EXISTS posts CASCADE;
--DROP TABLE IF EXISTS users CASCADE;

-- ユーザーテーブル
-- 【修正】CREATE TABLE IF NOT EXISTS users に変更
CREATE TABLE IF NOT EXISTS users (
id BIGSERIAL PRIMARY KEY,
username VARCHAR(255) NOT NULL UNIQUE,
password VARCHAR(255) NOT NULL,
role VARCHAR(50) NOT NULL
);

-- 投稿テーブル
-- 【修正】CREATE TABLE IF NOT EXISTS posts に変更
CREATE TABLE IF NOT EXISTS posts (
id BIGSERIAL PRIMARY KEY,
title VARCHAR(255) NOT NULL,
content TEXT,
created_at TIMESTAMP,
user_id BIGINT,
-- ユーザーが削除されたら、そのユーザーの投稿も削除する (ON DELETE CASCADE)
FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- ▼▼▼ 【Likesテーブルの追加】 ▼▼▼
-- 【修正】CREATE TABLE IF NOT EXISTS likes に変更
CREATE TABLE IF NOT EXISTS likes (
-- 複合主キー (user_id と post_id の組み合わせ)
user_id BIGINT NOT NULL,
post_id BIGINT NOT NULL,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

-- 複合主キーの定義
PRIMARY KEY (user_id, post_id),

-- 外部キーの定義
FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE
);
-- ▲▲▲ ここまで ▲▲▲

-- ▼▼▼ 【Commentsテーブルの追加】 ▼▼▼
-- 【修正】CREATE TABLE IF NOT EXISTS comments に変更
CREATE TABLE IF NOT EXISTS comments (
    id BIGSERIAL PRIMARY KEY,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,

    -- 外部キーの定義
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE
);
-- ▲▲▲ ここまで追加 ▲▲▲

-- 自動採番シーケンスのリセット (重要)
-- 【注意】シーケンスリセットはテーブルが既に存在する場合に失敗することがあるため、
--        この行は削除またはコメントアウトするのが最も安全ですが、
--        PostgreSQLのCOALESCE関数と組み合わせることで通常は問題なく動作します。
SELECT setval('users_id_seq', (SELECT COALESCE(MAX(id), 0) FROM users) + 1, false);
SELECT setval('posts_id_seq', (SELECT COALESCE(MAX(id), 0) FROM posts) + 1, false);

-- (Likesテーブルは複合主キーであり、BIGSERIALではないため、シーケンスリセットは不要)