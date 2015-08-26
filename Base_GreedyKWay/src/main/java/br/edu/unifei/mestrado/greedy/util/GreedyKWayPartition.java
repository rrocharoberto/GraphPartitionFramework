package br.edu.unifei.mestrado.greedy.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.unifei.mestrado.commons.graph.NodeWrapper;
import br.edu.unifei.mestrado.commons.partition.AbstractPartition;
import br.edu.unifei.mestrado.commons.partition.index.CutIndex;
import br.edu.unifei.mestrado.commons.partition.index.PartitionIndex;
import br.edu.unifei.mestrado.greedy.SubSet;


//o código da classe SetHelper foi colocado nessa classe
public class GreedyKWayPartition extends AbstractPartition {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Estrutura de dados PARTITION.
	 * 
	 *id_set, SubSet
	 */
	private Map<Integer, SubSet> sets = new HashMap<Integer, SubSet>();
	
	private Frontier frontier = null;
	
	public GreedyKWayPartition(Integer level, int k, PartitionIndex partitionIdx, CutIndex cutIdx) {
		super(level, k, partitionIdx, cutIdx);
		for (int set = 1; set <= k; set++) {
			//inicializa os sets
			sets.put(set, new SubSet(set));
		}
		
		frontier = new Frontier();
	}

	public SubSet getSetById(Integer setId) {
		return sets.get(setId);
	}

	public Map<Integer, SubSet> getSets() {
		return sets;
	}

	public List<NodeWrapper> includeNodesToFrontier(NodeWrapper baseNode) {
		return frontier.includeNodesToFrontier(baseNode);
	}

	public int getFrontierCount() {
		return frontier.getNodes().size();
	}

	/**
	 * Atualiza o valor de Diff para cada set e vértice da fronteira.
	 * 
	 * @param k
	 */
	public void updateDiffFor(List<Long> affectedNeighbors) {//TODO: esse método pode estar lento, pois faz todos os sets vezes todos os nodes
		long d = System.currentTimeMillis();
		for (SubSet set : this.getSets().values()) { // sets
			
			// TODO_OK: verificar a performance
			//UTIL: é atualizar os diffs dos vizinhos afetados para cada subset
			set.updateDiff(this.getSets().values(), affectedNeighbors);
			// setResult.updateDiff(set, justIncluded);
			// TODO: atualizar o diff para os outros nós também, que podem ter sido influenciados pelos
			// justIncluded
		}
		d = System.currentTimeMillis() - d;
		logger.debug("Tempo gasto para PartitionGreedy.updateDiffFor: {}.", d);
	}

	public void removeFromFrontier(Long nodeId) {
		frontier.remove(nodeId);
	}

	public void printNForSet(final SubSet setTmp) {
		logger.debug("N    for " + setTmp + ": {}", new Object() {
			@Override
			public String toString() {
				StringBuffer b = new StringBuffer();
				for (NodeWrapper node : frontier.getNodes()) {
					b.append(node.getId() + "=" + setTmp.getN(node.getId()) + ", ");
				}
				return b.toString();
			}
		});
	}

	public void printFrontier() {
		logger.debug("{}", frontier);
	}
	
	//no SetHelper aqui estava os prints de N, diff, minVal, int e ext
}
