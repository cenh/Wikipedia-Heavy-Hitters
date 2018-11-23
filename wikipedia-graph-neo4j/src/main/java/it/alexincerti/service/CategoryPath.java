package it.alexincerti.service;

public class CategoryPath {

	private String startCategory;
	private String endCategory;
	private Long length;

	public String getStartCategory() {
		return startCategory;
	}

	public void setStartCategory(String startCategory) {
		this.startCategory = startCategory;
	}

	public String getEndCategory() {
		return endCategory;
	}

	public void setEndCategory(String endCategory) {
		this.endCategory = endCategory;
	}

	public Long getLength() {
		return length;
	}

	public void setLength(Long length) {
		this.length = length;
	}

//	private Category startCategory;
//	private Category endNCategory;
//
//	private List<Category> nodes;
//
//	public Category getStartCategory() {
//		return startCategory;
//	}
//
//	public void setStartCategory(Category startCategory) {
//		this.startCategory = startCategory;
//	}
//
//	public Category getEndNCategory() {
//		return endNCategory;
//	}
//
//	public void setEndCategory(Category endNCategory) {
//		this.endNCategory = endNCategory;
//	}
//
//	public List<Category> getNodes() {
//		if (nodes == null) {
//			nodes = new ArrayList<>();
//		}
//		return nodes;
//	}
//
//	public void setNodes(List<Category> nodes) {
//		this.nodes = nodes;
//	}
//
//	public void addNodeToPath(Category category) {
//		getNodes().add(category);
//	}

}
