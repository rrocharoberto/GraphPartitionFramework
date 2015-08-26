package br.edu.unifei.mestrado.commons.graph.mem;

import br.edu.unifei.mestrado.commons.graph.EdgeWrapper;
import br.edu.unifei.mestrado.commons.graph.NodeWrapper;
import br.edu.unifei.mestrado.commons.iterable.EdgeIterable;
import br.edu.unifei.mestrado.commons.iterable.EdgeStaticIterable;
import br.edu.unifei.mestrado.commons.partition.AbstractPartition;
import br.edu.unifei.mestrado.commons.partition.index.PartitionIndexMem;

public class NodeMem implements NodeWrapper {

	private Node innerNode;

	/**
	 * Usado em {@link GraphMem#getAllNodesStatic()} e {@link PartitionIndexMem#queryNodes(int)}}
	 */
	NodeMem() {
	
	}
	
	public NodeMem(Node node) {
		this.innerNode = node;
	}

	@Override
	public Iterable<EdgeWrapper> getEdges() {
		return new EdgeIterable<Relationship, EdgeMem>(innerNode.getEdges().iterator(), Relationship.class, EdgeMem.class);
	}
	
	@Override
	public Iterable<EdgeWrapper> getEdgesStatic() {
		return new EdgeStaticIterable<Relationship>(innerNode.getEdges().iterator(), new EdgeMem());
	}

	@Override
	public void setWeight(Integer weight) {
		innerNode.setWeight(weight);
	}

	@Override
	public void setPartition(Integer partition) {
		if(partition == -1) { //não pode certar. ele já inicia com -1
			throw new RuntimeException("Valor de partição inválido " + partition + " para vertice: " + innerNode.getId());
		}
		innerNode.setPartition(partition);
	}
	
	@Override
	public void resetPartition() {
		innerNode.setPartition(AbstractPartition.NO_PARTITION);
	}

	@Override
	public void lock() {
		innerNode.lock();
	}

	@Override
	public void unlock() {
		innerNode.unLock();
	}

	@Override
	public void setD(Integer d) {
		innerNode.setD(d);
	}

	@Override
	public void setDegree(Integer degree) {
		innerNode.setDegree(degree);
	}

	@Override
	public int getWeight() {
		return innerNode.getWeight();
	}

	@Override
	public int getPartition() {
		return innerNode.getPartition();
	}

	@Override
	public boolean isLocked() {
		return innerNode.isLocked();
	}

	@Override
	public long getId() {
		return innerNode.getId();
	}

	@Override
	public boolean hasProperty(String key) {
		return true;
	}

	public Node getInnerNode() {
		return innerNode;
	}
	
	@Override
	public int getD() {
		return innerNode.getD();
	}

	@Override
	public int getDegree() {
		return innerNode.getDegree();
	}

	@Override
	public long getInsideOf() {
		return innerNode.getInsideOf();
	}
	
	@Override
	public void setInsideOf(Long coarsedNodeId) {
		innerNode.setInsideOf(coarsedNodeId);
	}
	
	@Override
	public boolean hasInsideOf() {
		return innerNode.getInsideOf() != null;
	}
	
	@Override
	public void resetInsideOf() {
		innerNode.setInsideOf(null);
	}
	
	@Override
	public boolean isCoarsed() {
		return innerNode.isCoarsed();
	}
	
	@Override
	public void setCoarsed(boolean coarsed) {
		innerNode.setCoarsed(coarsed);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof NodeMem) {
			return innerNode.equals(((NodeMem)obj).getInnerNode());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return innerNode.hashCode();
	}
	
	@Override
	public String toString() {
		return innerNode.toString();
	}
	
	@Override
	public void setInnerNode(Object innerObject) {
		this.innerNode = (Node)innerObject;
	}
}
