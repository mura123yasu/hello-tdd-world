# hello-tdd-world

AI 駆動開発（Claude Code）における **テスト駆動開発（TDD）の実装サイクルを検証する** ためのサンプルリポジトリです。

簡単な業務アプリケーション（商品在庫管理システム）を題材に、「先にテストを書き → 全て失敗（RED）→ 実装してパス（GREEN）→ リファクタリング（REFACTOR）」という TDD サイクルを、AI エージェントが自律的に回せる状態を作ることを目的としています。

---

## 1. 本リポジトリの目的

- **AI 駆動 × TDD の検証**: Claude Code を用いて、テストファーストで実装を進めるワークフローが再現可能か検証する。
- **進捗の可視化**: 実装対象の機能ごとに「テストケースが何件あり、どれが PASS / どれが FAIL か」をひと目で確認できるダッシュボードを用意する。
- **再現性のあるハーネス**: Claude Code のデファクトな設定（`CLAUDE.md` / Agent / Skill / Hook）を整備し、作業のブレを抑える。揺らぎを許容できずスクリプト化可能な作業は積極的にスクリプト化する。
- **継続的な品質担保**: CI でデグレを継続的に検出する。

> このリポジトリは「TDD と AI 駆動開発のワークフロー検証」が主目的です。題材アプリの業務的な厳密さよりも、**テストケースの網羅性とサイクルの再現性** を重視します。

---

## 2. 題材アプリケーション

**商品在庫管理システム（Inventory Management System）**

社内で扱う商品マスタと在庫を管理する、最小限の業務アプリケーションです。

- 画面一覧（検索一覧画面）と単票明細（詳細／登録・更新画面）を持つ
- 商品の検索・登録・更新・削除ができる
- 在庫の照会・入出庫ができる

詳細な仕様・画面一覧・機能一覧・テストケース設計は [docs/application-spec.md](./docs/application-spec.md) を参照してください。

---

## 3. 技術スタック

| レイヤ | 技術 | ビルド/実行 |
| --- | --- | --- |
| フロントエンド | Vue.js (Vite + TypeScript) | npm |
| バックエンド | Java / Spring Boot | mvn (Maven) |
| データベース | SQL Server | docker |
| マイグレーション | Flyway | mvn |
| 単体テスト(BE) | JUnit 5 / Mockito | mvn |
| 結合テスト(BE) | Spring Boot Test / Testcontainers | mvn |
| 単体テスト(FE) | Vitest / Vue Test Utils | npm |
| E2E テスト | Playwright | npm |
| CI | GitHub Actions | - |
| ローカル実行 | Docker Compose | docker |
| デプロイ先 | Azure | - |

---

## 4. リポジトリ構成（予定）

```
hello-tdd-world/
├── README.md                  # 本ファイル
├── CLAUDE.md                  # Claude Code 向けハーネス（プロジェクト規約）
├── docs/
│   ├── application-spec.md     # アプリ仕様・画面/機能一覧・テストケース設計
│   └── coding-standards/
│       ├── java-coding-standards.md  # Java コーディング規約（コンテキスト用）
│       └── sql-coding-standards.md   # SQL(SQL Server) コーディング規約（コンテキスト用）
├── backend/                   # Spring Boot アプリ（mvn）
├── frontend/                  # Vue.js アプリ（npm）
├── e2e/                       # Playwright E2E テスト
├── dashboard/                 # TDD 進捗モニタリングダッシュボード
├── scripts/                   # テスト結果集計などのスクリプト
├── db/                        # DB スキーマ / Flyway マイグレーション
├── docker-compose.yml         # ローカル実行（FE + BE + SQL Server）
├── .github/workflows/         # CI 定義
└── .claude/                   # Agent / Skill / 設定
```

> 各ディレクトリの中身は TDD サイクルの中で、対応する Issue に沿って整備していきます。

---

## 5. 開発の進め方（TDD サイクル）

本リポジトリの全機能は、以下のサイクルで実装します。

