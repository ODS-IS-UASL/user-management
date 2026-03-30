package jp.go.meti.drone.user_attr.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

import java.util.Map;


/**
 * UserRoleDefinitions　ユーザロール定義設定
 */
@Data
@Component
@ConfigurationProperties(prefix = "user.attribute")
public class UserRoleDefinitions {
    private Map<String, String> userRole;
    
    private Map<String, String> operatorRole;
}
