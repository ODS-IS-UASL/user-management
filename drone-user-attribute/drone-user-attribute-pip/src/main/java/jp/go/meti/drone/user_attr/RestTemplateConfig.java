package jp.go.meti.drone.user_attr;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate設定
 */
@Configuration
public class RestTemplateConfig {

	/**
	 * restTemplateのBean
	 * @return　restTemplate
	 */
    @Bean(name = "userAttrRestTemplate")
    public RestTemplate restTemplate() {
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        return restTemplateBuilder.build();
    }
}
