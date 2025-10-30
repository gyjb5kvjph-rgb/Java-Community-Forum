-- 既存のテーブルがあれば、関連する制約ごと削除 (CASCADE)
-- (LikesはPostとUserに依存、PostはUserに依存するため、この順序で削除)
--DROP TABLE IF EXISTS likes CASCADE;
--DROP TABLE IF EXISTS posts CASCADE;
--DROP TABLE IF EXISTS users CASCADE;

-- ユーザーテーブル (変更なし)
CREATE TABLE users (
id BIGSERIAL PRIMARY KEY,
username VARCHAR(255) NOT NULL UNIQUE,
password VARCHAR(255) NOT NULL,
role VARCHAR(50) NOT NULL
);

-- 投稿テーブル (変更なし)
-- (テーブル名を "posts" に変更)
CREATE TABLE posts (
id BIGSERIAL PRIMARY KEY,
title VARCHAR(255) NOT NULL,
content TEXT,
created_at TIMESTAMP,
user_id BIGINT,
-- user_id が users テーブルの id を参照する外部キーであることを定義
-- ユーザーが削除されたら、そのユーザーの投稿も削除する (ON DELETE CASCADE)
FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- ▼▼▼ 【Likesテーブルの追加】 ▼▼▼
-- 「いいね」の中間テーブル
CREATE TABLE likes (
-- 複合主キー (user_id と post_id の組み合わせ)
user_id BIGINT NOT NULL,
post_id BIGINT NOT NULL,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

-- 複合主キーの定義
PRIMARY KEY (user_id, post_id),

-- 外部キーの定義
-- ユーザーが削除されたら、その「いいね」も削除
FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
-- 投稿が削除されたら、その「いいね」も削除
FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE


);
-- ▲▲▲ ここまで ▲▲▲

-- 自動採番シーケンスのリセット (重要)
-- (テーブル作成後に実行することで、IDが1から始まることを保証)
SELECT setval('users_id_seq', (SELECT COALESCE(MAX(id), 0) FROM users) + 1, false);
SELECT setval('posts_id_seq', (SELECT COALESCE(MAX(id), 0) FROM posts) + 1, false);

-- (Likesテーブルは複合主キーであり、BIGSERIALではないため、シーケンスリセットは不要)