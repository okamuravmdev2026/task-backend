package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 【GitHub Actions / CI専用】
 * テスト実行時のみ、接続先が欠落しているデータベースの自動初期化をスキップし、
 * 環境依存によるCIの自爆（Assert.java:97）を根本から隔離するためのクラスです。
 */
@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
    "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
class TaskBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
