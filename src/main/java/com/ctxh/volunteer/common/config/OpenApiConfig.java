package com.ctxh.volunteer.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Volunteer API")
                        .version("1.0")
                        .description("API documentation for the Volunteer Management System")
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")
                        )
                        .termsOfService("http://swagger.io/terms/")
                        .contact(new Contact()
                                .name("CT Volunteer Team")
                                .email("thangvip030201@gmail.com")
                        )
                );
    }

    @Bean
    public GroupedOpenApi studentOpenApi() {
        return GroupedOpenApi.builder()
                .group("students")
                .pathsToMatch("/api/v1/students/**")
                .build();
    }

    @Bean
    public GroupedOpenApi organizationOpenApi() {
        return GroupedOpenApi.builder()
                .group("organizations")
                .pathsToMatch("/api/v1/organization/**")
                .build();
    }

    @Bean
    public GroupedOpenApi authOpenApi() {
        return GroupedOpenApi.builder()
                .group("auth")
                .pathsToMatch("/api/v1/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userOpenApi() {
        return GroupedOpenApi.builder()
                .group("users")
                .pathsToMatch("/api/v1/users/**")
                .build();
    }
}
