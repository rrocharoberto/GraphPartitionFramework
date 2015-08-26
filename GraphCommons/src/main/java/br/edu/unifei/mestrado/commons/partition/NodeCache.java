package br.edu.unifei.mestrado.commons.partition;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import br.edu.unifei.mestrado.commons.graph.mem.Node;

/**
 * Armazena os nós internos de um set. <br>
 * Armazena os nós da fronteira que estão ligados ao nós internos desse set. <br>
 * 
 * Classe usada dentro de PartitionIndexMem
 * 
 * @author roberto
 * 
 */
public class NodeCache {

	private int id;

	/**
	 * id_node | node
	 * 
	 * Lista de nodes que já entraram no set
	 */
	private Map<Long, Node> mapNodes = new HashMap<Long, Node>();
	
	private int nodeWeight = 0;

	public NodeCache(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void addNode(Node node) {
		mapNodes.put(node.getId(), node);
		nodeWeight += node.getWeight();
	}

	public Collection<Node> getNodes() {
		return mapNodes.values();
	}

	public int getSizeNodes() {
		return mapNodes.size();
	}

	public void removeNode(Node node) {
		mapNodes.remove(node.getId());
		nodeWeight -= node.getWeight();
	}

	public boolean isEmpty() {
		return mapNodes.isEmpty();
	}
	
	public int getNodeWeight() {
		return nodeWeight;
	}

	@Override
	public String toString() {
		return "SET_" + id + " N: " + mapNodes.size();
	}

}
