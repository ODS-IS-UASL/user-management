package jp.go.meti.drone.user_attr.apimodel.credential;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * GetOperatorInfoResponseData
 */
@Data
public class GetOperatorInfoResponseData {
    @JsonProperty("operator_id")
    private String operatorId;

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

    @JsonProperty("deleted_flag")
    private boolean deletedFlag;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;
}
