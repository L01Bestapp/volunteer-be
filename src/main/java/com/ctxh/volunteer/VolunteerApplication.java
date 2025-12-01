package com.ctxh.volunteer;

import com.ctxh.volunteer.common.init.EnvLoader;
import com.ctxh.volunteer.module.auth.config.RSAKeyRecord;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableConfigurationProperties(RSAKeyRecord.class)
public class VolunteerApplication {

	public static void main(String[] args) {
		EnvLoader.initEnv();
		SpringApplication.run(VolunteerApplication.class, args);
	}

}
