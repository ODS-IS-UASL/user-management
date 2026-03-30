package jp.go.meti.drone.user_attr.controller.user_info;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
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
import jp.go.meti.drone.user_attr.model.request.OperatorInfoRequest;
import jp.go.meti.drone.user_attr.model.request.UserInfoRequest;
import jp.go.meti.drone.user_attr.model.response.OperatorInfoResponse;
import jp.go.meti.drone.user_attr.model.response.UserInfoResponse;
import jp.go.meti.drone.user_attr.service.user_info.AttributeAdminService;
import jp.go.meti.drone.user_attr.util.AuthorizationKeyUtil;
import jp.go.meti.drone.user_attr.util.JsonPropertyUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * ユーザ情報コントローラー
 */
@Slf4j
@RestController
@RequestMapping("${drone-route-base-path}")
public class UserInfoControllerImpl implements UserInfoController {

	/**
	 * ユーザ情報サービス
	 */
    private final AttributeAdminService attributeAdminService;

    /**
     * L3-identity-component/OpenFGA:Authorizationキー項目名
     */
    @Value("${user.attribute.auth_key_name}")
    private String authKeyName;

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
     * コンストラクタ
     * 
     * @param attributeAdminService　ユーザ情報サービス
     */
    public UserInfoControllerImpl(AttributeAdminService attributeAdminService) {
        this.attributeAdminService = attributeAdminService;
    }

    /**
     * [POST]ユーザ登録
     * 
     * @param userInfoRequest リクエストボディ
     * @param headers リクエストヘッダ
     * @param authorizations Authorizationヘッダ
     * @return ユーザ登録レスポンス
     */
    @Override
    @PostMapping("user_info")
    public ResponseEntity<?> createUserInfo(@Valid @RequestBody UserInfoRequest userInfoRequest,
        @RequestHeader Map<String, String> headers,
        @RequestHeader(name = "Authorization", required = false) List<String> authorizations) {
        log.debug("UserInfoControllerImpl::createUserInfo called widh UserInfoRequest: " + userInfoRequest.toString());

        String uuid = AuthorizationKeyUtil.getAuthorizationKey(authorizations, authKeyName);
        log.debug("============ createUserInfo\n");
        log.debug(authKeyName + " :" + uuid);
        log.debug("opearator_id :" + userInfoRequest.getAttribute().getOperatorId());

        try {
            UserInfoResponse userInfoResponse = null;
            if (uuid != null) {
                // 自事業者へのユーザ登録
                if (userInfoRequest.getAttribute().getOperatorId() != null && !userInfoRequest.getAttribute()
                    .getOperatorId()
                    .isBlank()) {
                    UserInfoRequest.Attribute tmpAttribute = new UserInfoRequest.Attribute();
                    tmpAttribute = userInfoRequest.getAttribute();
                    tmpAttribute.setOperatorId(null);
                    userInfoRequest.setAttribute(tmpAttribute);
                }
                log.debug("regist user own operator. calling : Service.registUser\n");
                userInfoResponse = attributeAdminService.registUser(userInfoRequest, uuid, headers);
                return new ResponseEntity<>(userInfoResponse, HttpStatus.CREATED);
            } else {
                // 設定必須のため400エラー返却
                log.debug("regist user OTHER operator superuser. calling : CommonResponseBadRequestError\n");
                CommonResponseBadRequestError e = new CommonResponseBadRequestError(
                    HttpStatus.BAD_REQUEST.value(), "Authorization:operateUidの設定は必須です。");
                return e.errorResponse();
            }
        } catch (CommonResponseBadRequestError e) {
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseNotFoundError e) {
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseUnAuthorizedError e) {
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseForbiddenError e) {
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseConflictError e) {
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseInternalServerError e) {
            log.debug(e.getErrorMessage());
            return e.errorResponse();
        } catch (Exception e) {
            CommonResponseInternalServerError err = new CommonResponseInternalServerError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
            return err.errorResponse();
        }
    }

