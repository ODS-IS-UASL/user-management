package jp.go.meti.drone.com.common.logging;

import org.slf4j.Logger;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 本システム用のLoggerを作成するFactoryクラス。
 * 
 * @version $Revision$
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoggerFactory {

    /**
     * アプリケーションログ出力オブジェクトを返却する
     * 
     * @param delegateLogger SLF4Jロガー
     * @return アプリケーションログ出力オブジェクト
     */
    public static ApplicationLogger getApplicationLogger(Logger delegateLogger) {
        return new ApplicationLoggerImpl(delegateLogger);
    }

}
