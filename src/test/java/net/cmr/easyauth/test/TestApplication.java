package net.cmr.easyauth.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootConfiguration
@EnableAutoConfiguration
@Profile("test")
@EntityScan(basePackages = {"net.cmr.easyauth"}) // Scan the entire easyauth package
@ComponentScan("net.cmr.easyauth")
@EnableJpaRepositories("net.cmr.easyauth")
public class TestApplication {
    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "test");
        SpringApplication.run(TestApplication.class, args);
    }
}
