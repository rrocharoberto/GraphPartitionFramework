package br.edu.unifei.mestrado.commons.graph.db;

import org.neo4j.graphdb.Relationship;

import br.edu.unifei.mestrado.commons.graph.GraphProperties;
import br.edu.unifei.mestrado.commons.graph.NodeWrapper;
import br.edu.unifei.mestrado.commons.graph.EdgeWrapper;
import br.edu.unifei.mestrado.commons.partition.AbstractPartition;

public class EdgeDB implements EdgeWrapper {

	private Relationship innerEdge;
	
	/**
	 * Usado em {@link NodeDB#getEdgesStatic()}
	 */
	EdgeDB() {

	}
	
	public EdgeDB(Relationship edge) {
		this.innerEdge = edge;
	}

	/**
	 * Usado em {@link NodeDB#getEdgesStatic()}
	 */
	public void setInnerEdge(Object innerObject) {
		this.innerEdge = (Relationship)innerObject;
	}

	@Override
	public NodeWrapper getEndNode() {
		return new NodeDB(innerEdge.getEndNode());
	}

	@Override
	public long getId() {
		Long id = (Long) innerEdge.getProperty(GraphProperties.ID);
		if (id == null) {
			throw new UnsupportedOperationException("Propriedade " + GraphProperties.ID
					+ " não encontrada para aresta: " + innerEdge.getProperty(GraphProperties.ID));
		}
		return id;
	}

	@Override
	public NodeWrapper getOtherNode(NodeWrapper node) {
		return new NodeDB(innerEdge.getOtherNode(((NodeDB) node).getInnerNode()));
	}

	@Override
	public NodeWrapper getStartNode() {
		return new NodeDB(innerEdge.getStartNode());
	}

	@Override
	public int getWeight() {
		Integer peso = (Integer) innerEdge.getProperty(GraphProperties.WEIGHT);
		if (peso == null) {
			throw new UnsupportedOperationException("Propriedade " + GraphProperties.WEIGHT
					+ " não encontrada para aresta: " + innerEdge.getProperty(GraphProperties.ID));
		}
		return peso;
	}

	@Override
	public boolean isEdgeOnCut() {
		int p1 = (Integer)innerEdge.getStartNode().getProperty(GraphProperties.PARTITION);
		if(p1 == AbstractPartition.NO_PARTITION) {
			return false;
		}
		int p2 = (Integer)innerEdge.getEndNode().getProperty(GraphProperties.PARTITION);
		return p1 != p2;
	}

	public Relationship getInnerEdge() {
		return innerEdge;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof EdgeDB) {
			return innerEdge.equals(((EdgeDB)obj).getInnerEdge());
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
