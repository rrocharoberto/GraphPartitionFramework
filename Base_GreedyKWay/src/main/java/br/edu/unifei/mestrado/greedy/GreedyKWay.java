package br.edu.unifei.mestrado.greedy;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.unifei.mestrado.commons.algo.AlgorithmObject;
import br.edu.unifei.mestrado.commons.graph.EdgeWrapper;
import br.edu.unifei.mestrado.commons.graph.GraphWrapper;
import br.edu.unifei.mestrado.commons.graph.NodeWrapper;
import br.edu.unifei.mestrado.commons.graph.TransactionControl;
import br.edu.unifei.mestrado.commons.partition.AbstractPartition;
import br.edu.unifei.mestrado.commons.partition.BestPartition;
import br.edu.unifei.mestrado.commons.view.ViewListener;
import br.edu.unifei.mestrado.greedy.util.GreedyKWayPartition;

/*
 * GreedyKWay.c
 *
 *  Created on: Jun 4, 2011
 *      Author: roberto
 */

public class GreedyKWay extends AlgorithmObject {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private GreedyKWayPartition partition = null;
	
	private int k;
	private List<Integer> seeds;

	public GreedyKWay(GraphWrapper graph, int k, List<Integer> seeds) { //update diagram
		super(graph);
		this.k = k;
		this.seeds = seeds;
		this.partition = new GreedyKWayPartition(graph.getLevel(), k, 
				graph.getCurrentPartitionIndex(), graph.getCurrentCutIndex());
	}

	public GreedyKWay(GraphWrapper graph, int k, List<Integer> seeds, ViewListener view) { //update diagram
		super(graph, view);
		this.k = k;
		this.seeds = seeds;
		this.partition = new GreedyKWayPartition(graph.getLevel(), k, 
				graph.getCurrentPartitionIndex(), graph.getCurrentCutIndex());
	}

	/**
	 * Atualiza os valores de N para os nós que ainda não foram escolhidos. <br>
	 * Os nós são visinhos de justPicked, pq ele entrou em algum set então seus visinhos não escolhidos terão
	 * seu N incrementado.
	 * 
	 * //WARN: não funcionou pq um mesmo nó pode ser ???
	 * 
	 * @param justPicked
	 */
	private List<Long> updateNForNeighborsOfNode(NodeWrapper justPicked) {
		logger.debug("Atualizando N para os vizinhos do node {}.", justPicked.getId());
		
		List<Long> neighborsOfNode = new ArrayList<Long>();
		// UTIL: o subSet é o mesmo do node justPicked.
		SubSet set = partition.getSetById(justPicked.getPartition());

		Iterable<EdgeWrapper> arestas = justPicked.getEdges();
		
		//UTIL: tem que ser de todos os vizinhos, não somente os que acabaram de entrar na fronteira
		for (EdgeWrapper edge : arestas) { 
			NodeWrapper other = edge.getOtherNode(justPicked);
			int partitionOther = other.getPartition();
			if (partitionOther == AbstractPartition.NO_PARTITION) {// TODO_OK: rever isso!. Só incrementa o N e adiciona na lista de vizinhos os nodes que ainda não estão em nenhuma partição
				// inclui o nó na lista de candidatos para do set
				// visinhos externos
				set.incrementN(other.getId());
				
				neighborsOfNode.add(other.getId());
				// pega o set onde está a outra ponta
			} else { 
				// UTIL: se a outra ponta já foi usada
				if (partitionOther != set.getId()) { // se a outra ponta está em outra partição, então a aresta está no corte
					partition.insertEdgeToCut(edge);
//					setHelper.addEdgeToCut(partition, edge);
//					setHelper.addEdgeToCut(set.getId(), edge);
				}
//TODO_OK: Verificar se precisa desse else. Não precisa pois a partir do momento que a aresta entra no corte, ela não sai mais.
//				} else {
//					partition.removeEdgeFromCut(edge);
//				}
			}
		}
		partition.printNForSet(set);
		
		return neighborsOfNode;
	}

