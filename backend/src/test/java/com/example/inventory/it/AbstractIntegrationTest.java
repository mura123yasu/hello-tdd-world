package com.example.inventory.it;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MSSQLServerContainer;

/**
 * 結合テストの基底クラス。実際の SQL Server を Testcontainers で起動する。
 *
 * <p>コンテナは静的初期化子で一度だけ起動し、全 IT クラスで共有する（シングルトンコンテナ・パターン）。
 * {@code @Testcontainers} / {@code @Container} を使うと最初のテストクラスの {@code afterAll} で
 * 共有コンテナが停止され、後続の IT クラスが接続できず {@code CannotCreateTransaction} となる。
 * そのため JUnit のライフサイクルに任せず手動で起動し、JVM 終了時に Ryuk が破棄する。
 *
 * <p>Docker デーモンが必要なため、ローカルの {@code mvn test}（surefire）では実行されない。
 * {@code *IT} 命名により failsafe（{@code mvn verify}）で実行され、CI（Docker あり）で動作する。
 */
@SpringBootTest
public abstract class AbstractIntegrationTest {

	@ServiceConnection
	static final MSSQLServerContainer<?> SQL_SERVER =
			new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2022-latest")
					.acceptLicense();

	static {
		SQL_SERVER.start();
	}

	@DynamicPropertySource
	static void registerProperties(final DynamicPropertyRegistry registry) {
		registry.add("spring.flyway.enabled", () -> "true");
	}
}
