package jp.go.meti.drone.user_attr.service.com;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.go.meti.drone.user_attr.apimodel.credential.GetOperatorInfoResponse;
import jp.go.meti.drone.user_attr.apimodel.credential.OpenFGAEvaluationRequest;
import jp.go.meti.drone.user_attr.apimodel.credential.OpenFGAEvaluationResponse;
import jp.go.meti.drone.user_attr.apimodel.credential.RegistOperatorRequest;
import jp.go.meti.drone.user_attr.apimodel.credential.RegistOperatorResponse;
import jp.go.meti.drone.user_attr.apimodel.credential.UpdateOperatorRequest;
import jp.go.meti.drone.user_attr.apimodel.credential.UpdateOperatorResponse;
import jp.go.meti.drone.user_attr.apimodel.credential.UpdateOperatorStatusRequest;
import jp.go.meti.drone.user_attr.apimodel.credential.UpdateOperatorStatusResponse;
import jp.go.meti.drone.user_attr.model.commonmodel.CommonResponseBadRequestError;
import jp.go.meti.drone.user_attr.model.commonmodel.CommonResponseForbiddenError;
import jp.go.meti.drone.user_attr.model.commonmodel.CommonResponseInternalServerError;
import jp.go.meti.drone.user_attr.model.commonmodel.CommonResponseUnAuthorizedError;
import lombok.extern.slf4j.Slf4j;

/**
 * L3-identity-componentおよびOpenFGAアクセス用サービス サービスクラス
 */
@Slf4j
@Service
public class CredentialServiceImpl implements CredentialService {

	/**
	 * L3-identity-component:[POST]事業者情報登録API URL
	 */
    @Value("${user.attribute.idp.url.operator.post}")
    private String postOperatorUrl;

    /**
     * L3-identity-component:[GET]事業者情報取得API URL
     */
    @Value("${user.attribute.idp.url.operator.get}")
    private String getOperatorUrl;

    /**
     * L3-identity-component:[PUT]事業者情報ステータス更新API URL
     */
    @Value("${user.attribute.idp.url.operator.put}")
    private String putOperatorUrl;

    /**
     * L3-identity-component:[PUT]事業者情報更新API URL
     */
    @Value("${user.attribute.idp.url.operator.attrput}")
    private String putOperatorAttrUrl;

    /**
     * OpenFGA:[POST]認可判定API URL
     */
    @Value("${user.attribute.idp.url.operator.fga}")
    private String evaluationApiUrl;

    /**
     * L3-identity-component/OpenFGA:APIキー項目名
     */
    @Value("${user.attribute.idp.idp_apikey_name}")
    private String idpApikeyName;

    /**
     * L3-identity-component/OpenFGA:APIキー
     */
    @Value("${user.attribute.idp.idp_apikey}")
    private String idpApikey;

    /**
     * L3-identity-component/OpenFGA:Authorizationキー項目名
     */
    @Value("${user.attribute.auth_key_name}")
    private String authKey;

    /**
     * L3-identity-component/OpenFGA:MediaType
     */
    @Value("${user.attribute.idp.idp_content_type}")
    private String idpContentType;

    /**
     * RestTemplate
     */
    private final RestTemplate restTemplate;

    /**
     * HttpHeaderProxy
     */
    private final HttpHeaderProxy headerProxy;

