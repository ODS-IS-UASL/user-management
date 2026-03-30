package jp.go.meti.drone.user_attr.service.user_info;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.fasterxml.jackson.core.JsonProcessingException;

import jp.go.meti.drone.user_attr.apimodel.credential.GetOperatorInfoResponse;
import jp.go.meti.drone.user_attr.apimodel.credential.OpenFGAEvaluationRequest;
import jp.go.meti.drone.user_attr.apimodel.credential.OpenFGAEvaluationResponse;
import jp.go.meti.drone.user_attr.apimodel.credential.RegistOperatorRequest;
import jp.go.meti.drone.user_attr.apimodel.credential.RegistOperatorResponse;
import jp.go.meti.drone.user_attr.apimodel.credential.UpdateOperatorStatusRequest;
import jp.go.meti.drone.user_attr.apimodel.credential.UpdateOperatorStatusResponse;
import jp.go.meti.drone.user_attr.config.UserRoleDefinitions;
import jp.go.meti.drone.user_attr.model.commonmodel.CommonResponseBadRequestError;
import jp.go.meti.drone.user_attr.model.commonmodel.CommonResponseConflictError;
import jp.go.meti.drone.user_attr.model.commonmodel.CommonResponseForbiddenError;
import jp.go.meti.drone.user_attr.model.commonmodel.CommonResponseInternalServerError;
import jp.go.meti.drone.user_attr.model.commonmodel.CommonResponseNotFoundError;
import jp.go.meti.drone.user_attr.model.request.OperatorInfoRequest;
import jp.go.meti.drone.user_attr.model.request.UserInfoRequest;
import jp.go.meti.drone.user_attr.model.response.OperatorInfoResponse;
import jp.go.meti.drone.user_attr.model.response.UserInfoResponse;
import jp.go.meti.drone.user_attr.repository.OperatorAttributeRepository;
import jp.go.meti.drone.user_attr.repository.UserAttributeRepository;
import jp.go.meti.drone.user_attr.repository.entity.OperatorAttributeEntity;
import jp.go.meti.drone.user_attr.repository.entity.OperatorAttributeEntityExample;
import jp.go.meti.drone.user_attr.repository.entity.UserAttributeEntity;
import jp.go.meti.drone.user_attr.repository.entity.UserAttributeEntityExample;
import jp.go.meti.drone.user_attr.repository.entity.UserOperatorAttributeEntity;
import jp.go.meti.drone.user_attr.service.com.CommonChecker;
import jp.go.meti.drone.user_attr.service.com.CreateUserInfoChecker;
import jp.go.meti.drone.user_attr.service.com.CredentialService;
import jp.go.meti.drone.user_attr.util.JsonPropertyUtil;
import jp.go.meti.drone.user_attr.util.OpenFGAEvaluationUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * AttributeAdminServiceImpl ユーザ情報サービス サービスクラス 
 */
@Slf4j
@Service
public class AttributeAdminServiceImpl implements AttributeAdminService {

	/**
	 * L3-identity-componentおよびOpenFGAアクセス用サービス
	 */
    private final CredentialService credentialService;

    /**
     * ユーザ属性リポジトリ
     */
    private final UserAttributeRepository userAttributeRepository;

    /**
     * 事業者属性リポジトリ
     */
    private final OperatorAttributeRepository operatorAttributeRepository;

    /**
     * ユーザ情報（[POST]ユーザ登録API(スーパーユーザ)/[POST]ユーザ登録API/[POST]事業者登録API）Util：チェッカー
     */
    private final CreateUserInfoChecker checker;

    /**
     * サービス共通Util：チェッカー
     */
    private final CommonChecker commonChecker;

    /**
     * デフォルト事業者所在地（L3-identity-component:[POST]事業者情報登録APIの際に固定値として設定）
     */
    @Value("${user.attribute.default_operator_address}")
    private String defaultOperatorAddress;

    /**
     * デフォルト事業者識別子（L3-identity-component:[POST]事業者情報登録APIの際に固定値として設定）
     */
    @Value("${user.attribute.default_open_operator_id}")
    private String defaultOpenOperatorId;

