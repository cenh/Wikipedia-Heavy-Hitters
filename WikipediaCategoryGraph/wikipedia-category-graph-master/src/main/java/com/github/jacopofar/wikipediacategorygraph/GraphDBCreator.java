package com.github.jacopofar.wikipediacategorygraph;

import java.io.File;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public class GraphDBCreator {
	public static GraphDatabaseService getGraphDatabase(String filePath) {
		File db = new File("C:\\Users\\Alex\\Desktop\\testdb.db");
		return new GraphDatabaseFactory().newEmbeddedDatabase(db);
	}
}