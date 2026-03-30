package jp.go.meti.drone.user_attr.util;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import jp.go.meti.drone.user_attr.apimodel.credential.OpenFGAEvaluationRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * OpenFGA認可判定のユーティリティクラス
 */
@Slf4j
public class OpenFGAEvaluationUtil {

	/**
	 * コンストラクタ
	 */
    private OpenFGAEvaluationUtil() {
        // ユーティリティクラスのため、インスタンス化禁止
    }

    /**
     * OpenFGA:[POST]認可判定API用リクエストセット
     * 
     * @param resourceId
     * @param actionName
     * @return
     */
    public static OpenFGAEvaluationRequest openFGAEvaluation(String resourceId, String actionName) {
        OpenFGAEvaluationRequest openFGAEvaluationRequest = new OpenFGAEvaluationRequest();
        openFGAEvaluationRequest.setSubject(OpenFGAEvaluationRequest.Subject.builder().build());
        openFGAEvaluationRequest.setResource(OpenFGAEvaluationRequest.Resource.builder().id(resourceId).build());
        openFGAEvaluationRequest.setAction(OpenFGAEvaluationRequest.Action.builder().name(actionName).build());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmXXX");
        openFGAEvaluationRequest.setContext(
            OpenFGAEvaluationRequest.Context.builder().currentTime(OffsetDateTime.now().format(formatter)).build());
        return openFGAEvaluationRequest;
    }
}
