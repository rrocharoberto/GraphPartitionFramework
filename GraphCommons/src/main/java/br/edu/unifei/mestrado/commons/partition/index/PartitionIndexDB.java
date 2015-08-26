package br.edu.unifei.mestrado.commons.partition.index;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;

import br.edu.unifei.mestrado.commons.graph.GraphProperties;
import br.edu.unifei.mestrado.commons.graph.NodeWrapper;
import br.edu.unifei.mestrado.commons.graph.db.NodeDB;
import br.edu.unifei.mestrado.commons.iterable.NodeStaticIterable;

/**
 * Keeps the partition index of the nodes, using the DB internal index.
 * 
 * @author roberto
 *
 */
public class PartitionIndexDB implements PartitionIndex {

	private Index<Node> indexPartition = null;

	private Map<Integer, Integer> amountOfNodesPerPartition = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> nodeWeightPerPartition = new HashMap<Integer, Integer>();
	
	public PartitionIndexDB(Index<Node> indexPartition) {
		this.indexPartition = indexPartition;
	}
	
	@Override
	public void initialize(int k) {
		
	}

	@Override
	public void addNodeToSet(int setId, NodeWrapper node) {
		indexPartition.add(((NodeDB) node).getInnerNode(), GraphProperties.PARTITION, setId);
		incrementAmountOfNodes(setId);
		increaseWeight(setId, node.getWeight());
	}
	
	@Override
	public void removeNodeFromSet(int setId, NodeWrapper node) {
		indexPartition.remove(((NodeDB) node).getInnerNode(), GraphProperties.PARTITION, setId);
		decrementAmountOfNodes(setId);
		decreaseWeight(setId, node.getWeight());
	}
	
	@Override
	public Iterable<NodeWrapper> queryNodes(final int value) {
		Iterator<Node> it = indexPartition.query(GraphProperties.PARTITION, value).iterator();
		return new NodeStaticIterable<Node>(it, new NodeDB(null));
	}
	
	@Override
	public int queryAmountOfNodes(int setId) {
		Integer qtd = amountOfNodesPerPartition.get(setId);
		if(qtd == null) {
			return 0;
		}
		return qtd.intValue();
	}
	
	@Override
	public int queryTotalNodeWeightPerPartition(int setId) {
		Integer qtd = nodeWeightPerPartition.get(setId);
		if(qtd == null) {
			return 0;
		}
		return qtd.intValue();
	}
	
	private void incrementAmountOfNodes(int setId) {
		Integer qtd = amountOfNodesPerPartition.get(setId);
		if(qtd == null) {
			qtd = 1;
		} else {
			qtd++;
		}
		amountOfNodesPerPartition.put(setId, qtd);
	}
	
	private void decrementAmountOfNodes(int setId) {
		Integer qtd = amountOfNodesPerPartition.get(setId);
		if(qtd == null) {
			throw new RuntimeException("Erro: não é possível decrementar a quantidade pois não existe nenhum node.");
		} else {
			qtd--;
		}
		amountOfNodesPerPartition.put(setId, qtd);
	}

	private void increaseWeight(int setId, int weight) {
		Integer qtd = nodeWeightPerPartition.get(setId);
		if(qtd == null) {
			qtd = 1;
		} else {
			qtd++;
		}
		nodeWeightPerPartition.put(setId, qtd);
	}
	
	private void decreaseWeight(int setId, int weight) {
		Integer qtd = nodeWeightPerPartition.get(setId);
		if(qtd == null) {
			throw new RuntimeException("Erro: não é possível decrementar a quantidade pois não existe nenhum node.");
		} else {
			qtd--;
		}
		nodeWeightPerPartition.put(setId, qtd);
	}

	@Override
	public void clear() {
		indexPartition.delete();
		amountOfNodesPerPartition.clear();
		nodeWeightPerPartition.clear();
	}
	
	@Override
	public String toString() {
		return amountOfNodesPerPartition.toString();
	}
}
