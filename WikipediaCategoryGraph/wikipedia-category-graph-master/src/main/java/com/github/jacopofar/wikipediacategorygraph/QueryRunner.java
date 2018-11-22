package com.github.jacopofar.wikipediacategorygraph;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;

public class QueryRunner {
	public static void main(String[] args) {

		GraphDatabaseService graphDb = GraphDBCreator.getGraphDatabase(args[0]);

		Map<String, Object> params = new HashMap<>();
		String query = "MATCH (n)--(other) "//
				+ "WITH n, count(other) as degree "//
				+ "WHERE n.name CONTAINS 'Categories_requiring_diffusion' AND degree > 4000 " + "DELETE n";
		Result result = graphDb.execute(query, params);
		result.forEachRemaining(r -> {
			System.out.println(r.get("n.name") + " " + r.get("count(other)"));
		});
	}
}
