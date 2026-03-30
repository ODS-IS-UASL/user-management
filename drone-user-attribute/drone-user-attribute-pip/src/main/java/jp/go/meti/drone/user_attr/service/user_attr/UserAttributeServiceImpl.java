package jp.go.meti.drone.user_attr.service.user_attr;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import jp.go.meti.drone.user_attr.apimodel.credential.GetOperatorInfoResponse;
import jp.go.meti.drone.user_attr.apimodel.credential.OpenFGAEvaluationRequest;
import jp.go.meti.drone.user_attr.apimodel.credential.OpenFGAEvaluationResponse;
import jp.go.meti.drone.user_attr.apimodel.credential.UpdateOperatorRequest;
import jp.go.meti.drone.user_attr.apimodel.credential.UpdateOperatorResponse;
import jp.go.meti.drone.user_attr.config.UserRoleDefinitions;
import jp.go.meti.drone.user_attr.model.commonmodel.CommonResponseBadRequestError;
import jp.go.meti.drone.user_attr.model.commonmodel.CommonResponseConflictError;
import jp.go.meti.drone.user_attr.model.commonmodel.CommonResponseForbiddenError;
import jp.go.meti.drone.user_attr.model.commonmodel.CommonResponseInternalServerError;
import jp.go.meti.drone.user_attr.model.commonmodel.CommonResponseNotFoundError;
import jp.go.meti.drone.user_attr.model.commonmodel.CommonResponseUnAuthorizedError;
import jp.go.meti.drone.user_attr.model.request.UpdateOperatorAttrRequest;
import jp.go.meti.drone.user_attr.model.request.UpdateUserAttrRequest;
import jp.go.meti.drone.user_attr.model.response.UpdateOperatorAttrResponse;
import jp.go.meti.drone.user_attr.model.response.UpdateUserAttrResponse;
import jp.go.meti.drone.user_attr.model.response.UserAttrResponse;
import jp.go.meti.drone.user_attr.model.response.UserAttrResponseList;
import jp.go.meti.drone.user_attr.repository.OperatorAttributeRepository;
import jp.go.meti.drone.user_attr.repository.UserAttributeRepository;
import jp.go.meti.drone.user_attr.repository.entity.OperatorAttributeEntity;
import jp.go.meti.drone.user_attr.repository.entity.OperatorAttributeEntityExample;
import jp.go.meti.drone.user_attr.repository.entity.UserAttributeEntity;
import jp.go.meti.drone.user_attr.repository.entity.UserAttributeEntityExample;
import jp.go.meti.drone.user_attr.repository.entity.UserOperatorAttributeEntity;
import jp.go.meti.drone.user_attr.service.com.CommonChecker;
import jp.go.meti.drone.user_attr.service.com.CredentialService;
import jp.go.meti.drone.user_attr.service.com.GetUserAttributesChecker;
import jp.go.meti.drone.user_attr.service.com.UpdateUserAttrChecker;
import jp.go.meti.drone.user_attr.util.JsonPropertyUtil;
import jp.go.meti.drone.user_attr.util.OpenFGAEvaluationUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * UserAttributeServiceImpl ユーザ属性サービス サービスクラス
 */
@Slf4j
@Service
public class UserAttributeServiceImpl implements UserAttributeService {

	/**
	 * ユーザ属性リポジトリ
	 */
    private final UserAttributeRepository userAttributeRepository;

	/**
	 * 事業者属性リポジトリ
	 */
    private final OperatorAttributeRepository operatorAttributeRepository;

	/**
	 * ユーザロール定義設定
	 */
    private final UserRoleDefinitions userRoleDefinitions;

    /**
     * ユーザ属性情報取得用Util：チェッカー
     */
    private final GetUserAttributesChecker checker;

    /**
     * サービス共通Util：チェッカー
     */
    private final CommonChecker commonChecker;

    /**
     * ユーザ属性更新用Util：チェッカー
     */
    private final UpdateUserAttrChecker updateChecker;

    /**
     * L3-identity-componentおよびOpenFGAアクセス用サービス
     */
    private final CredentialService credentialService;

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
     * デフォルトユーザID(ユーザ登録/削除(スーパーユーザ)、事業者登録削除時のCreateId、UpdateID用)
     */
    @Value("${user.attribute.default_user}")
    private String defaultUserId;

