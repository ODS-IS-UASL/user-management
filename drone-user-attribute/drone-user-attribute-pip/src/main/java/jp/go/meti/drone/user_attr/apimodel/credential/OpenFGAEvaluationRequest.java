package jp.go.meti.drone.user_attr.apimodel.credential;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OpenFGAEvaluationRequest
 */
@Data
public class OpenFGAEvaluationRequest {
    @JsonProperty("subject")
    private Subject subject;

    @JsonProperty("resource")
    private Resource resource;

    @JsonProperty("action")
    private Action action;

    @JsonProperty("context")
    private Context context;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Subject {
        @Builder.Default
        private String type = "user";

        private String id;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Resource {
        @Builder.Default
        private String type = "document";

        private String id;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Action {
        @Builder.Default
        private String name = "can_read";
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Context {
        @JsonProperty("current_time")
        private String currentTime;
    }
}