1. **RED**: 機能仕様（[docs/application-spec.md](./docs/application-spec.md)）のテストケースに対応するテストを先に書く。この時点ではすべて失敗する。
2. **GREEN**: テストがパスする最小限の実装を行う。
3. **REFACTOR**: テストを保ったままリファクタリングする（コーディング規約に準拠）。

各機能の進捗（テストケース数 / PASS / FAIL）は、テスト実行結果を集計してダッシュボードで可視化します。

---

## 6. セットアップ & 実行

> 以下のコマンドは各コンポーネント整備後に有効になります（対応 Issue を参照）。

### ローカル実行（Docker Compose）

```bash
docker compose up -d        # SQL Server + backend + frontend を起動
# frontend: http://localhost:5173
# backend : http://localhost:8080
```

### バックエンド（mvn）

```bash
cd backend
./mvnw clean verify          # ビルド + 全テスト
./mvnw test                  # 単体テストのみ
```

### フロントエンド（npm）

```bash
cd frontend
npm ci
npm run dev                  # 開発サーバ
npm run test:unit            # Vitest 単体テスト
```

### E2E

```bash
cd e2e
npm ci
npm run test:e2e             # Playwright
```

---

## 7. テスト & 進捗ダッシュボード

各テストランナーの結果（JUnit XML / Vitest JSON / Playwright JSON）を `scripts/` の集計スクリプトで 1 つの機械可読な JSON（機能 → テストケース → 結果）に変換し、`dashboard/` がそれを読み込んで表示します。

- 機能一覧と、それぞれの機能のテストケース数・PASS/FAIL がひと目で分かる
- AI が自律的に実行・検証できる（CLI から集計 → 静的成果物を生成）

詳細は [docs/application-spec.md](./docs/application-spec.md#テストケース設計) を参照。

---

## 8. CI

GitHub Actions で、Push / Pull Request 時に以下を実行しデグレを検出します。

- バックエンド: `mvn verify`（単体 + 結合）
- フロントエンド: `npm run test:unit` / Lint
- E2E: Playwright（Docker Compose 上で起動して実行）
- テスト結果の集計とダッシュボード成果物の生成

---

## 9. AI 利用方針（Claude Code）

- **ハーネス整備**: [CLAUDE.md](./CLAUDE.md) にプロジェクト規約・コマンド・TDD ルールを集約。`.claude/` に Agent / Skill / 設定を配置する。
- **再現性**: タスクの再現性を高めるため、定型作業は Skill / スクリプト化する。揺らぎを許容できない作業（テスト結果集計、スキャフォルド生成等）は決定論的なスクリプトにする。
- **マルチエージェント**: バックエンド / フロントエンド / E2E などを担当する Agent を用意し、並行して実装を進める。
- **コーディング規約の参照**: 作業時に参照しやすいよう、規約を [docs/coding-standards/](./docs/coding-standards/) に Markdown 化して同梱している。

---

## 10. コーディング規約

[Future 社のコーディング規約（CC-BY-4.0）](https://future-architect.github.io/coding-standards/) を採用します。作業時の参照用に、要点を Markdown 化して同梱しています。

- Java: [docs/coding-standards/java-coding-standards.md](./docs/coding-standards/java-coding-standards.md)
- SQL（SQL Server 向けに読み替え）: [docs/coding-standards/sql-coding-standards.md](./docs/coding-standards/sql-coding-standards.md)

---

## 11. ロードマップ / タスク

整備すべきタスクは GitHub Issue として起票しています。フェーズ構成は以下の通りです。

- Phase 0: 基盤整備（リポジトリ構成 / ハーネス）
- Phase 1: ローカル実行基盤（Docker Compose / DB / CI）
- Phase 2: バックエンド（Spring Boot, TDD）
- Phase 3: フロントエンド（Vue, TDD）
- Phase 4: E2E & 進捗ダッシュボード
- Phase 5: Azure デプロイ

進捗はトラッキング用の Epic Issue を参照してください。

---

## License

題材コードは本リポジトリのライセンスに従います。同梱しているコーディング規約の要約は、原典である Future 社コーディング規約（CC-BY-4.0）に基づきます。