    /**
     * [POST]ユーザ登録(スーパーユーザ)
     * 
     * @param userInfoAdminRequest リクエストボディ
     * @param headers リクエストヘッダ
     * @param authorizations Authorizationヘッダ
     * @return ユーザ登録レスポンス
     */
    @Override
    @PostMapping("admin/user_info")
    public ResponseEntity<?> createUserInfoAdmin(@Valid @RequestBody UserInfoRequest userInfoRequest,
        @RequestHeader Map<String, String> headers,
        @RequestHeader(name = "Authorization", required = false) List<String> authorizations) {
        log.debug("UserInfoControllerImpl::createUserInfo called widh UserInfoRequest: " + userInfoRequest.toString());

        log.debug("============ createUserInfo\n");
        log.debug("opearator_id :" + userInfoRequest.getAttribute().getOperatorId());

        try {
            UserInfoResponse userInfoResponse = null;
            // 他事業者へのユーザ登録
            if (userInfoRequest.getAttribute().getOperatorId() != null && !userInfoRequest.getAttribute()
                .getOperatorId()
                .isBlank()) {
                log.debug("regist user superuser. calling : Service.registUserAcrossOperator\n");
                userInfoResponse = attributeAdminService.registUserAcrossOperator(userInfoRequest, headers);
                return new ResponseEntity<>(userInfoResponse, HttpStatus.CREATED);
            } else {
                log.debug("regist user OTHER operator superuser. calling : CommonResponseBadRequestError\n");
                CommonResponseBadRequestError e = new CommonResponseBadRequestError(
                    HttpStatus.BAD_REQUEST.value(), "attribute.operatorIdは必須項目です。");
                return e.errorResponse();
            }
        } catch (CommonResponseBadRequestError e) {
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseNotFoundError e) {
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseUnAuthorizedError e) {
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseForbiddenError e) {
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseConflictError e) {
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseInternalServerError e) {
            log.debug(e.getErrorMessage());
            return e.errorResponse();
        } catch (Exception e) {
            CommonResponseInternalServerError err = new CommonResponseInternalServerError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
            return err.errorResponse();
        }
    }

    /**
     * [POST]事業者登録
     * 
     * @param operatorInfoRequest リクエストボディ
     * @param headers リクエストヘッダ
     * @param authorizations Authorizationヘッダ
     * @return ユーザ登録レスポンス
     */
    @Override
    @PostMapping("operator")
    public ResponseEntity<?> createOperatorInfo(@Valid @RequestBody OperatorInfoRequest operatorInfoRequest,
        @RequestHeader Map<String, String> headers,
        @RequestHeader(name = "Authorization", required = false) List<String> authorizations) {
        log.debug(
            "UserInfoControllerImpl::createUserInfo called widh OperatorInfoRequest: " + operatorInfoRequest
                .toString());

        String uuid = AuthorizationKeyUtil.getAuthorizationKey(authorizations, authKeyName);
        log.debug("============ createOperatorInfo\n");
        log.debug(authKeyName + " :" + uuid);

        try {
            OperatorInfoResponse operatorInfoResponse = null;
            // 事業者登録
            log.debug("regist operator superuser. calling : Service.registOperator\n");
            operatorInfoResponse = attributeAdminService.registOperator(operatorInfoRequest, headers);
            return new ResponseEntity<>(operatorInfoResponse, HttpStatus.CREATED);
        } catch (CommonResponseBadRequestError e) {
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseUnAuthorizedError e) {
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseForbiddenError e) {
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseConflictError e) {
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseInternalServerError e) {
            log.debug(e.getErrorMessage());
            return e.errorResponse();
        } catch (Exception e) {
            CommonResponseInternalServerError err = new CommonResponseInternalServerError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
            return err.errorResponse();
        }
    }

