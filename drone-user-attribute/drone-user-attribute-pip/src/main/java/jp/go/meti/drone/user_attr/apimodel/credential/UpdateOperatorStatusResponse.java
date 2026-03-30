package jp.go.meti.drone.user_attr.apimodel.credential;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * UpdateOperatorStatusResponse
 */
@Data
public class UpdateOperatorStatusResponse {

    @JsonProperty("type")
    private String type;

    @JsonProperty("title")
    private String title;

    @JsonProperty("status")
    private Integer status;

    @JsonProperty("detail")
    private String detail;

    @JsonProperty("data")
    private UpdateOperatorStatusResponseData data;

}
