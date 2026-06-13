#!/usr/bin/env python3
"""テスト結果集計スクリプト（決定論的）。

各テストランナーの結果を読み込み、テスト名に含まれる機能ID（例: F-03-UT-01 /
Java の F_03_UT_01_... / SKELETON-UT-01 / E2E-01）を抽出して機能ごとに集計し、
ダッシュボード入力 `dashboard/test-results.json` を生成する。

入力（存在するものだけ処理する）:
  - JUnit XML : backend/target/surefire-reports/*.xml, backend/target/failsafe-reports/*.xml
  - Vitest JSON: frontend/test-results/vitest-results.json
  - Playwright JSON: e2e/test-results/*.json

分母は scripts/test-catalog.json（仕様のテストケース台帳）。台帳にあって結果に
無いケースは "not_run"（未実装/未実行）として扱う。
"""
from __future__ import annotations

import argparse
import glob
import json
import os
import re
import sys
import xml.etree.ElementTree as ET
from datetime import datetime, timezone

# 機能ID付きテストID を抽出する正規表現（大文字化・アンダースコア→ハイフン後に適用）
ID_PATTERNS = [
    re.compile(r"(F-\d{2}-(?:UT|IT|FE|E2E)-\d+)"),
    re.compile(r"(SKELETON-(?:UT|IT|FE)-\d+)"),
    re.compile(r"(E2E-\d+)"),
]

# status 優先度（同一IDが複数回出た場合、より悪い結果を採用）
STATUS_RANK = {"passed": 0, "skipped": 1, "not_run": 1, "failed": 2}


def normalize(name: str) -> str:
    return re.sub(r"_", "-", name).upper()


def extract_test_id(name: str) -> str | None:
    norm = normalize(name)
    for pattern in ID_PATTERNS:
        m = pattern.search(norm)
        if m:
            return m.group(1)
    return None


def feature_of(test_id: str) -> str:
    if test_id.startswith("F-"):
        return "-".join(test_id.split("-")[:2])  # F-03
    if test_id.startswith("E2E"):
        return "E2E"
    if test_id.startswith("SKELETON"):
        return "SKELETON"
    return "OTHER"


def merge(results: dict[str, str], test_id: str, status: str) -> None:
    current = results.get(test_id)
    if current is None or STATUS_RANK[status] > STATUS_RANK[current]:
        results[test_id] = status


def parse_junit(paths: list[str], results: dict[str, str]) -> None:
    for path in paths:
        try:
            root = ET.parse(path).getroot()
        except ET.ParseError:
            continue
        for testcase in root.iter("testcase"):
            name = testcase.get("name", "")
            test_id = extract_test_id(name)
            if test_id is None:
                continue
            if testcase.find("failure") is not None or testcase.find("error") is not None:
                status = "failed"
            elif testcase.find("skipped") is not None:
                status = "skipped"
            else:
                status = "passed"
            merge(results, test_id, status)


def parse_vitest(path: str, results: dict[str, str]) -> None:
    if not os.path.isfile(path):
        return
    with open(path, encoding="utf-8") as fh:
        data = json.load(fh)
    for suite in data.get("testResults", []):
        for assertion in suite.get("assertionResults", []):
            name = " ".join(filter(None, [assertion.get("fullName"), assertion.get("title")]))
            test_id = extract_test_id(name)
            if test_id is None:
                continue
            raw = assertion.get("status", "")
            status = {"passed": "passed", "failed": "failed"}.get(raw, "skipped")
            merge(results, test_id, status)


def parse_playwright(paths: list[str], results: dict[str, str]) -> None:
    def walk(suite: dict) -> None:
        for spec in suite.get("specs", []):
            title = spec.get("title", "")
            test_id = extract_test_id(title)
            if test_id is not None:
                status = "passed" if spec.get("ok") else "failed"
                merge(results, test_id, status)
        for child in suite.get("suites", []):
            walk(child)

    for path in paths:
        if not os.path.isfile(path):
            continue
        with open(path, encoding="utf-8") as fh:
            try:
                data = json.load(fh)
            except json.JSONDecodeError:
                continue
        for suite in data.get("suites", []):
            walk(suite)


def build_report(catalog: dict, results: dict[str, str]) -> dict:
    features_out = []
    summary = {"total": 0, "passed": 0, "failed": 0, "skipped": 0, "notRun": 0}
    seen: set[str] = set()

    for feature in catalog["features"]:
        cases_out = []
        counts = {"passed": 0, "failed": 0, "skipped": 0, "not_run": 0}
        # 台帳記載のケース
        case_ids = list(feature["cases"])
        # 結果にあるが台帳に無い同機能のケースも取り込む
        for found_id in results:
            if feature_of(found_id) == feature["featureId"] and found_id not in case_ids:
                case_ids.append(found_id)
        for case_id in case_ids:
            status = results.get(case_id, "not_run")
            seen.add(case_id)
            layer = next((p for p in case_id.split("-") if p in ("UT", "IT", "FE", "E2E")), "E2E")
            cases_out.append({"id": case_id, "layer": layer, "status": status})
            counts[status] += 1
        total = sum(counts.values())
        features_out.append({
            "featureId": feature["featureId"],
            "name": feature["name"],
            "total": total,
            "passed": counts["passed"],
            "failed": counts["failed"],
            "skipped": counts["skipped"],
            "notRun": counts["not_run"],
            "cases": sorted(cases_out, key=lambda c: c["id"]),
        })
        summary["total"] += total
        summary["passed"] += counts["passed"]
        summary["failed"] += counts["failed"]
        summary["skipped"] += counts["skipped"]
        summary["notRun"] += counts["not_run"]

    return {
        "generatedAt": datetime.now(timezone.utc).isoformat(),
        "summary": summary,
        "features": features_out,
    }


def main() -> int:
    repo_root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    parser = argparse.ArgumentParser(description="テスト結果を機能別に集計する")
    parser.add_argument("--root", default=repo_root)
    parser.add_argument("--catalog", default=os.path.join(repo_root, "scripts", "test-catalog.json"))
    parser.add_argument("--out", default=os.path.join(repo_root, "dashboard", "test-results.json"))
    args = parser.parse_args()

    with open(args.catalog, encoding="utf-8") as fh:
        catalog = json.load(fh)

    results: dict[str, str] = {}
    parse_junit(
        glob.glob(os.path.join(args.root, "backend", "target", "surefire-reports", "*.xml"))
        + glob.glob(os.path.join(args.root, "backend", "target", "failsafe-reports", "*.xml")),
        results,
    )
    parse_vitest(os.path.join(args.root, "frontend", "test-results", "vitest-results.json"), results)
    parse_playwright(glob.glob(os.path.join(args.root, "e2e", "test-results", "*.json")), results)

    report = build_report(catalog, results)

    os.makedirs(os.path.dirname(args.out), exist_ok=True)
    with open(args.out, "w", encoding="utf-8") as fh:
        json.dump(report, fh, ensure_ascii=False, indent=2)
        fh.write("\n")

    s = report["summary"]
    print(f"集計完了: {args.out}")
    print(f"  total={s['total']} passed={s['passed']} failed={s['failed']} "
          f"skipped={s['skipped']} notRun={s['notRun']}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
