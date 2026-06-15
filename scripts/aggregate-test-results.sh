#!/usr/bin/env bash
# テスト結果集計のラッパー。決定論的な Python スクリプトを呼び出す。
# 使い方: bash scripts/aggregate-test-results.sh
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
python3 "${SCRIPT_DIR}/aggregate_test_results.py" "$@"
