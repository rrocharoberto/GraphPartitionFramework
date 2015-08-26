package br.edu.unifei.mestrado.commons.graph.mem;

import java.util.HashMap;
import java.util.Map;

public class Node {

	private long id;
	private int weight;
	private Map<Long, Relationship> edges = new HashMap<Long, Relationship>();
	private Long insideOf; //indica que o this esta contraido dentro de insideOf
	private int partition;
	private boolean locked;
	private int d;
	private boolean coarsed;
	private int degree;

	public Node(long newId, int weight) {
		this.id = newId;
		this.weight = weight;
		insideOf = null;
		partition = -1;
		locked = false;
		coarsed = false;
	}

	public void addEdge(Relationship aresta) {
		edges.put(aresta.getId(), aresta);
	}

	public Node getOtherNode(Relationship a) {
		Node v = this;
		if (v.getId() == a.getStartNode().getId()) { //se for o proprio v
			return a.getEndNode(); //pega a outra ponta
		}
		return a.getStartNode();
	}

	public boolean isAdjacent(Node v2) {
		Node v1 = this;
		if (v1 == v2) {
			System.err.println("isAdjacent com mesmo vertice");
			return false;
		}
		for (Relationship aresta : v1.getEdges()) {
			Node other = v1.getOtherNode(aresta);
			if (other.getId() == v2.getId()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Retorna a aresta que liga os 2 vertices, se existir.
	 */
	public Relationship getArestaLinking(Node vb) {
		Node va = this;
		Node aux1 = va;
		Node aux2 = vb;
		if(va.getEdgesSize() > vb.getEdgesSize()) {
			aux1 = vb;
			aux2 = va;
		}
		for (Relationship aresta : aux1.getEdges()) {
			Node other = aux1.getOtherNode(aresta);
			if (other.getId() == aux2.getId()) {
				return aresta;
			}
		}
		//logger.warn("N�o existe aresta ligando " + va.getId() + " a " + vb.getId());
		return null;
	}
	
	public int getEdgesSize() {
		return edges.size();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public Iterable<Relationship> getEdges() {
		return edges.values();
	}

	public Long getInsideOf() {
		return insideOf;
	}

	public void setInsideOf(Long insideOf) {
		this.insideOf = insideOf;
	}

	public int getPartition() {
		return partition;
	}

	public void setPartition(int partition) {
		this.partition = partition;
	}

	public boolean isLocked() {
		return locked;
	}

	public void lock() {
		this.locked = true;
	}
	
	public void unLock() {
		this.locked = false;
	}
	
	public int getD() {
		return d;
	}
	
	public void setD(int d) {
		this.d = d;
	}
	
	public int getDegree() {
		return degree;
	}

	public void setDegree(int degree) {
		this.degree = degree;
	}

	public boolean isCoarsed() {
		return coarsed;
	}
	
	public void setCoarsed(boolean coarsed) {
		this.coarsed = coarsed;
	}

	@Override
	public String toString() {
		return "V" + id;// + ":" + getD();
	}
	
	@Override
	public int hashCode() {
		return (int) id;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Node) {
			return ((Node)obj).getId() == id;
		}
		return false;
	}
	
}