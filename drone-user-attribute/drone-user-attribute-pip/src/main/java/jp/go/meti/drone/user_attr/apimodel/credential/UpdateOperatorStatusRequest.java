package jp.go.meti.drone.user_attr.apimodel.credential;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * UpdateOperatorStatusRequest
 */
@Data
public class UpdateOperatorStatusRequest {
    @JsonProperty("updated_at")
    private String updatedAt;

    @JsonProperty("effective_start_date")
    private String effectiveStartDate;

    @JsonProperty("effective_end_date")
    private String effectiveEndDate;

    @JsonProperty("deleted_flag")
    private boolean deletedFlag;
}
