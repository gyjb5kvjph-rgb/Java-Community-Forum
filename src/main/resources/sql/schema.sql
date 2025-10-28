-- 既にテーブルが存在する場合は削除 (PostgreSQL用)
-- CASCADE をつけることで、外部キー制約のあるテーブルもまとめて削除されます
DROP TABLE IF EXISTS post CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ユーザーテーブルの作成
CREATE TABLE users (
id BIGSERIAL PRIMARY KEY,
username VARCHAR(255) UNIQUE NOT NULL,
password VARCHAR(255) NOT NULL,
role VARCHAR(255) NOT NULL
);

-- 投稿テーブルの作成 (user_id が users テーブルの id を参照)
CREATE TABLE post (
id BIGSERIAL PRIMARY KEY,
title VARCHAR(255) NOT NULL,
content TEXT NOT NULL,
created_at TIMESTAMP WITHOUT TIME ZONE,
user_id BIGINT REFERENCES users(id)
);

-- ★ PostgreSQLの自動採番（シーケンス）の値をリセットする
-- これにより、テーブルを作り直すたびにIDの採番が必ず 1 から始まるようになり、
-- IDの重複エラー (500エラーの原因) を防ぎます。
SELECT setval(pg_get_serial_sequence('users', 'id'), 1, false);
SELECT setval(pg_get_serial_sequence('post', 'id'), 1, false);