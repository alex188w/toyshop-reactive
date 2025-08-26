package example.toyshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс приложения "Toyshop".
 * <p>
 * Запускает Spring Boot контекст и инициализирует все компоненты
 * (контроллеры, сервисы, репозитории) для работы приложения.
 * <p>
 * Используется реактивный стек Spring WebFlux и Spring Data R2DBC для работы
 * с PostgreSQL.
 */
@SpringBootApplication
public class ToyshopApplication {

	/**
	 * Точка входа в приложение.
	 *
	 * @param args аргументы командной строки (не обязательны)
	 */
	public static void main(String[] args) {
		SpringApplication.run(ToyshopApplication.class, args);
	}
}
