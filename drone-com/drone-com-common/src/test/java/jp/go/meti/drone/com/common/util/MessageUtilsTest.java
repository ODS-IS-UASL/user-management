package jp.go.meti.drone.com.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.go.meti.drone.com.common.interceptor.CommonPropertyInterceptorMessageKeys;
import lombok.extern.slf4j.Slf4j;

/**
 * MessageUtils.java のテストクラス
 */
@Slf4j
class MessageUtilsTest {

    /**
     * com-common-messages.properties の DB共通カラム更新時の変数Nullチェックログ のメッセージ確認
     */
    @Test
    void testMessageOutput() {
        String acutualMessage = MessageUtils.getContextMessage(
            CommonPropertyInterceptorMessageKeys.I_COMMON_0001.getKey(),
            "Invocation#getArgs");
        assertThat(acutualMessage).isEqualTo(
            "Argument of Executor#update method is null. Null argument is Invocation#getArgs.");
    }

}
