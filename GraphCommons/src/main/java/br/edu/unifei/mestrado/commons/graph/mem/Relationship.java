package br.edu.unifei.mestrado.commons.graph.mem;

import br.edu.unifei.mestrado.commons.partition.AbstractPartition;

public class Relationship {

	private Long id;
	private int weight;
	private Node startNode;
	private Node endNode;

	public Relationship(long newId, int newWeight, Node newVa, Node newVb) {
		id = newId;
		weight = newWeight;
		startNode = newVa;
		endNode = newVb;
	}
	
	public Node getOtherNode(Node v) {
		if (v.getId() == startNode.getId()) { //se for o proprio v
			return endNode; //pega a outra ponta
		}
		return startNode;
	}


	public long getId() {
		return id;
	}

	public int getWeight() {
		return weight;
	}

	public Node getStartNode() {
		return startNode;
	}

	public Node getEndNode() {
		return endNode;
	}

	public boolean isCut() {
		int p1 = startNode.getPartition();
		if(p1 == AbstractPartition.NO_PARTITION) {
			return false;
		}
		int p2 = endNode.getPartition();
		if(p2 == AbstractPartition.NO_PARTITION) {
			return false;
		}
		return p1 != p2;
	}
	
	@Override
	public String toString() {
		return "E" + id + " " + startNode.getId() + "-" + endNode.getId() + " w:" + weight;
	}
	
	@Override
	public int hashCode() {
		return id.intValue();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Relationship) {
			return ((Relationship)obj).getId() == id;
		}
		return false;
	}
	
}
