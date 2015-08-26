package br.edu.unifei.mestrado.commons.graph.mem;

import br.edu.unifei.mestrado.commons.graph.NodeWrapper;
import br.edu.unifei.mestrado.commons.graph.EdgeWrapper;
import br.edu.unifei.mestrado.commons.graph.db.NodeDB;

public class EdgeMem implements EdgeWrapper {

	private Relationship innerEdge;

	/**
	 * Usado em {@link NodeDB#getEdgesStatic()}
	 */
	EdgeMem() {

	}

	public EdgeMem(Relationship edge) {
		this.innerEdge = edge;
	}

	/**
	 * Usado em {@link NodeMem#getEdgesStatic()}
	 */
	@Override
	public void setInnerEdge(Object innerObject) {
		this.innerEdge = (Relationship)innerObject;
	}

	@Override
	public int getWeight() {
		return innerEdge.getWeight();
	}

	@Override
	public long getId() {
		return innerEdge.getId();
	}

	@Override
	public NodeWrapper getOtherNode(NodeWrapper node) {
		return new NodeMem(innerEdge.getOtherNode(((NodeMem)node).getInnerNode()));
	}

	@Override
	public boolean isEdgeOnCut() {
		return innerEdge.isCut();
	}

	@Override
	public NodeWrapper getStartNode() {
		return new NodeMem(innerEdge.getStartNode());
	}

	@Override
	public NodeWrapper getEndNode() {
		return new NodeMem(innerEdge.getEndNode());
	}
//	
//	public Relationship getInnerObj() {
//		return innerEdge;
//	}
	
	public Relationship getInnerRelationship() {
		return innerEdge;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof EdgeMem) {
			return innerEdge.equals(((EdgeMem)obj).getInnerRelationship());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return innerEdge.hashCode();
	}
	
	@Override
	public String toString() {
		return innerEdge.toString();
	}

	
}
