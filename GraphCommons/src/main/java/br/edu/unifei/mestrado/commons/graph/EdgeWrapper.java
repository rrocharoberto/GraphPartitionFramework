package br.edu.unifei.mestrado.commons.graph;


public interface EdgeWrapper {

	public int getWeight();

	public long getId();

	public NodeWrapper getOtherNode(NodeWrapper node);

	public boolean isEdgeOnCut();

	public NodeWrapper getStartNode();

	public NodeWrapper getEndNode();
	
	void setInnerEdge(Object innerObject);
}
