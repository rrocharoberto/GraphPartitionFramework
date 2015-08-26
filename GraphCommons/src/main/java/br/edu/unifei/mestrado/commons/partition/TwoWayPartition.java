package br.edu.unifei.mestrado.commons.partition;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.unifei.mestrado.commons.graph.EdgeWrapper;
import br.edu.unifei.mestrado.commons.graph.GraphWrapper;
import br.edu.unifei.mestrado.commons.graph.NodeWrapper;
import br.edu.unifei.mestrado.commons.graph.TransactionControl;
import br.edu.unifei.mestrado.commons.graph.TransactionInterface;
import br.edu.unifei.mestrado.commons.partition.index.CutIndex;
import br.edu.unifei.mestrado.commons.partition.index.PartitionIndex;

public class TwoWayPartition extends AbstractPartition {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public TwoWayPartition(Integer level, PartitionIndex partitionIdx, CutIndex cutIdx) {
		super(level, AbstractPartition.TWO_WAY, partitionIdx, cutIdx);
	}

	private interface PartitionChooser {
		public int getNextPartition(NodeWrapper node);
	}

	private class FixedChooser implements PartitionChooser {

		private int id = 0;
		
		public FixedChooser() {
			logger.info("Gerando particao fixa.");
		}

		@Override
		public int getNextPartition(NodeWrapper node) {
			id++;
			return (id % 2) + 1;
		}
	}

	private class ArbitraryChooser implements PartitionChooser {

		private Random rand;

		public ArbitraryChooser(long seed) {
			this.rand = new Random(seed);
			logger.info("New random partition with seed: {}.", seed);
		}
		
		// UTIL: variaveis para balancear o particionamento baseado nos pesos dos nós
		private int weightSet1 = 0;
		private int weightSet2 = 0;
		private int partition;
		
		@Override
		public int getNextPartition(NodeWrapper node) {
			
			int weight = node.getWeight();//usado para fazer o balanceamento dos pesos dos nodes
			if (weightSet1 + weight > weightSet2) {
				partition = AbstractPartition.PART_2;
			} else if (weightSet2 + weight > weightSet1) {
				partition = AbstractPartition.PART_1;
			} else {
				partition = Math.abs(rand.nextInt() % 2) + 1;
			}
			if (partition == AbstractPartition.PART_1) {
				weightSet1 += weight;
			} else {
				weightSet2 += weight;
			}
			return partition;
		}
	}
	
	/**
	 * Divide os nodes, tal que cada parte contém aproximadamente metade dos pesos dos nodes do grafo.
	 */
	private class BalancedWeightChooser implements PartitionChooser {

		// UTIL: variaveis para balancear o particionamento baseado nos pesos dos nós
		private int weightSet1 = 0;
		private int weightSet2 = 0;
		private int partition;
		
		private FixedChooser internalChooser = new FixedChooser();

		@Override
		public int getNextPartition(NodeWrapper node) {
			
			int weight = node.getWeight();//usado para fazer o balanceamento dos pesos dos nodes
			if (weightSet1 + weight > weightSet2) {
				partition = AbstractPartition.PART_2;
			} else if (weightSet2 + weight > weightSet1) {
				partition = AbstractPartition.PART_1;
			} else {
				partition = internalChooser.getNextPartition(node);
			}
			if (partition == AbstractPartition.PART_1) {
				weightSet1 += weight;
			} else {
				weightSet2 += weight;
			}
			return partition;
		}
	}
	/**
	 * Creates a fixed partition
	 * @param graph
	 * @return the size of nodes in partition A.
	 */
	public int initialFixedBalancedPartition(GraphWrapper graph) {
		return initialPartition(graph, new BalancedWeightChooser());
	}

	/**
	 * Creates an arbitrary partition
	 * @param it Iteration to be used as seed
	 * @param graph
	 * @return the size of nodes in partition A.
	 */
	public int initialArbitraryPartition(int it, GraphWrapper graph) {
		//TODO: Verificar: sempre está pegando os mesmos nós
		return initialPartition(graph, new ArbitraryChooser((long) (it * Math.PI)));
		// TODO: verificar a melhor semente pra colocar aqui.
		//return initialPartition(graph, new ArbitraryChooser(new Random(it)));
	}

