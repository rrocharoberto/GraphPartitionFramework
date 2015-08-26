package br.edu.unifei.mestrado.greedy;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.unifei.mestrado.commons.graph.NodeWrapper;

/**
 * Armazena os valores de N para cada vértice de um set. <br>
 * Armazena .<br>
 * Armazena e manipula os vértices da fronteira com os valores de diff em um bucket,
 * indexado pelo valor do diff, que estão ligados ao vértices internos desse set. <br>
 * 
 * @author roberto
 * 
 */
public class SubSet {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private int id;

	/**
	 * Quantidade de arestas internas de cada node da fronteira. id_node | N value
	 */
	private ValueMap NMap = new ValueMap();

	/**
	 * Valor do diff para cada node da fronteira. Diff = Externo - Interno.
	 */
	private DiffMap diffMap = new DiffMap();

	// UTIL: não usa pq já guarda direto o diff, baseado no calculo de externo e interno
	// /**
	// * Quantidade de arestas externas para cada node da fronteira.
	// */
	// private ValueMap extEdges = new ValueMap();

	public SubSet(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}

	public Integer getMinVal() {
		return diffMap.getMinDiff();
	}

	private void removeNodeWithDiff(int diff, long node) {
		diffMap.removeNodeWithDiff(diff, node);
	}

	private void setDiff(int newDiff, Integer oldDiff, Long nodeId) {
		if (oldDiff == null) {
			diffMap.insertDiffForNode(newDiff, nodeId);
		} else {
			diffMap.updateDiffForNode(newDiff, oldDiff, nodeId);
		}
	}

	public Long getAnyNodeWithDiff(int diff) {
		return diffMap.getNodesWithDiff(diff).iterator().next();
	}

	/**
	 * Retrieves the N value of the vertex.
	 * 
	 * @see {@link NArray#getNArray(int, NodeWrapper, int)}
	 * 
	 * @param set
	 * @param node
	 * @return
	 */
	public int getN(Long nodeId) {
		return NMap.getValue(nodeId);
	}

	public void incrementN(Long nodeId) {
		NMap.incrementN(nodeId);
	}

	public void printDiff() {
		logger.debug("Diffs for set: " + getId() + ": {}", diffMap);
	}

	public void updateDiff(Collection<SubSet> subSets, Collection<Long> affectedNeighbors) {
		SubSet baseSet_P = this;
		logger.debug("Atualizando diff para " + baseSet_P + " qtd nodes: " + affectedNeighbors.size());

		// for (NodeWrapper node : frontierNodes) {
		// baseSet_P.setExt(node, 0);// inicializa o ext com zero // TODO: colocar junto com o for debaixo
		// }
		for (Long nodeId : affectedNeighbors) {
			Integer oldDiff = baseSet_P.diffMap.getDiffOfNode(nodeId);
			int extSum = 0;
			int intSum = 0;
			for (SubSet set_J : subSets) {// loop com todos os sets, para saber se conta interno ou externo
				if (set_J.getId() != baseSet_P.getId()) {
					extSum += set_J.getN(nodeId);// acumula os N externos
				} else {
					intSum += baseSet_P.getN(nodeId);// acumula os N internos
				}
			}
			// não usa pq já guarda direto o diff, baseado no calculo de externo e interno
			// baseSet_P.setExt(node, extSum);
			int diff = extSum - intSum;

			// UTIL: precisa fazer um update no diff do node 2, para ele não ficar duplicado na estrutura do
			// diff.

			baseSet_P.setDiff(diff, oldDiff, nodeId); // salva o diff para o nodes em relação ao set
		}
		this.printDiff();
	}

	public void removeNodeFromBucket(Long nodeId) {
		Integer diff = diffMap.getDiffOfNode(nodeId);
		if (diff != null) {
			this.removeNodeWithDiff(diff, nodeId);
		}
	}

	@Override
	public String toString() {
		return "Diff: " + diffMap.getDiffList();
	}
	// public int getExt(NodeWrapper node) {
	// return extEdges.getValue(node);
	// }
	//
	// public void setExt(NodeWrapper node, int valor) {
	// extEdges.setValue(node, valor);
	// }

}
