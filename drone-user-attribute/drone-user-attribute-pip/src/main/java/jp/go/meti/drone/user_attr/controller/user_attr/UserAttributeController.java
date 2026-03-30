package jp.go.meti.drone.user_attr.controller.user_attr;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import jp.go.meti.drone.user_attr.model.request.GetUserAttrRequest;
import jp.go.meti.drone.user_attr.model.request.UpdateOperatorAttrRequest;
import jp.go.meti.drone.user_attr.model.request.UpdateUserAttrRequest;

/**
 * ユーザ属性コントローラーインターフェース
 * <ol>
 * 以下の4機能を提供
 * <li>[POST]ユーザ属性取得</li>
 * <li>[PUT]ユーザ属性更新</li>
 * <li>[PUT]ユーザ属性更新(スーパーユーザ)</li>
 * <li>[PUT]事業者属性更新</li>
 * </ol>
 */
public interface UserAttributeController {

    /**
     * [POST]ユーザ属性取得
     * 
     * @param requestBody　リクエストボディ
     * @param headers　リクエストヘッダ
     * @return　ユーザ属性取得レスポンス
     */
    public ResponseEntity<?> getUserAttr(@RequestBody(required = false) GetUserAttrRequest requestBody,
        @RequestHeader Map<String, String> headers);

    /**
     * [PUT]ユーザ属性更新
     * 
     * @param userId　ユーザID（UUID）
     * @param updateUserAttrRequest　リクエストボディ
     * @param headers リクエストヘッダ
     * @param authorizations Authorizationヘッダ
     * @return
     */
    public ResponseEntity<?> updateUserAttr(@RequestParam(name = "user_id", required = false) String userId,
        @RequestBody UpdateUserAttrRequest updateUserAttrRequest, @RequestHeader Map<String, String> headers,
        @RequestHeader(name = "Authorization", required = false) List<String> authorizations);

    /**
     * [PUT]ユーザ属性更新(スーパーユーザ)
     * 
     * @param userId ユーザID（UUID）
     * @param updateUserAttrRequest リクエストボディ
     * @param headers リクエストヘッダ
     * @param authorizations Authorizationヘッダ
     * @return
     */
    public ResponseEntity<?> updateUserAttrAdmin(@RequestParam(name = "user_id", required = false) String userId,
        @RequestBody UpdateUserAttrRequest updateUserAttrRequest, @RequestHeader Map<String, String> headers,
        @RequestHeader(name = "Authorization", required = false) List<String> authorizations);

    
    /**
     * [PUT]事業者属性更新
     * 
     * @param userId 事業者ID(UUID)
     * @param updateOperatorAttrRequest リクエストボディ
     * @param headers リクエストヘッダ
     * @param authorizations Authorizationヘッダ
     * @return
     */
    public ResponseEntity<?> updateOperatorAttr(@RequestParam(name = "user_id", required = false) String userId,
        @RequestBody UpdateOperatorAttrRequest updateOperatorAttrRequest, @RequestHeader Map<String, String> headers,
        @RequestHeader(name = "Authorization", required = false) List<String> authorizations);
}
