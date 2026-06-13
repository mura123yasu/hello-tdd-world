# SQL コーディング規約（SQL Server 向け・コンテキスト用要約）

> 本書は [Future 社 SQL コーディング規約（PostgreSQL）](https://future-architect.github.io/coding-standards/documents/forSQL/SQL%E3%82%B3%E3%83%BC%E3%83%87%E3%82%A3%E3%83%B3%E3%82%B0%E8%A6%8F%E7%B4%84%EF%BC%88PostgreSQL%EF%BC%89.html)（CC-BY-4.0）を要約し、**SQL Server 向けに読み替えた** ものです。原典は PostgreSQL 向けのため、データ型・関数・方言の差異は SQL Server に合わせています。詳細・最新は原典を参照してください。

## 0. SQL Server 読み替え早見表
| 観点 | PostgreSQL（原典） | SQL Server（本プロジェクト） |
| --- | --- | --- |
| 文字列型 | `text` / `varchar` | `VARCHAR` / `NVARCHAR`（多言語は `NVARCHAR`） |
| 日時型 | `timestamp` | `DATETIME2` |
| 真偽型 | `boolean` | `BIT` |
| 連番 | `serial` / `IDENTITY` | `INT IDENTITY(1,1)` |
| LIMIT/OFFSET | `LIMIT n OFFSET m` | `OFFSET m ROWS FETCH NEXT n ROWS ONLY` |
| 文字列結合 | `||` | `CONCAT()` または `+` |
| 真偽値比較 | `= true` | `= 1`（BIT） |
| ヒント | `/*+ ... */` | `OPTION (...)` / テーブルヒント |

> SQL Server の予約語は大文字慣習も一般的だが、本プロジェクトでは原典に従い **キーワードは小文字** に統一する。識別子（テーブル名・カラム名）も小文字＋スネークケース。

## 1. フォーマット・構造
- **1 行 1 文**。インデントは原典に従いタブ（Java と整合）。1 行は 80 文字程度を目安に改行。
- DML のキーワード（`select` / `from` / `where` など）は左寄せ、項目は改行＋インデント。
- **カンマは行頭** に置く（行末ではない）。
- ファイル先頭にヘッダコメントを置かない（DB 解析を妨げるため）。ファイルは `select`/`update`/`insert`/`delete` で始める。
- 論理名は行末の単一行コメントで付与: `-- 論理名`。インラインは `/* コメント */`。
- フレームワーク利用時は末尾のセミコロンを省略する。
- **キーワード・識別子は小文字**。

## 2. 命名・エイリアス
- 略語: 外来語は原綴を短縮 or 母音除去（corporation→corp）。ローマ字は語境界 or 子音（nichijo→nchj）。
- **カラムには必ずテーブルエイリアスを付与**。単一テーブルの SELECT でもエイリアスを付ける。
- エイリアスは 1 つの SQL 内で一意。サブクエリのエイリアス名をメインクエリで再利用しない。
- SQL ファイルは UTF-8。

## 3. 文の整形例

### SELECT
```sql
select
	prd.product_id		as	product_id		-- 商品ID
,	prd.product_name	as	product_name	-- 商品名
,	prd.unit_price		as	unit_price		-- 単価
from
	product	prd	-- 商品
where
	prd.status			=	'ACTIVE'
and	prd.deleted_flag	=	0
order by
	prd.product_id
```
- WHERE 句の `=` / `!=` / `is` は縦に揃える。
- 全トップレベル列に `as` 別名を付与（同名でも省略しない）。

### INSERT
```sql
insert
into
	product	-- 商品
(
	product_code	-- 商品コード
,	product_name	-- 商品名
) values (
	'A001'
,	N'サンプル商品'
)
```
- INSERT は列を明示列挙（`*` 禁止）。

### UPDATE
```sql
update
	product	prd	-- 商品
set
	prd.product_name	=	N'更新後名称'	-- 商品名
,	prd.version			=	prd.version + 1
where
	prd.product_id		=	10
and	prd.version			=	3
```

### DELETE
```sql
delete
from
	product	prd	-- 商品
where
	prd.product_id	=	1
```

## 4. 高度な整形
- **CASE**: `case` / `when` / `then` / `else` / `end` の後で改行。`case`〜`end` の間は 1 段インデント。
- **WITH(CTE)**: `with` の前後で改行、CTE 間はカンマ行頭区切り。
- **OFFSET/FETCH**(SQL Server): キーワード前で改行。
- **IN**: カンマ後にスペース 1 つ。
- 比較演算子の前後はタブ/スペース 1 つ。

## 5. コメント
- 単一行は `--`（列の論理名など）。強調する複数行は `/* */`（アスタリスク縦揃え）。複数行コメントアウトは各行 `--`（`/* */` のネスト回避）。
- 修正履歴コメント:
```sql
-- YYYY/MM/DD 課題ID ADD(MOD/DEL) 担当者 S
（修正コード）
-- YYYY/MM/DD 課題ID ADD(MOD/DEL) 担当者 E
```
- 論理名は SELECT/INSERT/UPDATE/MERGE の列・テーブル部に必須。ERD 定義と一致させる。

## 6. データ型・式
- 暗黙の型変換を禁止し、明示的に変換する（SQL Server は `CAST`/`CONVERT`）。
- 不等号は `!=` で統一（`<>` を使わない）。
- ORDER BY / GROUP BY は列名で指定（序数禁止）。GROUP BY は常に明示。
- `EXISTS` のサブクエリ SELECT は定数 `1` を返す（`*` や `'X'` 不可）。
- 文字列リテラル中のシングルクォートは `''` でエスケープ（バックスラッシュを使わない）。
- 多言語文字列リテラルは `N'...'` を使う（SQL Server）。

## 7. WHERE 句
- 左辺に列、右辺に式/値。
- 条件は (1) FROM の順にテーブルでグルーピング、(2) 各テーブル内は「絞り込み条件 → 結合条件」の順。`and`/`or` は行頭に改行して置く。
```sql
where
-- product 絞り込み
	prd.status		=	?
-- product と stock の結合
and	stk.product_id	=	prd.product_id
-- stock 絞り込み
and	stk.quantity	>	0
```

## 8. パフォーマンス
- 中間/後方一致（`like '%x%'` / `like '%x'`）はインデックスが効かないため避ける。可能なら `=` を使う。
- 列選択に `*` を使わない（INSERT も列明示）。
- インデックス列を加工しない: `col1 + 1 > 100` ❌ → `col1 > 99` ✓ / `to_char/CONVERT(col1) > v` ❌ → `col1 >= 変換(v)` ✓。インデックス列への OR は `IN` / JOIN に置換。
- 主キー・（パーティション）キーの更新を避ける。VIEW 経由の更新を避け基テーブルを更新する。
- 大量行（数百万件規模）の一括 DELETE を避ける。
- `DISTINCT` は暗黙ソートを招くため `EXISTS` を検討。`IN` は 100 件まで（超過はサブクエリ分割）。`NOT IN` は `not exists` に置換（フルスキャン回避）。`UNION` より `UNION ALL`。
- 複数行ロックの `SELECT ... FOR UPDATE`（SQL Server では `WITH (UPDLOCK)` 等）では `ORDER BY` を付けデッドロック回避。
- 分析関数・インデックス採否は DBA に相談。外部結合は想定件数が最小のテーブルを起点（駆動表）にする。

---

## 本プロジェクトでの適用メモ
- マイグレーションは Flyway（`db/migration`）で管理し、上記整形に従う。
- 物理名・型は [application-spec.md のデータモデル](../application-spec.md#2-データモデル論理) に準拠（SQL Server 型）。
- テスト容易性のため、複雑な検索は明示的な JOIN と条件順序で記述する。
