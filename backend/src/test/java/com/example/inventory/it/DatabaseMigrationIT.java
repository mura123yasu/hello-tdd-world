package com.example.inventory.it;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Flyway マイグレーションが実 SQL Server に対して適用されることを検証する結合テスト（CI / Docker 必須）。
 */
class DatabaseMigrationIT extends AbstractIntegrationTest {

	@Autowired
	private DataSource dataSource;

	@Test
	@DisplayName("SKELETON-IT-01 マイグレーションでテーブルが作成され、初期カテゴリが投入される")
	void skeleton_it_01_migration_applied() {
		JdbcTemplate jdbc = new JdbcTemplate(dataSource);

		Integer productTable = jdbc.queryForObject(
				"select count(*) from information_schema.tables where table_name = 'product'",
				Integer.class);
		Integer categoryCount = jdbc.queryForObject("select count(*) from category", Integer.class);

		assertThat(productTable).isEqualTo(1);
		assertThat(categoryCount).isGreaterThanOrEqualTo(3);
	}
}
