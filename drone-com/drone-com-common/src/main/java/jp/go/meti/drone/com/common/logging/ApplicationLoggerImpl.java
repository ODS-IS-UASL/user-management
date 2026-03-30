package jp.go.meti.drone.com.common.logging;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.helpers.MessageFormatter;

import jp.go.meti.drone.com.common.util.MessageUtils;
import lombok.RequiredArgsConstructor;

/**
 * 業務ログ用ロガーの実装クラス。
 * 
 * @version $Revision$
 */
@RequiredArgsConstructor
public class ApplicationLoggerImpl implements ApplicationLogger {

    /**
     * ログパラメータ最大出力文字数
     */
    private static final int MAX_LOGPARAM_STR_LEN = 1000;

    /**
     * ログ出力に使用するSLF4Jロガー
     */
    private final Logger delegateLogger;

    /**
     * {@inheritDoc}
     */
    @Override
    public void trace(String format, Object... args) {
        delegateLogger.trace(format, args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTraceEnabled() {
        return delegateLogger.isTraceEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debug(String format, Object... args) {
        delegateLogger.debug(format, args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDebugEnabled() {
        return delegateLogger.isDebugEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info(String messageId, Object... args) {
        if (delegateLogger.isInfoEnabled()) {
            // 文字数が一定数を超えた場合、打ち切る
            Object[] objArgs = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                objArgs[i] = StringUtils.left(String.valueOf(args[i]), MAX_LOGPARAM_STR_LEN);
            }
            logging(delegateLogger::info, delegateLogger::info, messageId, objArgs);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInfoEnabled() {
        return delegateLogger.isInfoEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warn(final String messageId, final Object... args) {
        if (delegateLogger.isWarnEnabled()) {
            logging(delegateLogger::warn, delegateLogger::warn, messageId, args);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWarnEnabled() {
        return delegateLogger.isWarnEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(final String messageId, final Object... args) {
        if (delegateLogger.isErrorEnabled()) {
            logging(delegateLogger::error, delegateLogger::error, messageId, args);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isErrorEnabled() {
        return delegateLogger.isErrorEnabled();
    }

    /**
     * ロギングメソッド。
     * 
     * @param loggingWithThrowable 例外オブジェクトありのロガー
     * @param logging 例外オブジェクトなしのロガー
     * @param messageId メッセージID
     * @param args 埋め込み文字列
     */
    private void logging(BiConsumer<String, Throwable> loggingWithThrowable, Consumer<String> logging, String messageId,
        Object... args) {
        Throwable throwableCandidate = MessageFormatter.getThrowableCandidate(args);
        if (throwableCandidate != null) {
            Object[] trimmedCopy = MessageFormatter.trimmedCopy(args);
            loggingWithThrowable.accept(MessageUtils.getMessage(messageId, trimmedCopy), throwableCandidate);
        } else {
            logging.accept(MessageUtils.getMessage(messageId, args));
        }
    }

}