    /**
     * デフォルトユーザID(ユーザ登録/削除(スーパーユーザ)、事業者登録削除時のCreateId、UpdateID用)
     */
    @Value("${user.attribute.default_user}")
    private String defaultUserId;

    /**
     * ユーザ管理用ストアID
     */
    @Value("${user.attribute.idp.fga_store_id}")
    private String storeId;

    /**
     * ユーザ管理用リソースID
     */
    @Value("${user.attribute.idp.fga_resource_id}")
    private String resourceId;

    /**
     * OpenFGA:登録/更新/削除操作の認可判定用アクション設定
     */
    @Value("${user.attribute.idp.fga_tuple_action_write}")
    private String actionWrite;

    /**
     * コンストラクタ
     * 
     * @param credentialService　L3-identity-componentおよびOpenFGAアクセス用サービス
     * @param userAttributeRepository　ユーザ属性リポジトリ
     * @param operatorAttributeRepository　事業者属性リポジトリ
     * @param userRoleDefinitions　ユーザロール定義設定 ※未使用
     * @param checker　ユーザ情報（[POST]ユーザ登録API(スーパーユーザ)/[POST]ユーザ登録API/[POST]事業者登録API）Util：チェッカー
     * @param commonChecker　サービス共通Util：チェッカー
     */
    public AttributeAdminServiceImpl(CredentialService credentialService,
        UserAttributeRepository userAttributeRepository, OperatorAttributeRepository operatorAttributeRepository,
        UserRoleDefinitions userRoleDefinitions, CreateUserInfoChecker checker, CommonChecker commonChecker) {
        this.credentialService = credentialService;
        this.userAttributeRepository = userAttributeRepository;
        this.operatorAttributeRepository = operatorAttributeRepository;
        this.checker = checker;
        this.commonChecker = commonChecker;
    }

