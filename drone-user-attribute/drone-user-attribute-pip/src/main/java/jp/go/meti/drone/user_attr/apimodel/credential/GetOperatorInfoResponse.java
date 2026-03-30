package jp.go.meti.drone.user_attr.apimodel.credential;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * GetOperatorInfoResponse
 */
@Data
public class GetOperatorInfoResponse {
    @JsonProperty("type")
    private String type;

    @JsonProperty("title")
    private String title;

    @JsonProperty("status")
    private String status;

    @JsonProperty("detail")
    private String detail;

    @JsonProperty("data")
    private GetOperatorInfoResponseData data;
}
