package com.github.jacopofar.wikipediacategorygraph;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Label;

public class DBSchemaProperties {
	public final static Label articleLbl = DynamicLabel.label("Article");
	public final static Label categoryLbl = DynamicLabel.label("Category");
	public final static DynamicRelationshipType inCategoryRel = DynamicRelationshipType.withName("IN_CATEGORY");
	public final static DynamicRelationshipType subCategoryOfRel = DynamicRelationshipType.withName("SUBCATEGORY_OF");
}
