package jp.go.meti.drone.user_attr.model.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UpdateOperatorAttrRequest APIリクエストパラメータ
 */
@Data
public class UpdateOperatorAttrRequest {
    
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
        
        @JsonProperty("roleIds")
        private List<String> roleId;

        @JsonProperty("dipsAccountId")
        private String dipsAccountId;

        @JsonProperty("dipsAccountName")
        private String dipsAccountName;

        @JsonProperty("phone")
        private String phone;

        @JsonProperty("swimOperatorId")
        private String swimOperatorId;
    }
}
