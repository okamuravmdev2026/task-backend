package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.domain.Task;

/**
 * タスクテーブルへのデータアクセスを提供するリポジトリです。
 * 【Java機能アピール】インターフェースの活用、SpringによるDI（依存性の注入）の前提設計
 * JpaRepositoryを継承するだけで、基本的なCRUD操作（保存、削除、ID検索など）が自動実装されます。
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // 【アンチパターン対策】生SQLを文字列結合して発行するのを防ぎ、SQLインジェクション脆弱性を根本排除します。
}
