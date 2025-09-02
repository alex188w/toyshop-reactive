package example.toyshop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * Конфигурация веб-приложения для статических ресурсов.
 * <p>
 * Данный класс настраивает обработку статических файлов,
 * которые находятся в папке {@code uploads/}, расположенной рядом с JAR-файлом.
 * </p>
 *
 * <p>
 * После настройки файлы будут доступны по URL:
 * <pre>
 *   http://localhost:8085/uploads/имя_файла
 * </pre>
 * </p>
 *
 * <p><b>Пример:</b></p>
 * Если в папке {@code uploads/} находится файл {@code image.png},  
 * то он будет доступен по адресу:
 * <pre>
 *   http://localhost:8085/uploads/image.png
 * </pre>
 * </p>
 *
 * @see WebFluxConfigurer
 */
@Configuration
public class WebConfig implements WebFluxConfigurer {

    /**
     * Добавляет обработчик статических ресурсов для папки {@code uploads/}.
     *
     * @param registry реестр обработчиков ресурсов
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/"); // папка рядом с JAR
    }
}
