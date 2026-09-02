package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.example.demo.repository.TaskRepository;

/**
 * アプリケーションのコンテキスト起動テストです。
 * 【CI/CD最適化】GitHub Actions上にデータベースが存在しない環境を想定し、
 * テスト起動に必要なRepositoryを@MockBeanとしてコンテキストに登録することで、
 * 外部インフラに依存しない高速かつクリーンなCIパイプラインを実証します。
 */
@SpringBootTest
class TaskBackendApplicationTests {

    // 🌟 これを1行追加するだけ！先ほどの重たい exclude のプロパティ記述は【すべて削除】して元に戻します。
    // Springの起動時に偽物の部品を自動注入させ、JPAのDB接続チェックを完全にバイパス（黙らせる）します。
    @MockBean
    private TaskRepository taskRepository;

    @Test
    void contextLoads() {
        // 本番コードには1ミリも影響を与えず、コンテキストの起動確認のみを100%安全にパスさせます
    }

}
