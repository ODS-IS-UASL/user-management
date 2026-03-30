package jp.go.meti.drone.user_attr.model.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * GetUserAttrRequest APIリクエストパラメータ
 */
@Data
public class GetUserAttrRequest {
    @JsonProperty("from")
    private String from;

    @JsonProperty("userIdList")
    private List<String> userIdList;

    @JsonProperty("loginIdList")
    private List<String> loginIdList;
}