    /**
     * 新規事業者登録
     * 
     * @param operatorInfoRequest　[POST]事業者登録API　リクエストボディ
     * @param headers リクエストヘッダ
     * @return
     * @throws CommonResponseBadRequestError
     * @throws CommonResponseNotFoundError
     * @throws HttpClientErrorException
     * @throws HttpServerErrorException
     * @throws DuplicateKeyException
     * @throws JsonProcessingException
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OperatorInfoResponse registOperator(OperatorInfoRequest operatorInfoRequest, Map<String, String> headers)
        throws HttpClientErrorException, HttpServerErrorException, DuplicateKeyException, JsonProcessingException {
        OperatorInfoResponse operatorInfoResponse = new OperatorInfoResponse();
        try {

            String errMsg = checker.validateParamOperator(operatorInfoRequest);
            if (errMsg != null) {
                // 400エラー
                throw new CommonResponseBadRequestError(HttpStatus.BAD_REQUEST.value(), errMsg);
            }

            // LoginUserId重複確認
            validateLoginUserIdNotDuplicated(operatorInfoRequest.getLoginUserId());

            // 作成対象のSWIM連携用事業者IDを持つ事業者有無チェック
            checkExistsOperatorBySwimOperatorId(operatorInfoRequest.getAttribute().getSwimOperatorId());
            commonChecker.validateOperatorRoleId(operatorInfoRequest.getAttribute().getRoleId());
            
            // 認可判定API(L3)実行
            OpenFGAEvaluationRequest openFGAEvaluationRequest;
            openFGAEvaluationRequest = OpenFGAEvaluationUtil.openFGAEvaluation(resourceId, actionWrite);
            OpenFGAEvaluationResponse openFGAEvaluationResponse = credentialService.postEvaluation(
                storeId,
                openFGAEvaluationRequest,
                headers);
            log.debug("OpenFGA Evaluation OK:" + openFGAEvaluationResponse.getData().toString());
            if (!openFGAEvaluationResponse.getData().isDecision()) {
                throw new CommonResponseForbiddenError(HttpStatus.FORBIDDEN.value(), "事業者登録をする権限がありません。");
            }

            RegistOperatorResponse registResponse = registOperatorToIdp(operatorInfoRequest, headers);
            OperatorAttributeEntity operatorAttributeEntity = new OperatorAttributeEntity();

            // from IdP
            operatorAttributeEntity.setOperatorId(registResponse.getData().getOperatorId());
            operatorAttributeEntity.setDeletedFlag(registResponse.getData().getDeletedFlag());
            // from request
            operatorAttributeEntity.setOperatorName(operatorInfoRequest.getOperatorName());
            operatorAttributeEntity.setRole(
                JsonPropertyUtil.getCSVString(operatorInfoRequest.getAttribute().getRoleId()));
            operatorAttributeEntity.setDipsAccountId(operatorInfoRequest.getAttribute().getDipsAccountId());
            operatorAttributeEntity.setDipsAccountName(operatorInfoRequest.getAttribute().getDipsAccountName());
            operatorAttributeEntity.setPhoneNumber(operatorInfoRequest.getAttribute().getPhone());
            operatorAttributeEntity.setMailAddress(operatorInfoRequest.getLoginUserId());
            operatorAttributeEntity.setSwimOperatorId(operatorInfoRequest.getAttribute().getSwimOperatorId());

            operatorAttributeEntity.setCreationId(defaultUserId);
            operatorAttributeEntity.setUpdateId(defaultUserId);

            operatorAttributeRepository.insertSelective(operatorAttributeEntity);

            // responses
            // from IdP
            operatorInfoResponse.setOperatorId(registResponse.getData().getOperatorId());
            operatorInfoResponse.setPassword(registResponse.getData().getPassword());

            return operatorInfoResponse;
        } catch (HttpClientErrorException e) {
            log.debug(e.getMessage());
            throw new CommonResponseNotFoundError(e.getStatusCode().value(), e.getMessage());
        } catch (HttpServerErrorException e) {
            log.debug(e.getMessage());
            throw e;
        } catch (DuplicateKeyException e) {
            log.debug(e.getMessage());
            throw e;
        } catch (JsonProcessingException e) {
            log.debug(e.getMessage());
            throw e;
        }
    }

    /**
     * 自事業者に所属するユーザを登録
     * 
     * @param userInfoRequest [POST]ユーザ登録API　リクエストボディ
     * @param userUuid 操作ユーザID(uuid)
     * @param headers リクエストヘッダ
     * @return
     * @throws CommonResponseBadRequestError
     * @throws CommonResponseNotFoundError
     * @throws JsonProcessingException
     * @throws DuplicateKeyException
     * @throws HttpServerErrorException
     * @throws HttpClientErrorException
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoResponse registUser(UserInfoRequest userInfoRequest, String userUuid, Map<String, String> headers)
        throws HttpClientErrorException, HttpServerErrorException, DuplicateKeyException, JsonProcessingException {
        log.debug("registUser!" + userInfoRequest);
        try {

            String errMsg = checker.validateParamUser(userInfoRequest, userUuid);
            if (errMsg != null) {
                // 400エラー
                throw new CommonResponseBadRequestError(HttpStatus.BAD_REQUEST.value(), errMsg);
            }

            // LoginUserId重複確認
            validateLoginUserIdNotDuplicated(userInfoRequest.getLoginUserId());

            commonChecker.validateUserRoleId(userInfoRequest.getAttribute().getRoleId());
            // operateUidユーザ有無チェック
            commonChecker.validateExistsUserByOperateUid(userUuid);
            
            // 認可判定API(L3)実行
            OpenFGAEvaluationRequest openFGAEvaluationRequest;
            openFGAEvaluationRequest = OpenFGAEvaluationUtil.openFGAEvaluation(resourceId, actionWrite);
            OpenFGAEvaluationResponse openFGAEvaluationResponse = credentialService.postEvaluation(
                storeId,
                openFGAEvaluationRequest,
                headers);
            log.debug("OpenFGA Evaluation OK:" + openFGAEvaluationResponse.getData().toString());
            if (!openFGAEvaluationResponse.getData().isDecision()) {
                throw new CommonResponseForbiddenError(HttpStatus.FORBIDDEN.value(), "ユーザ登録をする権限がありません。");
            }

            String currentOperatorId = getCurrentOperatorId(userUuid);
            RegistOperatorResponse registResponse = registUserToIdp(userInfoRequest, headers);

            UserAttributeEntity userAttributeEntity = new UserAttributeEntity();
            // from IdP
            userAttributeEntity.setUserId(registResponse.getData().getOperatorId());
            userAttributeEntity.setDeletedFlag(registResponse.getData().getDeletedFlag());

            // from DB
            userAttributeEntity.setOperatorId(currentOperatorId);
            // from request
            userAttributeEntity.setUserName(userInfoRequest.getOperatorName());
            userAttributeEntity.setRole(JsonPropertyUtil.getCSVString(userInfoRequest.getAttribute().getRoleId()));
            userAttributeEntity.setMailAddress(userInfoRequest.getLoginUserId());
            // from header
            userAttributeEntity.setCreationId(userUuid);
            userAttributeEntity.setUpdateId(userUuid);

            userAttributeRepository.insertSelective(userAttributeEntity);

            // responses
            UserInfoResponse userInfoResponse = new UserInfoResponse();
            userInfoResponse.setOperatorId(currentOperatorId);
            userInfoResponse.setUserId(registResponse.getData().getOperatorId());
            userInfoResponse.setPassword(registResponse.getData().getPassword());

            return userInfoResponse;

        } catch (HttpClientErrorException e) {
            log.debug(e.getMessage());
            throw new CommonResponseNotFoundError(e.getStatusCode().value(), e.getMessage());
        } catch (HttpServerErrorException e) {
            log.debug(e.getMessage());
            throw e;
        } catch (DuplicateKeyException e) {
            log.debug(e.getMessage());
            throw e;
        } catch (JsonProcessingException e) {
            log.debug(e.getMessage());
            throw e;
        }
    }

    /**
     * 任意の事業者に所属するユーザを登録
     * 
     * @param userInfoRequest [POST]ユーザ登録(スーパーユーザ)API　リクエストボディ
     * @param headers リクエストヘッダ
     * @return
     * @throws CommonResponseBadRequestError
     * @throws CommonResponseNotFoundError
     * @throws HttpClientErrorException
     * @throws HttpServerErrorException
     * @throws DuplicateKeyException
     * @throws JsonProcessingException
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoResponse registUserAcrossOperator(UserInfoRequest userInfoRequest, Map<String, String> headers)
        throws HttpClientErrorException, HttpServerErrorException, DuplicateKeyException, JsonProcessingException {

        try {

            String errMsg = checker.validateParamUser(userInfoRequest, null);
            if (errMsg != null) {
                // 400エラー
                throw new CommonResponseBadRequestError(HttpStatus.BAD_REQUEST.value(), errMsg);
            }

            // LoginUserId重複確認
            validateLoginUserIdNotDuplicated(userInfoRequest.getLoginUserId());

            commonChecker.validateUserRoleId(userInfoRequest.getAttribute().getRoleId());
            // 登録対象事業者有無チェック
            
            commonChecker.validateExistsOperatorByOperatorId(userInfoRequest.getAttribute().getOperatorId());
            
            // 認可判定API(L3)実行
            OpenFGAEvaluationRequest openFGAEvaluationRequest;
            openFGAEvaluationRequest = OpenFGAEvaluationUtil.openFGAEvaluation(resourceId, actionWrite);
            OpenFGAEvaluationResponse openFGAEvaluationResponse = credentialService.postEvaluation(
                storeId,
                openFGAEvaluationRequest,
                headers);
            log.debug("OpenFGA Evaluation OK:" + openFGAEvaluationResponse.getData().toString());
            if (!openFGAEvaluationResponse.getData().isDecision()) {
                throw new CommonResponseForbiddenError(HttpStatus.FORBIDDEN.value(), "ユーザ登録をする権限がありません。");
            }

            RegistOperatorResponse registResponse = registUserToIdp(userInfoRequest, headers);

            UserAttributeEntity userAttributeEntity = new UserAttributeEntity();

            // from IdP
            userAttributeEntity.setUserId(registResponse.getData().getOperatorId());
            userAttributeEntity.setDeletedFlag(registResponse.getData().getDeletedFlag());

            // from request
            userAttributeEntity.setOperatorId(userInfoRequest.getAttribute().getOperatorId());
            userAttributeEntity.setUserName(userInfoRequest.getOperatorName());
            userAttributeEntity.setRole(JsonPropertyUtil.getCSVString(userInfoRequest.getAttribute().getRoleId()));
            userAttributeEntity.setMailAddress(userInfoRequest.getLoginUserId());

            userAttributeEntity.setCreationId(defaultUserId);
            userAttributeEntity.setUpdateId(defaultUserId);

            userAttributeRepository.insertSelective(userAttributeEntity);

            // responses
            UserInfoResponse userInfoResponse = new UserInfoResponse();
            userInfoResponse.setOperatorId(userInfoRequest.getAttribute().getOperatorId());
            userInfoResponse.setUserId(registResponse.getData().getOperatorId());
            userInfoResponse.setPassword(registResponse.getData().getPassword());

            return userInfoResponse;

        } catch (HttpClientErrorException e) {
            log.debug(e.getMessage());
            throw new CommonResponseNotFoundError(e.getStatusCode().value(), e.getMessage());
        } catch (HttpServerErrorException e) {
            log.debug(e.getMessage());
            throw e;
        } catch (DuplicateKeyException e) {
            log.debug(e.getMessage());
            throw e;
        } catch (JsonProcessingException e) {
            log.debug(e.getMessage());
            throw e;
        }
    }

    /**
     * 事業者削除
     * 
     * @param operatorId [DELETE]事業者削除API　削除対象事業者の事業者ID
     * @param headers リクエストヘッダ
     * @return
     * @throws HttpClientErrorException
     * @throws HttpServerErrorException
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteOperator(String operatorId, Map<String, String> headers) throws HttpClientErrorException,
        HttpServerErrorException {
        try {
            // 削除対象事業者&削除対象事業所に所属するユーザ有無チェック
            commonChecker.validateExistsOperatorByOperatorId(operatorId);
            checkExistsUserByOperatorId(operatorId);

            // 認可判定API(L3)実行
            OpenFGAEvaluationRequest openFGAEvaluationRequest;
            openFGAEvaluationRequest = OpenFGAEvaluationUtil.openFGAEvaluation(resourceId, actionWrite);
            OpenFGAEvaluationResponse openFGAEvaluationResponse = credentialService.postEvaluation(
                storeId,
                openFGAEvaluationRequest,
                headers);
            log.debug("OpenFGA Evaluation OK:" + openFGAEvaluationResponse.getData().toString());
            if (!openFGAEvaluationResponse.getData().isDecision()) {
                throw new CommonResponseForbiddenError(HttpStatus.FORBIDDEN.value(), "事業者削除をする権限がありません。");
            }

            // update DB.
            int affectedrows = disableOperator(operatorId, defaultUserId);

            // IdP に削除リクエスト
            deleteFromIdp(operatorId, headers);

            return affectedrows;
        } catch (HttpClientErrorException e) {
            log.debug(e.getMessage());
            throw e;
        } catch (HttpServerErrorException e) {
            log.debug(e.getMessage());
            throw e;
        }
    }

    /**
     * 自事業者に所属するユーザを削除
     * 
     * @param userId [DELETE]ユーザ削除API　削除対象ユーザのユーザID
     * @param userUuid 操作ユーザID(uuid)
     * @param headers リクエストヘッダ
     * @return
     * @throws HttpClientErrorException
     * @throws HttpServerErrorException
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteOwnOperatorsUser(String userId, String userUuid, Map<String, String> headers)
        throws CommonResponseNotFoundError, CommonResponseInternalServerError {
        try {
            // operateUidユーザ&削除対象ユーザ有無チェック
            commonChecker.validateExistsUserByOperateUid(userUuid);
            commonChecker.validateExistsUserByUserId(userId);

            // 認可判定API(L3)実行
            OpenFGAEvaluationRequest openFGAEvaluationRequest;
            openFGAEvaluationRequest = OpenFGAEvaluationUtil.openFGAEvaluation(resourceId, actionWrite);
            OpenFGAEvaluationResponse openFGAEvaluationResponse = credentialService.postEvaluation(
                storeId,
                openFGAEvaluationRequest,
                headers);
            log.debug("OpenFGA Evaluation OK:" + openFGAEvaluationResponse.getData().toString());
            if (!openFGAEvaluationResponse.getData().isDecision()) {
                throw new CommonResponseForbiddenError(HttpStatus.FORBIDDEN.value(), "ユーザ削除をする権限がありません。");
            }

            UserAttributeEntity currentUserEntity = userAttributeRepository.selectByPrimaryKey(userUuid);
            OperatorAttributeEntity currentOperatorEntity = operatorAttributeRepository.selectByPrimaryKey(
                currentUserEntity.getOperatorId());
            if (currentOperatorEntity == null) {
                throw new CommonResponseNotFoundError(
                    HttpStatus.NOT_FOUND.value(), "指定された事業者ID(" + currentUserEntity.getOperatorId() + ")は存在しません。");
            }
            String currrentOperatorId = currentOperatorEntity.getOperatorId();

            // 削除対象ユーザ
            UserAttributeEntity userAttributeEntity = userAttributeRepository.selectByPrimaryKey(userId);

            // 他事業者のユーザは削除できない
            if (!currrentOperatorId.equals(userAttributeEntity.getOperatorId())) {
                throw new CommonResponseNotFoundError(
                    HttpStatus.NOT_FOUND.value(), "指定されたユーザID(" + userId + ")は削除できません。");
            }

            int affectedrows = disableUser(userId, userUuid);
            if (affectedrows == 0) {
                throw new CommonResponseNotFoundError(HttpStatus.NOT_FOUND.value(), "対象レコードなし:" + userId);
            }

            // IdP に削除リクエスト
            deleteFromIdp(userId, headers);

            return affectedrows;

        } catch (HttpClientErrorException e) {
            log.debug(e.getMessage());
            throw e;
        } catch (HttpServerErrorException e) {
            log.debug(e.getMessage());
            throw e;
        }
    }

    /**
     * 任意の事業者に所属するユーザを削除
     * 
     * @param userId [DELETE]ユーザ削除API(スーパーユーザ)　削除対象ユーザのユーザID
     * @param headers リクエストヘッダ
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteUserAcrossOperator(String userId, Map<String, String> headers) {
        try {

            // 削除対象ユーザ有無チェック
            commonChecker.validateExistsUserByUserId(userId);

            // 認可判定API(L3)実行
            OpenFGAEvaluationRequest openFGAEvaluationRequest;
            openFGAEvaluationRequest = OpenFGAEvaluationUtil.openFGAEvaluation(resourceId, actionWrite);
            OpenFGAEvaluationResponse openFGAEvaluationResponse = credentialService.postEvaluation(
                storeId,
                openFGAEvaluationRequest,
                headers);
            log.debug("OpenFGA Evaluation OK:" + openFGAEvaluationResponse.getData().toString());
            if (!openFGAEvaluationResponse.getData().isDecision()) {
                throw new CommonResponseForbiddenError(HttpStatus.FORBIDDEN.value(), "ユーザ削除をする権限がありません。");
            }

            int affectedrows = disableUser(userId, defaultUserId);
            if (affectedrows == 0) {
                throw new CommonResponseNotFoundError(HttpStatus.NOT_FOUND.value(), "対象レコードなし:" + userId);
            }

            // IdP に削除リクエスト
            deleteFromIdp(userId, headers);

            return affectedrows;
        } catch (HttpClientErrorException e) {
            log.debug(e.getMessage());
            throw e;
        } catch (HttpServerErrorException e) {
            log.debug(e.getMessage());
            throw e;
        }
    }

    /**
     * 削除対象の事業者に所属するユーザが存在するか確認
     * 
     * @param operatorId
     */
    private void checkExistsUserByOperatorId(String operatorId) {
        UserAttributeEntityExample userAttributeEntityExample = new UserAttributeEntityExample();
        userAttributeEntityExample.createCriteria().andOperatorIdEqualTo(operatorId).andDeletedFlagEqualTo(false);
        long count = userAttributeRepository.countByExample(userAttributeEntityExample);
        if (count > 0) {
            throw new CommonResponseConflictError(
                HttpStatus.CONFLICT.value(), "削除対象の事業者(" + operatorId + ")に所属するユーザが存在するため削除できません。");
        }
    }

