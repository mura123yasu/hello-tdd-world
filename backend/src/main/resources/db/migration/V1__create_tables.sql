-- product 在庫管理システム 初期スキーマ (SQL Server)
-- 物理名・型は docs/application-spec.md のデータモデルに準拠する。
create table category (
	category_id		int				identity(1, 1)	not null
,	category_name	nvarchar(50)					not null
,	created_at		datetime2						not null
,	updated_at		datetime2						not null
,	constraint pk_category primary key (category_id)
,	constraint uq_category_name unique (category_name)
);

create table product (
	product_id		int				identity(1, 1)	not null
,	product_code	varchar(20)						not null
,	product_name	nvarchar(100)					not null
,	category_id		int								not null
,	unit_price		decimal(12, 2)					not null
,	status			varchar(10)						not null
,	description		nvarchar(500)						null
,	deleted_flag	bit								not null	constraint df_product_deleted_flag default (0)
,	version			int								not null	constraint df_product_version default (0)
,	created_at		datetime2						not null
,	updated_at		datetime2						not null
,	constraint pk_product primary key (product_id)
,	constraint uq_product_code unique (product_code)
,	constraint fk_product_category foreign key (category_id) references category (category_id)
,	constraint ck_product_unit_price check (unit_price >= 0)
,	constraint ck_product_status check (status in ('ACTIVE', 'INACTIVE'))
);

create table stock (
	product_id	int			not null
,	quantity	int			not null	constraint df_stock_quantity default (0)
,	updated_at	datetime2	not null
,	constraint pk_stock primary key (product_id)
,	constraint fk_stock_product foreign key (product_id) references product (product_id)
,	constraint ck_stock_quantity check (quantity >= 0)
);

create table stock_transaction (
	transaction_id		bigint			identity(1, 1)	not null
,	product_id			int								not null
,	transaction_type	varchar(3)						not null
,	quantity			int								not null
,	transaction_at		datetime2						not null
,	note				nvarchar(200)						null
,	constraint pk_stock_transaction primary key (transaction_id)
,	constraint fk_stock_transaction_product foreign key (product_id) references product (product_id)
,	constraint ck_stock_transaction_type check (transaction_type in ('IN', 'OUT'))
,	constraint ck_stock_transaction_quantity check (quantity > 0)
);
