package jp.go.meti.drone.user_attr.service.com;

import java.util.List;

/**
 * サービス共通Util：チェッカー インターフェースクラス
 */
public interface CommonChecker {

    /**
     * 対象のユーザIDがユーザテーブルに存在しているかをチェック
     * 
     * @param userId
     */
    public void validateExistsUserByUserId(String userId);

    /**
     * 対象のユーザロールIDが未定義でないかをチェック
     * 
     * @param roleIdList
     */
    public void validateUserRoleId(List<String> roleIdList);

    /**
     * 対象の事業者ロールIDが未定義でないかをチェック
     * 
     * @param roleIdList
     */
    public void validateOperatorRoleId(List<String> roleIdList);

    /**
     * 対象の事業者IDが事業者テーブルに存在しているかをチェック
     * 
     * @param operatorId
     */
    public void validateExistsOperatorByOperatorId(String operatorId);

    /**
     * 対象のAthorization:OperateUidがユーザテーブルに存在しているかをチェック
     * 
     * @param operateUid
     */
    public void validateExistsUserByOperateUid(String operateUid);
    
    /**
     * ロールIDに重複がないかをチェック
     * 
     * @param roleIdList
     */
    public void validateRoleIdNotDuplicated(List<String> roleIdList);
}
