package jp.go.meti.drone.user_attr.apimodel.credential;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OpenFGAEvaluationResponse
 */
@Data
public class OpenFGAEvaluationResponse {
    private String type;

    private String title;

    private String status;

    private String detail;

    private DataObject data;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DataObject {
        private boolean decision;
    }

}
