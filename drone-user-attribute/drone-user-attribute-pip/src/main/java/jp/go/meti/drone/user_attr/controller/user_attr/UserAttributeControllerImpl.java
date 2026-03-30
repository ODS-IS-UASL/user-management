package jp.go.meti.drone.user_attr.controller.user_attr;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jp.go.meti.drone.user_attr.model.commonmodel.CommonResponseBadRequestError;
import jp.go.meti.drone.user_attr.model.commonmodel.CommonResponseConflictError;
import jp.go.meti.drone.user_attr.model.commonmodel.CommonResponseForbiddenError;
import jp.go.meti.drone.user_attr.model.commonmodel.CommonResponseInternalServerError;
import jp.go.meti.drone.user_attr.model.commonmodel.CommonResponseNotFoundError;
import jp.go.meti.drone.user_attr.model.commonmodel.CommonResponseUnAuthorizedError;
import jp.go.meti.drone.user_attr.model.request.GetUserAttrRequest;
import jp.go.meti.drone.user_attr.model.request.UpdateOperatorAttrRequest;
import jp.go.meti.drone.user_attr.model.request.UpdateUserAttrRequest;
import jp.go.meti.drone.user_attr.model.response.UpdateOperatorAttrResponse;
import jp.go.meti.drone.user_attr.model.response.UpdateUserAttrResponse;
import jp.go.meti.drone.user_attr.model.response.UserAttrResponseList;
import jp.go.meti.drone.user_attr.service.user_attr.UserAttributeService;
import jp.go.meti.drone.user_attr.util.AuthorizationKeyUtil;
import jp.go.meti.drone.user_attr.util.JsonPropertyUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * ユーザ属性コントローラー
 */
@Slf4j
@RestController
@RequestMapping("${drone-route-base-path}")
public class UserAttributeControllerImpl implements UserAttributeController {

	/**
	 * ユーザ属性サービス
	 */
    private final UserAttributeService userAttributeService;

    /**
     * L3-identity-component/OpenFGA:Authorizationキー項目名
     */
    @Value("${user.attribute.auth_key_name}")
    private String authKeyName;

    /**
     * コンストラクタ
     * 
     * @param userAttributeService　ユーザ属性サービス
     */
    public UserAttributeControllerImpl(UserAttributeService userAttributeService) {
        this.userAttributeService = userAttributeService;
    }

