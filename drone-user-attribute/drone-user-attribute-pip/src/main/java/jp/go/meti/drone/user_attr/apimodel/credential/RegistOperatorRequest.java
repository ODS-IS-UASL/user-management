package jp.go.meti.drone.user_attr.apimodel.credential;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * RegistOperatorRequest
 */
@Data
public class RegistOperatorRequest {
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

    @JsonProperty("effective_start_date")
    private String effectiveStartDate;

    @JsonProperty("effective_end_date")
    private String effectiveEndDate;

    @JsonProperty("create_password_flag")
    private Boolean createPasswordFlag;

    @JsonProperty("password_temporary_flag")
    private Boolean passwordTemporaryFlag;

}
