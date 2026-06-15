-- カテゴリの初期マスタデータ
insert
into
	category
(
	category_name
,	created_at
,	updated_at
) values
	(N'文房具', sysutcdatetime(), sysutcdatetime())
,	(N'家電',   sysutcdatetime(), sysutcdatetime())
,	(N'食品',   sysutcdatetime(), sysutcdatetime())
;
