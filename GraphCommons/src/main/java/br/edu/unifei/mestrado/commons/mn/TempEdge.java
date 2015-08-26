package br.edu.unifei.mestrado.commons.mn;


public class TempEdge {

	private long id;
	private int weight;
	private long startNode;
	private long endNode;

	public TempEdge(long id, int weight, long startNode, long endNode) {
		super();
		this.id = id;
		this.weight = weight;
		this.startNode = startNode;
		this.endNode = endNode;
	}

	public long getId() {
		return id;
	}

	public int getWeight() {
		return weight;
	}

	public long getStartNode() {
		return startNode;
	}

	public long getEndNode() {
		return endNode;
	}

	@Override
	public String toString() {
		return "E:" + id + " " + startNode + "-" + endNode + " w:" + weight;
	}

	public void sumWeight(int weightToAdd) {
		this.weight = this.weight + weightToAdd;
	}

}
