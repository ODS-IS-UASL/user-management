package jp.go.meti.drone.user_attr.model.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UpdateUserAttrRequest APIリクエストパラメータ
 */
@Data
public class UpdateUserAttrRequest {
    
    @JsonProperty("login_user_id")
    private String loginUserId;

    @JsonProperty("operator_name")
    private String operatorName;

    @JsonProperty("attribute")
    private Attribute attribute;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Attribute {
        
        @JsonProperty("operatorId")
        private String operatorId;

        @JsonProperty("roleIds")
        private List<String> roleId;
    }
}
