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

public class ShortestPathCalculator {

	public static void main(String[] args) {
		String dbFolder = "DummyGraph";
		System.out.println("Initializing the database...");
		//
		GraphDatabaseService graphDb = GraphDBCreator.getGraphDatabase("C:\\Users\\Alex\\Desktop\\testdb.db");
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
			if (pairDistance.getRight() <= closestPair.getRight()) {
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
			Node endNode = graphDb.findNodes(DBSchemaProperties.categoryLbl, "name", endCategoryNode).next();
			Node startNode = graphDb.findNodes(DBSchemaProperties.categoryLbl, "name", startingCategoryNode).next();

			Path path = finder.findSinglePath(startNode, endNode);
			String pathStr = "";
			if (path != null) {
				for (Node node : path.nodes()) {
					pathStr += node.getProperty("name") + " -> ";
				}
			} else {
				pathStr = endNode.getProperty("name").toString();
			}
			return Triple.of(startNode.getProperty("name").toString(), pathStr,
					path != null ? path.length() : Integer.MAX_VALUE);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(
					String.format("Something went wrong calculating distances. starting node |%s| and end node |%s|",
							startingCategoryNode, endCategoryNode));
			return Triple.of(null, null, Integer.MAX_VALUE);
		}
	}
}