	/**
	 * Creates a partition using the chooser for partition.
	 * @param graph
	 * @param chooser
	 * @return the size of nodes in partition A.
	 */
	private int initialPartition(GraphWrapper graph, PartitionChooser chooser) {
		int sizePartitionA = 0;
		Iterable<NodeWrapper> allNodes = graph.getAllNodesStatic();
		TransactionControl transaction = new TransactionControl(graph);
		try{
			transaction.beginTransaction();
			
			for (NodeWrapper node : allNodes) {
//				if (node.hasProperty(GraphProperties.ID)) { // se não for referenceNode
					int partition = chooser.getNextPartition(node);
					node.setPartition(partition);
					super.insertNodeToIndex(partition, node);
					if(partition == AbstractPartition.PART_1) {
						sizePartitionA++;
						transaction.intermediateCommit();
					}
//				}
			}
		} finally {
			transaction.commit();
		}
		//TODO_OK: atualizar o corte, pois agora não faz mais automático.
//		super.createEdgeCut(graph.getAllEdgesStatic(), graph, graph);
		//UTIL: coloquei somente para calcular, para não precisar armazenar o corte.
		super.calculateEdgeCut(graph.getAllEdgesStatic());
		logger.info("Corte initial gerado: " + super.getCutWeight());
		return sizePartitionA;
	}

	/**
	 * Cria um particionamento utilizando somente os nodes que possuem arestas em {@link edgesOnCut}.
	 * @param edgesOnCut Arestas que estão no corte.
	 */
	public void generateBoundaryPartition(Iterable<EdgeWrapper> edges, TransactionInterface transactionIf) {//update diagram
		
		TransactionControl tc = new TransactionControl(transactionIf);
		tc.beginTransaction();

		for (EdgeWrapper edge : edges) {
			if (edge.isEdgeOnCut()) { //TODO_OK: tirar esse if, pois as arestas já estão no corte. Voltei pois o multinível não usa mais partições intermediárias
				NodeWrapper startNode = edge.getStartNode();
				NodeWrapper endNode = edge.getEndNode();
				// inclui somente os nodes que estiverem no corte
				
				super.insertNodeToIndex(startNode.getPartition(), startNode);
				super.insertNodeToIndex(endNode.getPartition(), endNode);
				super.insertEdgeToCut(edge);
				
				tc.intermediateCommit();
//				super.insertNodeToSetWithEdgeOnCut(startNode, startNode.getPartition(), edge);
//				super.insertNodeToSetWithEdgeOnCut(endNode, endNode.getPartition(), edge);
			}
		}
		tc.commit();
	}

	/**
	 * Exchange the pairs of the initial partition
	 * @param node
	 */
	public void changeNodePartition(NodeWrapper node) {
		int oldPartition = node.getPartition();
		int newPartition = -1;
		if (AbstractPartition.PART_1 == oldPartition) {
			newPartition = AbstractPartition.PART_2;
		} else {
			newPartition = AbstractPartition.PART_1;
		}
		if(logger.isTraceEnabled()) {
			logger.trace("Changing node {} from partition {} to {}", new Object[] { node.getId(),
				oldPartition, newPartition });
		}
		node.setPartition(newPartition);
		super.updateNodePartition(node, oldPartition, newPartition);
	}
	
	/**
	 * Retorna o iterable de nodes correspondente aos nodes da partição 1.
	 * 
	 * @return
	 */
	public Iterable<NodeWrapper> getAllNodesSet1() {
		return super.queryNodesFromSet(AbstractPartition.PART_1);
	}

	/**
	 * Retorna o iterable de nodes correspondente aos nodes da partição 2.
	 * 
	 * @return
	 */
	public Iterable<NodeWrapper> getAllNodesSet2() {
		return super.queryNodesFromSet(AbstractPartition.PART_2);
	}
	
//	/**
//	 * Retorna o iterable de nodes correspondente aos vértices as duas partições.
//	 */
//	public Iterable<NodeWrapper> getAllNodes() {
//		final Iterator<NodeWrapper> part1 = super.queryNodes(GraphProperties.PARTITION, AbstractPartition.PART_1).iterator();
//		final Iterator<NodeWrapper> part2 = super.queryNodes(GraphProperties.PARTITION, AbstractPartition.PART_2).iterator();
//		return new Iterable<NodeWrapper>() {
//			@Override
//			public Iterator<NodeWrapper> iterator() {
//				return new Iterator<NodeWrapper>() {
//					@Override
//					public NodeWrapper next() {
//						if (part1.hasNext()) {
//							return part1.next();
//						}
//						return part2.next();
//					}
//					@Override
//					public boolean hasNext() {
//						if (part1.hasNext()) {
//							return true;
//						}
//						return part2.hasNext();
//					}
//					@Override
//					public void remove() {
//					}
//				};
//			}
//		};
//	}
}
