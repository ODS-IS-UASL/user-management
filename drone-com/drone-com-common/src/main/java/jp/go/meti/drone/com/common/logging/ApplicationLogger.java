package jp.go.meti.drone.com.common.logging;

/**
 * 業務ログ用出力インタフェース。
 * 
 * @version $Revision$
 */
public interface ApplicationLogger {

    /**
     * traceログを出力する
     * 
     * @param format メッセージフォーマット
     * @param args 埋め込み文字列、スタックトレースを出力する場合は最後に例外を指定する
     */
    void trace(String format, Object... args);

    /**
     * traceレベルが有効かどうかを評価する
     * 
     * @return 有効の場合は{@code true}、無効の場合は{@code false}を返却
     */
    boolean isTraceEnabled();

    /**
     * debugログを出力する
     * 
     * @param format メッセージフォーマット
     * @param args 埋め込み文字列、スタックトレースを出力する場合は最後に例外を指定する
     */
    void debug(String format, Object... args);

    /**
     * debugレベルが有効かどうかを評価する
     * 
     * @return 有効の場合は{@code true}、無効の場合は{@code false}を返却
     */
    boolean isDebugEnabled();

    /**
     * infoログを出力する
     * 
     * @param messageId メッセージID
     * @param args 埋め込み文字列、スタックトレースを出力する場合は最後に例外を指定する
     */
    void info(String messageId, Object... args);

    /**
     * infoレベルが有効かどうかを評価する
     * 
     * @return 有効の場合は{@code true}、無効の場合は{@code false}を返却
     */
    boolean isInfoEnabled();

    /**
     * warnログを出力する
     * 
     * @param messageId メッセージID
     * @param args 埋め込み文字列、スタックトレースを出力する場合は最後に例外を指定する
     */
    void warn(String messageId, Object... args);

    /**
     * warnレベルが有効かどうかを評価する
     * 
     * @return 有効の場合は{@code true}、無効の場合は{@code false}を返却
     */
    boolean isWarnEnabled();

    /**
     * errorログを出力する
     * 
     * @param messageId メッセージID
     * @param args 埋め込み文字列、スタックトレースを出力する場合は最後に例外を指定する
     */
    void error(String messageId, Object... args);

    /**
     * errorレベルが有効かどうかを評価する
     * 
     * @return 有効の場合は{@code true}、無効の場合は{@code false}を返却
     */
    boolean isErrorEnabled();
}
