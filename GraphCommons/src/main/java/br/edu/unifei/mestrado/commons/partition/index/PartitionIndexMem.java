package br.edu.unifei.mestrado.commons.partition.index;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.unifei.mestrado.commons.graph.NodeWrapper;
import br.edu.unifei.mestrado.commons.graph.mem.Node;
import br.edu.unifei.mestrado.commons.graph.mem.NodeMem;
import br.edu.unifei.mestrado.commons.iterable.NodeStaticIterable;
import br.edu.unifei.mestrado.commons.partition.NodeCache;

/**
 * Keeps the partition index of the nodes, using java.util.HashMap index.
 * 
 * @author roberto
 *
 */
public class PartitionIndexMem implements PartitionIndex {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Estrutura de dados para armazenar os nós de cada partição.
	 */
	private Map<Integer, NodeCache> sets = new HashMap<Integer, NodeCache>();

	public PartitionIndexMem() {
	}

	@Override
	public void initialize(int k) {
		for (int id = 1; id <= k; id++) {
			sets.put(id, new NodeCache(id));
		}
	}

	@Override
	public void addNodeToSet(int setId, NodeWrapper node) {
		NodeCache set = sets.get(setId);
		if (set != null) {
			set.addNode(((NodeMem)node).getInnerNode());
		} else {
			logger.error("ERRO: SET "+setId+" ERRADO PARA ADICIONAR O VERTICE " + node);
		}
	}

	@Override
	public void removeNodeFromSet(int setId, NodeWrapper node) {
		NodeCache set = sets.get(setId);
		if (set != null) {
			set.removeNode(((NodeMem)node).getInnerNode());
		} else {
			logger.error("ERRO: SET ERRADO PARA REMOVER O VERTICE " + node);
		}
	}

	@Override
	public Iterable<NodeWrapper> queryNodes(int setId) {
		NodeCache set = sets.get(setId);
		if (set != null) {
			return new NodeStaticIterable<Node>(set.getNodes().iterator(), new NodeMem(null));
		} else {
			throw new RuntimeException("ERRO: SET " + setId + " ERRADO PARA QUERY");
		}
	}
	
	@Override
	public int queryAmountOfNodes(int setId) {
		NodeCache set = sets.get(setId);
		if (set != null) {
			return set.getSizeNodes();
		} else {
			throw new RuntimeException("ERRO: SET " + setId + " ERRADO PARA QUERY");
		}
	}
	
	@Override
	public int queryTotalNodeWeightPerPartition(int setId) {
		NodeCache set = sets.get(setId);
		if (set != null) {
			return set.getNodeWeight();
		} else {
			throw new RuntimeException("ERRO: SET " + setId + " ERRADO PARA QUERY");
		}
	}
	
	@Override
	public void clear() {
		sets.clear();
	}
	
	@Override
	public String toString() {
		return sets.toString();
	}
}
