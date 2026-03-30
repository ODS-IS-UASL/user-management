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

@Schema(name = "common.ResponseInternalServerError", description = "エラーオブジェクト")
@JsonTypeName("common.ResponseInternalServerError")
public class CommonResponseInternalServerError extends RuntimeException {

    private Integer code;

    private String errorMessage;

    public CommonResponseInternalServerError() {
        super();
    }

    /**
     * Constructor with only required parameters
     */
    public CommonResponseInternalServerError(Integer code, String errorMessage) {
        this.code = code;
        this.errorMessage = errorMessage;
    }

    public CommonResponseInternalServerError code(Integer code) {
        this.code = code;
        return this;
    }

    /**
     * エラーの種類を示すエラーコード
     * 
     * @return code
     */
    @NotNull
    @Schema(name = "code", example = "500", description = "エラーの種類を示すエラーコード", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("code")
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public CommonResponseInternalServerError errorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    /**
     * エラーの説明
     * 
     * @return errorMessage
     */
    @NotNull
    @Schema(name = "errorMessage", example = "予期しないシステムエラーが発生しました。", description = "エラーの説明", requiredMode = Schema.RequiredMode.REQUIRED)
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
        CommonResponseInternalServerError commonResponseInternalServerError = (CommonResponseInternalServerError) o;
        return Objects.equals(this.code, commonResponseInternalServerError.code) && Objects.equals(
            this.errorMessage,
            commonResponseInternalServerError.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, errorMessage);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class CommonResponseInternalServerError {\n");
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
     * Internal Server Error サーバーエラー
     * 
     * @return ResponseEntity<Resource>
     */
    public ResponseEntity<String> errorResponse() {
        String resource = createErrorResponse();
        return ResponseEntity.status(code).contentType(MediaType.APPLICATION_JSON).body(resource);
    }

}
