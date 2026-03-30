package jp.go.meti.drone.user_attr.service.user_info;

import java.util.Map;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.fasterxml.jackson.core.JsonProcessingException;

import jp.go.meti.drone.user_attr.model.commonmodel.CommonResponseBadRequestError;
import jp.go.meti.drone.user_attr.model.commonmodel.CommonResponseNotFoundError;
import jp.go.meti.drone.user_attr.model.request.OperatorInfoRequest;
import jp.go.meti.drone.user_attr.model.request.UserInfoRequest;
import jp.go.meti.drone.user_attr.model.response.OperatorInfoResponse;
import jp.go.meti.drone.user_attr.model.response.UserInfoResponse;

/**
 * AttributeAdminService ユーザ情報サービス インターフェースクラス 
 */
public interface AttributeAdminService {

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
    public OperatorInfoResponse registOperator(OperatorInfoRequest operatorInfoRequest, Map<String, String> headers)
        throws CommonResponseBadRequestError, CommonResponseNotFoundError, HttpClientErrorException,
        HttpServerErrorException, DuplicateKeyException, JsonProcessingException;

    /**
     * 事業者削除
     * 
     * @param operatorId [DELETE]事業者削除API　削除対象事業者の事業者ID
     * @param headers リクエストヘッダ
     * @return
     * @throws HttpClientErrorException
     * @throws HttpServerErrorException
     */
    public int deleteOperator(String operatorId, Map<String, String> headers) throws HttpClientErrorException,
        HttpServerErrorException;

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
    public UserInfoResponse registUser(UserInfoRequest userInfoRequest, String userUuid,
        Map<String, String> headers) throws CommonResponseBadRequestError, CommonResponseNotFoundError,
        HttpClientErrorException, HttpServerErrorException, DuplicateKeyException, JsonProcessingException;

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
    public int deleteOwnOperatorsUser(String userId, String userUuid, Map<String, String> headers)
        throws HttpClientErrorException, HttpServerErrorException;

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
    public UserInfoResponse registUserAcrossOperator(UserInfoRequest userInfoRequest, Map<String, String> headers)
        throws CommonResponseBadRequestError, CommonResponseNotFoundError, HttpClientErrorException,
        HttpServerErrorException, DuplicateKeyException, JsonProcessingException;

    /**
     * 任意の事業者に所属するユーザを削除
     * 
     * @param userId [DELETE]ユーザ削除API(スーパーユーザ)　削除対象ユーザのユーザID
     * @param headers リクエストヘッダ
     * @return
     */
    public int deleteUserAcrossOperator(String userId, Map<String, String> headers);

}
