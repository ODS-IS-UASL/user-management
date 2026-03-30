package jp.go.meti.drone.user_attr.service.com;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * ユーザ属性情報取得用Util：チェッカー、コンバータ　サービスクラス
 */
@Slf4j
@Service
public class GetUserAttributesCheckerImpl implements GetUserAttributesChecker {

	/**
	 * 日時フォーマット
	 */
    private static final String SDF_PATTERN = "yyyyMMdd-HHmmss";

    /**
     * 必須チェック、日時フォーマットチェックを行い、対応するエラーメッセージを返却
     * 
     * @param startDatetime 開始日時(yyyyMMdd-HHmmss)
     * @param userIdList ユーザID(UUID)リスト
     * @param loginIdList ログインIDリスト
     * @return null:正常, 非null:対応するエラーメッセージ
     */
    @Override
    public String validateParam(String startDatetime, List<String> userIdList, List<String> loginIdList) {
        String errMsg = validateParamOneof(startDatetime, userIdList, loginIdList);
        if (errMsg != null) {
            return errMsg;
        }

        try {
            if (StringUtils.hasText(startDatetime)) {
                if (startDatetime.length() != SDF_PATTERN.length()) {
                    errMsg = "startDatetime文字数不正" + startDatetime;
                }
                convertToLocalDatetime(startDatetime);
            }
        } catch (DateTimeParseException e) {
            errMsg = "startDatetimeフォーマットエラー:" + startDatetime;
        }
        return errMsg;
    }

    /**
     * 日時変換 to LocalDateTime
     * 
     * @param dateStr 日時(yyyyMMdd-HHmmss)
     * @return LocalDateTime　オブジェクトへ変換した結果
     * @throws DateTimeParseException フォーマット不正
     */
    @Override
    public LocalDateTime convertToLocalDatetime(String startDatetime) throws DateTimeParseException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(SDF_PATTERN);
        return LocalDateTime.parse(startDatetime, formatter);
    }

    /**
     * 日時変換 to String(yyyyMMdd-HHmmss)
     * 
     * @param datetime 日時
     * @return String(yyyyMMdd-HHmmss)
     */
    @Override
    public String convertDatetimeToStr(LocalDateTime startDatetime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(SDF_PATTERN);
        return startDatetime.format(formatter);
    }

    /**
     * パラメタチェック（どれか1つのみ指定）
     * 
     * @param startDatetime
     * @param userIdList
     * @param loginIdList
     * @return null:正常, 非null:エラーメッセージ
     */
    private String validateParamOneof(String startDatetime, List<String> userIdList, List<String> loginIdList) { // NOSONAR
        String errMsg = null;
        List<String> errMsgList = new ArrayList<String>();

        // パラメタ指定なし: 全検索のため正常
        if ((!StringUtils.hasText(startDatetime)) && (userIdList == null || userIdList.isEmpty())
            && (loginIdList == null || loginIdList.isEmpty())) {
            log.debug("パラメタ指定なし：正常");
            return null;
        }
        // startDatetimeのみ：正常
        if (StringUtils.hasText(startDatetime) && (userIdList == null || userIdList.isEmpty()) && (loginIdList == null
            || loginIdList.isEmpty())) {
            log.debug("開始日時指定：正常");
            return null;
        } else if (StringUtils.hasText(startDatetime)) {
            errMsgList.add("from");
        }

        // userIdListのみ
        if (!StringUtils.hasText(startDatetime) && (userIdList != null && !userIdList.isEmpty()) && (loginIdList == null
            || loginIdList.isEmpty())) {
            log.debug("ユーザID指定：正常");
            return null;
        } else if (userIdList != null && !userIdList.isEmpty()) {
            errMsgList.add("userIdList");
        }

        // loginIdListのみ
        if (!StringUtils.hasText(startDatetime) && (userIdList == null || userIdList.isEmpty()) && (loginIdList != null
            && !loginIdList.isEmpty())) {
            log.debug("ログインID指定：正常");
            return null;
        } else if (loginIdList != null && !loginIdList.isEmpty()) {
            errMsgList.add("loginIdList");
        }

        if (errMsgList != null || !errMsgList.isEmpty()) {
            errMsg = "いずれか1つのみ指定してください:" + String.join(",", errMsgList);
        }
        return errMsg;
    }

}
