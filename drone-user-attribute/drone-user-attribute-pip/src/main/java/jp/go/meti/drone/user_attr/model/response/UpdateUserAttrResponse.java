package jp.go.meti.drone.user_attr.model.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UpdateUserAttrResponse [PUT]ユーザ属性更新(スーパーユーザ)/[PUT]ユーザ属性更新用レスポンスオブジェクト
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserAttrResponse {
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
        private List<UserAttrResponse.Role> role;

        @JsonProperty("updateDatetime")
        private String updateDatetime;
    }
}
