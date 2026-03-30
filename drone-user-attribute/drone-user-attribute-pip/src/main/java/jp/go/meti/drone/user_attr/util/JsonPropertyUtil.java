package jp.go.meti.drone.user_attr.util;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

import lombok.extern.slf4j.Slf4j;

/**
 * JsonPropertyのユーティリティクラス
 */
@Slf4j
public class JsonPropertyUtil {

	/**
	 * オブジェクトマッパー
	 */
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * コンストラクタ
     */
    private JsonPropertyUtil() {
        // ユーティリティクラスのため、インスタンス化禁止
    }

    /**
     * リクエストクラスからJsonProperty名を取得する
     * 
     * @param clazz JsonProperty名取得対象のクラス
     * @param fieldName フィールド名
     * @return デコードした対象クレームの値
     */

    public static String getJsonPropertyName(Class<?> clazz, String fieldName) {
        String[] parts = fieldName.split("\\.");

        Class<?> currentClass = clazz;
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            String javaFieldName = parts[i];

            // JSON名を取得
            String jsonName = getJsonPropertyNameSingle(currentClass, javaFieldName);

            // 親クラスの@JsonPropertyを追加
            if (result.length() > 0) {
                result.append(".");
            }
            result.append(jsonName);

            // 次の階層へ進む
            try {
                Field field = clazz.getDeclaredField(javaFieldName);
                currentClass = field.getType();
            } catch (NoSuchFieldException e) {
                log.debug("対象項目名：" + result.toString());
                return result.toString();
            }
        }

        log.debug("対象項目名：" + result.toString());
        return result.toString();
    }

    /**
     * JsonProperty名取得対象のクラスからJSON名を取得
     * 
     * @param clazz
     * @param fieldName
     * @return
     */
    private static String getJsonPropertyNameSingle(Class<?> clazz, String fieldName) {
        JavaType javaType = mapper.getTypeFactory().constructType(clazz);
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(javaType);

        for (BeanPropertyDefinition propDef : beanDesc.findProperties()) {
            if (propDef.getInternalName().equals(fieldName)) {
                return propDef.getName();
            }
        }
        return fieldName;
    }

    /**
     * List→csv変換
     * 
     * @param list
     * @return
     */
    public static String getCSVString(List<?> list) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            Object x = list.get(i);
            b.append(x);
            if (i < list.size() - 1) {
                b.append(",");
            }
        }
        return b.toString();
    }

    /**
     * csv→List変換
     * 
     * @param csvString
     * @return
     */
    public static List<String> getList(String csvString) {
        String value = csvString;
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(",")).map(String::trim).filter(k -> !k.isEmpty()).toList();
    }

}