    /**
     * 指定されたログインユーザIDの重複チェック
     * 
     * @param loginUserId
     */
    private void validateLoginUserIdNotDuplicated(String loginUserId) {
        List<UserOperatorAttributeEntity> userOperatorAttributeEntityListByMailAddress = userAttributeRepository
            .selectByMailAddressUnion(List.of(loginUserId), false);
        userOperatorAttributeEntityListByMailAddress.addAll(userAttributeRepository
                .selectByMailAddressUnion(List.of(loginUserId), true));
        if (!userOperatorAttributeEntityListByMailAddress.isEmpty()) {
            throw new CommonResponseConflictError(
                HttpStatus.CONFLICT.value(), "指定されたlogin_user_id(" + loginUserId + ")は既に存在します。");
        }
    }

    /**
     * 指定されたSWIM連携用事業者IDの重複チェック
     * 
     * @param swimOperatorId
     */
    private void checkExistsOperatorBySwimOperatorId(String swimOperatorId) {
        OperatorAttributeEntityExample operatorAttributeEntityExample = new OperatorAttributeEntityExample();
        operatorAttributeEntityExample.createCriteria()
            .andSwimOperatorIdEqualTo(swimOperatorId)
            .andDeletedFlagEqualTo(false);
        long count = operatorAttributeRepository.countByExample(operatorAttributeEntityExample);
        if (count > 0) {
            throw new CommonResponseConflictError(
                HttpStatus.CONFLICT.value(), "指定されたswimOperatorId(" + swimOperatorId + ")は既に存在します。");
        }
    }

