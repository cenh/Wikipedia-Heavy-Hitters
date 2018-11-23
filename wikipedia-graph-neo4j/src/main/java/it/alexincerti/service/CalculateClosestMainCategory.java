package it.alexincerti.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CalculateClosestMainCategory {
	Logger logger = LoggerFactory.getLogger(CalculateClosestMainCategory.class);

	@Autowired
	private ShortestPathCalculator shortestPathCalculator;

	public ShortestPathCalculator getShortestPathCalculator() {
		return shortestPathCalculator;
	}

	public void calculateShortestDistancesToMainCategories(String categoryFile, String mainCategoriesString,
			String outputFile, Long statementToStartFrom) {
		try {
			final AtomicInteger done = new AtomicInteger(0);
			final AtomicInteger notMappedCategories = new AtomicInteger(0);

			List<String> mainCategories = Arrays.asList(mainCategoriesString.split("\\|"));

			try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, true))) {
				// read the file line by line
				try (BufferedReader br = new BufferedReader(new FileReader(categoryFile))) {
					br.lines().parallel().forEach(line -> {
						// while((line=br.readLine())!=null){
						// pick the lines containing inserts, not comments or DDL
						if (!line.startsWith("INSERT INTO ") || line.length() < 2)
							return;

						Arrays.stream(line.split("[0-9]\\),\\((?=[0-9])")).filter(v -> !v.startsWith("INSERT INTO"))
								.forEach(statement -> {
									if (done.getAndIncrement() + 1 < statementToStartFrom) {
										return;
									}
									String categoryName = statement.replaceAll("[0-9]+,'", "").replaceAll("',.+", "");
									if (isInternalCategory(categoryName))
										return;
									//
									logger.debug(String.format("Analyzing paths for |%s| (statement |%d|)...",
											categoryName, done.get()));
									CategoryPath closestNode = getShortestPathCalculator().getClosestNode(categoryName,
											mainCategories);
									if (closestNode == null) {
										if (notMappedCategories.get() > 100 && notMappedCategories.get() % 100 == 0) {
											logger.debug(
													" - was not able to map " + done.get() + " categories so far)");
										}
										return;
									}
									//
									String summary = String.format("|%s| -> |%s|: %d", closestNode.getStartCategory(),
											closestNode.getEndCategory(), closestNode.getLength());
									logger.debug(summary);
									try {
										writer.append(summary);
										writer.newLine();
										writer.flush();
									} catch (IOException e) {
										logger.error("Unable to write to file");
										e.printStackTrace();
									}

									if (done.get() % 100 == 0) {
										logger.debug(
												" - calculated distances for " + done.get() + " categories so far)");
									}
								});
					});
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static boolean isInternalCategory(String name) {
		if (name.startsWith("Wikipedia_articles_"))
			return true;
		if (name.startsWith("Suspected_Wikipedia_sockpuppets"))
			return true;
		if (name.startsWith("Articles_with_"))
			return true;
		if (name.startsWith("Redirects_"))
			return true;
		if (name.startsWith("WikiProject_"))
			return true;
		if (name.startsWith("Articles_needing_"))
			return true;
		return name.startsWith("Wikipedians_");
	}
}
