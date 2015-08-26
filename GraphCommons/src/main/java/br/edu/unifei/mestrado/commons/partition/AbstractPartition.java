package br.edu.unifei.mestrado.commons.partition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.unifei.mestrado.commons.graph.EdgeWrapper;
import br.edu.unifei.mestrado.commons.graph.NodeWrapper;
import br.edu.unifei.mestrado.commons.graph.PartitionKeeper;
import br.edu.unifei.mestrado.commons.graph.TransactionControl;
import br.edu.unifei.mestrado.commons.graph.TransactionInterface;
import br.edu.unifei.mestrado.commons.partition.index.CutIndex;
import br.edu.unifei.mestrado.commons.partition.index.PartitionIndex;

/**
 * Almost a delegate to AbstractPartitionIndex methods.
 * 
 * @author roberto
 * 
 */
//TODO: verificar quais métodos precisam ser final, para evitar override nas
//subclasses.
public abstract class AbstractPartition {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public static final int NO_PARTITION = -1;

	public static int PART_1 = 1;
	public static int PART_2 = 2;
	public static int PART_N = PART_2;

	public static final int TWO_WAY = 2;

	public static final int FIRST_PART = 1;

	private Integer level;
	private int k;

	private PartitionIndex partitionIndex;
	private CutIndex cutIndex;

	public AbstractPartition(Integer level, int k, PartitionIndex partitionIdx, CutIndex cutIdx) {
		this.level = level;
		this.k = k;
		this.partitionIndex = partitionIdx;
		this.cutIndex = cutIdx;

		this.partitionIndex.initialize(k); 
	}

	/**
	 * 
	 * @param level
	 * @param k
	 * @param partitionIdx
	 * @param cutIdx
	 * @param best true if this creating is used by {@link #BestPartition}
	 */
	protected AbstractPartition(Integer level, int k, PartitionIndex partitionIdx, CutIndex cutIdx, boolean best) {
		this.level = level;
		this.k = k;
		this.partitionIndex = partitionIdx;
		this.cutIndex = cutIdx;

		if(!best) {
			this.partitionIndex.initialize(k);
		}
	}

	public int getK() {
		return k;
	}

	// **************** metodos relacionados aos nodes ****************
	/**
	 * Insere o node no indice interno e atualizada o corte
	 * 
	 * @param node
	 * @param newSet
	 */
	public void insertNodeToIndex(int setId, NodeWrapper node) {
		partitionIndex.addNodeToSet(setId, node);
		// updateCut(node);
	}

	/**
	 * Remove o node do indice interno e atualizada o corte
	 * 
	 * @param node
	 * @param setNovo
	 */
	public void removeNodeFromIndex(int setId, NodeWrapper node) {
		partitionIndex.removeNodeFromSet(setId, node);
		// updateCut(node);
	}

//	/**
//	 * Insere o node no indice interno, e ja sabe qual a aresta que está no
//	 * corte.<br>
//	 * Usado pelo BKL.
//	 * 
//	 * @param node
//	 * @param newSet
//	 * @param edgeOnCut
//	 */
//	public void insertNodeToSetWithEdgeOnCut(NodeWrapper node, int newSet, EdgeWrapper edgeOnCut) {//update diagram removed
//		partitionIndex.addNodeToSet(newSet, node);
//		cutIndex.addEdgeToCut(edgeOnCut);
//	}

	/**
	 * Atualiza o node no indice interno
	 * 
	 * @param node
	 * @param oldSet
	 * @param newSet
	 */
	public void updateNodePartition(NodeWrapper node, int oldSet, int newSet) {
		partitionIndex.removeNodeFromSet(oldSet, node);
		partitionIndex.addNodeToSet(newSet, node);
		// updateCut(node);
	}

	public Iterable<NodeWrapper> queryNodesFromSet(int setId) {
		return partitionIndex.queryNodes(setId);
	}

	public int getAmountOfNodesFromSet(int setId) {
		return partitionIndex.queryAmountOfNodes(setId);
	}
	
	public int getTotalNodeWeightFromSet(int setId) {
		return partitionIndex.queryTotalNodeWeightPerPartition(setId);
	}
	

	// **************** metodos relacionados aas edges ****************


	public void insertEdgeToCut(EdgeWrapper edge) {
		cutIndex.addEdgeToCut(edge);
	}

	public void removeEdgeFromCut(EdgeWrapper edge) {
		cutIndex.removeEdgeFromCut(edge);
	}

	public Iterable<EdgeWrapper> queryEdgesOnCut() {
		return cutIndex.queryEdgesOnCut();
	}

	 /**
	 * Faz o cálculo do cutWeight sem inserir as arestas no cutIndex, baseado em edge.isEdgeOnCut().
	 * 
	 * @param allEdges
	 */
	public int calculateEdgeCut(Iterable<EdgeWrapper> allEdges) {//update diagram
		int cutWeight = 0;
		for (EdgeWrapper edge : allEdges) {
			// se a aresta está no corte, soma o peso dela.
			if (edge.isEdgeOnCut()) {
				cutWeight += edge.getWeight();
			}
		}
		cutIndex.setCutWeight(cutWeight);
		logger.debug("CutWeight calculated: {}", getCutWeight());
		return getCutWeight();
	}

