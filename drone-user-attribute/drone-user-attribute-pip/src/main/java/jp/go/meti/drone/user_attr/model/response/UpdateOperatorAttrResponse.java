package jp.go.meti.drone.user_attr.model.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UpdateOperatorAttrResponse [PUT]事業者属性更新用レスポンスオブジェクト
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateOperatorAttrResponse {
    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("user_login_id")
    private String loginId;

    @JsonProperty("operator_name")
    private String operatorName;

    @JsonProperty("attribute")
    private Attribute attribute;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Attribute {
        @JsonProperty("roles")
        private List<UserAttrResponse.Role> role;

        @JsonProperty("dipsAccountId")
        private String dipsAccountId;

        @JsonProperty("dipsAccountName")
        private String dipsAccountName;

        @JsonProperty("phone")
        private String phone;

        @JsonProperty("swimOperatorId")
        private String swimOperatorId;

        @JsonProperty("updateDatetime")
        private String updateDatetime;
    }
}
