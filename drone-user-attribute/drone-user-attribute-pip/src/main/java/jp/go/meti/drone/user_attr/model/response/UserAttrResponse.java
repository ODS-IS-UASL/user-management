package jp.go.meti.drone.user_attr.model.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UserAttrResponse [POST]ユーザ属性取得用レスポンスオブジェクト
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAttrResponse {
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
        @JsonProperty("operatorId")
        private String operatorId;

        @JsonProperty("roles")
        private List<Role> role;

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

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Role {
        @JsonProperty("roleId")
        private String roleId;

        @JsonProperty("roleName")
        private String roleName;
    }
}
