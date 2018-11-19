package com.github.jacopofar.wikipediacategorygraph;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Triple;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpanders;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public class ShortestPathCalculator {

	public static void main(String[] args) {
		String dbFolder = args[0];
		System.out.println("Initializing the database...");
		//
		GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dbFolder);
		//
		Triple<String, String, Integer> result = getClosestNode(graphDb, "Computers",
				Arrays.asList("Cats|History".split("\\|")));
		System.out.println(result.getLeft() + " " + result.getMiddle() + " " + result.getRight());
	}

	public static Triple<String, String, Integer> getClosestNode(GraphDatabaseService graphDb, String startingNode,
			List<String> endNodes) {
		Triple<String, String, Integer> closestPair = Triple.of(null, null, Integer.MAX_VALUE);

		for (String endNode : endNodes) {
			Triple<String, String, Integer> pairDistance = getShortestPath(graphDb, startingNode, endNode);
			if (pairDistance.getRight() < closestPair.getRight()) {
				closestPair = pairDistance;
			}
		}

		return closestPair;
	}

	public static Triple<String, String, Integer> getShortestPath(GraphDatabaseService graphDb,
			String startingCategoryNode, String endCategoryNode) {
		try (Transaction tx = graphDb.beginTx()) {
			PathFinder<Path> finder = GraphAlgoFactory
					.shortestPath(PathExpanders.forType(DBSchemaProperties.subCategoryOfRel), 30000);
			Node endNode = graphDb.findNodesByLabelAndProperty(DBSchemaProperties.categoryLbl, "name", endCategoryNode)
					.iterator().next();
			Node startNode = graphDb
					.findNodesByLabelAndProperty(DBSchemaProperties.categoryLbl, "name", startingCategoryNode)
					.iterator().next();

			Path path = finder.findSinglePath(startNode, endNode);
			return Triple.of(startNode.getProperty("name").toString(), endNode.getProperty("name").toString(),
					path.length());
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(
					String.format("Something went wrong calculating distances. starting node |%s| and end node |%s|",
							startingCategoryNode, endCategoryNode));
			return Triple.of(null, null, Integer.MAX_VALUE);
		}
	}
}
