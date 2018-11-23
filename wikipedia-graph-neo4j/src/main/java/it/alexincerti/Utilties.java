package it.alexincerti;

import java.util.Map;

import org.neo4j.driver.internal.InternalPath;
import org.neo4j.driver.v1.types.Node;

public class Utilties {

	public static Iterable<Node> extractPath(Iterable<Map<String, Object>> shortestPath) {
		Map<String, Object> map = shortestPath.iterator().next();
		InternalPath path = (InternalPath) map.get("p");
		return path.nodes();
	}

}