    /**
     * [DELETE]ユーザ削除
     * 
     * @param userId ユーザID（UUID）
     * @param headers リクエストヘッダ
     * @param authorizations Authorizationヘッダ
     * @return ユーザ削除レスポンス
     */
    @Override
    @DeleteMapping("user_info")
    public ResponseEntity<?> deleteUser(@RequestParam(name = "user_id", required = false) String userId,
        @RequestHeader Map<String, String> headers,
        @RequestHeader(name = "Authorization", required = false) List<String> authorizations) {
        log.debug("UserInfoControllerImpl::deleteOperator called widh userId: " + userId);

        try {
            if (userId == null) {
                throw new CommonResponseBadRequestError(HttpStatus.BAD_REQUEST.value(), "クエリパラメータ(user_id)を設定してください。");
            }

            String uuid = AuthorizationKeyUtil.getAuthorizationKey(authorizations, authKeyName);
            log.debug("operatorId: " + userId);
            log.debug("uuid: " + uuid);

            if (uuid != null) {
                if (uuid.equals(userId)) { // no suicide
                    log.debug("delete own operator. excepts.\n");
                    throw new CommonResponseBadRequestError(
                        HttpStatus.BAD_REQUEST.value(), "操作ユーザ(Authorization:operateUid)と削除ユーザ(user_id)が同一のため削除できません。");
                } else {
                    // remove own operator's user.
                    log.debug("delete own operator's user. calling : Service.deleteUser\n");
                    attributeAdminService.deleteOwnOperatorsUser(userId, uuid, headers);
                }
            } else {
                log.debug("delete user OTHER operator superuser. calling : CommonResponseBadRequestError\n");
                throw new CommonResponseBadRequestError(
                    HttpStatus.BAD_REQUEST.value(), "Authorization:operateUidの設定は必須です。");
            }
            return ResponseEntity.ok().body(Collections.emptyMap());
        } catch (CommonResponseBadRequestError e) {
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseNotFoundError e) {
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseUnAuthorizedError e) {
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseForbiddenError e) {
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseInternalServerError e) {
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (Exception e) {
            CommonResponseInternalServerError err = new CommonResponseInternalServerError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
            return err.errorResponse();
        }
    }

    /**
     * [DELETE]ユーザ削除(スーパーユーザ)
     * 
     * @param userId ユーザID（UUID）
     * @param headers リクエストヘッダ
     * @param authorizations Authorizationヘッダ
     * @return ユーザ削除レスポンス
     */
    @Override
    @DeleteMapping("admin/user_info")
    public ResponseEntity<?> deleteUserAdmin(@RequestParam(name = "user_id", required = false) String userId,
        @RequestHeader Map<String, String> headers,
        @RequestHeader(name = "Authorization", required = false) List<String> authorizations) {
        log.debug("UserInfoControllerImpl::deleteOperator called widh userId: " + userId);

        try {
            if (userId == null) {
                throw new CommonResponseBadRequestError(HttpStatus.BAD_REQUEST.value(), "クエリパラメータ(user_id)を設定してください。");
            }

            String uuid = AuthorizationKeyUtil.getAuthorizationKey(authorizations, authKeyName);
            log.debug("operatorId: " + userId);
            log.debug("uuid: " + uuid);

            log.debug("SUPER!! delete calling : Service.deleteUserAcrossOperator\n");
            attributeAdminService.deleteUserAcrossOperator(userId, headers);

            return ResponseEntity.ok().body(Collections.emptyMap());
        } catch (CommonResponseBadRequestError e) {
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseNotFoundError e) {
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseUnAuthorizedError e) {
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseForbiddenError e) {
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseInternalServerError e) {
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (Exception e) {
            CommonResponseInternalServerError err = new CommonResponseInternalServerError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
            return err.errorResponse();
        }
    }

    /**
     * [DELETE]事業者削除
     * 
     * @param operatorId 事業者ID(UUID)
     * @param headers リクエストヘッダ
     * @param authorizations Authorizationヘッダ
     * @return 事業者削除レスポンス
     */
    @Override
    @DeleteMapping("operator")
    public ResponseEntity<?> deleteOperator(@RequestParam(name = "operator_id", required = false) String operatorId,
        @RequestHeader Map<String, String> headers,
        @RequestHeader(name = "Authorization", required = false) List<String> authorizations) {
        log.debug("UserInfoControllerImpl::deleteOperator called widh userId: " + operatorId);
        try {
            if (operatorId == null) {
                throw new CommonResponseBadRequestError(
                    HttpStatus.BAD_REQUEST.value(), "クエリパラメータ(operator_id)を設定してください。");
            }

            String uuid = AuthorizationKeyUtil.getAuthorizationKey(authorizations, authKeyName);
            log.debug("operatorId: " + operatorId);
            log.debug("uuid: " + uuid);

            attributeAdminService.deleteOperator(operatorId, headers);

            return ResponseEntity.ok().body(Collections.emptyMap());
        } catch (CommonResponseBadRequestError e) {
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseNotFoundError e) {
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseUnAuthorizedError e) {
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseForbiddenError e) {
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseConflictError e) {
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (CommonResponseInternalServerError e) {
            log.debug(e.getMessage());
            return e.errorResponse();
        } catch (Exception e) {
            CommonResponseInternalServerError err = new CommonResponseInternalServerError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
            return err.errorResponse();
        }
    }

}
