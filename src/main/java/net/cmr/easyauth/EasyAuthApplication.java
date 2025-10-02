package net.cmr.easyauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EasyAuthApplication {
	public static void main(String[] args) {
		SpringApplication.run(EasyAuthApplication.class, args);
	}
}
