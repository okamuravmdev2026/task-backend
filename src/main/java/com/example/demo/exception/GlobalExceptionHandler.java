package com.example.demo.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * アプリケーション全体の例外を一括でキャッチし、適切なJSONレスポンスに変換するクラスです。
 * 【アンチパターン対策】例外を握り潰す、または生のシステムエラーをそのままフロントへ露出させる脆弱性を排除
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @Validによるバリデーションエラー（400 Bad Request）をハンドリングします。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        
        // エラーが発生したフィールドとメッセージを詰め込む
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    /**
     * ビジネスロジックで発生した引数エラーや、見つからないエラー（400 Bad Request）をハンドリングします。
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * 予期せぬすべてのシステム例外（500 Internal Server Error）をハンドリングします。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllExceptions(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "システムエラーが発生しました。管理者にお問い合わせください。");
        // ※セキュリティの観点から、生のex.getMessage()はクライアントに返さず一律固定メッセージにします
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
