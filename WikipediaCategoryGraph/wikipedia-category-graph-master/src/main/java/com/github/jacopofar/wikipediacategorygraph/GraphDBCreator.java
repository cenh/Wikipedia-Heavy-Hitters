package com.github.jacopofar.wikipediacategorygraph;

import java.io.File;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public class GraphDBCreator {
	public static GraphDatabaseService getGraphDatabase(String filePath) {
		File db = new File(filePath);
		return new GraphDatabaseFactory().newEmbeddedDatabase(db);
	}
}
