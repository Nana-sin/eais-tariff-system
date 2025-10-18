package com.ttp.evaluation.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Конфигурация OpenAPI/Swagger с JWT авторизацией
 */
@Configuration
public class OpenApiConfiguration {

    @Value("${spring.application.name:ttp-evaluation-system}")
    private String applicationName;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("TTP Evaluation System API")
                        .version("1.0.0")
                        .description("""
                                # Единая Аналитическая Информационная Система для оценки эффективности мер ТТП
                                
                                ## Основные возможности:
                                - 🔍 **Классификация товаров** по кодам ТН ВЭД ЕАЭС
                                - ⚖️ **Оценка эффективности** 6 типов мер торгово-тарифного регулирования
                                - 📊 **Интеграция** с Trade Map, UN Comtrade, Росстат, WTO
                                - 🤖 **ML-assisted классификация** (планируется)
                                
                                ## Авторизация:
                                1. Зарегистрируйтесь через `/api/v1/auth/register`
                                2. Получите JWT токен через `/api/v1/auth/login`
                                3. Используйте токен в заголовке: `Authorization: Bearer <token>`
                                
                                ## Роли пользователей:
                                - **ENTERPRISE** - Создает запросы на классификацию и оценку мер
                                - **EXPERT** - Проверяет и одобряет классификацию
                                - **ADMIN** - Полный доступ ко всем функциям
                                """)
                        .contact(new Contact()
                                .name("TTP Development Team")
                                .email("support@ttp-evaluation.ru")
                                .url("https://github.com/your-org/ttp-evaluation-system"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development"),
                        new Server().url("https://api-dev.ttp-evaluation.ru").description("Development"),
                        new Server().url("https://api.ttp-evaluation.ru").description("Production")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Введите JWT токен полученный при авторизации")));
    }
}