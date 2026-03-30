package jp.go.meti.drone.com.common.util;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * メッセージ操作用のユーティリティクラス。
 * 
 * @version $Revision$
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class MessageUtils {

    /** メッセージ未定義通知用の文字列 */
    private static final String UNDEFINED_MESSAGE_FORMAT = "UNDEFINED-MESSAGE: messageId={0}, args={1}";

    /** MessageSourceオブジェクト */
    private static final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();

    static {
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setDefaultLocale(Locale.JAPAN);

        try {
            // メッセージプロパティのリソース一覧取得
            var resolver = new PathMatchingResourcePatternResolver();
            var resources = resolver.getResources("classpath*:messages/*-messages.properties");

            // リソースのパスからメッセージリソースのベース名を抽出し、messageSource に設定
            var re = Pattern.compile("^.*(messages/[^/]+-messages).properties$");
            for (var resource : resources) {
                // ※URI#getPath() は schema が jar のときは null になる。
                // ※schema が file, jar のどちらも場合も、URI#toString() の最後はパスになっているため、これを採用する。
                var path = resource.getURI().toString();
                var m = re.matcher(path);
                if (m.matches()) {
                    var basename = m.group(1);
                    log.debug("basename: {}", basename);
                    messageSource.addBasenames(basename);
                }
            }
        } catch (IOException e) {
            log.error("MessageSource initialize failed.", e);
        }
    }

    /**
     * メッセージを取得する。
     * <p>
     * 言語に依存しないロケールのメッセージを取得する。
     * 
     * @param messageId メッセージID
     * @param args メッセージパラメータ
     * @return メッセージ
     */
    public static String getMessage(String messageId, Object... args) {
        return getMessage(messageId, args, Locale.ROOT);
    }

    /**
     * メッセージを取得する。
     * <p>
     * 現在のロケールのメッセージを取得する。
     *
     * @param messageId メッセージID
     * @param args メッセージパラメータ
     * @return メッセージ
     */
    public static String getContextMessage(String messageId, Object... args) {
        return getMessage(messageId, args, LocaleContextHolder.getLocale());
    }

    /**
     * メッセージを取得する。
     * <p>
     * ロケールに応じたメッセージを取得する。
     * 
     * @param messageId メッセージID
     * @param args メッセージパラメータ
     * @param locale ロケール
     * @return メッセージ
     */
    public static String getMessage(String messageId, Object[] args, Locale locale) {
        try {
            return messageSource.getMessage(messageId, args, locale);
        } catch (NoSuchMessageException e) {
            return MessageFormat.format(UNDEFINED_MESSAGE_FORMAT, messageId, Arrays.toString(args));
        }
    }

    /**
     * MessageSource 取得。
     * 
     * @return MessgeSource
     */
    public static MessageSource getMessageSource() {
        return messageSource;
    }
}
