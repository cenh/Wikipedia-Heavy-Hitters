package it.alexincerti;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import it.alexincerti.service.CalculateClosestMainCategory;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
//		WikiCategoryGraphDBCreator dbCreator = SpringApplication.run(Application.class, args)
//				.getBean(WikiCategoryGraphDBCreator.class);
//		dbCreator.create();
		CalculateClosestMainCategory calculateClosestMainCategory = SpringApplication.run(Application.class, args)
				.getBean(CalculateClosestMainCategory.class);
		calculateClosestMainCategory.calculateShortestDistancesToMainCategories(
				"C:\\Users\\Alex\\Documents\\category.sql",
				"Culture|Geography|History|Humanities|Religion|Science|Sports", "categorymapping.txt", 44l);
	}
}