    /**
     * L3-identity-component:[POST]事業者情報登録API 実行(ユーザの登録)
     * 
     * @param userInfoRequest
     * @param headers
     * @return
     * @throws HttpClientErrorException
     * @throws HttpServerErrorException
     * @throws JsonProcessingException
     */
    private RegistOperatorResponse registUserToIdp(UserInfoRequest userInfoRequest, Map<String, String> headers)
        throws HttpClientErrorException, HttpServerErrorException, JsonProcessingException {
        RegistOperatorRequest registOperatorRequest = buildUserRequest(userInfoRequest);
        RegistOperatorResponse response = credentialService.postUserInfo(registOperatorRequest, headers);
        log.debug(response.toString());
        return response;
    }

    /**
     * L3-identity-component:[POST]事業者情報登録API 実行(事業者の登録)
     * 
     * @param operatorInfoRequest
     * @param headers
     * @return
     * @throws HttpClientErrorException
     * @throws HttpServerErrorException
     * @throws JsonProcessingException
     */
    private RegistOperatorResponse registOperatorToIdp(OperatorInfoRequest operatorInfoRequest,
        Map<String, String> headers) throws HttpClientErrorException, HttpServerErrorException,
        JsonProcessingException {
        RegistOperatorRequest registOperatorRequest = buildOperatorRequest(operatorInfoRequest);
        RegistOperatorResponse response = credentialService.postUserInfo(registOperatorRequest, headers);
        log.debug(response.toString());
        return response;
    }

