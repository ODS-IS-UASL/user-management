package jp.go.meti.drone.user_attr.service.com;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import jp.go.meti.drone.user_attr.model.commonmodel.CommonResponseBadRequestError;
import jp.go.meti.drone.user_attr.model.commonmodel.CommonResponseForbiddenError;
import jp.go.meti.drone.user_attr.model.request.UpdateOperatorAttrRequest;
import jp.go.meti.drone.user_attr.model.request.UpdateUserAttrRequest;
import jp.go.meti.drone.user_attr.repository.UserAttributeRepository;
import jp.go.meti.drone.user_attr.repository.entity.UserOperatorAttributeEntity;
import jp.go.meti.drone.user_attr.util.JsonPropertyUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * ユーザ属性更新用Util：チェッカー サービスクラス
 */
@Slf4j
@Service
public class UpdateUserAttrCheckerImpl implements UpdateUserAttrChecker {

	/**
	 * ユーザ属性リポジトリ
	 */
    private final UserAttributeRepository userAttributeRepository;

    /**
     * コンストラクタ
     * 
     * @param userAttributeRepository　ユーザ属性リポジトリ
     */
    public UpdateUserAttrCheckerImpl(UserAttributeRepository userAttributeRepository) {
        this.userAttributeRepository = userAttributeRepository;
    }

    /**
     * ユーザ更新時の各項目の桁数チェック、更新項目チェック、所属事業者IDチェックを行い、対応するエラーメッセージを返却
     * 
     * @param updateUserAttrRequest [PUT]ユーザ属性更新API(スーパーユーザ)/[PUT]ユーザ属性更新APIのリクエスト
     * @param operateUid 操作ユーザのユーザID（Athorization:OperateUid）
     */
    @Override
    public void validateParamUser(UpdateUserAttrRequest updateUserAttrRequest, String operateUid) {

        if (updateUserAttrRequest.getLoginUserId() == null && updateUserAttrRequest.getOperatorName() == null
            && updateUserAttrRequest.getAttribute() == null) {
            throw new CommonResponseBadRequestError(HttpStatus.BAD_REQUEST.value(), "更新する項目を設定してください。");
        }

        List<String> errList = new ArrayList<>();
        errList.addAll(
            paramCheckCommon(updateUserAttrRequest.getLoginUserId(), updateUserAttrRequest.getOperatorName()));
        if (operateUid != null && validateLength(operateUid.length(), 40)) {
            errList.add("header:operateUid(40)");
        }
        if (updateUserAttrRequest.getAttribute() != null) {
            errList.addAll(paramCheckUser(updateUserAttrRequest.getAttribute()));
            if (operateUid != null && updateUserAttrRequest.getAttribute().getOperatorId() != null) {
                throw new CommonResponseForbiddenError(
                    HttpStatus.FORBIDDEN.value(), "スーパーユーザ以外はoperator_id(所属事業者ID)を変更できません。");
            }
        }
        if (!errList.isEmpty()) {
            throw new CommonResponseBadRequestError(
                HttpStatus.BAD_REQUEST.value(), "リクエストの各項目は()の文字数以内にしてください。" + String.join(",", errList));
        }
    }

    /**
     * 事業者更新時の各項目の桁数チェック、更新項目チェックを行い、対応するエラーメッセージを返却
     * 
     * @param updateOperatorAttrRequest [PUT]事業者属性更新APIのリクエスト
     */
    @Override
    public void validateParamOperator(UpdateOperatorAttrRequest updateOperatorAttrRequest) {

        if (updateOperatorAttrRequest.getLoginUserId() == null && updateOperatorAttrRequest.getOperatorName() == null
            && updateOperatorAttrRequest.getAttribute() == null) {
            throw new CommonResponseBadRequestError(HttpStatus.BAD_REQUEST.value(), "更新する項目を設定してください。");
        }

        List<String> errList = new ArrayList<>();
        errList.addAll(
            paramCheckCommon(updateOperatorAttrRequest.getLoginUserId(), updateOperatorAttrRequest.getOperatorName()));
        if (updateOperatorAttrRequest.getAttribute() != null) {
            errList.addAll(paramCheckOperator(updateOperatorAttrRequest.getAttribute()));
        }

        if (!errList.isEmpty()) {
            throw new CommonResponseBadRequestError(
                HttpStatus.BAD_REQUEST.value(), "リクエストの各項目は()の文字数以内にしてください。" + String.join(",", errList));
        }
    }

    /**
     * 対象ユーザに対して既存から更新される内容があるかチェックを行い、対応するエラーメッセージを返却
     * 
     * @param userId　更新対象ユーザのuserId
     * @param updateUserAttrRequest　[PUT]ユーザ属性更新API(スーパーユーザ)/[PUT]ユーザ属性更新APIのリクエスト
     */
    @Override
    public void validateUserUpdatable(String userId, UpdateUserAttrRequest updateUserAttrRequest) {

        List<UserOperatorAttributeEntity> userOperatorAttributeEntityList = userAttributeRepository
            .selectByOperatorIdUnion(List.of(userId), false);

        if (hasChanges(
            updateUserAttrRequest.getLoginUserId(),
            userOperatorAttributeEntityList.getFirst().getMailAddress())) {
            return;
        }
        if (hasChanges(
            updateUserAttrRequest.getOperatorName(),
            userOperatorAttributeEntityList.getFirst().getOperatorName())) {
            return;
        }
        if (updateUserAttrRequest.getAttribute() != null) {
            if (hasChanges(
                updateUserAttrRequest.getAttribute().getOperatorId(),
                userOperatorAttributeEntityList.getFirst().getAssociatedOperatorId())) {
                return;
            }
            if (updateUserAttrRequest.getAttribute().getRoleId() != null && hasChanges(
                JsonPropertyUtil.getCSVString(updateUserAttrRequest.getAttribute().getRoleId()),
                userOperatorAttributeEntityList.getFirst().getRole())) {
                return;
            }
        }
        throw new CommonResponseBadRequestError(HttpStatus.BAD_REQUEST.value(), "既存情報から更新する内容がありません。リクエスト条件を見直してください。");

    }