	 /**
	 * Insere as arestas no corte de acordo com edge.isEdgeOnCut().
	 *
	 * @param node
	 */
	public void createEdgeCut(Iterable<EdgeWrapper> allEdges, TransactionInterface transactionIf, PartitionKeeper partKeeper) {//update diagram
		TransactionControl tc = new TransactionControl(transactionIf);
		tc.beginTransaction();
		
		//remove o indice atual
		cutIndex.remove();
		tc.commit();
		tc.beginTransaction();
		
		//cria um indice novo
		partKeeper.createNewCutIndex();
		cutIndex = partKeeper.getCurrentCutIndex();
		for (EdgeWrapper edge : allEdges) {
			// se a aresta está no corte, adiciona ela no corte.
			if (edge.isEdgeOnCut()) {
				cutIndex.addEdgeToCut(edge);
				tc.intermediateCommit();
				
			//UTIL: não precisa mais remover pois quando inicia este método, o corte de arestas está vazio.
//			} else { // se a aresta não está no corte, remove ela do corte
//				removeEdgeFromCut(edge);
			}
		}
		tc.commit();
		logger.debug("Corte atualizado. EdgeCutWeight: {}", getCutWeight());
	}

	public int getCutWeight() {
		logger.debug("CutWeight: {}", cutIndex.getCutWeight());
		logger.trace("Arestas no corte: {}", new Object() {
			@Override
			public String toString() {
				StringBuffer b = new StringBuffer();
				int count = 0;
				for (EdgeWrapper rel : cutIndex.queryEdgesOnCut()) {
					b.append(rel + ", ");
					count++;
				}
				return " qtd: " + count + " : " + b.toString();
			}
		});
		return cutIndex.getCutWeight();
	}

	// TODO: Levar esse método para a subclasse que precisa atualizar o corte
	// junto com a mudança de partição do node.
	// A atualização do corte esta sendo feita de 2 maneiras: ou no final do algoritmo
	// ou no meio da execução. Depende do algoritmo
	// /**
	// * Remove todas as arestas do node e <br>
	// * adiciona somente as arestas do node que estão no corte.
	// *
	// * @param node
	// */
	// private void updateCut(NodeWrapper node) {
	// for (EdgeWrapper edge : node.getEdges()) {
	// // se a aresta entrou no corte, adiciona ela no corte.
	// if (edge.isEdgeOnCut()) {
	// insertEdgeToCut(edge);
	// } else { // se a aresta saiu do corte, remove ela do corte
	// removeEdgeFromCut(edge);
	// }
	// }
	// logger.debug("Corte atualizado. EdgeCutWeight: {}", getCutWeight());
	// }

	/**
	 * Creates a new BestPartition instance from the internal index. 
	 * It uses current internal index for best partition and 
	 * creates a new internal indexes for continuing the algorithm.
	 * 
	 * @param indexPart
	 * @param indexCut
	 * @return
	 */
	public BestPartition createBestPartition(TransactionInterface transactionIf, PartitionKeeper keeper) {
		PartitionIndex oldPartIndex = this.partitionIndex;
		CutIndex oldCutIndex = this.cutIndex;

		BestPartition part = new BestPartition(level, k, oldPartIndex, oldCutIndex);
		
		keeper.createNewPartitionIndex();
		keeper.createNewCutIndex();
		
		this.partitionIndex = keeper.getCurrentPartitionIndex();
		this.cutIndex = keeper.getCurrentCutIndex();
		this.partitionIndex.initialize(k);

		TransactionControl tc = new TransactionControl(transactionIf);
		tc.beginTransaction();
		for (int setId = AbstractPartition.FIRST_PART; setId <= k; setId++) {
			Iterable<NodeWrapper> nodes = oldPartIndex.queryNodes(setId);
			for (NodeWrapper node : nodes) {
				partitionIndex.addNodeToSet(setId, node);
				tc.intermediateCommit();
			}
		}
		//atualiza o corte
		cutIndex.setCutWeight(oldCutIndex.getCutWeight());
//		for (EdgeWrapper edge : oldCutIndex.queryEdgesOnCut()) {
//			cutIndex.addEdgeToCut(edge);
//			tc.intermediateCommit();
//		}
		tc.commit();
		return part;
	}

	protected Integer getLevel() {
		return level;
	}
	
	/**
	 * Prints the elements of internal sets. It's used just for debugging.
	 */
	public void printSets() {
		// TODO_OK: melhor não: verificar se esse método pode virar toString()
//		if (logger.isTraceEnabled()) {
			logger.info("Nodes from partition");
			for (int setId = 1; setId <= k; setId++) {
				final Iterable<NodeWrapper> nodes = queryNodesFromSet(setId);

				logger.info("\tset " + setId + "({}): {}", getAmountOfNodesFromSet(setId), new Object() {
					@Override
					public String toString() {
						StringBuffer b = new StringBuffer();
						for (NodeWrapper node : nodes) {
							b.append(node.getId() + ", ");
						}
						return b.toString();
					}
				});
			}
//		}
	}
	
	protected void clearInternalIndexes() {
		cutIndex.remove();
		partitionIndex.clear();
	}
}
