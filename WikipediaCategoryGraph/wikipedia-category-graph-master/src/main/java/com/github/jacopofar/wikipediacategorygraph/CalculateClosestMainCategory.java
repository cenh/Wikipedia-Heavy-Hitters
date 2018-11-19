package com.github.jacopofar.wikipediacategorygraph;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.tuple.Triple;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public class CalculateClosestMainCategory {
	public static void main(String[] args) throws FileNotFoundException, IOException {
		if (args.length < 4) {
			System.err.println(
					"Provide 4 arguments: db_folder, category_sql_file_path, main_categories(one string separated by |), outputfile_path");
			return;
		}
		//
		String dbFolder = args[0];
		System.out.println("Initializing the database...");
		//
		GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dbFolder);
		//
		final AtomicInteger done = new AtomicInteger(0);
		final AtomicInteger notMappedCategories = new AtomicInteger(0);

		List<String> mainCategories = Arrays.asList(args[2].split("\\|"));

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(args[3], true))) {
			// read the file line by line
			try (BufferedReader br = new BufferedReader(new FileReader(args[1]))) {
				br.lines().parallel().forEach(line -> {
					// while((line=br.readLine())!=null){
					// pick the lines containing inserts, not comments or DDL
					if (!line.startsWith("INSERT INTO ") || line.length() < 2)
						return;

					Arrays.stream(line.split("[0-9]\\),\\((?=[0-9])")).filter(v -> !v.startsWith("INSERT INTO"))
							.forEach(category -> {
								done.getAndIncrement();
								String name = category.replaceAll(".+[0-9],'", "").replaceAll("',[0-9]+.+", "");
								if (isInternalCategory(name))
									return;
								//
								Triple<String, String, Integer> closestNode = ShortestPathCalculator
										.getClosestNode(graphDb, name, mainCategories);
								if (closestNode == null || closestNode.getLeft() == null
										|| closestNode.getMiddle() == null
										|| closestNode.getRight() == Integer.MAX_VALUE) {
									if (notMappedCategories.get() > 100 && notMappedCategories.get() % 100 == 0) {
										System.out.println(
												" - was not able to map " + done.get() + " categories so far)");
									}
									return;
								}
								//
								System.out.println(String.format("Starting node |%s| and end node |%s| distance: |%d|",
										closestNode.getLeft(), closestNode.getMiddle(), closestNode.getRight()));
								//
								try {
									System.out.println("Writing to file..");
									writer.append(closestNode.getLeft() + " -> " + closestNode.getMiddle() + " : "
											+ closestNode.getRight());
									writer.newLine();
									writer.flush();
								} catch (IOException e) {
									System.err.println("Unable to write to file");
									e.printStackTrace();
								}

								if (done.get() % 100 == 0) {
									System.out.println(
											" - calculated distances for " + done.get() + " categories so far)");
								}
							});
				});
			}
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
