package jp.go.meti.drone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

import jp.go.meti.drone.com.common.ComCommonConfiguration;

/**
 * SpringBootアプリケーション起動メインクラス。
 */
@SpringBootApplication
@EnableScheduling
@Import(ComCommonConfiguration.class)
@ComponentScan("jp.go.meti.drone.user_attr")
public class UserAttributeApplication {

    /**
     * メイン。
     *
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        SpringApplication.run(UserAttributeApplication.class, args);
    }
}
