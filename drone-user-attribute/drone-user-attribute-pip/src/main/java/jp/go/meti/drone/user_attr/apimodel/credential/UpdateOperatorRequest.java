package jp.go.meti.drone.user_attr.apimodel.credential;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * UpdateOperatorRequest
 */
@Data
public class UpdateOperatorRequest {
    @JsonProperty("updated_at")
    private String updatedAt;

    @JsonProperty("login_user_id")
    private String loginUserId;

    @JsonProperty("operator_name")
    private String operatorName;

    @JsonProperty("operator_address")
    private String operatorAddress;

    @JsonProperty("open_operator_id")
    private String openOperatorId;

    @JsonProperty("global_operator_id")
    private String globalOperatorId;
}
