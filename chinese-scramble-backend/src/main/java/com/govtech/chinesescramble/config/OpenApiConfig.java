package com.govtech.chinesescramble.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for API documentation
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI chineseScrambleOpenAPI() {
        Server localServer = new Server()
            .url("http://localhost:8080")
            .description("Local development server");

        Server productionServer = new Server()
            .url("https://api.chinesescramble.govtech.com")
            .description("Production server");

        Contact contact = new Contact()
            .name("GovTech Development Team")
            .email("dev@govtech.com")
            .url("https://govtech.com");

        License license = new License()
            .name("Apache 2.0")
            .url("https://www.apache.org/licenses/LICENSE-2.0.html");

        Info info = new Info()
            .title("Chinese Word Scramble Game API")
            .version("1.0.0")
            .description("RESTful API for Chinese Word Scramble Game - A gamified Chinese language learning platform featuring idiom scramble and sentence crafting games with progressive difficulty levels, achievements, and leaderboards.")
            .contact(contact)
            .license(license)
            .termsOfService("https://govtech.com/terms");

        return new OpenAPI()
            .info(info)
            .servers(List.of(localServer, productionServer));
    }
}