    /**
     * 必須項目不足エラーハンドラー
     * 
     * @param ex
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {

        Class<?> targetClass = ex.getBindingResult().getTarget().getClass();

        List<String> fields = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(err -> JsonPropertyUtil.getJsonPropertyName(targetClass, err.getField()))
            .toList();
        String joined = String.join(", ", fields);
        String message = joined + "は必須項目です。";
        CommonResponseBadRequestError e = new CommonResponseBadRequestError();
        e.setCode(HttpStatus.BAD_REQUEST.value());
        e.setErrorMessage(message);

        return e.errorResponse();
    }

    /**
     * リクエストボディ(Json)エラーハンドラー
     * 
     * @return
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleEnptyBody() {

        CommonResponseBadRequestError e = new CommonResponseBadRequestError();
        e.setCode(HttpStatus.BAD_REQUEST.value());
        e.setErrorMessage("リクエストボディ(Json)エラー");

        return e.errorResponse();
    }

    /**
     * メディアタイプ違反エラーハンドラー
     * 
     * @return
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<?> handleUnsupportedMediaType() {

        CommonResponseBadRequestError e = new CommonResponseBadRequestError();
        e.setCode(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value());
        e.setErrorMessage("適切なメディアタイプ(application/json)を指定してください。");

        return e.errorResponse();
    }

    /**
     * [POST]ユーザ属性取得
     * 
     * @param requestBody　リクエストボディ
     * @param headers　リクエストヘッダ
     * @return　ユーザ属性取得レスポンス
     */
    @PostMapping("user_attr")
    public ResponseEntity<?> getUserAttr(@RequestBody(required = false) GetUserAttrRequest requestBody,
        @RequestHeader Map<String, String> headers) {

        try {
            if (requestBody == null) {
                requestBody = new GetUserAttrRequest();
            }
            UserAttrResponseList response = userAttributeService.getUserAttributes(
                requestBody.getFrom(),
                requestBody.getUserIdList(),
                requestBody.getLoginIdList(),
                headers);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (CommonResponseBadRequestError e) {
            // リクエスト不正:400
            log.error("CommonResponseBadRequestError:" + e.getErrorMessage());
            return e.errorResponse();

        } catch (CommonResponseUnAuthorizedError e) {
            // 認証失敗:401
            log.error("CommonResponseUnAuthorizedError:" + e.getErrorMessage());
            return e.errorResponse();

        } catch (CommonResponseForbiddenError e) {
            // アクセス権限なし:403
            log.error("CommonResponseForbiddenError:" + e.getErrorMessage());
            return e.errorResponse();

        } catch (CommonResponseNotFoundError e) {
            // 検索結果０件:404
            log.error("CommonResponseNotFoundError:" + e.getErrorMessage());
            return e.errorResponse();

        } catch (CommonResponseInternalServerError e) {
            // 業務処理で予期せぬエラー:500
            log.error("CommonResponseInternalServerError:" + e.getErrorMessage());
            return e.errorResponse();
        } catch (Exception e) {
            // 予期せぬエラー
            String messageTmp = "予期せぬエラーが発生しました。";
            CommonResponseInternalServerError err = new CommonResponseInternalServerError(500, messageTmp);
            log.error(messageTmp, e);
            return err.errorResponse();
        }
    }

    /**
     * [PUT]ユーザ属性更新
     * 
     * @param userId　ユーザID（UUID）
     * @param updateUserAttrRequest　リクエストボディ
     * @param headers リクエストヘッダ
     * @param authorizations Authorizationヘッダ
     * @return
     */
    @PutMapping("user_attr")
    public ResponseEntity<?> updateUserAttr(@RequestParam(name = "user_id", required = false) String userId,
        @Valid @RequestBody UpdateUserAttrRequest updateUserAttrRequest, @RequestHeader Map<String, String> headers,
        @RequestHeader(name = "Authorization", required = false) List<String> authorizations) {

        log.debug(
            "UserAttrControllerImpl::updateUserAttr called widh UpdateUserAttrRequest: " + updateUserAttrRequest
                .toString());

        String uuid = AuthorizationKeyUtil.getAuthorizationKey(authorizations, authKeyName);
        log.debug("============ updateUserAttr\n");
        log.debug(authKeyName + " :" + uuid);
        log.debug("user_id :" + userId);

        try {
            if (userId == null) {
                throw new CommonResponseBadRequestError(HttpStatus.BAD_REQUEST.value(), "クエリパラメータ(user_id)を設定してください。");
            }
            
            if (uuid != null) {
                // 自事業者のユーザ属性更新
                UpdateUserAttrResponse response = userAttributeService.updateOwnOperatorsUserAttributes(
                    userId,
                    updateUserAttrRequest,
                    uuid,
                    headers);
                log.debug("update user own operator. calling : Service.updateOwnOperatorsUserAttributes\n");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                // 設定必須のため400エラー返却
                log.debug("update user OTHER operator superuser. calling : CommonResponseBadRequestError\n");
                CommonResponseBadRequestError e = new CommonResponseBadRequestError(
                    HttpStatus.BAD_REQUEST.value(), "Authorization:operateUidの設定は必須です。");
                return e.errorResponse();
            }
        } catch (CommonResponseBadRequestError e) {
            // リクエスト不正:400
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseNotFoundError e) {
            // 対象ユーザなし:404
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseUnAuthorizedError e) {
            // 認証失敗:401
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseForbiddenError e) {
            // アクセス権限なし:403
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseConflictError e) {
            // 更新値コンフリクト:409
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseInternalServerError e) {
            // 業務処理で予期せぬエラー:500
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (Exception e) {
            // 予期せぬエラー
            CommonResponseInternalServerError err = new CommonResponseInternalServerError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
            return err.errorResponse();
        }
    }

    /**
     * [PUT]ユーザ属性更新(スーパーユーザ)
     * 
     * @param userId ユーザID（UUID）
     * @param updateUserAttrRequest リクエストボディ
     * @param headers リクエストヘッダ
     * @param authorizations Authorizationヘッダ
     * @return
     */
    @PutMapping("admin/user_attr")
    public ResponseEntity<?> updateUserAttrAdmin(@RequestParam(name = "user_id", required = false) String userId,
        @Valid @RequestBody UpdateUserAttrRequest updateUserAttrRequest, @RequestHeader Map<String, String> headers,
        @RequestHeader(name = "Authorization", required = false) List<String> authorizations) {

        log.debug(
            "UserAttrControllerImpl::updateUserAttr called widh UpdateUserAttrRequest: " + updateUserAttrRequest
                .toString());

        String uuid = AuthorizationKeyUtil.getAuthorizationKey(authorizations, authKeyName);
        log.debug("============ updateUserAttr\n");
        log.debug(authKeyName + " :" + uuid);
        log.debug("user_id :" + userId);

        try {
            if (userId == null) {
                throw new CommonResponseBadRequestError(HttpStatus.BAD_REQUEST.value(), "クエリパラメータ(user_id)を設定してください。");
            }
        	
            // 他事業者のユーザ属性更新(superuser)
            UpdateUserAttrResponse response = userAttributeService.updateUserAttributes(
                userId,
                updateUserAttrRequest,
                headers);
            log.debug("update user other operator. calling : Service.updateUserAttributes\n");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (CommonResponseBadRequestError e) {
            // リクエスト不正:400
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseNotFoundError e) {
            // 対象ユーザなし:404
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseUnAuthorizedError e) {
            // 認証失敗:401
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseForbiddenError e) {
            // アクセス権限なし:403
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseConflictError e) {
            // 更新値コンフリクト:409
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseInternalServerError e) {
            // 業務処理で予期せぬエラー:500
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (Exception e) {
            // 予期せぬエラー
            CommonResponseInternalServerError err = new CommonResponseInternalServerError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
            return err.errorResponse();
        }
    }

    /**
     * [PUT]事業者属性更新
     * 
     * @param userId 事業者ID(UUID)
     * @param updateOperatorAttrRequest リクエストボディ
     * @param headers リクエストヘッダ
     * @param authorizations Authorizationヘッダ
     * @return
     */
    @PutMapping("operator_attr")
    public ResponseEntity<?> updateOperatorAttr(@RequestParam(name = "operator_id", required = false) String operatorId,
        @Valid @RequestBody UpdateOperatorAttrRequest updateOperatorAttrRequest,
        @RequestHeader Map<String, String> headers,
        @RequestHeader(name = "Authorization", required = false) List<String> authorizations) {

        log.debug(
            "UserAttrControllerImpl::updateUserAttr called widh UpdateOperatorAttrRequest: " + updateOperatorAttrRequest
                .toString());

        String uuid = AuthorizationKeyUtil.getAuthorizationKey(authorizations, authKeyName);
        log.debug("============ updateOperatorAttr\n");
        log.debug(authKeyName + " :" + uuid);
        log.debug("operator_id :" + operatorId);

        try {
            if (operatorId == null) {
                throw new CommonResponseBadRequestError(HttpStatus.BAD_REQUEST.value(), "クエリパラメータ(operator_id)を設定してください。");
            }
        	
            // 事業者属性更新(superuser)
            UpdateOperatorAttrResponse response = userAttributeService.updateOperatorAttributes(
                operatorId,
                updateOperatorAttrRequest,
                headers);
            log.debug("update operator. calling : Service.updateOperatorAttributes\n");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (CommonResponseBadRequestError e) {
            // リクエスト不正:400
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseNotFoundError e) {
            // 対象ユーザなし:404
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseUnAuthorizedError e) {
            // 認証失敗:401
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseForbiddenError e) {
            // アクセス権限なし:403
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseConflictError e) {
            // 更新値コンフリクト:409
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseInternalServerError e) {
            // 業務処理で予期せぬエラー:500
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (Exception e) {
            // 予期せぬエラー
            CommonResponseInternalServerError err = new CommonResponseInternalServerError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
            return err.errorResponse();
        }
    }

}
