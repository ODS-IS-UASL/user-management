package jp.go.meti.drone.user_attr.model.commonmodel;

import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * エラーオブジェクト
 */

@Schema(name = "common.ResponseUnAuthorizedError", description = "エラーオブジェクト")
@JsonTypeName("common.ResponseUnAuthorizedError")
public class CommonResponseUnAuthorizedError extends RuntimeException {

    private Integer code;

    private String errorMessage;

    public CommonResponseUnAuthorizedError() {
        super();
    }

    /**
     * Constructor with only required parameters
     */
    public CommonResponseUnAuthorizedError(Integer code, String errorMessage) {
        this.code = code;
        this.errorMessage = errorMessage;
    }

    public CommonResponseUnAuthorizedError code(Integer code) {
        this.code = code;
        return this;
    }

    /**
     * エラーの種類を示すエラーコード
     * 
     * @return code
     */
    @NotNull
    @Schema(name = "code", example = "401", description = "エラーの種類を示すエラーコード", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("code")
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public CommonResponseUnAuthorizedError errorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    /**
     * エラーの説明
     * 
     * @return errorMessage
     */
    @NotNull
    @Schema(name = "errorMessage", example = "アクセストークンエラー", description = "エラーの説明", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("errorMessage")
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CommonResponseUnAuthorizedError commonResponseUnAuthorizedError = (CommonResponseUnAuthorizedError) o;
        return Objects.equals(this.code, commonResponseUnAuthorizedError.code) && Objects.equals(
            this.errorMessage,
            commonResponseUnAuthorizedError.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, errorMessage);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class CommonResponseUnAuthorizedError {\n");
        sb.append("    code: ").append(toIndentedString(code)).append("\n");
        sb.append("    errorMessage: ").append(toIndentedString(errorMessage)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

    private String createErrorResponse() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("    \"code\": ").append(toIndentedString(code)).append(",\n");
        sb.append("    \"errorMessage\": \"").append(toIndentedString(errorMessage)).append("\"\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * アクセストークンの認証に失敗した場合
     * 
     * @return ResponseEntity<Resource>
     */
    public ResponseEntity<String> errorResponse() {
        String resource = createErrorResponse();
        return ResponseEntity.status(code).contentType(MediaType.APPLICATION_JSON).body(resource);
    }

}
