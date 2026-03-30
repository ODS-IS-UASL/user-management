/*
 * 開発システム： ドローン航路基盤システム
 * ファイル名： HealthIndicatorLogger.java
 * 著作権： Copyright (C) 202X-20XX,  経済産業省
 * 会社名： NTT DATA Corporation
 * 更新日： $Date$
 *
 */
package jp.go.meti.drone.com.common.logging.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * health checkログ出力クラス。 各コンポーネントのメソッドの実行が一定時間を超えた時に警告ログを出力する。
 */
@Slf4j
@Aspect
@Component
public class HealthIndicatorLogger {
    /**
     * HealthCheckの経過時間
     */

    @Value("${drone.commom.logging.helthIndicator.delayThreshold:5000}")
    private int delayThreshold;

    /**
     * Pointcut すべてのヘルスチェックを作動させる。
     */
    @Pointcut("execution(* org.springframework.boot.actuate.health.HealthIndicator.*(..))")
    public void healthIndicatorPointcut() {
        // Pointcutとしてのメソッドのため、中身はなし。
    }

    /**
     * Advice 経過時間が一定時間を超えたときにログを出力する。
     *
     * @param joinPoint 処理を実行するタイミング
     * @return メソッドの実行結果
     * @throws Throwable すべてのエラーと例外
     */
    @Around("healthIndicatorPointcut()")
    public Object logActuateHealthAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        try {
            return joinPoint.proceed();
        } finally {
            long endTime = System.currentTimeMillis();
            if (endTime - startTime >= delayThreshold) {
                String declaringTypeName = joinPoint.getTarget().getClass().getName();
                String name = joinPoint.getSignature().getName();
                log.warn(
                    "HealthCheck is delayed - {}.{}() elapsedTime: {}[ms]",
                    declaringTypeName,
                    name,
                    endTime - startTime);
            }
        }
    }
}