    /**
     * L3-identity-component:[POST]事業者情報登録API用のリクエストビルド（ユーザ）
     * 
     * @param userInfoRequest
     * @return
     */
    private RegistOperatorRequest buildUserRequest(UserInfoRequest userInfoRequest) {
        RegistOperatorRequest registOperatorRequest = new RegistOperatorRequest();

        // Required
        registOperatorRequest.setLoginUserId(userInfoRequest.getLoginUserId());
        registOperatorRequest.setOperatorName(userInfoRequest.getOperatorName());
        registOperatorRequest.setOperatorAddress(defaultOperatorAddress);
        registOperatorRequest.setOpenOperatorId(defaultOpenOperatorId);
        registOperatorRequest.setCreatePasswordFlag(true);
        return registOperatorRequest;
    }

    /**
     * L3-identity-component:[POST]事業者情報登録API用のリクエストビルド（事業者）
     * 
     * @param operatorInfoRequest
     * @return
     */
    private RegistOperatorRequest buildOperatorRequest(OperatorInfoRequest operatorInfoRequest) {
        RegistOperatorRequest registOperatorRequest = new RegistOperatorRequest();

        // Required
        registOperatorRequest.setLoginUserId(operatorInfoRequest.getLoginUserId());
        registOperatorRequest.setOperatorName(operatorInfoRequest.getOperatorName());
        registOperatorRequest.setOperatorAddress(defaultOperatorAddress);
        registOperatorRequest.setOpenOperatorId(operatorInfoRequest.getAttribute().getSwimOperatorId());
        registOperatorRequest.setCreatePasswordFlag(true);
        return registOperatorRequest;
    }

