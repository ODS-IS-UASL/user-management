package jp.go.meti.drone.user_attr.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OperatorInfoResponse [POST]事業者登録用レスポンスオブジェクト
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OperatorInfoResponse {
    @JsonProperty("operator_id")
    private String operatorId;

    @JsonProperty("password")
    private String password;
}
