package com.example.inventory.it;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * 結合テストの基底クラス。実際の SQL Server を Testcontainers で起動する。
 *
 * <p>Docker デーモンが必要なため、ローカルの {@code mvn test}（surefire）では実行されない。
 * {@code *IT} 命名により failsafe（{@code mvn verify}）で実行され、CI（Docker あり）で動作する。
 */
@Testcontainers
@SpringBootTest
public abstract class AbstractIntegrationTest {

	@Container
	@ServiceConnection
	static final MSSQLServerContainer<?> SQL_SERVER =
			new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2022-latest")
					.acceptLicense();

	@DynamicPropertySource
	static void registerProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.flyway.enabled", () -> "true");
	}
}
