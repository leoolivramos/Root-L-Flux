package com.rootl.flux.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Root L Flux — API de Orquestração de Fluxos de Eventos")
                        .version("0.1.0-SNAPSHOT")
                        .description("Plataforma de orquestração, processamento e gerenciamento de fluxos de eventos distribuídos")
                        .contact(new Contact().name("Time Root L Flux"))
                        .license(new License().name("Apache 2.0")));
    }
}
