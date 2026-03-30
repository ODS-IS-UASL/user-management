package jp.go.meti.drone.user_attr.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * AuthorizedKeyのユーティリティクラス
 */
@Slf4j
public class AuthorizationKeyUtil {

	/**
	 * コンストラクタ
	 */
    private AuthorizationKeyUtil() {
        // ユーティリティクラスのため、インスタンス化禁止
    }

    /**
     * Authorizationから指定したキー名の値を取得
     * 
     * @param authorizations
     * @param authKeyName
     * @return
     */
    public static String getAuthorizationKey(List<String> authorizations, String authKeyName) {
        if (authorizations == null || authorizations.isEmpty()) {
            return null;
        }
        List<String> tokens = new ArrayList<>();
        for (String header : authorizations) {
            // ヘッダ値が","区切りで複数列挙されていた場合に tokens に格納
            Arrays.stream(header.split(",")).map(String::trim).filter(s -> !s.isEmpty()).forEach(tokens::add);
        }
        String uuid = null;
        for (String value : tokens) {
            if (value.startsWith(authKeyName)) {
                if (value.length() == authKeyName.length()) {
                    uuid = "";
                } else {
                    uuid = value.substring(authKeyName.length() + " ".length());
                }
            }
        }
        return uuid;
    }
}
