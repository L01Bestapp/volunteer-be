package com.ctxh.volunteer;

import com.ctxh.volunteer.common.init.EnvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VolunteerApplication {

	public static void main(String[] args) {
		EnvLoader.initEnv();
		SpringApplication.run(VolunteerApplication.class, args);
	}

}
