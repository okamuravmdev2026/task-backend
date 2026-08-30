package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * フロントエンドからの登録・更新リクエストを受け取るDTOです。
 * 【Java機能アピール】Recordによる不変オブジェクト設計
 * 【アンチパターン対策】JSR-383標準バリデーションによる不正データの徹底排除
 */
public record TaskRequestDto(
    @NotBlank(message = "タイトルは必須入力です")
    @Size(max = 100, message = "タイトルは100文字以内で入力してください")
    String title,

    @Size(max = 500, message = "説明は500文字以内で入力してください")
    String description,

    @NotBlank(message = "ステータスは必須入力です")
    String status
) {
}
