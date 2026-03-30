package jp.go.meti.drone.user_attr.service.com;

import jp.go.meti.drone.user_attr.model.request.OperatorInfoRequest;
import jp.go.meti.drone.user_attr.model.request.UserInfoRequest;

/**
 * ユーザ情報（[POST]ユーザ登録API(スーパーユーザ)/[POST]ユーザ登録API/[POST]事業者登録API）Util：チェッカー インターフェースクラス
 */
public interface CreateUserInfoChecker {

    /**
     * 各項目の桁数チェックを行い、対応するエラーメッセージを返却(ユーザ)
     * 
     * @param userInfoRequest [POST]ユーザ登録API(スーパーユーザ)/[POST]ユーザ登録APIのリクエスト
     * @param operatorUid 操作ユーザのユーザID（Athorization:OperateUid）
     * @return
     */
    public String validateParamUser(UserInfoRequest userInfoRequest, String operateUid);
    
    /**
     * 各項目の桁数チェックを行い、対応するエラーメッセージを返却(事業者)
     * 
     * @param operatorInfoRequest [POST]事業者登録APIのリクエスト
     * @return
     */
    public String validateParamOperator(OperatorInfoRequest operatorInfoRequest);
}
