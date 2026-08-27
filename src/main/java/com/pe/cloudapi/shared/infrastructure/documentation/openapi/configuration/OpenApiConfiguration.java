package com.pe.cloudapi.shared.infrastructure.documentation.openapi.configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI cloudApiOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Acoustic and Thermal Comfort Cloud API")
                        .description("""
                                Cloud API for acoustic and thermal comfort monitoring \
                                in coworking spaces. Receives per-minute aggregates from \
                                the Edge layer and serves analytics to the Web and Mobile \
                                applications.""")
                        .version("v1.0.0")
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local")))
                .externalDocs(new ExternalDocumentation()
                        .description("Project repository")
                        .url("https://github.com/Grupo03-IOT/cloud-api"));
    }
}
