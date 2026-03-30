package jp.go.meti.drone.user_attr.service.com;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import jp.go.meti.drone.user_attr.model.request.OperatorInfoRequest;
import jp.go.meti.drone.user_attr.model.request.UserInfoRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * ユーザ情報（[POST]ユーザ登録API(スーパーユーザ)/[POST]ユーザ登録API/[POST]事業者登録API）Util：チェッカー サービスクラス
 */
@Slf4j
@Service
public class CreateUserInfoCheckerImpl implements CreateUserInfoChecker {

    /**
     * 各項目の桁数チェックを行い、対応するエラーメッセージを返却(ユーザ)
     * 
     * @param userInfoRequest [POST]ユーザ登録API(スーパーユーザ)/[POST]ユーザ登録APIのリクエスト
     * @param operatorUid 操作ユーザのユーザID（Athorization:OperateUid）
     * @return
     */
    @Override
    public String validateParamUser(UserInfoRequest userInfoRequest, String operateUid) {

        List<String> errList = new ArrayList<>();
        if (validateLength(userInfoRequest.getLoginUserId().length(), 255)) {
            errList.add("body:login_user_id(255)");
        }
        if (validateLength(userInfoRequest.getOperatorName().length(), 255)) {
            errList.add("body:operator_name(255)");
        }
        if (operateUid != null && validateLength(operateUid.length(), 40)) {
            errList.add("header:operateUid(40)");
        }
        if (userInfoRequest.getAttribute().getOperatorId() != null && validateLength(
            userInfoRequest.getAttribute().getOperatorId().length(),
            40)) {
            errList.add("body:operatorId(40)");
        }

        String errMsg = null;
        if (!errList.isEmpty()) {
            errMsg = "リクエストの各項目は()の文字数以内にしてください。" + String.join(",", errList);
        }

        return errMsg;
    }

    /**
     * 各項目の桁数チェックを行い、対応するエラーメッセージを返却(事業者)
     * 
     * @param operatorInfoRequest [POST]事業者登録APIのリクエスト
     * @return
     */
    @Override
    public String validateParamOperator(OperatorInfoRequest operatorInfoRequest) {

        List<String> errList = new ArrayList<>();
        if (validateLength(operatorInfoRequest.getLoginUserId().length(), 255)) {
            errList.add("body:login_user_id(255)");
        }
        if (validateLength(operatorInfoRequest.getOperatorName().length(), 255)) {
            errList.add("body:operator_name(255)");
        }
        if (operatorInfoRequest.getAttribute().getDipsAccountId() != null && validateLength(
            operatorInfoRequest.getAttribute().getDipsAccountId().length(),
            40)) {
            errList.add("body:dipsAccountId(40)");
        }
        if (operatorInfoRequest.getAttribute().getDipsAccountName() != null && validateLength(
            operatorInfoRequest.getAttribute().getDipsAccountName().length(),
            40)) {
            errList.add("body:dipsAccountName(40)");
        }
        if (operatorInfoRequest.getAttribute().getPhone() != null && validateLength(
            operatorInfoRequest.getAttribute().getPhone().length(),
            20)) {
            errList.add("body:phone(20)");
        }
        if (operatorInfoRequest.getAttribute().getSwimOperatorId() != null && validateLength(
            operatorInfoRequest.getAttribute().getSwimOperatorId().length(),
            3)) {
            errList.add("body:swimOperatorId(3)");
        }

        String errMsg = null;
        if (!errList.isEmpty()) {
            errMsg = "リクエストの各項目は()の文字数以内にしてください。" + String.join(",", errList);
        }

        return errMsg;
    }

    /**
     * パラメータの最大桁数チェック
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
