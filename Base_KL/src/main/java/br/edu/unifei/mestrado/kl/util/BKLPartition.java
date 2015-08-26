package br.edu.unifei.mestrado.kl.util;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.unifei.mestrado.commons.graph.EdgeWrapper;
import br.edu.unifei.mestrado.commons.graph.NodeWrapper;
import br.edu.unifei.mestrado.commons.partition.index.CutIndex;
import br.edu.unifei.mestrado.commons.partition.index.PartitionIndex;

public class BKLPartition extends KLPartition {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public BKLPartition(Integer level, PartitionIndex partitionIdx, CutIndex cutIdx) {
		super(level, partitionIdx, cutIdx);
	}

	/**
	 * Inclui os nós visinhos a baseNode que ainda não estão em nenhuma partição. <br>
	 * E inclui os vizinhos de I e J na partição de I e J respectivamente <br>
	 * 
	 * Usado somente pelo BKL.
	 * 
	 * @param baseNode
	 * @return
	 */
	public List<NodeWrapper> addNeighborsToFrontier(NodeWrapper baseNode) { //TODO: revisar esta logica
		final List<NodeWrapper> justIncluded = new ArrayList<NodeWrapper>();
		// UTIL: inclui os vértices adjacentes a baseNode, pq eles se tornaram fronteira
		for (EdgeWrapper aresta : baseNode.getEdges()) {
			NodeWrapper other = aresta.getOtherNode(baseNode);
			if (!other.isLocked()) { // se a outra ponta nao foi usada
//				if (other.getPartition() == AbstractPartition.NO_PARTITION ) { //TODO_OK: não precisa disto, pois no refinamento, todos os nodes já estão em uma partição
					justIncluded.add(other);

					// UTIL: já adiciona o vertice na partição correta.
//					index.insertToIndex(other, GraphProperties.PARTITION, baseNode.getPartition(), aresta);
//UTIL: aqui não precisa mexer na partição. -> precisa sim, para incluir os nodes que se tornaram fronteira
//					super.insertNodeToSetWithEdgeOnCut(other, baseNode.getPartition(), aresta);
					super.insertNodeToIndex(baseNode.getPartition(), other);
//				}
			}
		}
		if (justIncluded.size() > 0) {
			logger.debug("Novos vertices na fronteira (" + justIncluded.size() + "): ");
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

}
