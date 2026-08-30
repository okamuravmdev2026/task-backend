package com.example.demo.domain;

import java.util.Arrays;
import java.util.Optional;

/**
 * タスクのステータスを管理する列挙型です。
 * 【Java機能アピール】Enum, Stream API, Optional の活用
 */
public enum TaskStatus {
    TODO,
    DOING,
    DONE;

    /**
     * 文字列から安全にTaskStatusを取得します。
     * @param statusStr 状態文字列 (例: "TODO")
     * @return 該当するTaskStatus（存在しない場合は空のOptional）
     */
    public static Optional<TaskStatus> of(String statusStr) {
        if (statusStr == null) {
            return Optional.empty();
        }
        
        // Stream APIとラムダ式を使って配列から安全に検索（アンチパターン：for文での泥臭いループを回避）
        return Arrays.stream(TaskStatus.values())
                .filter(status -> status.name().equalsIgnoreCase(statusStr.trim()))
                .findFirst();
    }
}
