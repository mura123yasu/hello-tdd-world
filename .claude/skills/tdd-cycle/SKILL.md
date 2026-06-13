---
name: tdd-cycle
description: 本リポジトリの標準 TDD サイクル（RED→GREEN→REFACTOR）で 1 機能を実装する手順。新しい機能 ID（F-xx）に着手するとき、またはユーザーが「TDD で実装して」と言ったときに使う。
---

# tdd-cycle

このリポジトリの全機能は以下の手順で実装する。機能 ID は `docs/application-spec.md` の機能一覧（F-01〜F-10, E2E-xx）に対応する。

## 手順

1. **仕様確認**: `docs/application-spec.md` で対象機能 ID のテストケース（`F-xx-UT/IT/FE-nn`）と API/検証ルールを確認する。
2. **RED**: 全テストケースに対応するテストを先に書く。テスト名に機能 ID を含める（Java: `F_03_UT_01_...` / TS: `F-03-FE-01 ...`）。テストを実行し **全件失敗** を確認する。
   - backend: `cd backend && ./mvnw test`
   - frontend: `cd frontend && npm run test:unit`
   - e2e: `cd e2e && npm run test:e2e`
3. **GREEN**: テストがパスする最小限の実装を行う。コーディング規約（`docs/coding-standards/`）に準拠。
4. **REFACTOR**: テストを緑に保ったままリファクタリング。
5. **集計**: `bash scripts/aggregate-test-results.sh` で `test-results.json` を更新し、ダッシュボードに機能の PASS/FAIL が反映されることを確認する。
6. **報告**: 機能 ID ごとのテストケース数と PASS/FAIL を簡潔に報告する。

## 並行実装
独立した機能はバックエンド/フロントエンドで `backend-tdd` / `frontend-tdd` エージェントに分担し、並行して進めてよい。

## 注意
- テストを後追いで書かない（テストファースト厳守）。
- 機能 ID をテスト名に含めないと集計・ダッシュボードに反映されない（揺らぎ排除のための規約）。
