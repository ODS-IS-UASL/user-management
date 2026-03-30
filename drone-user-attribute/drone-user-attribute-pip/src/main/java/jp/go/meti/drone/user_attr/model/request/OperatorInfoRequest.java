package jp.go.meti.drone.user_attr.model.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OperatorInfoRequest APIリクエストパラメータ
 */
@Data
public class OperatorInfoRequest {

    @JsonProperty("login_user_id")
    @NotBlank
    private String loginUserId;

    @JsonProperty("operator_name")
    @NotBlank
    private String operatorName;

    // operator_addressとopen_operator_idはyamlで固定値に設定
    @NotNull
    @Valid
    private Attribute attribute;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Attribute {

        @JsonProperty("roleIds")
        @NotEmpty
        private List<@NotBlank String> roleId;

        @JsonProperty("dipsAccountId")
        private String dipsAccountId;

        @JsonProperty("dipsAccountName")
        private String dipsAccountName;

        @JsonProperty("phone")
        private String phone;

        @JsonProperty("swimOperatorId")
        @NotEmpty
        private String swimOperatorId;
    }
}
