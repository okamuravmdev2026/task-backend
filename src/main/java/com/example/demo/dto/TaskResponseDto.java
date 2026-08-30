package com.example.demo.dto;

import java.time.LocalDateTime;

import com.example.demo.domain.TaskStatus;

/**
 * フロントエンドに返却するタスク情報のデータ転送オブジェクト(DTO)です。
 * 【Java機能アピール】Java 14以降の目玉機能である「Record」を採用し、データの不変性(Immutable)を保証
 */
public record TaskResponseDto(
    Long id,
    String title,
    String description,
    TaskStatus status,
    LocalDateTime createdAt
) {
    // ゲッターやコンストラクタはJavaが自動生成するため、コードが非常にスッキリします
}