    /**
     * コンストラクタ
     * 
     * @param restTemplate　RestTemplate
     * @param headerProxy　HttpHeaderProxy
     */
    public CredentialServiceImpl(@Qualifier("userAttrRestTemplate") RestTemplate restTemplate,
        HttpHeaderProxy headerProxy) {
        this.restTemplate = restTemplate;
        this.headerProxy = headerProxy;
    }

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
    @Override
    public RegistOperatorResponse postUserInfo(RegistOperatorRequest registOperatorRequest,
        Map<String, String> headerMap) throws HttpClientErrorException, HttpServerErrorException,
        JsonProcessingException {
        log.debug(
            "CredentialServiceImpl::postUserInfo called widh userId/userPasswrod: " + registOperatorRequest.toString());
        log.debug("CredentialServiceImpl::postUserInfo called widh postOperatorUrl: " + postOperatorUrl);
        try {
            log.debug(headerMap.toString());

            checkBearer(headerMap);

            // opearateUidを抜き出して保持
            String opearateUid = headerProxy.removeHeaderElement(headerMap, "authorization", authKey);
            // API-Key追加
            headerProxy.addHeader(headerMap, idpApikeyName, idpApikey);

            // TrackingId追加
            String trackingId = setTrackingId(headerMap);

            log.debug(headerMap.toString());

            HttpHeaders headers = convertHeader(headerMap);

            HttpEntity<RegistOperatorRequest> entity = new HttpEntity<>(registOperatorRequest, headers);

            // for debug
            logAsJson(registOperatorRequest);

            ResponseEntity<RegistOperatorResponse> response = restTemplate.exchange(
                postOperatorUrl,
                HttpMethod.POST,
                entity,
                RegistOperatorResponse.class);

            log.debug("header:{" + entity.getHeaders() + "}, body:{" + entity.getBody() + "}");
            log.debug(response.getHeaders().toString());
            // for debug
            logAsJson(entity.getBody());

            // API-Key除去&opearateUidを戻す
            headerProxy.removeHeader(headerMap, idpApikeyName);
            headerProxy.addHeaderElement(headerMap, "authorization", authKey, opearateUid);

            // TrackingId検証
            verifyTrackingId(response.getHeaders(), trackingId);

            log.debug(headerMap.toString());

            return response.getBody();

        } catch (HttpClientErrorException e) {
            handleClientErrorException(e);
        } catch (HttpServerErrorException e) {
            log.debug(e.getStatusCode().toString());
            log.debug(e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (IllegalStateException e) {
            log.debug(e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("HTTP Request Failure. ", e);
            throw e;
        }
        return null;
    }

    /**
     * L3-identity-component:[GET]事業者情報取得APIにてユーザ/事業者情報取得
     * 
     * @param userId 取得対象のユーザのuserId/取得対象の事業者のoperatorId
     * @param headers ヘッダー
     * @return　[GET]事業者情報取得API　レスポンス
     * @throws HttpClientErrorException 外部APIでの400,401,403エラー発生
     * @throws HttpServerErrorException 外部APIでの400,401,403以外のエラー発生
     */
    public GetOperatorInfoResponse getOperator(String operatorId, Map<String, String> headerMap)
        throws HttpClientErrorException, HttpServerErrorException {
        log.debug("CredentialServiceImpl::getOperator called widh operatorId: " + operatorId);
        log.debug("CredentialServiceImpl::getOperator called widh getOperatorUrl: " + getOperatorUrl);
        ResponseEntity<GetOperatorInfoResponse> response = null;
        try {
            log.debug(headerMap.toString());

            checkBearer(headerMap);

            // opearateUidを抜き出して保持
            String opearateUid = headerProxy.removeHeaderElement(headerMap, "authorization", authKey);
            // API-Key追加
            headerProxy.addHeader(headerMap, idpApikeyName, idpApikey);
            // TrackingId追加
            String trackingId = setTrackingId(headerMap);
            // L3用にContent-Typeを設定
            headerProxy.removeHeader(headerMap, "Content-Type");
            headerProxy.addHeader(headerMap, "Content-Type", idpContentType);

            log.debug(headerMap.toString());

            HttpHeaders headers = convertHeader(headerMap);

            String uri = UriComponentsBuilder.fromUriString(getOperatorUrl).pathSegment(operatorId).toUriString();

            HttpEntity<GetOperatorInfoResponse> entity = new HttpEntity<>(headers);

            response = restTemplate.exchange(uri, HttpMethod.GET, entity, GetOperatorInfoResponse.class);
            log.debug("header:{" + entity.getHeaders() + "}, body:{" + entity.getBody() + "}");

            // for debug
            logAsJson(entity.getBody());

            // API-Key除去&opearateUidを戻す
            headerProxy.removeHeader(headerMap, idpApikeyName);
            headerProxy.addHeaderElement(headerMap, "authorization", authKey, opearateUid);

            // TrackingId検証
            verifyTrackingId(response.getHeaders(), trackingId);

            log.debug(headerMap.toString());

            log.debug(response.toString());
            return response.getBody();
        } catch (HttpClientErrorException e) {
            handleClientErrorException(e);
        } catch (HttpServerErrorException e) {
            log.debug(e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (IllegalStateException e) {
            log.debug(e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("HTTP Request Failure. ", e);
            throw e;
        }
        return null;
    }

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
        UpdateOperatorStatusRequest updateOperatorStatusRequest, Map<String, String> headerMap)
        throws HttpClientErrorException, HttpServerErrorException {
        ResponseEntity<UpdateOperatorStatusResponse> response = null;
        try {
            log.debug(headerMap.toString());

            checkBearer(headerMap);

            // opearateUidを抜き出して保持
            String opearateUid = headerProxy.removeHeaderElement(headerMap, "authorization", authKey);
            // API-Key追加
            headerProxy.addHeader(headerMap, idpApikeyName, idpApikey);
            // TrackingId追加
            String trackingId = setTrackingId(headerMap);

            log.debug(headerMap.toString());

            HttpHeaders headers = convertHeader(headerMap);

            String uri = UriComponentsBuilder.fromUriString(putOperatorUrl).pathSegment(operatorId).toUriString();
            HttpEntity<UpdateOperatorStatusRequest> entity = new HttpEntity<>(updateOperatorStatusRequest, headers);
            log.debug("header:{" + entity.getHeaders() + "}, body:{" + entity.getBody() + "}");

            // for debug
            logAsJson(updateOperatorStatusRequest);

            response = restTemplate.exchange(uri, HttpMethod.PUT, entity, UpdateOperatorStatusResponse.class);

            // for debug
            logAsJson(entity.getBody());

            // API-Key除去&opearateUidを戻す
            headerProxy.removeHeader(headerMap, idpApikeyName);
            headerProxy.addHeaderElement(headerMap, "authorization", authKey, opearateUid);

            // TrackingId検証
            verifyTrackingId(response.getHeaders(), trackingId);

            log.debug(headerMap.toString());

            return response.getBody();
        } catch (HttpClientErrorException e) {
            handleClientErrorException(e);
        } catch (HttpServerErrorException e) {
            log.debug(e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (IllegalStateException e) {
            log.debug(e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            log.error("HTTP Request Failure. ", e);
            e.printStackTrace();
            throw e;
        }
        return null;
    }

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
    public OpenFGAEvaluationResponse postEvaluation(String storeId, OpenFGAEvaluationRequest openFGAEvaluationRequest,
        Map<String, String> headerMap) throws HttpClientErrorException, HttpServerErrorException {
        ResponseEntity<OpenFGAEvaluationResponse> response = null;
        try {
            log.debug(headerMap.toString());

            checkBearer(headerMap);

            // opearateUidを抜き出して保持
            String opearateUid = headerProxy.removeHeaderElement(headerMap, "authorization", authKey);

            // アクセストークンから認可判定APIの"subject > id"用のid抽出
            String subjectId = getSubjectId(headerMap);

            // "subject > id"に抽出したidをセット
            openFGAEvaluationRequest.getSubject().setId(subjectId);

            // API-Key追加
            headerProxy.addHeader(headerMap, idpApikeyName, idpApikey);

            log.debug(headerMap.toString());

            HttpHeaders headers = convertHeader(headerMap);

            String uri = UriComponentsBuilder.fromUriString(evaluationApiUrl).buildAndExpand(storeId).toUriString();
            HttpEntity<OpenFGAEvaluationRequest> entity = new HttpEntity<>(openFGAEvaluationRequest, headers);
            log.debug("header:{" + entity.getHeaders() + "}, body:{" + entity.getBody() + "}");

            response = restTemplate.exchange(uri, HttpMethod.POST, entity, OpenFGAEvaluationResponse.class);

            // for debug
            logAsJson(entity.getBody());

            // API-Key除去&opearateUidを戻す
            headerProxy.removeHeader(headerMap, idpApikeyName);
            if (opearateUid != null) {
                headerProxy.addHeaderElement(headerMap, "authorization", authKey, opearateUid);
            }

            log.debug(headerMap.toString());

            return response.getBody();

        } catch (HttpClientErrorException e) {
            handleClientErrorException(e);
        } catch (HttpServerErrorException e) {
            log.debug(e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (IllegalStateException e) {
            log.debug(e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (CommonResponseBadRequestError e) {
            log.debug(e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("HTTP Request Failure. ", e);
            e.printStackTrace();
            throw e;
        }
        return null;
    }

	/**
	 * L3-identity-component:[PUT]事業者情報更新APIにてユーザ/事業者情報更新
	 * 
	 * @param operatorId 取得対象のユーザのuserId/取得対象の事業者のoperatorId
	 * @param updateOperatorRequest [PUT]事業者情報更新API　リクエスト
	 * @param headers　ヘッダー
	 * @return
	 */
    public UpdateOperatorResponse putOperator(String operatorId, UpdateOperatorRequest updateOperatorRequest,
        Map<String, String> headerMap) {
        ResponseEntity<UpdateOperatorResponse> response = null;
        try {
            log.debug(headerMap.toString());

            checkBearer(headerMap);

            // opearateUidを抜き出して保持
            String opearateUid = headerProxy.removeHeaderElement(headerMap, "authorization", authKey);
            // API-Key追加
            headerProxy.addHeader(headerMap, idpApikeyName, idpApikey);
            // TrackingId追加
            String trackingId = setTrackingId(headerMap);

            log.debug(headerMap.toString());

            HttpHeaders headers = convertHeader(headerMap);

            String uri = UriComponentsBuilder.fromUriString(putOperatorAttrUrl).pathSegment(operatorId).toUriString();
            HttpEntity<UpdateOperatorRequest> entity = new HttpEntity<>(updateOperatorRequest, headers);
            log.debug("header:{" + entity.getHeaders() + "}, body:{" + entity.getBody() + "}");

            // for debug
            logAsJson(updateOperatorRequest);

            response = restTemplate.exchange(uri, HttpMethod.PUT, entity, UpdateOperatorResponse.class);

            // for debug
            logAsJson(entity.getBody());

            // API-Key除去&opearateUidを戻す
            headerProxy.removeHeader(headerMap, idpApikeyName);
            headerProxy.addHeaderElement(headerMap, "authorization", authKey, opearateUid);

            // TrackingId検証
            verifyTrackingId(response.getHeaders(), trackingId);

            log.debug(headerMap.toString());

            return response.getBody();
        } catch (HttpClientErrorException e) {
            handleClientErrorException(e);
        } catch (HttpServerErrorException e) {
            log.debug(e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (IllegalStateException e) {
            log.debug(e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            log.error("HTTP Request Failure. ", e);
            e.printStackTrace();
            throw e;
        }
        return null;
    }

    /**
     * L3-identity-component/OpenFGAのエラーレスポンス割り振り（400,401,403以外は500エラーとして処理）
     * 
     * @param e
     * @throws HttpClientErrorException
     * @throws HttpServerErrorException
     */
    private void handleClientErrorException(HttpClientErrorException e) throws HttpClientErrorException,
        HttpServerErrorException {
        log.debug(e.getStatusText());
        log.debug(e.getMessage());
        HttpStatusCode statusCode = e.getStatusCode();
        if (statusCode.equals(HttpStatus.BAD_REQUEST)) { // 400
            log.debug("400 BAD_REQUEST");
            throw new CommonResponseBadRequestError(e.getStatusCode().value(), "リクエストパラメータに不正な値が含まれています。");
        } else if (statusCode.equals(HttpStatus.UNAUTHORIZED)) { // 401
            log.debug("401 UNAUTHORIZED");
            throw new CommonResponseUnAuthorizedError(e.getStatusCode().value(), "認証に失敗しました。有効な認証情報を提供してください。");
        } else if (statusCode.equals(HttpStatus.FORBIDDEN)) { // 403
            log.debug("403 FORBIDDEN");
            throw new CommonResponseForbiddenError(e.getStatusCode().value(), "アクセスが拒否されました。この操作を実行する権限がありません。");
        } else { // 400,401,403以外500返却
            log.debug(statusCode.toString());
            throw new CommonResponseInternalServerError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), "予期しないシステムエラーが発生しました。");
        }
    }

    /**
     * headerのBearerの先頭Bを大文字に変換
     * 
     * @param headerMap
     */
    private void checkBearer(Map<String, String> headerMap) {
        String bearer = headerProxy.getHeaderElement(headerMap, "authorization", "Bearer");
        if (bearer == null) {
            bearer = headerProxy.getHeaderElement(headerMap, "authorization", "bearer");
            if (bearer != null) {
                headerProxy.removeHeaderElement(headerMap, "authorization", "bearer");
                headerProxy.addHeaderElement(headerMap, "authorization", "Bearer", bearer);
            }
        }

        log.debug(bearer);
        if (bearer == null) {
            throw new HttpClientErrorException(
                HttpStatus.UNAUTHORIZED,
                "{\"error\":\"Authorization Failed\",\"message\":\"Authorization: bearer required.\"}");
        }
    }

    /**
     * headerのBearerに含まれるclient_id(クライアントシステム認証時)/operator_id(認可コードフロー時)を抜き出す（操作アカウント特定のため）
     * 
     * @param headerMap
     * @return
     */
    private String getSubjectId(Map<String, String> headerMap) {
        String bearer = headerProxy.getHeaderElement(headerMap, "authorization", "Bearer");
        if (bearer == null) {
            bearer = headerProxy.getHeaderElement(headerMap, "authorization", "bearer");
        }

        log.debug(bearer);
        DecodedJWT jwt = JWT.decode(bearer);
        String subjectId = jwt.getClaim("client_id").asString();
        if (subjectId == null) {
            subjectId = jwt.getClaim("operator_id").asString();
        }
        if (subjectId == null) {
            // 400エラー
            throw new CommonResponseBadRequestError(HttpStatus.BAD_REQUEST.value(), "正しいアクセストークンを設定してください。");
        }
        return subjectId;
    }

    /**
     * headerにL3-identity-component/OpenFGA用のTrackingId設定
     * 
     * @param headerMap
     * @return
     */
    private String setTrackingId(Map<String, String> headerMap) {
        String trackingId = UUID.randomUUID().toString();
        headerProxy.addHeader(headerMap, "X-TrackingID", trackingId);
        return trackingId;
    }

    /**
     * headerのTrackingIdの整合性確認
     * 
     * @param headers
     * @param trackingId
     */
    private void verifyTrackingId(HttpHeaders headers, String trackingId) {
        boolean found = false;
        List<String> headerElements = headerProxy.getEntry(headers, "X-TrackingID");
        if (headerElements != null) {
            for (String element : headerElements) {
                log.debug(element);
                if (element.equals(trackingId)) {
                    found = true;
                }
            }
        } else {
            throw new IllegalStateException("X-TrackingID not found.");
        }
        if (!found) {
            throw new IllegalStateException("X-TrackingID mismatch");
        }
    }

    /**
     * headerにMediaType設定
     * 
     * @param headerMap
     * @return
     */
    private HttpHeaders convertHeader(Map<String, String> headerMap) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (headerMap != null) {
            headerMap.remove("content-length");
            headerMap.forEach(headers::set);
        }
        return headers;
    }

    // for debug!
    /**
     * オブジェクトをJSON形式に変換してログ出力
     * 
     * @param obj
     */
    private void logAsJson(Object obj) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            String json;
            json = mapper.writeValueAsString(obj);
            log.debug(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
