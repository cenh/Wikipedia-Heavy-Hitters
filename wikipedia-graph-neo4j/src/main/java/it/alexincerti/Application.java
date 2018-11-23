package it.alexincerti;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import it.alexincerti.service.WikiCategoryGraphDBCreator;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		WikiCategoryGraphDBCreator dbCreator = SpringApplication.run(Application.class, args)
				.getBean(WikiCategoryGraphDBCreator.class);
		dbCreator.create();
	}
}
