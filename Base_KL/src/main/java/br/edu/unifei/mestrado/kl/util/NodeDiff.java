package br.edu.unifei.mestrado.kl.util;

public class NodeDiff implements Comparable<NodeDiff>{

	private long nodeId;
	private int partition;
	private int d;

	public NodeDiff(long nodeId, int partition, int d) {
		super();
		this.nodeId = nodeId;
		this.partition = partition;
		this.d = d;
	}
	
	//UTIL: usado para ordenar os nodes de acordo com o valor de D (usa valor absoluto)
	@Override
	public int compareTo(NodeDiff o) {
		if(nodeId == o.getNodeId()) {
			return 0;
		}
		int d2 = o.getD();
		int diff = 0;
		if (d >= 0 && d2 >= 0) {
			diff = d - d2;
		} else if (d < 0 && d2 < 0) {
			diff = Math.abs(d) - Math.abs(d2);
		} else if (d >= 0) { // UTIL: dá a prioridade para o positivo
			return 1;
		} else {
			return -1;
		}
		if(diff == 0) {
			return (int) (nodeId - o.getNodeId());
		}
		return diff;
	}

	public long getNodeId() {
		return nodeId;
	}

	public void setNodeId(long nodeId) {
		this.nodeId = nodeId;
	}

	public int getPartition() {
		return partition;
	}

	public void setPartition(int partition) {
		this.partition = partition;
	}

	public int getD() {
		return d;
	}

	public void setD(int d) {
		this.d = d;
	}

}