	/**
	 * Atualiza vertices da fronteira.
	 */
	private void updateFrontier(NodeWrapper baseNode) {

		@SuppressWarnings("unused")
		List<NodeWrapper> justIncluded = partition.includeNodesToFrontier(baseNode);
		partition.printFrontier();

		// UTIL: só recalcula para os nós que estão ligados aos que acabaram de serem incluidos na fronteira.
		// não pode ser todos da fronteira, senão vai incrementar o N novamente, daí fica com N maior que o
		// correto.
		// TODO_OK: não funciona, pq o nó 2 não é atualizado mesmo o nó 7 entrando em S1. E o nó 2 deveria ter
		// seu N incrementado. -> ver UTIL do método updateNForNeighborsOfNode()
		// setHelper.updateNArrayToFrontier(justIncluded);
		List<Long> neighborsOfNode = updateNForNeighborsOfNode(baseNode);

		partition.updateDiffFor(neighborsOfNode);

		// setHelper.printExt();
		// setHelper.printDiff();
	}

	private NodeWrapper peekNode(int setId, long nodeId) {
		NodeWrapper node = getGraph().getNode(nodeId);
		node.lock();
		node.setPartition(setId);
		partition.insertNodeToIndex(setId, node);

		partition.removeFromFrontier(nodeId);

		// UTIL: precisa remover o node de todos os sets, e não somente do set que ele foi adicionado
		for (SubSet setTmp : partition.getSets().values()) { // sets
			setTmp.removeNodeFromBucket(nodeId);
		}

		// updateNForNeighborsOfNode(vertice);
		return node;
	}

	private List<NodeWrapper> chooseNodes(List<Integer> seeds) {
		List<NodeWrapper> nodes = new ArrayList<NodeWrapper>();
		int setId = AbstractPartition.FIRST_PART;
		for (Integer nodeId : seeds) {
			NodeWrapper node = peekNode(setId, nodeId);
			nodes.add(node);
			
			partition.insertNodeToIndex(setId, node);

			setId++;
		}
		return nodes;
	}

	/**
	 * Escolhe k vertices iniciais e coloca cada um em um set diferente.
	 */
	private void getSeedNodes(List<Integer> seeds) {
		// chooseRandomVertices(k);
		TransactionControl tc = new TransactionControl(getGraph());
		tc.beginTransaction();
		List<NodeWrapper> nodes = chooseNodes(seeds);
		tc.intermediateCommit();
		for (NodeWrapper node : nodes) {
			updateFrontier(node);
			tc.intermediateCommit();
		}
		tc.commit();
	}

	private SubSet getMinP() {
		// UTIL: verificar onde faz o setMinval
		// no Bucket.updateDiff
		// no Bucket.calculateMinval

		int avgSubSetSize = 0;
		int sumSubSetSize = 0;
		for (SubSet set : partition.getSets().values()) { // sets
			sumSubSetSize += partition.getAmountOfNodesFromSet(set.getId());
		}
		avgSubSetSize = sumSubSetSize / partition.getK();

		SubSet addset = null;
		int minval = Integer.MAX_VALUE;
		for (SubSet set : partition.getSets().values()) { // sets
			if (partition.getAmountOfNodesFromSet(set.getId()) <= avgSubSetSize) {
				Integer min = set.getMinVal();// TODO_OK: verificar se aqui pode vir null. Pode se não houver mais node no DiffMap
				if (min != null && min < minval) {// pega o minval minimo e seu set
					minval = set.getMinVal();
					addset = set;
				}
			}
		}
		if(addset == null) {
			throw new RuntimeException("addset is null");
		}
		logger.debug("getMinP: Set escolhido: {} com minVal {}.", addset, addset.getMinVal());
		return addset;
	}

	/**
	 * Pega o vertice com maior valor de N.
	 * 
	 * @param set
	 * @return
	 */
	private Long getMaxN(SubSet set) {

		// TODO: verificar essa lógica aqui.
		/*
		 * int diff = minValArray[set]; int vertice = bucketArray[set][diff][sizeArray[set][diff]];
		 * deleteVertice(set, vertice); return vertice;
		 */

		// UTIL: usar o diff para pegar o melhor elemento
		int diff = set.getMinVal();
		long escolhido = set.getAnyNodeWithDiff(diff);

		logger.debug("Vertice escolhido: " + escolhido + " com diff " + diff);
		return escolhido;
	}

