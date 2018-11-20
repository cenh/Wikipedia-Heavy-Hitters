/*
 * Copyright 2014 Jacopo farina.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This file has been modified, by removing the articles from the graphs.
 *
 *
 *
 */
package com.github.jacopofar.wikipediacategorygraph;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.Schema;

/**
 * Creates a Neo4j graph representing the categories and page titles listed in
 * the SQL dump of the corresponding tables. Both articles and categories are
 * represented as nodes with the "name" and "id" properties, and there are two
 * relationship types: SUBCATEGORY_OF is from a category to one that contains it
 * IN_CATEGORY is from an article node to the containing category
 */
public class CreateCategoryGraph {
	public final static Label categoryLbl = DynamicLabel.label("Category");
	public final static DynamicRelationshipType inCategoryRel = DynamicRelationshipType.withName("IN_CATEGORY");
	public final static DynamicRelationshipType subCategoryOfRel = DynamicRelationshipType.withName("SUBCATEGORY_OF");

	public static void main(String args[]) throws FileNotFoundException, IOException {

		if (args.length != 4) {
			System.err.println(
					"wrong usage, expecting 4 arguments: category.sql categorylinks.sql graphfolder pagefile.sql");
		}
		String categoryFile = args[0];
		String categoryLinksFile = args[1];
		String dbFolder = args[2];
		String pageFile = args[3];
		System.out.println("Initializing the database...");

		GraphDatabaseService graphDb = GraphDBCreator.getGraphDatabase(dbFolder);

		try (Transaction tx = graphDb.beginTx()) {
			Schema schema = graphDb.schema();
			// articles and categories have both a name and an ID
			schema.indexFor(categoryLbl).on("name").create();
			schema.indexFor(categoryLbl).on("ID").create();
			tx.success();
		}

		createCategoryNodes(categoryFile, graphDb);
//		createIDCategoryNameFromPageLink(pageFile, "pageid_categoryname.txt", graphDb);
//
//		System.out.println("waiting up to 2 minutes for the names and ID indexes to be online...");
//		try (Transaction tx = graphDb.beginTx()) {
//			Schema schema = graphDb.schema();
//			schema.awaitIndexesOnline(2, TimeUnit.MINUTES);
//		}
//		createRelationships(categoryLinksFile, "", graphDb);
		graphDb.shutdown();
	}

	private static void createRelationships(String categoryLinksFile, String pageidCategoryNameFile,
			GraphDatabaseService graphDb) throws FileNotFoundException, IOException {
		final AtomicInteger doneCats = new AtomicInteger(0);

		long lastTime = System.currentTimeMillis();

		System.out.println("Loading the subcategory edges");
		try (BufferedReader br = new BufferedReader(new FileReader(categoryLinksFile))) {
			final ConcurrentHashMap<Integer, Long> articleNodes = new ConcurrentHashMap<>(5000);
			br.lines().forEach(line -> {
				// pick the lines containing inserts, not comments or DDL
				if (!line.startsWith("INSERT INTO ") || line.length() < 2)
					return;

				try (Transaction tx = graphDb.beginTx()) {
					// split values in single inserts, in the form
					// (cl_from,cl_to,sortkey,...)
					// where the first value is the ID of the sub-category or article,
					// the second is the name of the containing category
					// and the third is the uppercase normalized name of the article or category
					Arrays.stream(line.split("'\\),\\((?=[0-9])"))

							.filter(v -> !v.startsWith("INSERT INTO")).forEach(edge -> {
								if (edge.endsWith("','file")) {
									return;
								}
								int ID = Integer.parseInt(edge.split(",")[0]);
								String catname = edge.replaceAll("[0-9]+,'", "").replaceAll("',.+", "");
								ResourceIterator<Node> matches = graphDb.findNodes(categoryLbl, "name", catname);
								if (!matches.hasNext()) {
									matches.close();
									return;
								}
								// System.out.println(edge);

								Node container = matches.next();
								matches.close();
								if (edge.endsWith("'subcat")) {
									// if the subcategory was stored, is already indexed
									matches = graphDb.findNodes(categoryLbl, "ID", ID);
									if (!matches.hasNext()) {
										matches.close();
										return;
									}
									doneCats.incrementAndGet();

									matches.next().createRelationshipTo(container, subCategoryOfRel);
									matches.close();
									if (doneCats.incrementAndGet() % 100000 == 0)
										System.out.println(" - parsed " + doneCats.get() + " categories so far)");
									return;
								}
								if (doneCats.incrementAndGet() % 100000 == 0)
									System.out.println(" - parsed " + doneCats.get() + " categories so far");
							});
					tx.success();
				}
			});
		}
		System.out.println("Loaded " + doneCats.get() + " categories) in "
				+ (System.currentTimeMillis() - lastTime) / 1000 + " seconds");
	}

