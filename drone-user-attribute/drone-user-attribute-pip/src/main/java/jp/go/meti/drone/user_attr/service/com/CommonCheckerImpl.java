package jp.go.meti.drone.user_attr.service.com;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import jp.go.meti.drone.user_attr.config.UserRoleDefinitions;
import jp.go.meti.drone.user_attr.model.commonmodel.CommonResponseBadRequestError;
import jp.go.meti.drone.user_attr.model.commonmodel.CommonResponseNotFoundError;
import jp.go.meti.drone.user_attr.repository.OperatorAttributeRepository;
import jp.go.meti.drone.user_attr.repository.UserAttributeRepository;
import jp.go.meti.drone.user_attr.repository.entity.OperatorAttributeEntityExample;
import jp.go.meti.drone.user_attr.repository.entity.UserAttributeEntityExample;
import lombok.extern.slf4j.Slf4j;

/**
 * サービス共通Util：チェッカー サービスクラス
 */
@Slf4j
@Service
public class CommonCheckerImpl implements CommonChecker {

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
     * コンストラクタ
     * 
     * @param userAttributeRepository ユーザ属性リポジトリ
     * @param operatorAttributeRepository　事業者属性リポジトリ
     * @param userRoleDefinitions　ユーザロール定義設定
     */
    public CommonCheckerImpl(UserAttributeRepository userAttributeRepository,
        OperatorAttributeRepository operatorAttributeRepository, UserRoleDefinitions userRoleDefinitions) {
        this.userAttributeRepository = userAttributeRepository;
        this.operatorAttributeRepository = operatorAttributeRepository;
        this.userRoleDefinitions = userRoleDefinitions;
    }

    /**
     * 対象のユーザIDがユーザテーブルに存在しているかをチェック
     * 
     * @param userId　ユーザID
     */
    @Override
    public void validateExistsUserByUserId(String userId) {
        UserAttributeEntityExample userAttributeEntityExample = new UserAttributeEntityExample();
        userAttributeEntityExample.createCriteria().andUserIdEqualTo(userId).andDeletedFlagEqualTo(false);
        long count = userAttributeRepository.countByExample(userAttributeEntityExample);
        if (count == 0) {
            throw new CommonResponseNotFoundError(HttpStatus.NOT_FOUND.value(), "指定されたuser_id(" + userId + ")は存在しません。");
        }
    }

    /**
     * 対象のユーザロールIDが未定義でないかをチェック
     * 
     * @param roleIdList　ロールIDリスト
     */
    @Override
    public void validateUserRoleId(List<String> roleIdList) {
        for (String roleId : roleIdList) {
            if (!userRoleDefinitions.getUserRole().containsKey(roleId)) {
                throw new CommonResponseBadRequestError(
                    HttpStatus.BAD_REQUEST.value(), "指定されたroleID(" + roleId + ")は存在しません。");
            }
        }
    }

    /**
     * 対象の事業者ロールIDが未定義でないかをチェック
     * 
     * @param roleIdList ロールIDリスト
     */
    @Override
    public void validateOperatorRoleId(List<String> roleIdList) {
        for (String roleId : roleIdList) {
            if (!userRoleDefinitions.getOperatorRole().containsKey(roleId)) {
                throw new CommonResponseBadRequestError(
                    HttpStatus.BAD_REQUEST.value(), "指定されたroleID(" + roleId + ")は存在しません。");
            }
        }
    }

    /**
     * ロールIDに重複がないかをチェック
     * 
     * @param roleIdList　ロールIDリスト
     */
    @Override
    public void validateRoleIdNotDuplicated(List<String> roleIdList) {
        Set<String> unique = new HashSet<>();
        for (String roleId : roleIdList) {
            if (!unique.add(roleId)) {
                throw new CommonResponseNotFoundError(
                    HttpStatus.BAD_REQUEST.value(), "指定されたroleID(" + roleId + ")が重複しています。");
            }
        }
    }

    /**
     * 対象の事業者IDが事業者テーブルに存在しているかをチェック
     * 
     * @param operatorId 事業者ID
     */
    @Override
    public void validateExistsOperatorByOperatorId(String operatorId) {
        OperatorAttributeEntityExample operatorAttributeEntityExample = new OperatorAttributeEntityExample();
        operatorAttributeEntityExample.createCriteria().andOperatorIdEqualTo(operatorId).andDeletedFlagEqualTo(false);
        long count = operatorAttributeRepository.countByExample(operatorAttributeEntityExample);
        if (count == 0) {
            throw new CommonResponseNotFoundError(
                HttpStatus.NOT_FOUND.value(), "指定されたoperatorId(" + operatorId + ")は存在しません。");
        }
    }

    /**
     * 対象の操作ユーザのユーザID（Athorization:OperateUid）がユーザテーブルに存在しているかをチェック
     * 
     * @param operateUid 操作ユーザのユーザID（Athorization:OperateUid）
     */
    @Override
    public void validateExistsUserByOperateUid(String operateUid) {
        UserAttributeEntityExample userAttributeEntityExample = new UserAttributeEntityExample();
        userAttributeEntityExample.createCriteria().andUserIdEqualTo(operateUid).andDeletedFlagEqualTo(false);
        long count = userAttributeRepository.countByExample(userAttributeEntityExample);
        if (count == 0) {
            throw new CommonResponseNotFoundError(
                HttpStatus.NOT_FOUND.value(), "指定されたoperateUid(" + operateUid + ")は存在しません。");
        }
    }

}
