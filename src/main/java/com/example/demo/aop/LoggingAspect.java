package com.example.demo.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * システムの横断的なログ出力を一括制御するアスペクトクラスです。
 * 【Spring機能アピール】AOP（面指向プログラミング）による共通関心事の分離
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    /**
     * controllerパッケージ配下のすべてのパブリックメソッドの実行前後にログを出力します。
     * 【アンチパターン対策】各Controllerに泥臭くログ出力を1行ずつ書く手間とバグを排除
     */
    @Around("execution(public * com.example.demo.controller..*.*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        
        log.info("[START] API実行: {}", methodName);
        long startTime = System.currentTimeMillis();

        try {
            // 本来の処理（Controllerメソッド）を実行
            Object result = joinPoint.proceed();
            
            long elapsedTime = System.currentTimeMillis() - startTime;
            log.info("[END] API正常終了: {} (処理時間: {}ms)", methodName, elapsedTime);
            return result;
            
        } catch (Throwable e) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            log.error("[ERROR] API異常終了: {} (発生例外: {}, 処理時間: {}ms)", methodName, e.getMessage(), elapsedTime);
            throw e; // 例外はそのまま上位（GlobalExceptionHandler）へ伝播
        }
    }
}
