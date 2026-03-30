package jp.go.meti.drone.com.common.interceptor;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DB共通カラム更新インターセプタークラスのメッセージID列挙クラス。
 * <p>
 * インターセプタークラスが出力するメッセージのメッセージIDを定義する。
 * </p>
 * 
 * @version $Revision$
 */
@AllArgsConstructor
public enum CommonPropertyInterceptorMessageKeys {

    /**
     * CommonPropertyUpdateInterceptorのinfoログ1。
     */
    I_COMMON_0001("i.common.0001");

    /**
     * メッセージID。<br>
     * メッセージプロパティファイルに定義されているメッセージ文字列を一意に識別するためのキーとして使用する。
     */
    @Getter
    private String key;

    /*
     * @AllArgsConstructor付与により、コンストラクタは自動生成される
     * @Getter付与により、ゲッターは自動生成される
     */
}
