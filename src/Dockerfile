# --- ステージ1: ビルド環境 ---
# Maven (Java 17) が入ったイメージを "build" という名前で使う
FROM maven:3.9-eclipse-temurin-17 AS build

# アプリの作業ディレクトリを作成
WORKDIR /app

# まず pom.xml だけコピーして、依存ライブラリをダウンロードする
# (ソースコードより先にやることで、ビルドが速くなる)
COPY pom.xml .
RUN mvn dependency:go-offline

# 次にソースコードをコピーする
COPY src ./src

# Maven で "mvn package" を実行して .jar ファイルを作る
# (テストはスキップして高速化)
RUN mvn package -DskipTests

# --- ステージ2: 実行環境 ---
# Java 17 の "スリム" (軽量) イメージを本番用に使う
FROM eclipse-temurin:17-jre-focal

# アプリの作業ディレクトリを作成
WORKDIR /app

# ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
# ステージ1 (build) で作成した .jar ファイルをコピーしてくる
# あなたの .jar の名前に合わせてください (pom.xml の artifactId と version)
COPY --from=build /app/target/demo-0.0.1-SNAPSHOT.jar app.jar
# ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★

# このコンテナが起動した時に実行するコマンド
# (java -jar app.jar と同じ)
CMD ["java", "-jar", "app.jar"]