package it.alexincerti.repository;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpanders;
import org.neo4j.graphdb.Transaction;
import org.springframework.stereotype.Repository;

import it.alexincerti.models.Category;
import it.alexincerti.models.DBSchemaProperties;

@Repository
public class CategoryRepositoryEmbedded {

	private GraphDatabaseService graphDb;

	public CategoryRepositoryEmbedded() {
		// graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(new
		// File("C:\\Neo4j\\data\\databases\\graph.db"));
	}

	public List<Category> getShortestPath(String startingCategoryNode, String endCategoryNode) {
		try (Transaction tx = graphDb.beginTx()) {
			PathFinder<Path> finder = GraphAlgoFactory.shortestPath(PathExpanders
					.forType(DynamicRelationshipType.withName(DBSchemaProperties.SUBCATEGORY_RELATIONSHIP)), 30000);
			Node endNode = graphDb.findNodes(DynamicLabel.label("Category"), "name", endCategoryNode).next();
			Node startNode = graphDb.findNodes(DynamicLabel.label("Category"), "name", startingCategoryNode).next();

			Path path = finder.findSinglePath(startNode, endNode);
			List<Category> nodes = null;
			if (path != null) {
				nodes = new ArrayList<>();
				for (Node node : path.nodes()) {
					nodes.add(new Category(node.getProperty("name").toString()));
				}
			}
			return nodes;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