    /**
     * 対象事業者に対して既存から更新される内容があるかチェックを行い、対応するエラーメッセージを返却
     * 
     * @param userId　更新対象事業者のoperatorId
     * @param updateOperatorAttrRequest [PUT]事業者属性更新APIのリクエスト
     */
    @Override
    public void validateOperatorUpdatable(String userId, UpdateOperatorAttrRequest updateOperatorAttrRequest) {

        List<UserOperatorAttributeEntity> userOperatorAttributeEntityList = userAttributeRepository
            .selectByOperatorIdUnion(List.of(userId), false);

        if (hasChanges(
            updateOperatorAttrRequest.getLoginUserId(),
            userOperatorAttributeEntityList.getFirst().getMailAddress())) {
            return;
        }
        if (hasChanges(
            updateOperatorAttrRequest.getOperatorName(),
            userOperatorAttributeEntityList.getFirst().getOperatorName())) {
            return;
        }
        if (updateOperatorAttrRequest.getAttribute() != null) {
            if (updateOperatorAttrRequest.getAttribute().getRoleId() != null && hasChanges(
                JsonPropertyUtil.getCSVString(updateOperatorAttrRequest.getAttribute().getRoleId()),
                userOperatorAttributeEntityList.getFirst().getRole())) {
                return;
            }
            if (hasChanges(
                updateOperatorAttrRequest.getAttribute().getDipsAccountId(),
                userOperatorAttributeEntityList.getFirst().getDipsAccountId())) {
                return;
            }
            if (hasChanges(
                updateOperatorAttrRequest.getAttribute().getDipsAccountName(),
                userOperatorAttributeEntityList.getFirst().getDipsAccountName())) {
                return;
            }
            if (hasChanges(
                updateOperatorAttrRequest.getAttribute().getPhone(),
                userOperatorAttributeEntityList.getFirst().getPhoneNumber())) {
                return;
            }
            if (hasChanges(
                updateOperatorAttrRequest.getAttribute().getSwimOperatorId(),
                userOperatorAttributeEntityList.getFirst().getSwimOperatorId())) {
                return;
            }
        }
        throw new CommonResponseBadRequestError(HttpStatus.BAD_REQUEST.value(), "既存情報から更新する内容がありません。リクエスト条件を見直してください。");

    }

    /**
     * リクエストパラメータとDBの値が合致しているか確認
     * 
     * @param reqParam
     * @param dbParam
     * @return
     */
    private boolean hasChanges(String reqParam, String dbParam) {

        if (reqParam == null || reqParam.isBlank() || dbParam.equals(reqParam)) {
            return false;
        }
        return true;
    }

    /**
     * ログインユーザIDおよび事業者名の桁数チェック
     * 
     * @param loginUserId
     * @param operatorName
     * @return
     */
    private List<String> paramCheckCommon(String loginUserId, String operatorName) {
        List<String> errList = new ArrayList<>();
        if (loginUserId != null && validateLength(loginUserId.length(), 255)) {
            errList.add("login_user_id(255)");
        }
        if (operatorName != null && validateLength(operatorName.length(), 255)) {
            errList.add("operator_name(255)");
        }
        return errList;
    }

    /**
     * 事業者IDの桁数チェック
     * 
     * @param updateUserAttribute
     * @return
     */
    private List<String> paramCheckUser(UpdateUserAttrRequest.Attribute updateUserAttribute) {
        List<String> errList = new ArrayList<>();
        if (updateUserAttribute.getOperatorId() != null && validateLength(
            updateUserAttribute.getOperatorId().length(),
            40)) {
            errList.add("body:operatorId(40)");
        }
        return errList;
    }

    /**
     * DIPSアカウントID、DIPSアカウント名、電話番号、SWIM連携用事業者IDの桁数チェック
     * 
     * @param updateOperatorAttribute
     * @return
     */
    private List<String> paramCheckOperator(UpdateOperatorAttrRequest.Attribute updateOperatorAttribute) {
        List<String> errList = new ArrayList<>();
        if (updateOperatorAttribute.getDipsAccountId() != null && validateLength(
            updateOperatorAttribute.getDipsAccountId().length(),
            40)) {
            errList.add("dipsAccountId(40)");
        }
        if (updateOperatorAttribute.getDipsAccountName() != null && validateLength(
            updateOperatorAttribute.getDipsAccountName().length(),
            40)) {
            errList.add("dipsAccountName(40)");
        }
        if (updateOperatorAttribute.getPhone() != null && validateLength(
            updateOperatorAttribute.getPhone().length(),
            20)) {
            errList.add("phone(20)");
        }
        if (updateOperatorAttribute.getSwimOperatorId() != null && validateLength(
            updateOperatorAttribute.getSwimOperatorId().length(),
            3)) {
            errList.add("swimOperatorId(3)");
        }
        return errList;
    }

    /**
     * 桁数チェック共通メソッド
     * 
     * @param paramLength
     * @param maxLength
     * @return
     */
    private boolean validateLength(int paramLength, int maxLength) {
        if (paramLength > maxLength) {
            return true;
        } else {
            return false;
        }
    }

}
