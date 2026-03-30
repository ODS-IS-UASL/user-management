package jp.go.meti.drone.user_attr.controller.user_info;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import jp.go.meti.drone.user_attr.model.request.OperatorInfoRequest;
import jp.go.meti.drone.user_attr.model.request.UserInfoRequest;

/**
 * ユーザ情報コントローラーインターフェース
 * <ol>
 * 以下の6機能を提供
 * <li>[POST]ユーザ登録</li>
 * <li>[POST]ユーザ登録(スーパーユーザ)</li>
 * <li>[POST]事業者登録</li>
 * <li>[DELETE]ユーザ削除</li>
 * <li>[DELETE]ユーザ削除(スーパーユーザ)</li>
 * <li>[DELETE]事業者削除</li>
 * </ol>
 */
public interface UserInfoController {

    /**
     * [POST]ユーザ登録
     * 
     * @param userInfoRequest リクエストボディ
     * @param headers リクエストヘッダ
     * @param authorizations Authorizationヘッダ
     * @return ユーザ登録レスポンス
     */
    public ResponseEntity<?> createUserInfo(@RequestBody UserInfoRequest userInfoRequest,
        @RequestHeader Map<String, String> headers,
        @RequestHeader(name = "Authorization", required = false) List<String> authorizations);

    /**
     * [POST]ユーザ登録(スーパーユーザ)
     * 
     * @param userInfoAdminRequest リクエストボディ
     * @param headers リクエストヘッダ
     * @param authorizations Authorizationヘッダ
     * @return ユーザ登録レスポンス
     */
    public ResponseEntity<?> createUserInfoAdmin(@RequestBody UserInfoRequest userInfoRequest,
        @RequestHeader Map<String, String> headers,
        @RequestHeader(name = "Authorization", required = false) List<String> authorizations);

    /**
     * [POST]事業者登録
     * 
     * @param operatorInfoRequest リクエストボディ
     * @param headers リクエストヘッダ
     * @param authorizations Authorizationヘッダ
     * @return ユーザ登録レスポンス
     */
    public ResponseEntity<?> createOperatorInfo(@RequestBody OperatorInfoRequest operatorInfoRequest,
        @RequestHeader Map<String, String> headers,
        @RequestHeader(name = "Authorization", required = false) List<String> authorizations);
    
    /**
     * [DELETE]ユーザ削除
     * 
     * @param userId ユーザID（UUID）
     * @param headers リクエストヘッダ
     * @param authorizations Authorizationヘッダ
     * @return ユーザ削除レスポンス
     */
    public ResponseEntity<?> deleteUser(@RequestParam(name = "user_id", required = false) String userId,
        @RequestHeader Map<String, String> headers,
        @RequestHeader(name = "Authorization", required = false) List<String> authorizations);

    /**
     * [DELETE]ユーザ削除(スーパーユーザ)
     * 
     * @param userId ユーザID（UUID）
     * @param headers リクエストヘッダ
     * @param authorizations Authorizationヘッダ
     * @return ユーザ削除レスポンス
     */
    public ResponseEntity<?> deleteUserAdmin(@RequestParam(name = "user_id", required = false) String userId,
        @RequestHeader Map<String, String> headers,
        @RequestHeader(name = "Authorization", required = false) List<String> authorizations);

    /**
     * [DELETE]事業者削除
     * 
     * @param operatorId 事業者ID(UUID)
     * @param headers リクエストヘッダ
     * @param authorizations Authorizationヘッダ
     * @return 事業者削除レスポンス
     */
    public ResponseEntity<?> deleteOperator(@RequestParam(name = "operator_id", required = false) String operatorId,
        @RequestHeader Map<String, String> headers,
        @RequestHeader(name = "Authorization", required = false) List<String> authorizations);

}