    /**
     * L3-identity-component:[PUT]事業者情報ステータス更新API 実行
     * 
     * @param operatorId
     * @param headers
     * @return
     */
    private UpdateOperatorStatusResponse deleteFromIdp(String operatorId, Map<String, String> headers) {
        UpdateOperatorStatusRequest updateOperatorStatusRequest = new UpdateOperatorStatusRequest();
        GetOperatorInfoResponse operatorInfoResponse = getOperatorFromIdp(operatorId, headers);
        updateOperatorStatusRequest.setUpdatedAt(operatorInfoResponse.getData().getUpdatedAt());
        updateOperatorStatusRequest.setDeletedFlag(true);
        UpdateOperatorStatusResponse response = credentialService.putOperatorStatus(
            operatorId,
            updateOperatorStatusRequest,
            headers);
        log.debug(response.toString());
        return response;
    }

    /**
     * 操作ユーザの所属事業者ID取得
     * 
     * @param userUuid
     * @return
     * @throws CommonResponseNotFoundError
     */
    private String getCurrentOperatorId(String userUuid) throws CommonResponseNotFoundError {
        UserAttributeEntity loginUserEntity = userAttributeRepository.selectByPrimaryKey(userUuid);
        String associatedOperatorId = loginUserEntity.getOperatorId();
        // 登録対象事業者有無チェック
        commonChecker.validateExistsOperatorByOperatorId(associatedOperatorId);
        return associatedOperatorId;
    }

