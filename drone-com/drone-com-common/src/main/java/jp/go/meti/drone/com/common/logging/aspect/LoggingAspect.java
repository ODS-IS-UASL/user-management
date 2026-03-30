/*
 * 開発システム： ドローン航路基盤システム
 * ファイル名： LoggingAspect.java
 * 著作権： Copyright (C) 202X-20XX,  経済産業省
 * 会社名： NTT DATA Corporation
 * 更新日： $Date$
 *
 */
package jp.go.meti.drone.com.common.logging.aspect;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * ログ出力クラス 各コンポーネントのメソッドの開始終了時に情報ログを出力する。
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect {

    /**
     * byte[]配列ログ 最大長
     */
    @Value("${drone.commom.logging.aspect.resultByteLoggingLimitSize:2000}")
    private int resultByteLoggingLimitSize;

    /**
     * String型配列ログ 最大長
     */
    @Value("${drone.commom.logging.aspect.resultStringLoggingLimitSize:1000}")
    private int resultStringLoggingLimitSize;

    /**
     * すべての Web REST エンドポイントに一致するポイントカット。
     */
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *) || within(@org.springframework.stereotype.Controller *)")
    public void controllerBeanPointcut() {
        // これは単なるポイントカットなので、メソッドは空です。
    }

    /**
     * すべてのリポジトリとサービスに一致するポイントカット。
     */
    @Pointcut("within(@org.springframework.stereotype.Repository *) || within(@org.springframework.stereotype.Service *)")
    public void springBeanPointcut() {
        // これは単なるポイントカットなので、メソッドは空です。
    }

    /**
     * メソッドの処理開始時と終了時のログを記録するアドバイス。
     *
     * @param joinPoint アドバイスのためのジョイン・ポイント
     * @return メソッドの実行結果
     * @throws Throwable 呼び出されたプロシージャが何らかをスローした場合
     */
    @Around("controllerBeanPointcut()")
    public Object logControllerAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String declaringTypeName = joinPoint.getSignature().getDeclaringTypeName();
        String name = joinPoint.getSignature().getName();
        long startTime = System.currentTimeMillis();

        if (log.isInfoEnabled()) {
            log.info(
                "Enter: {}.{}() with argument[s] = {}",
                declaringTypeName,
                name,
                Arrays.toString(joinPoint.getArgs()));
        }

        try {
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            if (log.isDebugEnabled()) {
                log.debug(
                    "Exit: {}.{}() with result = {}: {}[ms]",
                    declaringTypeName,
                    name,
                    getResultLogText(result),
                    endTime - startTime);
            } else {
                log.info("Exit: {}.{}(): {}[ms]", declaringTypeName, name, endTime - startTime);
            }
            return result;

        } catch (Throwable e) {
            log.error("Exception in {}.{}() with cause = {}", declaringTypeName, name, e);
            throw e;
        }
    }

    /**
     * メソッドの処理開始時と終了時のログを記録するアドバイス。
     *
     * @param joinPoint アドバイスのためのジョイン・ポイント
     * @return メソッドの実行結果
     * @throws Throwable 呼び出されたプロシージャが何らかをスローした場合
     */
    @Around("springBeanPointcut()")
    public Object logSpringBeanAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String declaringTypeName = joinPoint.getSignature().getDeclaringTypeName();
        String name = joinPoint.getSignature().getName();
        long startTime = System.currentTimeMillis();

        if (log.isDebugEnabled()) {
            log.debug(
                "START - {}.{}() with argument[s] = {}",
                declaringTypeName,
                name,
                Arrays.toString(joinPoint.getArgs()));
        } else {
            log.info("START - {}.{}()", declaringTypeName, name);
        }
        try {
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            if (log.isDebugEnabled()) {
                log.debug(
                    "END - {}.{}() with result = {}: {}[ms]",
                    declaringTypeName,
                    name,
                    getResultLogText(result),
                    endTime - startTime);
            } else {
                log.info("END - {}.{}(): {}[ms]", declaringTypeName, name, endTime - startTime);
            }
            return result;

        } catch (Throwable e) {
            log.info("ABEND - {}.{}() with cause = {}", declaringTypeName, name, e.getMessage());
            throw e;
        }
    }

    /**
     * resultのサイズによってログの出力内容を変えるメソッド
     *
     * @param result メソッドの実行結果
     * @return 結果ログテキスト
     */
    private Object getResultLogText(Object result) {

        // 型がバイト配列かつ、配列の長さが一定数以上のときはサイズを出力する
        if (result instanceof byte[] byteArray && byteArray.length >= resultByteLoggingLimitSize) {
            return "byte[] length=" + byteArray.length;
        }
        // 型が文字列かつ、文字数が一定数以上のときはサイズを出力する
        else if (result instanceof String string && string.length() >= resultStringLoggingLimitSize) {
            return "String length=" + string.length();
        }
        // 上記どれにも当てはまらない場合はそのまま出力する
        else {
            return result;
        }
    }
}
