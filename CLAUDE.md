# CLAUDE.md — プロジェクトハーネス

Claude Code がこのリポジトリで作業する際の規約・コマンド・進め方を集約する。
新しいセッションは本ファイルと [docs/application-spec.md](./docs/application-spec.md) を起点にすること。

## プロジェクトの目的
AI 駆動開発における **TDD（テスト駆動開発）サイクルの検証**。題材は商品在庫管理システム（Vue + Spring Boot + SQL Server）。詳細は [README](./README.md)。

## 最重要ルール
1. **テストファースト**: 実装より先にテストを書く。新機能は必ず RED（失敗）から開始し、GREEN → REFACTOR の順で進める。テストなしの実装を追加しない。
2. **コーディング規約準拠**:
   - Java → [docs/coding-standards/java-coding-standards.md](./docs/coding-standards/java-coding-standards.md)
   - SQL（SQL Server） → [docs/coding-standards/sql-coding-standards.md](./docs/coding-standards/sql-coding-standards.md)
3. **テスト命名規約**: テスト名に機能ID（`F-01-UT-01` 形式 / Java は `F_01_UT_01_...`）を含める。ダッシュボード集計がこの ID で機能とテストを対応づける。
4. **揺らぎの排除**: 集計・スキャフォルド・整形など決定論的であるべき作業はスクリプト化し、AI の自由記述に任せない。
5. **デグレ防止**: 変更後は関連テストを実行し、CI を緑に保つ。

## ディレクトリ
- `backend/` Spring Boot（mvn）/ `frontend/` Vue（npm）/ `e2e/` Playwright
- `db/` Flyway マイグレーション / `dashboard/` 進捗ダッシュボード / `scripts/` 集計等スクリプト
- `docs/` 仕様・規約 / `.claude/` Agent・Skill・設定

## よく使うコマンド
> 各コンポーネント整備後に有効。未整備のものは対応 Issue を参照。

```bash
# バックエンド
cd backend && ./mvnw clean verify      # ビルド + 単体/結合テスト
cd backend && ./mvnw test              # 単体のみ

# フロントエンド
cd frontend && npm ci && npm run test:unit

# E2E
cd e2e && npm ci && npm run test:e2e

# ローカル一括起動
docker compose up -d

# テスト結果の集計（ダッシュボード入力 test-results.json を生成）
bash scripts/aggregate-test-results.sh
```

## マルチエージェント方針
独立性の高い作業は専用 Agent に分担し並行実行する。想定する役割:
- **backend-tdd**: Spring Boot の機能を TDD で実装（規約準拠、Testcontainers）。
- **frontend-tdd**: Vue 画面/コンポーネントを TDD で実装（Vitest）。
- **e2e**: Playwright シナリオの作成・実行。
- 横断調査は `Explore` / `general-purpose` を活用。

Agent / Skill は再現性のため `.claude/agents/` `.claude/skills/` に定義を置き、増やしていく。

## TDD 作業の標準手順
1. 対象機能のテストケースを [application-spec.md](./docs/application-spec.md#6-テストケース設計) で確認。
2. テストを実装（機能IDを名前に含める）→ 実行して **失敗を確認（RED）**。
3. 最小実装で **パス（GREEN）**。
4. 規約に沿って **リファクタリング（REFACTOR）**、テスト緑を維持。
5. 集計スクリプトを実行し、ダッシュボードに反映されることを確認。

## やらないこと
- ユーザーの明示依頼なしに PR を作成しない。
- 認証/認可など [スコープ外](./docs/application-spec.md#8-スコープ外本検証では扱わない) を勝手に実装しない。