    /**
     * ユーザ属性テーブル更新(deleted_flag=true)
     * 
     * @param userId
     * @param updateBy
     * @return
     */
    private int disableUser(String userId, String updateBy) {
        UserAttributeEntity userAttributeEntity = userAttributeRepository.selectByPrimaryKey(userId);
        userAttributeEntity.setDeletedFlag(true);
        userAttributeEntity.setUpdateId(updateBy);
        int affectedrows = userAttributeRepository.updateByPrimaryKeySelective(userAttributeEntity);
        if (affectedrows != 1) {
            throw new CommonResponseInternalServerError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "更新対象ユーザが複数存在しています。");
        }
        return affectedrows;
    }

    /**
     * 事業者属性テーブル更新(deleted_flag=true)
     * 
     * @param operatorId
     * @param updateBy
     * @return
     */
    private int disableOperator(String operatorId, String updateBy) {
        OperatorAttributeEntity operatorAttributeEntity = operatorAttributeRepository.selectByPrimaryKey(operatorId);
        operatorAttributeEntity.setDeletedFlag(true);
        operatorAttributeEntity.setUpdateId(updateBy);
        int affectedrows = operatorAttributeRepository.updateByPrimaryKeySelective(operatorAttributeEntity);
        if (affectedrows != 1) {
            throw new CommonResponseInternalServerError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "更新対象事業者が複数存在しています。");
        }
        return affectedrows;
    }

    /**
     * L3-identity-component:[GET]事業者情報取得API 実行
     * 
     * @param operatorId
     * @param headers
     * @return
     * @throws HttpClientErrorException
     * @throws HttpServerErrorException
     */
    private GetOperatorInfoResponse getOperatorFromIdp(String operatorId, Map<String, String> headers)
        throws HttpClientErrorException, HttpServerErrorException {
        GetOperatorInfoResponse response = credentialService.getOperator(operatorId, headers);
        log.debug(response.toString());
        return response;
    }
}
