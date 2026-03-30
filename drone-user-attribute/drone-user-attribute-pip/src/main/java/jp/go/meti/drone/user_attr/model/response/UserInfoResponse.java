package jp.go.meti.drone.user_attr.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UserInfoResponse [POST]ユーザ登録(スーパーユーザ)/[POST]ユーザ登録用レスポンスオブジェクト
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoResponse {
    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("operator_id")
    private String operatorId;

    @JsonProperty("password")
    private String password;
}
