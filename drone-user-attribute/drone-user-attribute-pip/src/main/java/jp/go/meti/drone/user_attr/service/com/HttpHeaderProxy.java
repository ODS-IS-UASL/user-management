package jp.go.meti.drone.user_attr.service.com;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * HttpHeaderProxy　Httpヘッダー操作代理サービスクラス
 */
@Slf4j
@Service
public class HttpHeaderProxy {

	/**
	 * ヘッダーのフィールドをログに出力
	 * 
	 * @param headers
	 */
    public void showHeaderAsMap(Map<String, String> headers) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("\n\n==== HttpHeaderProxy showHeaderAsMap ================\n");
        headers.forEach((key, value) -> logMessage.append(key).append(" : ").append(value).append("\n"));
        logMessage.append("==== user_attr HttpHeaderProxy showHeaderAsMap ================\n\n");
        log.debug(logMessage.toString());
    }

	/**
	 * HTTPヘッダーのフィールドをログに出力
	 * 
	 * @param headers
	 */
    public void showHeader(HttpHeaders responseHeaders) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("\n\n==== HttpHeaderProxy showHeader ================\n");
        responseHeaders.forEach((key, value) -> logMessage.append(key).append(" : ").append(value).append("\n"));
        logMessage.append("==== HttpHeaderProxy showHeader ================\n\n");
        log.debug(logMessage.toString());
    }

    /**
     * ヘッダー情報をHTTPヘッダーに詰め替え
     * 
     * @param headerMap
     * @return
     */
    public HttpHeaders convertToHttpHeaders(Map<String, String> headerMap) {
        HttpHeaders httpHeaders = new HttpHeaders();
        if (headerMap != null) {
            headerMap.forEach(httpHeaders::set);
        }
        return httpHeaders;
    }

    /**
     * Httpヘッダーから指定したフィールドを取得
     * 
     * @param headers
     * @param entryKey
     * @return
     */
    public List<String> getEntry(HttpHeaders headers, String entryKey) {
        List<String> entryValue = null;
        for (Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(entryKey)) {
                entryValue = entry.getValue();
                break;
            }
        }
        return entryValue;
    }

    /**
     * ヘッダーから指定したフィールドのヘッダー値がパターンと一致するか確認して取得（一致しない場合はnull返却）
     * 
     * @param headers
     * @param key
     * @param elementName
     * @return
     */
    public String getHeaderElement(Map<String, String> headers, String key, String elementName) {
        if (headers == null || key == null || elementName == null) {
            return null;
        }

        String targetKey = headers.keySet().stream().filter(k -> k.equalsIgnoreCase(key)).findFirst().orElse(null);

        if (targetKey == null) {
            return null;
        }

        String value = headers.get(targetKey);
        if (value == null || value.isBlank()) {
            return null;
        }

        Pattern pattern = Pattern.compile("(^|,\\s*)" + elementName + "\\s+([^,\\s]+)(?=\\s*(,|$))");
        Matcher matcher = pattern.matcher(value);

        if (!matcher.find()) {
            return null;
        }

        String element = matcher.group(2);
        return element;
    }

    /**
     * ヘッダーに指定したフィールドを追加またはヘッダー値を置き換え
     * 
     * @param headers
     * @param key
     * @param value
     */
    public void addOrReplaceHeader(Map<String, String> headers, String key, String value) {
        headers.put(key, value);
    }

    /**
     * HTTPヘッダーに指定したフィールドを追加またはヘッダー値を置き換え
     * 
     * @param headers
     * @param key
     * @param value
     */
    public void addOrReplaceHeader(HttpHeaders headers, String key, String value) {
        headers.set(key, value);
    }

    /**
     * HTTPヘッダーにヘッダー情報追加
     * 
     * @param headers
     * @param newHeaders
     */
    public void addHeaders(HttpHeaders headers, Map<String, String> newHeaders) {
        newHeaders.forEach(headers::set);
    }

    /**
     * ヘッダーにヘッダー情報追加
     * 
     * @param headers
     * @param newHeaders
     */
    public void addHeaders(Map<String, String> headers, Map<String, String> newHeaders) {
        headers.putAll(newHeaders);
    }

    /**
     * ヘッダーで指定したフィールドから指定したヘッダー値を削除
     * 
     * @param headers
     * @param key
     * @param elementName
     * @return
     */
    public String removeHeaderElement(Map<String, String> headers, String key, String elementName) {
        if (headers == null || key == null || elementName == null) {
            return null;
        }

        String targetKey = headers.keySet().stream().filter(k -> k.equalsIgnoreCase(key)).findFirst().orElse(null);

        if (targetKey == null) {
            return null;
        }

        String value = headers.get(targetKey);
        if (value == null || value.isBlank()) {
            return null;
        }

        Pattern pattern = Pattern.compile("(^|,\\s*)" + elementName + "\\s+([^,\\s]+)(?=\\s*(,|$))");
        Matcher matcher = pattern.matcher(value);

        if (!matcher.find()) {
            return null;
        }

        String removedToken = matcher.group(2);
        String updated = matcher.replaceFirst("")
            .replaceAll("^\\s*,\\s*", "")
            .replaceAll("\\s*,\\s*$", "")
            .replaceAll("\\s*,\\s*,\\s*", ", ")
            .trim();
        headers.put(targetKey, updated);
        return removedToken;
    }

    /**
     * ヘッダーで指定したフィールドに指定したヘッダー値を追加
     * 
     * @param headers
     * @param key
     * @param elementName
     * @param elementValue
     */
    public void addHeaderElement(Map<String, String> headers, String key, String elementName, String elementValue) {
        if (headers == null || key == null || elementName == null || elementValue == null)
            return;
        String targetKey = headers.keySet().stream().filter(k -> k.equalsIgnoreCase(key)).findFirst().orElse(key);

        String current = headers.get(targetKey);

        String newToken = elementName + " " + elementValue;
        if (current == null || current.isBlank()) {
            headers.put(targetKey, newToken);
            return;
        }

        if (current.toLowerCase().contains(elementName.toLowerCase())) {
            return;
        }
        headers.put(targetKey, newToken + ", " + current.trim());
    }

    /**
     * ヘッダーに指定したフィールドを追加
     * 
     * @param headers
     * @param key
     * @param value
     */
    public void addHeader(Map<String, String> headers, String key, String value) {
        if (headers == null || key == null) {
            return;
        }
        headers.put(key, value);
    }

    /**
     * HTTPヘッダーに指定したフィールドを追加
     * 
     * @param headers
     * @param key
     * @param value
     */
    public void addHeader(HttpHeaders headers, String key, List<String> value) {
        if (headers == null || key == null) {
            return;
        }
        headers.put(key, value);
    }

    /** 
     * ヘッダーから指定したフィールドを削除
     * 
     * @param headers
     * @param key
     * @return
     */
    public String removeHeader(Map<String, String> headers, String key) {
        if (headers == null || key == null) {
            return null;
        }
        String value = headers.get(key);
        headers.remove(key);
        return value;
    }

    /**
     * HTTPヘッダーから指定したフィールドを削除
     * 
     * @param headers
     * @param key
     * @return
     */
    public List<String> removeHeader(HttpHeaders headers, String key) {
        if (headers == null || key == null) {
            return new ArrayList<>();
        }
        List<String> value = headers.get(key);
        headers.remove(key);
        return value;
    }
}
