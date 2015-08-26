package br.edu.unifei.mestrado.greedy.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.unifei.mestrado.commons.graph.NodeWrapper;
import br.edu.unifei.mestrado.commons.graph.EdgeWrapper;
import br.edu.unifei.mestrado.commons.partition.AbstractPartition;

public class Frontier {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private Map<Long, NodeWrapper> nodes = new HashMap<Long, NodeWrapper>();

	// TODO: o frontier está muito parecido com a estrutura do diffMap

	public void remove(Long nodeId) {
		nodes.remove(nodeId);
	}

	public Collection<NodeWrapper> getNodes() {
		return nodes.values();
	}

	/**
	 * Inclui os nós visinhos a baseNode que ainda não estão em nenhuma partição.
	 * 
	 * @param baseNode
	 * @return
	 */
	public List<NodeWrapper> includeNodesToFrontier(NodeWrapper baseNode) {
		final List<NodeWrapper> justIncluded = new ArrayList<NodeWrapper>();
		// UTIL: inclui os nodes adjacentes a baseNode, pq eles se tornaram fronteira
		for (EdgeWrapper edge : baseNode.getEdges()) {
			NodeWrapper other = edge.getOtherNode(baseNode);
			if (!other.isLocked()) { // se a outra ponta nao foi usada
				if (other.getPartition() == AbstractPartition.NO_PARTITION) {
					long idNode = other.getId();
					if (!nodes.containsKey(idNode)) {
						nodes.put(idNode, other);
						justIncluded.add(other);
					}
				} else {
					throw new RuntimeException("Node "+other.getId()+" is not locked but it is already in partition " + other.getPartition());
				}
			}
		}
		if (justIncluded.size() > 0) {
			logger.debug("Frontier size: {} . New nodes to frontier: {}", nodes.size(), justIncluded.size());
			logger.trace(": {}", new Object() {
				@Override
				public String toString() {
					StringBuffer b = new StringBuffer();
					for (NodeWrapper node : justIncluded) {
						b.append(node.getId() + ", ");
					}
					return b.toString();
				}
			});
		}
		return justIncluded;
	}

	@Override
	public String toString() {
		StringBuffer tx = new StringBuffer("Frontier (" + nodes.size() + "): ");
		if (logger.isDebugEnabled()) {
			int i = 0;
			for (Long id : nodes.keySet()) {
				tx.append(id).append(",");
				i++;
				if (i == 20) {
					break;
				}
			}
		}
		return tx.toString();
	}
}
