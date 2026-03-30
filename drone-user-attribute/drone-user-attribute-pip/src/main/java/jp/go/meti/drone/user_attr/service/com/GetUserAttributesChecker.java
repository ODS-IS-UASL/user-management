package jp.go.meti.drone.user_attr.service.com;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * ユーザ属性情報取得用Util：チェッカー、コンバータ　インターフェースクラス
 */
public interface GetUserAttributesChecker {

    /**
     * 必須チェック、日時フォーマットチェックを行い、対応するエラーメッセージを返却
     * 
     * @param startDatetime 開始日時(yyyyMMdd-HHmmss)
     * @param userIdList ユーザID(UUID)リスト
     * @param loginIdList ログインIDリスト
     * @return null:正常, 非null:対応するエラーメッセージ
     */
    public String validateParam(String startDatetime, List<String> userIdList, List<String> loginIdList);

    /**
     * 日時変換 to LocalDateTime
     * 
     * @param dateStr 日時(yyyyMMdd-HHmmss)
     * @return LocalDateTime　オブジェクトへ変換した結果
     * @throws DateTimeParseException フォーマット不正
     */
    public LocalDateTime convertToLocalDatetime(String dateStr) throws DateTimeParseException;

    /**
     * 日時変換 to String(yyyyMMdd-HHmmss)
     * 
     * @param datetime 日時
     * @return String(yyyyMMdd-HHmmss)
     */
    public String convertDatetimeToStr(LocalDateTime datetime);
}