	/*
	 * Metodo GreedyKWay.
	 */
	public BestPartition executeGreedyKWay() {
		
		initView(getGraph(), k);
		
		long tCalc = 0;
		long tMinP = 0;
		long tMaxN = 0;

		long tIncF = 0;
		long tUpdN = 0;
		long tUpdD = 0;

		long tUpdF = 0;
		long tPicV = 0;
		long tSeed = 0;

		long delta = 0;

		delta = System.currentTimeMillis();
		getGraph().unlockAllNodes();
		getGraph().resetPartitionAllNodes();
		long tUnlockReset = System.currentTimeMillis() - delta;
		logger.warn("tUnlockReset: " + tUnlockReset);

		int remaining = getGraph().getSizeNodes();
		delta = System.currentTimeMillis();
		getSeedNodes(seeds);
		
		repaint();
		
		tSeed += System.currentTimeMillis() - delta;
		logger.warn("tSeed: " + tSeed);
		remaining -= k;

		TransactionControl tc = new TransactionControl(getGraph());
		tc.beginTransaction();
		while (partition.getFrontierCount() > 0) {
			if ((remaining % 100) == 0) {
				logger.warn("Nós restantes: " + remaining + " Fronteira: " + partition.getFrontierCount());
			}
			delta = System.currentTimeMillis();
			// setHelper.calculateMinvalForAll(frontier.getVertices(), remaining);
			tCalc += System.currentTimeMillis() - delta;

			delta = System.currentTimeMillis();
			SubSet addset = getMinP();
			tMinP += System.currentTimeMillis() - delta;

			delta = System.currentTimeMillis();
			Long nodeIdChoosen = getMaxN(addset);
			tMaxN += System.currentTimeMillis() - delta;

			if (nodeIdChoosen == null) {
				logger.warn("Escolheu um vertice null. Verificar. fronteira.size: "
						+ partition.getFrontierCount() + " remaining: " + remaining);
				break;
			}
			logger.debug("Adicionando vertice {} no set {}.", nodeIdChoosen, addset);

			delta = System.currentTimeMillis();
			NodeWrapper Choosen = peekNode(addset.getId(), nodeIdChoosen);
			tc.intermediateCommit();
			tPicV += System.currentTimeMillis() - delta;

			repaint();

			// delta = System.currentTimeMillis();
			// frontier.includeNodesToFrontier(vertice);
			// tIncF += System.currentTimeMillis() - delta;
			//
			// delta = System.currentTimeMillis();
			// updateNArrayToFrontier();
			// tUpdN += System.currentTimeMillis() - delta;
			//
			// delta = System.currentTimeMillis();
			// updateDiffFor();
			// tUpdD += System.currentTimeMillis() - delta;
			// tUpdF = tIncF + tUpdN + tUpdD;

			delta = System.currentTimeMillis();
			updateFrontier(Choosen);
			tc.intermediateCommit();
			tUpdF += System.currentTimeMillis() - delta;

			remaining--;
		}
		tc.commit();
		
		logger.warn("Estatisticas: ");
		logger.warn("tCalc: " + tCalc);
		logger.warn("tMinP: " + tMinP);
		logger.warn("tMaxN: " + tMaxN);
		logger.warn("");
		logger.warn("tPicV: " + tPicV);
		logger.warn("tIncF: " + tIncF);
		logger.warn("tUpdN: " + tUpdN);
		logger.warn("tUpdD: " + tUpdD);
		logger.warn("tUpdF: " + tUpdF);

		// TODO_OK: calcular valor do corte
		delta = System.currentTimeMillis();
		int cutWeight = partition.calculateEdgeCut(getGraph().getAllEdgesStatic());
		long tCut = System.currentTimeMillis() - delta;
		logger.warn("CutWeight: " + cutWeight + " tempo gasto para calcular o cutWeight: " + tCut + " ms.");

		partition.printSets();
		
		BestPartition bestPartition = partition.createBestPartition(getGraph(), getGraph());
		return bestPartition;
	}

	public void execute() {
		long time = System.currentTimeMillis();
		try {
			executeGreedyKWay();
		} catch (Throwable e) {
			logger.error("Erro executando GreedyKWay.", e);
		}
		time = System.currentTimeMillis() - time;
		logger.warn("Fim do GreedyKWay. Tempo gasto: " + time + " ms File: " + getGraph().getGraphFileName());
	}

}