	private static void createCategoryNodes(String categoryFile, GraphDatabaseService graphDb)
			throws FileNotFoundException, IOException {
		System.out.println("Loading the categories and their IDs...");
		long lastTime = System.currentTimeMillis();
		final AtomicInteger done = new AtomicInteger(0);
		// read the file line by line
		try (BufferedReader br = new BufferedReader(new FileReader(categoryFile))) {
			br.lines().parallel().forEach(line -> {
				// while((line=br.readLine())!=null){
				// pick the lines containing inserts, not comments or DDL
				if (!line.startsWith("INSERT INTO ") || line.length() < 2)
					return;

				try (Transaction tx = graphDb.beginTx()) {
					// split values in single inserts, in the form
					// (2,'Unprintworthy_redirects',1102027,15,0)
					// where the first values are the ID and the category name (the others the
					// number of articles, subcategories and files)
					// the awkward regular expressions matches the numbers between values so strange
					// article titles do not break the splitting
					// Arrays.stream(line.split("[0-9]\\),\\((?=[0-9])")).forEach(v->System.out.println("
					// "+v));
					Arrays.stream(line.split("[0-9]\\),\\((?=[0-9])")).filter(v -> !v.startsWith("INSERT INTO"))
							.forEach(category -> {
								String name = category.replaceAll(".+[0-9],'", "").replaceAll("',[0-9]+.+", "");
								int ID = Integer.parseInt(category.replaceAll(",'.+", ""));
								if (isInternalCategory(name))
									return;

								Node cat = graphDb.createNode(categoryLbl);
								cat.setProperty("name", name);
								cat.setProperty("ID", ID);
								// System.out.println(name+" --> "+ID);
								if (done.incrementAndGet() % 100000 == 0)
									System.out.println(" - loaded " + done.get() + " categories");
							});
					tx.success();
				}
			});
			System.out.println("Loaded " + done.get() + " categories in "
					+ (System.currentTimeMillis() - lastTime) / 1000 + " seconds");
		}
	}

	private static void createIDCategoryNameFromPageLink(String pageLinkFile, String outputFile,
			GraphDatabaseService graphDb) throws FileNotFoundException, IOException {
		Pattern pattern = Pattern.compile("\\(([^()]*)\\)");
		// read the file line by line

		final AtomicInteger analyzedPages = new AtomicInteger(0);
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, false))) {
			try (BufferedReader br = new BufferedReader(new FileReader(pageLinkFile))) {
				br.lines().forEach(line -> {
					// while((line=br.readLine())!=null){
					// pick the lines containing inserts, not comments or DDL
					if (!line.startsWith("INSERT INTO ") || line.length() < 2)
						return;

					try (Transaction tx = graphDb.beginTx()) {
						Matcher matcher = pattern.matcher(line);
						while (matcher.find()) {
							String page = matcher.group();
							page = page.replace("(", "");
							page = page.replace(")", "");
							String name = page.split(",")[2];
							int ID = Integer.parseInt(page.split(",")[0].replaceAll(",'.+", ""));
							ResourceIterator<Node> matches = graphDb.findNodes(categoryLbl, "name", name);
							if (!matches.hasNext()) {
								matches.close();
								return;
							}
							try {
								writer.write(ID + ": " + name);
								writer.newLine();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							if (analyzedPages.incrementAndGet() % 100000 == 0)
								System.out.println(" - parsed " + analyzedPages.get() + " pages so far)");
						}
						tx.success();
					}
				});
			}
		}
	}

//			System.out.println("Loaded " + done.get() + " categories in "
//					+ (System.currentTimeMillis() - lastTime) / 1000 + " seconds");

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
