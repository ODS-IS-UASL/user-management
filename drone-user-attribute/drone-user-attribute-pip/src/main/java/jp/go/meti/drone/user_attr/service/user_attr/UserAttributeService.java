package jp.go.meti.drone.user_attr.service.user_attr;

import java.util.List;
import java.util.Map;

import jp.go.meti.drone.user_attr.model.request.UpdateOperatorAttrRequest;
import jp.go.meti.drone.user_attr.model.request.UpdateUserAttrRequest;
import jp.go.meti.drone.user_attr.model.response.UpdateOperatorAttrResponse;
import jp.go.meti.drone.user_attr.model.response.UpdateUserAttrResponse;
import jp.go.meti.drone.user_attr.model.response.UserAttrResponseList;


/**
 * UserAttributeService ユーザ属性サービス インターフェースクラス
 */
public interface UserAttributeService {

    /**
     * ユーザ属性更新(他事業者)
     * 
     * @param userId　更新対象のユーザのユーザID
     * @param updateUserAttrRequest [PUT]ユーザ属性更新API(スーパーユーザ) リクエストボディ
     * @param headers ヘッダー
     * @return
     */
    public UpdateUserAttrResponse updateUserAttributes(String userId, UpdateUserAttrRequest updateUserAttrRequest,
        Map<String, String> headers);

    /**
     * ユーザ属性更新(自事業者所属ユーザ)
     * 
     * @param userId　更新対象のユーザのユーザID
     * @param updateUserAttrRequest [PUT]ユーザ属性更新API リクエストボディ
     * @param userUuid　操作ユーザのユーザID
     * @param headers　ヘッダー
     * @return
     */
    public UpdateUserAttrResponse updateOwnOperatorsUserAttributes(String userId, UpdateUserAttrRequest updateUserAttrRequest,
        String userUuid, Map<String, String> headers);

    /**
     * 事業者属性更新
     * 
     * @param operatorId　更新対象の事業者の事業者ID
     * @param updateOperatorAttrRequest　[PUT]事業者属性更新API　リクエストボディ
     * @param headers　ヘッダー
     * @return
     */
    public UpdateOperatorAttrResponse updateOperatorAttributes(String operatorId, UpdateOperatorAttrRequest updateOperatorAttrRequest,
        Map<String, String> headers);

    /**
     * 引数の検索条件に応じてユーザ属性情報を返却
     * 
     * @param startDatetime 取得開始日時(指定値を含む）
     * @param uuidList UUIDリスト
     * @param loginUserIdList ログインIDリスト
     * @param headers ユーザ属性取得APIヘッダー
     * @return ユーザ属性情報リストレスポンス
     */
    public UserAttrResponseList getUserAttributes(String startDatetime, List<String> uuidList,
        List<String> loginUserIdList, Map<String, String> headers);
}
