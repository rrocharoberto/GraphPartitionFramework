package br.edu.unifei.mestrado.commons.graph.db;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Relationship;

import br.edu.unifei.mestrado.commons.graph.EdgeWrapper;
import br.edu.unifei.mestrado.commons.graph.GraphProperties;
import br.edu.unifei.mestrado.commons.graph.NodeWrapper;
import br.edu.unifei.mestrado.commons.iterable.EdgeIterable;
import br.edu.unifei.mestrado.commons.iterable.EdgeStaticIterable;
import br.edu.unifei.mestrado.commons.partition.AbstractPartition;
import br.edu.unifei.mestrado.commons.partition.index.PartitionIndexDB;

public class NodeDB implements NodeWrapper {

	private Node innerNode;

	/**
	 * Usado em {@link GraphDB#getAllNodesStatic()} e {@link PartitionIndexDB#queryNodes(int)}.
	 */
	NodeDB() {

	}
	
	public NodeDB(Node node) {
		this.innerNode = node;
	}
	
	/**
	 * Usado em {@link NodeDB#getEdgesStatic()}
	 */
	void setInnerNode(Node node) {
		this.innerNode = node;
	}

	@Override
	public int getD() {
		try {
			Integer d = (Integer) innerNode.getProperty(GraphProperties.D);
			assert d != null : "Propriedade " + GraphProperties.D + " não encontrada para Node: "
					+ innerNode.getProperty(GraphProperties.D);
			return d;
		} catch (NotFoundException e) {
			return 0;
		}
	}

	@Override
	public int getDegree() {
		try {
			Integer degree = (Integer) innerNode.getProperty(GraphProperties.Degree);
			assert degree != null : "Propriedade " + GraphProperties.Degree + " não encontrada para Node: "
					+ innerNode.getProperty(GraphProperties.Degree);
			return degree;
		} catch (NotFoundException e) {
			return 0;
		}
	}

	@Override
	public long getId() {
		Long id = (Long) innerNode.getProperty(GraphProperties.ID);
		assert id != null : "Propriedade " + GraphProperties.ID + " não encontrada para Node: "
				+ innerNode.getProperty(GraphProperties.ID);

		return id;
	}

	@Override
	public int getPartition() {
		Integer partition = (Integer) innerNode.getProperty(GraphProperties.PARTITION);

		// UTIL: aqui pode aceitar partição -1, indicando que o nó ainda não foi escolhido
		if (partition == null || partition < AbstractPartition.NO_PARTITION || partition > AbstractPartition.PART_N) {
			throw new RuntimeException("Partição inválida: " + partition + " para nó: " + getId());
		}

		assert partition != null : "Propriedade " + GraphProperties.PARTITION + " não encontrada para Node: "
				+ innerNode.getProperty(GraphProperties.ID);

		return partition;
	}

	@Override
	public Iterable<EdgeWrapper> getEdges() {
		return new EdgeIterable<Relationship, EdgeDB>(innerNode.getRelationships().iterator(), Relationship.class, EdgeDB.class);
	}
	
	@Override
	public Iterable<EdgeWrapper> getEdgesStatic() {
		return new EdgeStaticIterable<Relationship>(innerNode.getRelationships().iterator(), new EdgeDB());
	}

	@Override
	public int getWeight() {
		
		int peso = NodeWrapper.DEFAULT_WEIGHT; //UTIL: se não existir a propriedade peso, retorna 1.

		if(innerNode.hasProperty(GraphProperties.WEIGHT)) {
			peso = (Integer) innerNode.getProperty(GraphProperties.WEIGHT);
		}
//		if (peso == null) {
//			throw new UnsupportedOperationException("Propriedade " + GraphProperties.WEIGHT
//					+ " não encontrada para Node: " + innerNode.getProperty(GraphProperties.ID));
//		}
		return peso;
	}

	@Override
	public boolean hasProperty(String key) {
		return innerNode.hasProperty(key);
	}

	@Override
	public boolean isLocked() {
		Boolean locked = (Boolean) innerNode.getProperty(GraphProperties.LOCKED);
		if (locked == null) {
			throw new UnsupportedOperationException("Propriedade " + GraphProperties.LOCKED
					+ " não encontrada para Node: " + innerNode.getProperty(GraphProperties.ID));
		}
		return locked;
	}

	@Override
	public void lock() {
		innerNode.setProperty(GraphProperties.LOCKED, true);
	}

	@Override
	public void setD(Integer d) {
		innerNode.setProperty(GraphProperties.D, d);
	}

	@Override
	public void setDegree(Integer degree) {
		innerNode.setProperty(GraphProperties.Degree, degree);
	}

	@Override
	public void setPartition(Integer partition) {
		if (partition < AbstractPartition.PART_1 || partition > AbstractPartition.PART_N) {
			throw new RuntimeException("Partição inválida: " + partition);
		}
		innerNode.setProperty(GraphProperties.PARTITION, partition);
	}

	@Override
	public void resetPartition() {
		innerNode.setProperty(GraphProperties.PARTITION, AbstractPartition.NO_PARTITION);
	}

	@Override
	public void setWeight(Integer weight) {
		innerNode.setProperty(GraphProperties.WEIGHT, weight);
	}

	@Override
	public void unlock() {
		innerNode.setProperty(GraphProperties.LOCKED, false);
	}

	public Node getInnerNode() {
		return innerNode;
	}

	@Override
	public long getInsideOf() {
		Long idInside = (Long) innerNode.getProperty(GraphProperties.INSIDEOF);
		if (idInside == null) {
			throw new UnsupportedOperationException("Propriedade " + GraphProperties.INSIDEOF
					+ " não encontrada para Node: " + innerNode.getProperty(GraphProperties.ID));
		}
		return idInside;
	}

	@Override
	public void setInsideOf(Long coarsedNodeId) {
		innerNode.setProperty(GraphProperties.INSIDEOF, coarsedNodeId);
	}

	@Override
	public boolean hasInsideOf() {
		return innerNode.hasProperty(GraphProperties.INSIDEOF);
	}
	
	@Override
	public void resetInsideOf() {
		innerNode.removeProperty(GraphProperties.INSIDEOF);
	}

	@Override
	public boolean isCoarsed() {
		if (innerNode.hasProperty(GraphProperties.COARSED)) {
			Boolean coarsed = (Boolean) innerNode.getProperty(GraphProperties.COARSED);
			if (coarsed == null) {
				return false;
			}
			return coarsed;
		}
		return false;
	}

	@Override
	public void setCoarsed(boolean coarsed) {
		innerNode.setProperty(GraphProperties.COARSED, coarsed);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof NodeDB) {
			return innerNode.equals(((NodeDB) obj).getInnerNode());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return innerNode.hashCode();
	}

	@Override
	public String toString() {
		return "" + innerNode.getProperty(GraphProperties.ID);
	}
	
	@Override
	public void setInnerNode(Object innerObject) {
		this.innerNode = (Node)innerObject;
	}
}
