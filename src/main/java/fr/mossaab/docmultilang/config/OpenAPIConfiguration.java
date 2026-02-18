package fr.mossaab.docmultilang.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурационный класс OpenAPIConfiguration для настройки OpenAPI и Swagger.
 */
@Configuration
public class OpenAPIConfiguration {

    @Value("${app.server.base-url:http://localhost:8080}")
    private String baseUrl;

    private static final String TITLE_APP = "Doc Multi-lang API";
    private static final String VERSION = "0.0.1-SNAPSHOT";
    private static final String CONTACT_NAME = "Denis";
    private static final String CONTACT_EMAIL = "ladchenkovden@gmail.com";

    private static final String LICENSE_NAME = "MIT";
    private static final String LICENSE_URL = "https://opensource.org/licenses/MIT";

    @Bean
    public OpenAPI customOpenAPI() {
        String description = String.format("""
                Документация API:\n
                🔹 Swagger UI: %s/swagger-ui/index.html\n
                🔹 OpenAPI JSON: %s/v3/api-docs\n
                """, baseUrl, baseUrl);

        return new OpenAPI()
                .info(new Info()
                        .title(TITLE_APP)
                        .version(VERSION)
                        .description(description)
                        .contact(new Contact()
                                .name(CONTACT_NAME)
                                .email(CONTACT_EMAIL))
                .license(new License()
                    .name(LICENSE_NAME)
                    .url(LICENSE_URL)));
    }
}
