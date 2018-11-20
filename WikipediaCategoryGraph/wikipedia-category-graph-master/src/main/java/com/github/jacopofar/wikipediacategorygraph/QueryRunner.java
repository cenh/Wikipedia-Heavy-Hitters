package com.github.jacopofar.wikipediacategorygraph;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;

public class QueryRunner {
	public static void main(String[] args) {

		GraphDatabaseService graphDb = GraphDBCreator.getGraphDatabase(args[0]);

		Map<String, Object> params = new HashMap<>();
		String query = "MATCH (n)" + "WHERE n.name CONTAINS 'categories'" + "RETURN n.name";
		Result result = graphDb.execute(query, params);
		result.forEachRemaining(r -> {
			System.out.println(r.get("n.name"));
		});
	}
}