    /**
     * OpenFGA:参照操作の認可判定用アクション設定
     */
    @Value("${user.attribute.idp.fga_tuple_action_read}")
    private String actionRead;

    /**
     * OpenFGA:登録/更新/削除操作の認可判定用アクション設定
     */
    @Value("${user.attribute.idp.fga_tuple_action_write}")
    private String actionWrite;

    /**
     * コンストラクタ
     * 
     * @param userAttributeRepository　ユーザ属性リポジトリ
     * @param operatorAttributeRepository　事業者属性リポジトリ
     * @param userRoleDefinitions　ユーザロール定義設定
     * @param checker　ユーザ属性情報取得用Util：チェッカー
     * @param credentialService　L3-identity-componentおよびOpenFGAアクセス用サービス
     * @param commonChecker　サービス共通Util：チェッカー
     * @param updateChecker　ユーザ属性更新用Util：チェッカー
     */
    public UserAttributeServiceImpl(UserAttributeRepository userAttributeRepository,
        OperatorAttributeRepository operatorAttributeRepository, UserRoleDefinitions userRoleDefinitions,
        GetUserAttributesChecker checker, CredentialService credentialService, CommonChecker commonChecker,
        UpdateUserAttrChecker updateChecker) {
        this.userAttributeRepository = userAttributeRepository;
        this.operatorAttributeRepository = operatorAttributeRepository;
        this.userRoleDefinitions = userRoleDefinitions;
        this.checker = checker;
        this.credentialService = credentialService;
        this.commonChecker = commonChecker;
        this.updateChecker = updateChecker;
    }

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
        List<String> loginUserIdList, Map<String, String> headers) {

        try {

            String errMsg = checker.validateParam(startDatetime, uuidList, loginUserIdList);
            if (errMsg != null) {
                // 400エラー
                throw new CommonResponseBadRequestError(HttpStatus.BAD_REQUEST.value(), errMsg);
            }

            // 認可判定API(L3)実行
            OpenFGAEvaluationRequest openFGAEvaluationRequest;
            openFGAEvaluationRequest = OpenFGAEvaluationUtil.openFGAEvaluation(resourceId, actionRead);
            OpenFGAEvaluationResponse openFGAEvaluationResponse = credentialService.postEvaluation(
                storeId,
                openFGAEvaluationRequest,
                headers);

            log.debug("OpenFGA Evaluation OK:" + openFGAEvaluationResponse.getData().toString());

            if (!openFGAEvaluationResponse.getData().isDecision()) {
                throw new CommonResponseForbiddenError(HttpStatus.FORBIDDEN.value(), "ユーザ属性を取得する権限がありません。");
            }

            List<UserOperatorAttributeEntity> userOperatorAttributeEntityList = new ArrayList<UserOperatorAttributeEntity>();

            String condition = null;
            if (StringUtils.hasText(startDatetime)) {
                // 取得開始日時(ユーザ&事業者:UpdateDatetime)指定
                LocalDateTime dt = checker.convertToLocalDatetime(startDatetime);
                userOperatorAttributeEntityList = userAttributeRepository.selectByUpdateDatetimeUnion(dt, false);
                condition = startDatetime;
            } else if (uuidList != null && !uuidList.isEmpty()) {
                // UUID指定
                userOperatorAttributeEntityList = userAttributeRepository.selectByOperatorIdUnion(uuidList, false);
                condition = uuidList.toString();
            } else if (loginUserIdList != null && !loginUserIdList.isEmpty()) {
                // login_user_id(ユーザ&事業者:mail_address)指定
                userOperatorAttributeEntityList = userAttributeRepository.selectByMailAddressUnion(
                    loginUserIdList,
                    false);
                condition = loginUserIdList.toString();
            } else {
                // 指定なし：有効ユーザ全検索
                userOperatorAttributeEntityList = userAttributeRepository.selectAllUnion(false);
                condition = "検索条件なし";
            }

            if (userOperatorAttributeEntityList == null || userOperatorAttributeEntityList.isEmpty()) {
                // 検索結果0件：404エラー
                throw new CommonResponseNotFoundError(HttpStatus.NOT_FOUND.value(), "検索結果0件:" + condition);
            }

            List<UserAttrResponse> userAttrResponseList = new ArrayList<>();
            for (UserOperatorAttributeEntity userOperatorAttributeEntity : userOperatorAttributeEntityList) {

                List<UserAttrResponse.Role> tmpRole = this.csvToListUserRole(userOperatorAttributeEntity.getRole());
                if (tmpRole.getFirst().getRoleName() == null) {
                    tmpRole = this.csvToListOperatorRole(userOperatorAttributeEntity.getRole());
                }
                UserAttrResponse.Attribute tmpAttribute = UserAttrResponse.Attribute.builder()
                    .operatorId(userOperatorAttributeEntity.getAssociatedOperatorId())
                    .role(tmpRole)
                    .dipsAccountId(userOperatorAttributeEntity.getDipsAccountId())
                    .dipsAccountName(userOperatorAttributeEntity.getDipsAccountName())
                    .phone(userOperatorAttributeEntity.getPhoneNumber())
                    .updateDatetime(checker.convertDatetimeToStr(userOperatorAttributeEntity.getUpdateDatetime()))
                    .swimOperatorId(userOperatorAttributeEntity.getSwimOperatorId())
                    .build();

                UserAttrResponse userAttrResponse = UserAttrResponse.builder()
                    .userId(userOperatorAttributeEntity.getOperatorId())
                    .loginId(userOperatorAttributeEntity.getMailAddress())
                    .operatorName(userOperatorAttributeEntity.getOperatorName())
                    .attribute(tmpAttribute)
                    .build();

                userAttrResponseList.add(userAttrResponse);
            }
            return UserAttrResponseList.builder().attributeList(userAttrResponseList).build();
        } catch (HttpClientErrorException e) {
            log.debug(e.getMessage());
            throw new CommonResponseUnAuthorizedError(e.getStatusCode().value(), "アクセストークンの認証に失敗しました。");
        } catch (HttpServerErrorException e) {
            log.debug(e.getMessage());
            throw new CommonResponseInternalServerError(e.getStatusCode().value(), e.getMessage());
        }
    }

    /**
     * ユーザ属性更新(他事業者)
     * 
     * @param userId　更新対象のユーザのユーザID
     * @param updateUserAttrRequest [PUT]ユーザ属性更新API(スーパーユーザ) リクエストボディ
     * @param headers ヘッダー
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UpdateUserAttrResponse updateUserAttributes(String userId, UpdateUserAttrRequest updateUserAttrRequest,
        Map<String, String> headers) {

        // リクエスト内容確認
        updateChecker.validateParamUser(updateUserAttrRequest, null);

        // 更新対象ユーザ有無確認
        commonChecker.validateExistsUserByUserId(userId);

        // LoginUserId重複確認
        validateLoginUserIdNotDuplicated(userId, updateUserAttrRequest.getLoginUserId());

        // 更新内容確認
        updateChecker.validateUserUpdatable(userId, updateUserAttrRequest);

        if (updateUserAttrRequest.getAttribute() != null) {
            if (updateUserAttrRequest.getAttribute().getRoleId() != null) {
                // ロールチェック
                commonChecker.validateUserRoleId(updateUserAttrRequest.getAttribute().getRoleId());
                commonChecker.validateRoleIdNotDuplicated(updateUserAttrRequest.getAttribute().getRoleId());
            }
            if (updateUserAttrRequest.getAttribute().getOperatorId() != null && !updateUserAttrRequest.getAttribute()
                .getOperatorId()
                .isBlank()) {
                // 事業者存在チェック
                commonChecker.validateExistsOperatorByOperatorId(updateUserAttrRequest.getAttribute().getOperatorId());
            }
        }

        // 認可判定API(L3)実行
        OpenFGAEvaluationRequest openFGAEvaluationRequest;
        openFGAEvaluationRequest = OpenFGAEvaluationUtil.openFGAEvaluation(resourceId, actionWrite);
        OpenFGAEvaluationResponse openFGAEvaluationResponse = credentialService.postEvaluation(
            storeId,
            openFGAEvaluationRequest,
            headers);
        log.debug("OpenFGA Evaluation OK:" + openFGAEvaluationResponse.getData().toString());
        if (!openFGAEvaluationResponse.getData().isDecision()) {
            throw new CommonResponseForbiddenError(HttpStatus.FORBIDDEN.value(), "ユーザ属性を更新する権限がありません。");
        }

        // DB更新
        updateUser(userId, updateUserAttrRequest, defaultUserId);

        // IdP更新リクエスト
        if (updateUserAttrRequest.getLoginUserId() != null && !updateUserAttrRequest.getLoginUserId().isBlank()
            || updateUserAttrRequest.getOperatorName() != null && !updateUserAttrRequest.getOperatorName().isBlank()) {
            updateToIdp(
                userId,
                updateUserAttrRequest.getLoginUserId(),
                updateUserAttrRequest.getOperatorName(),
                null,
                headers);
        }

        // 変更後値取得
        List<String> uuidList = new ArrayList<>();
        uuidList.add(userId);
        List<UserOperatorAttributeEntity> userOperatorAttributeEntityList = userAttributeRepository
            .selectByOperatorIdUnion(uuidList, false);
        return setUserAttributes(userOperatorAttributeEntityList);
    }

    /**
     * ユーザ属性更新(自事業者所属ユーザ)
     * 
     * @param userId　更新対象のユーザのユーザID
     * @param updateUserAttrRequest [PUT]ユーザ属性更新API リクエストボディ
     * @param userUuid　操作ユーザのユーザID
     * @param headers　ヘッダー
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UpdateUserAttrResponse updateOwnOperatorsUserAttributes(String userId,
        UpdateUserAttrRequest updateUserAttrRequest, String userUuid, Map<String, String> headers) {

        // リクエスト内容確認
        updateChecker.validateParamUser(updateUserAttrRequest, userUuid);

        // 更新対象ユーザ有無確認
        commonChecker.validateExistsUserByUserId(userId);

        // LoginUserId重複確認
        validateLoginUserIdNotDuplicated(userId, updateUserAttrRequest.getLoginUserId());

        // 更新内容確認
        updateChecker.validateUserUpdatable(userId, updateUserAttrRequest);

        if (updateUserAttrRequest.getAttribute() != null && updateUserAttrRequest.getAttribute().getRoleId() != null) {
            // ロールチェック
            commonChecker.validateUserRoleId(updateUserAttrRequest.getAttribute().getRoleId());
            commonChecker.validateRoleIdNotDuplicated(updateUserAttrRequest.getAttribute().getRoleId());
        }

        // 操作ユーザ(operateUid)有無チェック
        commonChecker.validateExistsUserByOperateUid(userUuid);
        // 操作ユーザ(operateUid)と更新対象ユーザ(userId)の所属事業者合致チェック
        checkSameOperatorIdByOperateUidAndUserId(userUuid, userId);

        // 認可判定API(L3)実行
        OpenFGAEvaluationRequest openFGAEvaluationRequest;
        openFGAEvaluationRequest = OpenFGAEvaluationUtil.openFGAEvaluation(resourceId, actionWrite);
        OpenFGAEvaluationResponse openFGAEvaluationResponse = credentialService.postEvaluation(
            storeId,
            openFGAEvaluationRequest,
            headers);
        log.debug("OpenFGA Evaluation OK:" + openFGAEvaluationResponse.getData().toString());
        if (!openFGAEvaluationResponse.getData().isDecision()) {
            throw new CommonResponseForbiddenError(HttpStatus.FORBIDDEN.value(), "ユーザ属性を更新する権限がありません。");
        }

        // DB更新
        updateUser(userId, updateUserAttrRequest, userUuid);

        // IdP更新リクエスト
        if (updateUserAttrRequest.getLoginUserId() != null && !updateUserAttrRequest.getLoginUserId().isBlank()
            || updateUserAttrRequest.getOperatorName() != null && !updateUserAttrRequest.getOperatorName().isBlank()) {
            updateToIdp(
                userId,
                updateUserAttrRequest.getLoginUserId(),
                updateUserAttrRequest.getOperatorName(),
                null,
                headers);
        }

        // 変更後値取得
        List<String> uuidList = new ArrayList<>();
        uuidList.add(userId);
        List<UserOperatorAttributeEntity> userOperatorAttributeEntityList = userAttributeRepository
            .selectByOperatorIdUnion(uuidList, false);
        return setUserAttributes(userOperatorAttributeEntityList);

    }

    /**
     * 事業者属性更新
     * 
     * @param operatorId　更新対象の事業者の事業者ID
     * @param updateOperatorAttrRequest　[PUT]事業者属性更新API　リクエストボディ
     * @param headers　ヘッダー
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UpdateOperatorAttrResponse updateOperatorAttributes(String operatorId,
        UpdateOperatorAttrRequest updateOperatorAttrRequest, Map<String, String> headers) {

        // リクエスト内容確認
        updateChecker.validateParamOperator(updateOperatorAttrRequest);

        // 更新対象事業者有無確認
        commonChecker.validateExistsOperatorByOperatorId(operatorId);

        // LoginUserId重複確認
        validateLoginUserIdNotDuplicated(operatorId, updateOperatorAttrRequest.getLoginUserId());

        // 更新内容確認
        updateChecker.validateOperatorUpdatable(operatorId, updateOperatorAttrRequest);

        if (updateOperatorAttrRequest.getAttribute() != null) {
            if (updateOperatorAttrRequest.getAttribute().getRoleId() != null) {
                // ロールチェック
                commonChecker.validateOperatorRoleId(updateOperatorAttrRequest.getAttribute().getRoleId());
                commonChecker.validateRoleIdNotDuplicated(updateOperatorAttrRequest.getAttribute().getRoleId());
            }
            if (updateOperatorAttrRequest.getAttribute().getSwimOperatorId() != null) {
                // SWIM連携用事業者IDチェック
                validateExistsOperatorBySwimOperatorId(
                    operatorId,
                    updateOperatorAttrRequest.getAttribute().getSwimOperatorId());
            }
        }

        // 認可判定API(L3)実行
        OpenFGAEvaluationRequest openFGAEvaluationRequest;
        openFGAEvaluationRequest = OpenFGAEvaluationUtil.openFGAEvaluation(resourceId, actionWrite);
        OpenFGAEvaluationResponse openFGAEvaluationResponse = credentialService.postEvaluation(
            storeId,
            openFGAEvaluationRequest,
            headers);
        log.debug("OpenFGA Evaluation OK:" + openFGAEvaluationResponse.getData().toString());
        if (!openFGAEvaluationResponse.getData().isDecision()) {
            throw new CommonResponseForbiddenError(HttpStatus.FORBIDDEN.value(), "事業者属性を更新する権限がありません。");
        }

        // DB更新
        updateOperator(operatorId, updateOperatorAttrRequest);

        // IdP更新リクエスト
        String reqSwimOperatorId = null;
        if (updateOperatorAttrRequest.getAttribute() != null) {
            reqSwimOperatorId = updateOperatorAttrRequest.getAttribute().getSwimOperatorId();
        }
        if (updateOperatorAttrRequest.getLoginUserId() != null && !updateOperatorAttrRequest.getLoginUserId().isBlank()
            || updateOperatorAttrRequest.getOperatorName() != null && !updateOperatorAttrRequest.getOperatorName()
                .isBlank() || reqSwimOperatorId != null && !reqSwimOperatorId.isBlank()) {
            updateToIdp(
                operatorId,
                updateOperatorAttrRequest.getLoginUserId(),
                updateOperatorAttrRequest.getOperatorName(),
                reqSwimOperatorId,
                headers);
        }

        // 変更後値取得
        List<String> uuidList = new ArrayList<>();
        uuidList.add(operatorId);
        List<UserOperatorAttributeEntity> userOperatorAttributeEntityList = userAttributeRepository
            .selectByOperatorIdUnion(uuidList, false);
        return setOperatorAttributes(userOperatorAttributeEntityList);
    }

    /**
     * SWIM連携用事業者IDの重複確認
     * 
     * @param operatorId
     * @param swimOperatorId
     */
    private void validateExistsOperatorBySwimOperatorId(String operatorId, String swimOperatorId) {
        OperatorAttributeEntityExample operatorAttributeEntityExampleBySwim = new OperatorAttributeEntityExample();
        operatorAttributeEntityExampleBySwim.createCriteria()
            .andSwimOperatorIdEqualTo(swimOperatorId)
            .andDeletedFlagEqualTo(false);
        List<OperatorAttributeEntity> operatorAttributeEntityListBySwim = operatorAttributeRepository.selectByExample(
            operatorAttributeEntityExampleBySwim);
        OperatorAttributeEntityExample operatorAttributeEntityExampleByOperatorId = new OperatorAttributeEntityExample();
        operatorAttributeEntityExampleByOperatorId.createCriteria()
            .andOperatorIdEqualTo(operatorId)
            .andDeletedFlagEqualTo(false);
        List<OperatorAttributeEntity> operatorAttributeEntityListByOperatorId = operatorAttributeRepository
            .selectByExample(operatorAttributeEntityExampleByOperatorId);
        if (!operatorAttributeEntityListBySwim.isEmpty() && !operatorAttributeEntityListByOperatorId.getFirst()
            .getSwimOperatorId()
            .equals(swimOperatorId)) {
            throw new CommonResponseConflictError(
                HttpStatus.CONFLICT.value(), "指定されたswimOperatorId(" + swimOperatorId + ")は既に存在します。");
        }
    }
    
    /**
     * ログインユーザIDの重複確認
     * 
     * @param userId
     * @param loginUserId
     */
    private void validateLoginUserIdNotDuplicated(String userId, String loginUserId) {
        if (loginUserId != null) {
            List<UserOperatorAttributeEntity> userOperatorAttributeEntityListByMailAddress = userAttributeRepository
                .selectByMailAddressUnion(List.of(loginUserId), false);
            userOperatorAttributeEntityListByMailAddress.addAll(userAttributeRepository
                    .selectByMailAddressUnion(List.of(loginUserId), true));
            List<UserOperatorAttributeEntity> userOperatorAttributeEntityListByOperatorId = userAttributeRepository
                .selectByOperatorIdUnion(List.of(userId), false);
            userOperatorAttributeEntityListByOperatorId.addAll(userAttributeRepository
                    .selectByOperatorIdUnion(List.of(userId), true));
            if (!userOperatorAttributeEntityListByMailAddress.isEmpty() && !userOperatorAttributeEntityListByOperatorId
                .getFirst()
                .getMailAddress()
                .equals(loginUserId)) {
                throw new CommonResponseConflictError(
                    HttpStatus.CONFLICT.value(), "指定されたlogin_user_id(" + loginUserId + ")は既に存在します。");
            }

        }
    }

    /**
     * 操作ユーザの所属事業者に更新対象ユーザが存在しているか確認
     * 
     * @param operateUid
     * @param userId
     */
    private void checkSameOperatorIdByOperateUidAndUserId(String operateUid, String userId) {
        UserAttributeEntityExample userAttributeEntityExampleOperateUid = new UserAttributeEntityExample();
        userAttributeEntityExampleOperateUid.createCriteria().andUserIdEqualTo(operateUid).andDeletedFlagEqualTo(false);
        List<UserAttributeEntity> userAttributeEntityListOperateUid = userAttributeRepository.selectByExample(
            userAttributeEntityExampleOperateUid);
        UserAttributeEntityExample userAttributeEntityExampleUser = new UserAttributeEntityExample();
        userAttributeEntityExampleUser.createCriteria().andUserIdEqualTo(userId).andDeletedFlagEqualTo(false);
        List<UserAttributeEntity> userAttributeEntityListUser = userAttributeRepository.selectByExample(
            userAttributeEntityExampleUser);
        if (!userAttributeEntityListOperateUid.getFirst()
            .getOperatorId()
            .equals(userAttributeEntityListUser.getFirst().getOperatorId())) {
            throw new CommonResponseNotFoundError(HttpStatus.NOT_FOUND.value(), "指定されたuserId(" + userId + ")は存在しません。");
        }
    }

    /**
     * ユーザロールの型変換(csv→List<UserAttrResponse.Role>)
     * 
     * @param csvRole
     * @return
     */
    private List<UserAttrResponse.Role> csvToListUserRole(String csvRole) {
        List<UserAttrResponse.Role> ret = new ArrayList<>();
        List<String> list = JsonPropertyUtil.getList(csvRole);
        for (String roleId : list) {
            UserAttrResponse.Role role = new UserAttrResponse.Role();
            role.setRoleId(roleId);
            role.setRoleName(userRoleDefinitions.getUserRole().get(roleId));
            ret.add(role);
        }
        return ret;
    }

    /**
     * 事業者ロールの型変換(csv→List<UserAttrResponse.Role>)
     * 
     * @param csvRole
     * @return
     */
    private List<UserAttrResponse.Role> csvToListOperatorRole(String csvRole) {
        List<UserAttrResponse.Role> ret = new ArrayList<>();
        List<String> list = JsonPropertyUtil.getList(csvRole);
        for (String roleId : list) {
            UserAttrResponse.Role role = new UserAttrResponse.Role();
            role.setRoleId(roleId);
            role.setRoleName(userRoleDefinitions.getOperatorRole().get(roleId));
            ret.add(role);
        }
        return ret;
    }

    /**
     * [PUT]事業者属性更新 レスポンスセット
     * 
     * @param userOperatorAttributeEntityList
     * @return
     */
    private UpdateOperatorAttrResponse setOperatorAttributes(
        List<UserOperatorAttributeEntity> userOperatorAttributeEntityList) {

        List<UserAttrResponse.Role> tmpRole = this.csvToListOperatorRole(
            userOperatorAttributeEntityList.getFirst().getRole());
        UpdateOperatorAttrResponse.Attribute tmpAttribute = UpdateOperatorAttrResponse.Attribute.builder()
            .role(tmpRole)
            .dipsAccountId(userOperatorAttributeEntityList.getFirst().getDipsAccountId())
            .dipsAccountName(userOperatorAttributeEntityList.getFirst().getDipsAccountName())
            .phone(userOperatorAttributeEntityList.getFirst().getPhoneNumber())
            .updateDatetime(
                checker.convertDatetimeToStr(userOperatorAttributeEntityList.getFirst().getUpdateDatetime()))
            .swimOperatorId(userOperatorAttributeEntityList.getFirst().getSwimOperatorId())
            .build();

        return UpdateOperatorAttrResponse.builder()
            .userId(userOperatorAttributeEntityList.getFirst().getOperatorId())
            .loginId(userOperatorAttributeEntityList.getFirst().getMailAddress())
            .operatorName(userOperatorAttributeEntityList.getFirst().getOperatorName())
            .attribute(tmpAttribute)
            .build();
    }

    /**
     * [PUT]ユーザ属性更新/[PUT]ユーザ属性更新(スーパーユーザ) レスポンスセット
     * 
     * @param userOperatorAttributeEntityList
     * @return
     */
    private UpdateUserAttrResponse setUserAttributes(
        List<UserOperatorAttributeEntity> userOperatorAttributeEntityList) {
        List<UserAttrResponse.Role> tmpRole = this.csvToListUserRole(
            userOperatorAttributeEntityList.getFirst().getRole());
        UpdateUserAttrResponse.Attribute tmpAttribute = UpdateUserAttrResponse.Attribute.builder()
            .operatorId(userOperatorAttributeEntityList.getFirst().getAssociatedOperatorId())
            .role(tmpRole)
            .updateDatetime(
                checker.convertDatetimeToStr(userOperatorAttributeEntityList.getFirst().getUpdateDatetime()))
            .build();

        return UpdateUserAttrResponse.builder()
            .userId(userOperatorAttributeEntityList.getFirst().getOperatorId())
            .loginId(userOperatorAttributeEntityList.getFirst().getMailAddress())
            .operatorName(userOperatorAttributeEntityList.getFirst().getOperatorName())
            .attribute(tmpAttribute)
            .build();
    }

    /**
     * ユーザ属性テーブル更新
     * 
     * @param userId
     * @param updateUserAttrRequest
     * @param updateUserId
     */
    private void updateUser(String userId, UpdateUserAttrRequest updateUserAttrRequest, String updateUserId) {
        UserAttributeEntity userAttributeEntity = userAttributeRepository.selectByPrimaryKey(userId);
        userAttributeEntity.setMailAddress(
            updateItem(userAttributeEntity.getMailAddress(), updateUserAttrRequest.getLoginUserId()));
        userAttributeEntity.setUserName(
            updateItem(userAttributeEntity.getUserName(), updateUserAttrRequest.getOperatorName()));
        if (updateUserAttrRequest.getAttribute() != null) {
            if (updateUserAttrRequest.getAttribute().getRoleId() != null) {
                userAttributeEntity.setRole(
                    updateItem(
                        userAttributeEntity.getRole(),
                        JsonPropertyUtil.getCSVString(updateUserAttrRequest.getAttribute().getRoleId())));
            }
            userAttributeEntity.setOperatorId(
                updateItem(userAttributeEntity.getOperatorId(), updateUserAttrRequest.getAttribute().getOperatorId()));
            userAttributeEntity.setUpdateId(updateUserId);
        }
        int affectedrows = userAttributeRepository.updateByPrimaryKeySelective(userAttributeEntity);
        if (affectedrows > 1) {
            throw new CommonResponseInternalServerError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "更新対象ユーザが複数存在しています。");
        }
    }

    /**
     * 事業者属性テーブル更新
     * 
     * @param operatorId
     * @param updateOperatorAttrRequest
     */
    private void updateOperator(String operatorId, UpdateOperatorAttrRequest updateOperatorAttrRequest) {
        OperatorAttributeEntity operatorAttributeEntity = operatorAttributeRepository.selectByPrimaryKey(operatorId);
        operatorAttributeEntity.setMailAddress(
            updateItem(operatorAttributeEntity.getMailAddress(), updateOperatorAttrRequest.getLoginUserId()));
        operatorAttributeEntity.setOperatorName(
            updateItem(operatorAttributeEntity.getOperatorName(), updateOperatorAttrRequest.getOperatorName()));
        if (updateOperatorAttrRequest.getAttribute() != null) {
            if (updateOperatorAttrRequest.getAttribute().getRoleId() != null) {
                operatorAttributeEntity.setRole(
                    updateItem(
                        operatorAttributeEntity.getRole(),
                        JsonPropertyUtil.getCSVString(updateOperatorAttrRequest.getAttribute().getRoleId())));
            }
            operatorAttributeEntity.setDipsAccountId(
                updateItem(
                    operatorAttributeEntity.getDipsAccountId(),
                    updateOperatorAttrRequest.getAttribute().getDipsAccountId()));
            operatorAttributeEntity.setDipsAccountName(
                updateItem(
                    operatorAttributeEntity.getDipsAccountName(),
                    updateOperatorAttrRequest.getAttribute().getDipsAccountName()));
            operatorAttributeEntity.setPhoneNumber(
                updateItem(
                    operatorAttributeEntity.getPhoneNumber(),
                    updateOperatorAttrRequest.getAttribute().getPhone()));
            operatorAttributeEntity.setSwimOperatorId(
                updateItem(
                    operatorAttributeEntity.getSwimOperatorId(),
                    updateOperatorAttrRequest.getAttribute().getSwimOperatorId()));
        }
        operatorAttributeEntity.setUpdateId(defaultUserId);
        int affectedrows = operatorAttributeRepository.updateByPrimaryKeySelective(operatorAttributeEntity);
        if (affectedrows > 1) {
            throw new CommonResponseInternalServerError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "更新対象事業者が複数存在しています。");
        }
    }

    /**
     * L3-identity-component:[PUT]事業者情報更新API 実行
     * 
     * @param userId
     * @param loginUserId
     * @param operatorName
     * @param swimOperatorId
     * @param headers
     * @return
     */
    private UpdateOperatorResponse updateToIdp(String userId, String loginUserId, String operatorName,
        String swimOperatorId, Map<String, String> headers) {
        UpdateOperatorRequest updateOperatorRequest = new UpdateOperatorRequest();
        GetOperatorInfoResponse operatorInfoResponse = credentialService.getOperator(userId, headers);
        updateOperatorRequest.setUpdatedAt(operatorInfoResponse.getData().getUpdatedAt());
        if (loginUserId != null && !loginUserId.isBlank()) {
            updateOperatorRequest.setLoginUserId(loginUserId);
        }
        if (operatorName != null && !operatorName.isBlank()) {
            updateOperatorRequest.setOperatorName(operatorName);
        }
        if (swimOperatorId != null && !swimOperatorId.isBlank()) {
            updateOperatorRequest.setOpenOperatorId(swimOperatorId);
        }
        UpdateOperatorResponse response = credentialService.putOperator(userId, updateOperatorRequest, headers);
        log.debug(response.toString());
        return response;
    }

    /**
     * 対象項目の更新後の値セット
     * 
     * @param itemNowValue
     * @param requestValue
     * @return
     */
    private String updateItem(String itemNowValue, String requestValue) {
        String itemNewValue = itemNowValue;
        if (requestValue != null && !requestValue.isBlank()) {
            itemNewValue = requestValue;
        }
        return itemNewValue;
    }

}
