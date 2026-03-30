package jp.go.meti.drone.user_attr.service.com;

import java.util.Map;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.fasterxml.jackson.core.JsonProcessingException;

import jp.go.meti.drone.user_attr.apimodel.credential.GetOperatorInfoResponse;
import jp.go.meti.drone.user_attr.apimodel.credential.OpenFGAEvaluationRequest;
import jp.go.meti.drone.user_attr.apimodel.credential.OpenFGAEvaluationResponse;
import jp.go.meti.drone.user_attr.apimodel.credential.RegistOperatorRequest;
import jp.go.meti.drone.user_attr.apimodel.credential.RegistOperatorResponse;
import jp.go.meti.drone.user_attr.apimodel.credential.UpdateOperatorRequest;
import jp.go.meti.drone.user_attr.apimodel.credential.UpdateOperatorResponse;
import jp.go.meti.drone.user_attr.apimodel.credential.UpdateOperatorStatusRequest;
import jp.go.meti.drone.user_attr.apimodel.credential.UpdateOperatorStatusResponse;

/**
 * L3-identity-componentおよびOpenFGAアクセス用サービス インターフェースクラス
 */
public interface CredentialService {

    /**
     * L3-identity-component:[POST]事業者情報登録APIにてユーザ/事業者情報登録
     * 
     * @param registOperatorRequest [POST]事業者情報登録APIリクエスト
     * @param headers ヘッダー
     * @return [POST]事業者情報登録APIレスポンス
     * @throws HttpClientErrorException　外部APIでの400,401,403エラー発生
     * @throws HttpServerErrorException 外部APIでの400,401,403以外のエラー発生
     * @throws JsonProcessingException Debug用ログ出力エラー
     */
    public RegistOperatorResponse postUserInfo(RegistOperatorRequest registOperatorRequest, Map<String, String> headers)
        throws HttpClientErrorException, HttpServerErrorException, JsonProcessingException;

    /**
     * L3-identity-component:[GET]事業者情報取得APIにてユーザ/事業者情報取得
     * 
     * @param userId 取得対象のユーザのuserId/取得対象の事業者のoperatorId
     * @param headers ヘッダー
     * @return　[GET]事業者情報取得API　レスポンス
     * @throws HttpClientErrorException 外部APIでの400,401,403エラー発生
     * @throws HttpServerErrorException 外部APIでの400,401,403以外のエラー発生
     */
    public GetOperatorInfoResponse getOperator(String userId, Map<String, String> headers)
        throws HttpClientErrorException, HttpServerErrorException;

    /**
     * L3-identity-component:[PUT]事業者情報ステータス更新APIにてユーザ/事業者情報削除(deleted_flag=true)
     * 
     * @param operatorId 取得対象のユーザのuserId/取得対象の事業者のoperatorId
     * @param updateOperatorStatusRequest [PUT]事業者情報ステータス更新API　リクエスト(deleted_flag=true)
     * @param headers ヘッダー
     * @return[PUT]事業者情報ステータス更新API　レスポンス
     * @throws HttpClientErrorException 外部APIでの400,401,403エラー発生
     * @throws HttpServerErrorException 外部APIでの400,401,403以外のエラー発生
     */
    public UpdateOperatorStatusResponse putOperatorStatus(String operatorId,
        UpdateOperatorStatusRequest updateOperatorStatusRequest, Map<String, String> headers)
        throws HttpClientErrorException, HttpServerErrorException;

    /**
     * OpenFGA:[POST]認可判定APIにて対象ユーザ/事業者が対象操作のアクセス許可があるか確認
     * 
     * @param storeId ユーザ管理用ストアID
     * @param checkApiRequest [POST]認可判定API　リクエスト
     * @param headers ヘッダー
     * @return
     * @throws HttpClientErrorException 外部APIでの400,401,403エラー発生
     * @throws HttpServerErrorException 外部APIでの400,401,403以外のエラー発生
     */
    public OpenFGAEvaluationResponse postEvaluation(String storeId, OpenFGAEvaluationRequest checkApiRequest,
        Map<String, String> headers) throws HttpClientErrorException, HttpServerErrorException;

	/**
	 * L3-identity-component:[PUT]事業者情報更新APIにてユーザ/事業者情報更新
	 * 
	 * @param operatorId 取得対象のユーザのuserId/取得対象の事業者のoperatorId
	 * @param updateOperatorRequest [PUT]事業者情報更新API　リクエスト
	 * @param headers　ヘッダー
	 * @return
	 */
	public UpdateOperatorResponse putOperator(String operatorId, UpdateOperatorRequest updateOperatorRequest,
			Map<String, String> headers);
}
