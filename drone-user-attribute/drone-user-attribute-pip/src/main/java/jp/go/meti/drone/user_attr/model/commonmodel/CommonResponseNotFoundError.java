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

@Schema(name = "common.ResponseNotFoundError", description = "エラーオブジェクト")
@JsonTypeName("common.ResponseNotFoundError")
public class CommonResponseNotFoundError extends RuntimeException {

    private Integer code;

    private String errorMessage;

    public CommonResponseNotFoundError() {
        super();
    }

    /**
     * Constructor with only required parameters
     */
    public CommonResponseNotFoundError(Integer code, String errorMessage) {
        this.code = code;
        this.errorMessage = errorMessage;
    }

    public CommonResponseNotFoundError code(Integer code) {
        this.code = code;
        return this;
    }

    /**
     * エラーの種類を示すエラーコード
     * 
     * @return code
     */
    @NotNull
    @Schema(name = "code", example = "404", description = "エラーの種類を示すエラーコード", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("code")
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public CommonResponseNotFoundError errorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    /**
     * エラーの説明
     * 
     * @return errorMessage
     */
    @NotNull
    @Schema(name = "errorMessage", example = "指定された航路情報は取得できませんでした。", description = "エラーの説明", requiredMode = Schema.RequiredMode.REQUIRED)
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
        CommonResponseNotFoundError commonResponseNotFoundError = (CommonResponseNotFoundError) o;
        return Objects.equals(this.code, commonResponseNotFoundError.code) && Objects.equals(
            this.errorMessage,
            commonResponseNotFoundError.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, errorMessage);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class CommonResponseNotFoundError {\n");
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
     * Not Found 指定した航路情報が取得できない場合
     * 
     * @return ResponseEntity<Resource>
     */
    public ResponseEntity<String> errorResponse() {
        String resource = createErrorResponse();
        return ResponseEntity.status(code).contentType(MediaType.APPLICATION_JSON).body(resource);
    }

}
