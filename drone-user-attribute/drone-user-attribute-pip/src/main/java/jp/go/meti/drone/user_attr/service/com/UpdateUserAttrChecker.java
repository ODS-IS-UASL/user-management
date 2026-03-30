package jp.go.meti.drone.user_attr.service.com;

import jp.go.meti.drone.user_attr.model.request.UpdateOperatorAttrRequest;
import jp.go.meti.drone.user_attr.model.request.UpdateUserAttrRequest;

/**
 * ユーザ属性更新用Util：チェッカー インターフェースクラス
 */
public interface UpdateUserAttrChecker {

    /**
     * ユーザ更新時の各項目の桁数チェック、更新項目チェック、所属事業者IDチェックを行い、対応するエラーメッセージを返却
     * 
     * @param updateUserAttrRequest [PUT]ユーザ属性更新API(スーパーユーザ)/[PUT]ユーザ属性更新APIのリクエスト
     * @param operateUid 操作ユーザのユーザID（Athorization:OperateUid）
     */
    public void validateParamUser(UpdateUserAttrRequest updateUserAttrRequest, String operateUid);

    /**
     * 事業者更新時の各項目の桁数チェック、更新項目チェックを行い、対応するエラーメッセージを返却
     * 
     * @param updateOperatorAttrRequest [PUT]事業者属性更新APIのリクエスト
     */
    public void validateParamOperator(UpdateOperatorAttrRequest updateOperatorAttrRequest);

    /**
     * 対象ユーザに対して既存から更新される内容があるかチェックを行い、対応するエラーメッセージを返却
     * 
     * @param userId　更新対象ユーザのuserId
     * @param updateUserAttrRequest　[PUT]ユーザ属性更新API(スーパーユーザ)/[PUT]ユーザ属性更新APIのリクエスト
     */
    public void validateUserUpdatable(String userId, UpdateUserAttrRequest updateUserAttrRequest);

    /**
     * 対象事業者に対して既存から更新される内容があるかチェックを行い、対応するエラーメッセージを返却
     * 
     * @param userId　更新対象事業者のoperatorId
     * @param updateOperatorAttrRequest [PUT]事業者属性更新APIのリクエスト
     */
    public void validateOperatorUpdatable(String userId, UpdateOperatorAttrRequest updateOperatorAttrRequest);
}
