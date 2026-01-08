package com.ctxh.volunteer.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

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
                )
                .addServersItem(new Server().url("/"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"))
                )
                .security(List.of(new SecurityRequirement().addList("bearerAuth")))
                ;
    }

    @Bean
    public GroupedOpenApi studentOpenApi() {
        return GroupedOpenApi.builder()
                .group("students")
                .pathsToMatch("/api/v1/students/**")
                .build();
    }

    @Bean
    public GroupedOpenApi enrollmentOpenApi() {
        return GroupedOpenApi.builder()
                .group("enrollments")
                .pathsToMatch("/api/v1/enrollments/**")
                .build();
    }

    @Bean
    public GroupedOpenApi attendanceOpenApi() {
        return GroupedOpenApi.builder()
                .group("attendances")
                .pathsToMatch("/api/v1/attendance/**")
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
    public GroupedOpenApi activityOpenApi() {
        return GroupedOpenApi.builder()
                .group("activities")
                .pathsToMatch("/api/v1/activities/**")
                .build();
    }

    @Bean
    public GroupedOpenApi authOpenApi() {
        return GroupedOpenApi.builder()
                .group("auth")
                .pathsToMatch("/api/v1/auth/**")
                .build();
    }
}
