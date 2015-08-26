package br.edu.unifei.mestrado.kl.util;

import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.unifei.mestrado.commons.partition.TwoWayPartition;

/**
 * Mantém os dois conjuntos de vértices, um de cada partição, ordenados
 * decrescentemente pelo seu valor de D.
 * 
 * Isso facilita o cálculo do ganho, que é feito pegando somente os três
 * vértices com maior valor de D de cada partição
 * 
 * @author roberto
 * 
 */
public class DArray {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private class NodeDTreeSet extends TreeSet<NodeDiff> {
		private static final long serialVersionUID = 1L;
		
		@Override
		public String toString() {
			StringBuffer b = new StringBuffer();
			for (NodeDiff node : this) {
				b.append("N").append(node.getNodeId()).append(":").append(node.getD()).append(", ");
			}
			return b.toString();
		};
	}

	// UTIL: Armazena os vertices ordenados pelo valor de D para cada partição
	private TreeSet<NodeDiff> set1 = new NodeDTreeSet();
	private TreeSet<NodeDiff> set2 = new NodeDTreeSet();

	public void addNode(long nodeId, int set, int d) {
		NodeDiff node = new NodeDiff(nodeId, set, d);
		if (set == TwoWayPartition.PART_1) {
			set1.add(node);
		} else if (set == TwoWayPartition.PART_2) {
			set2.add(node);
		} else {
			throw new RuntimeException("Partição " + set + " inválida para adição do vertice " + node);
		}
	}

	public void removeNode(NodeDiff node) {
		if (node.getPartition() == TwoWayPartition.PART_1) {
			boolean t = set1.remove(node);
			if(!t) {
				System.out.println("não removeu");
			}
		} else if (node.getPartition() == TwoWayPartition.PART_2) {
			boolean t = set2.remove(node);
			if(!t) {
				System.out.println("não removeu");
			}
		} else {
			throw new RuntimeException("Partição " + node.getPartition() + " inválida para remoção do vertice " + node);
		}
	}

	public void removeNode(long nodeId, int partition, int d) {
		removeNode(new NodeDiff(nodeId, partition, d));	
	}

	public Set<NodeDiff> getSet1() {
		return set1.descendingSet();
	}

	public Set<NodeDiff> getSet2() {
		return set2.descendingSet();
	}

	@Override
	public String toString() {
		return "DArray [size1=" + set1.size() + " size2=" + set2.size() + "]";
	}

	public void printDs(int max) {
		logger.debug("Set1 ordenado: {}", new Object() {
			@Override
			public String toString() {
				StringBuffer b = new StringBuffer();
				int i = 0;
				int max = 20;
				for (NodeDiff node : set1) {
					b.append(node.getD()).append(", ");
					i++;
					if (i > max) {
						break;
					}
				}
				return b.toString();
			}
		});
		logger.debug("Set2 ordenado: {}", new Object() {
			@Override
			public String toString() {

				StringBuffer b = new StringBuffer();
				int i = 0;
				int max = 20;
				for (NodeDiff node : set2) {
					b.append(node.getD()).append(", ");
					i++;
					if (i > max) {
						break;
					}
				}
				return b.toString();
			}
		});
	}

////	//UTIL: esse código foi passado para o calculo do ganho
//	private void addNodeToSet(NodeWrapper node, SortedSet<NodeWrapper> set) {
//		// UTIL: armazena os MAX_NUNBER_WITH_GREAT_D vertices com maior valor de D
//		{
////			if (set.size() < MAX_NUNBER_WITH_GREAT_D) {
//				set.add(node);
////			} else {
////				NodeWrapper lowest = set.first();
////				// if (lowest == null) { //não precisa desse if, pq se o size é menor, então já adiciona direto.
////				// set.add(vertex);
////				// } else {
////				if (Math.abs(node.getD()) > Math.abs(lowest.getD())) {
////					set.remove(lowest);
////					set.add(node);
////				}
////				// }
////			}
//		}
//	}

}
