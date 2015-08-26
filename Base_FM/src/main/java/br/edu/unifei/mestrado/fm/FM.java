package br.edu.unifei.mestrado.fm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.unifei.mestrado.commons.algo.AlgorithmObject;
import br.edu.unifei.mestrado.commons.graph.EdgeWrapper;
import br.edu.unifei.mestrado.commons.graph.GraphWrapper;
import br.edu.unifei.mestrado.commons.graph.NodeWrapper;
import br.edu.unifei.mestrado.commons.graph.TransactionControl;
import br.edu.unifei.mestrado.commons.partition.AbstractPartition;
import br.edu.unifei.mestrado.commons.partition.BestPartition;
import br.edu.unifei.mestrado.commons.partition.TwoWayPartition;


/**
 * Implementation of the Fiduccia-Mattheyses partitioning algorithm.
 */
public class FM extends AlgorithmObject {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private TwoWayPartition partition;

	/**
	 * Number of iteration retries if there was no exchange in the iteration (G <= 0) 
	 */
	private static int MAX_NUMBER_OF_RETIES = 5;

	private static int MAX_NUMBER_OF_ITERATIONS = 20;

	private Map<Long, Integer> gainMap;
	private Bucket bucket;
		
	/** Total node size of partition A. */
	private int sizePartitionA;
	
	/** Ratio between partitions to be used as the balance condition. */
	private double ratio;
	
	private double F1;
	private double F2;

	/**
	 * Weight of the largest cell in the network
	 */
	private int SMAX = 1;
	
	/**
	 * Constructor.
	 * @param graph Graph the algorithm will be applied to. 
	 */
	public FM(GraphWrapper graph, double ratio) {
		super(graph);
		this.ratio = ratio;
				
		gainMap = new HashMap<Long, Integer>();
		bucket = new Bucket();
		
		this.partition = new TwoWayPartition(graph.getLevel(), 
				graph.getCurrentPartitionIndex(), graph.getCurrentCutIndex());
	}

	/**
	 * Calculates the initial gains of all the graph nodes.
	 */
	private void calculateInitialGains() {// OK

		for (NodeWrapper node : getGraph().getAllNodesStatic()) {
			int gain = 0;

			for (EdgeWrapper edge : node.getEdgesStatic()) {
				NodeWrapper neighbor = edge.getOtherNode(node);
				// If nodes are in the same partition, gain will be decreased.
				// Else, gain will be increased.
				if (neighbor.getPartition() == node.getPartition())
					gain -= edge.getWeight();
				else
					gain += edge.getWeight();
			}

			gainMap.put(node.getId(), gain);
			bucket.insertGainForNode(gain, node.getId(), node.getPartition());
		}
	}
	
	/**
	 * Gets the cell with the biggest gain(positive or negative) that satisfies the balance condition.
	 * @return The best cell to be moved
	 */
	private NodeWrapper selectBestCellToMove() {
		
		NavigableSet<Integer> orderedGains = bucket.getGainsOrdered();
		
		//Iterate on gains. If all nodes with this gain can't be moved, 
		//so I get the second greatest gain, and so on.
		for (Integer gain : orderedGains) {
			if(gain == 0) {
				logger.debug("Found gain zero.");
				return null;
			}
			Map<Long, Integer> nodesWithMaxGain = bucket.getNodesWithGain(gain);
			
			for (Long nodeId : nodesWithMaxGain.keySet()) {
				int nodePartition = nodesWithMaxGain.get(nodeId);
				NodeWrapper nodeObj = getGraph().getNode(nodeId);
				int nodeWeight = nodeObj.getWeight();
				if(nodePartition == AbstractPartition.PART_1) {
					nodeWeight *= -1;//Node leaving partition A
				}
//				} else {
//					nodeWeight = 1;//Node entering to partition A
//				}
				if (checkBalanceCondition(nodeWeight)) {
	
					// remove o node do bucket
					bucket.removeNodeWithGain(gain, nodeId);
					
					// Brauer's Step 7
					
					// marca os elementos para não serem usados em futuros processamentos
					nodeObj.lock();
					
					//Updates the sizePartitionA field
					sizePartitionA +=nodeWeight;
					return nodeObj;
				} 
			}
		}

		return null;
	}
	
	/**
	 * Check if the nodeWeight is allowed for moving for keeping the balance condition satisfied.
	 * @param nodeWeight
	 * @return true if the balance condition would be kept by moving the node
	 */
	private boolean checkBalanceCondition(int nodeWeight) {

		int partitionSize = sizePartitionA + nodeWeight;
		//logger.info("partitionSize: " + partitionSize + " Part: " + (sizechange > 0?"PART_B":"PART_A"));
		if (F1 <= partitionSize && partitionSize <= F2) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Updates the gains of the cells that are neighbors to the one being moved.
	 * @param node The cell being moved.
	 */
	private void updateGainOfAffectedNodes(NodeWrapper node) { // OK
		for (EdgeWrapper edge : node.getEdgesStatic()) {

			// Gets neighbor
			NodeWrapper neighbor = edge.getOtherNode(node);

			if (!neighbor.isLocked()) {
				// Gets neighbor's gain
				int neighborGain = gainMap.get(neighbor.getId());

				int weight = edge.getWeight();

				// Get neighbor's partition
				int neighborPartition = neighbor.getPartition();

				// Get the new partition of the cell being moved
				int newPartition = (AbstractPartition.PART_1 == node.getPartition()) ? AbstractPartition.PART_2 : AbstractPartition.PART_1;

				int newNeighborGain = 0;
				// If cells are now in the same partition, decrease gain
				if (newPartition == neighborPartition) {
					newNeighborGain = neighborGain - 2 * weight; // vindo do KL
				} else { // Otherwise, increase gain
					newNeighborGain = neighborGain + 2 * weight; // vindo do KL
				}

				// Updates the gainMap
				gainMap.put(neighbor.getId(), newNeighborGain);

				// Updates the corresponding bucket
				bucket.removeNodeWithGain(neighborGain, neighbor.getId());
				bucket.insertGainForNode(newNeighborGain, neighbor.getId(), neighborPartition);
			}
		}
	}

	/**
	 * Finds the best sequence of moves (M1, M2, ..., Mk) such that G(k) is maximum. 
	 * @param moves The whole sequence of moves
	 * @return The value of k whitch provides maximum gain.
	 */
	private int findMaximumPartialGain(List<Move> moves) { //OK
		int partialGain = 0;
		int maxGain = 0;
		int maxMove = -1;
		int k = 0;
		
		for (Move move : moves) {
			k++;
			partialGain += move.getGain();
			if (partialGain > maxGain) {
				maxGain = partialGain;
				maxMove = k;
			}
		}
		logger.info("Amount of nodes to move: " + maxMove + " Total move size: " + moves.size() + " maxGain: " + maxGain);
		return maxMove;
	}
	
	/**
	 * Changes the nodes in the graph with the best sequence of moves found, making the swaps
	 * permanent before going to the next iteration. 
	 * @param moves Sequence of moves to be made permanent
	 */
	private int makeAllMovesPermanent(List<Move> moves) { //OK
		
		TransactionControl transaction = new TransactionControl(getGraph());
		try{
			transaction.beginTransaction();
			for (Move move : moves) {
				NodeWrapper node = move.getNode();
				partition.changeNodePartition(node);
				
				transaction.intermediateCommit();
			}
		} catch (Throwable e) {
			logger.error("Erro no banco trocando nodes.", e);
		} finally {
			transaction.commit();
		}

		return partition.calculateEdgeCut(getGraph().getAllEdgesStatic());
	}
	
	/**
	 * Finds the maximum gain and moves of nodes if any.
	 */
	protected int performExchange(List<Move> moves, int it) {
		int k = findMaximumPartialGain(moves);
		int cutWeight = -1;
		if (k > 0) {
			// Brauer's Step 11
			cutWeight = makeAllMovesPermanent(moves.subList(0, k));
		}
		//just for logging
		if (cutWeight == -1) {
			logger.warn("Qtd de moves: " + k + " No exchanges made.");
		} else {
			logger.warn("Qtd de moves: " + k + " cutWeight: " + cutWeight);
		}
		return cutWeight;
	}
	
	/**
	 * Execute the Fiduccia-Mattheyses algorithm.
	 * 
	 * @param itNumber
	 * @return
	 */
	private int fmIteration(int itNumber) {
		
		logger.warn("Iteracao FM " + itNumber);
		
		//variaveis de contabilidade
		long delta = System.currentTimeMillis();
		long dt, tempoGI = 0, tempoBC = 0, tempoUG = 0, tempoEX = 0;

		/**
		 * 1. Gather input data and store into the relevant structures. <br>
		 * 2. Compute gains of all cells <br>
		 * 3. i = 1, Select base cell ci that has <br>
		 * (i) max. gain, <br>
		 * (ii) satisfies balance criterion. If tie, Then use size criterion or internal connections. If no
		 * base cell, then EXIT; <br>
		 * 4. Lock ci; update gains of cells of affected critical nets <br>
		 * 5. If free cell is not-empty, Then i = i+1; select next base cell and go to step 3. <br>
		 * 6. Select best sequence of moves c1, c2, ..., ck; (1<=k <= i) such that G(k) = is maximum. If tie,
		 * then choose subset that achieves a superior performance. If G <= 0, Then EXIT <br>
		 * 7. Make all i moves permanent; Free all cells; Goto step 1.
		 */
		
		// Brauer's steps 1, 2 and 3 are not necessary as we are not using hypergraphs
		
		// Flag to determine if the partitioning is finished
//		boolean stop = false;
		
		// Create a list to store the partial moves.
		List<Move> moves = new ArrayList<Move>();
		
		// Brauer's Step 4
		// Initially divide the graph into two partitions to be refined later.
//		setInitialPartitioning();
		
		// Begin FM algorithm
			
		// Brauer's Step 5
		// Calculate the initial gains of the nodes.
		tempoGI = System.currentTimeMillis();
		calculateInitialGains();
		tempoGI = System.currentTimeMillis() - tempoGI;
		
		getGraph().unlockAllNodes();
		
		TransactionControl transaction = new TransactionControl(getGraph());
		try{
			transaction.beginTransaction();
			do {
				// Brauer's Step 6
				// Brauer's Step 7
				
				dt = System.currentTimeMillis();
				NodeWrapper bestCell = selectBestCellToMove();
				tempoBC += System.currentTimeMillis() - dt;
				
				if (bestCell == null) {
					break;
				} 
				moves.add(new Move(bestCell, gainMap.get(bestCell.getId())));
				
				// Brauer's Step 8
				dt = System.currentTimeMillis();
				updateGainOfAffectedNodes(bestCell);
				tempoUG += System.currentTimeMillis() - dt;
				
				transaction.intermediateCommit();
				
			// Brauer's Step 9
			}  while(!(bucket.isEmpty()));
		} finally {
			transaction.commit();
		}

		// Brauer's Step 10
		// Brauer's Step 11
		// Brauer's Step 12
		
		tempoEX = System.currentTimeMillis();
		int cutWeight = performExchange(moves, itNumber);
		tempoEX = System.currentTimeMillis() - tempoEX;
		
		// Brauer's Step 12
		
		delta = System.currentTimeMillis() - delta;
		logger.debug("Tempos gastos na iteração " + itNumber 
				+ " tempoGI:" +  tempoGI + " tempoBC:" + tempoBC + " tempoUG:" +  tempoUG + " tempoEX:" + tempoEX);
		
		logger.warn("Fim da iteracao FM {}. Tempo total gasto: {} ms.", itNumber, delta);		
		return cutWeight;
	}


	public BestPartition executeFM() {
		
		int sizeNodes = getGraph().getSizeNodes();
		SMAX = (int) (sizeNodes * 0.05);//5 porcento do tamanho total de vértices
		F1 = ratio * sizeNodes / 2 - SMAX;
		F2 = ratio * sizeNodes / 2 + SMAX;

		long delta = System.currentTimeMillis();
//		sizePartitionA = partition.initialFixedBalancedPartition(getGraph());
		sizePartitionA = partition.initialArbitraryPartition((int) System.currentTimeMillis(), getGraph());
		delta = System.currentTimeMillis() - delta;
		logger.debug("Tempo gasto na geração da partição inicial: " + delta + " ms");
//		repaint();

		int bestCut = partition.getCutWeight();

		BestPartition bestPartition = partition.createBestPartition(getGraph(), getGraph());

		int it = 0;
		int cut = 0;
		int bestIt = 0;
		int retries = 0;
		
		do {
			it++;

//			repaint();

			cut = fmIteration(it);

			// se não houve trocas
			if (cut == -1) {
				logger.warn("Melhor corte FM: " + bestCut + " it " + bestIt + " retries: " + retries);
				retries++;
				if(retries < MAX_NUMBER_OF_RETIES) {
					// tenta outra partição arbitrária.
					partition.initialArbitraryPartition(it, getGraph());
				}
			} else {
				// usa a mesma partição atual, como sendo a partição inicial do passo seguinte
				if (cut < bestCut) {
					// armazena o melhor resultado
					bestIt = it;
					bestPartition = partition.createBestPartition(getGraph(), getGraph());
					bestCut = cut;
				}
				logger.warn("Melhor corte FM: " + bestCut + " it " + bestIt + " retries: " + retries);
			}
		} while(retries < MAX_NUMBER_OF_RETIES && it <= MAX_NUMBER_OF_ITERATIONS);
		return bestPartition;
	}
	
	@Override
	public void execute() {
		try {
			long time = System.currentTimeMillis();
			BestPartition result = executeFM();

			time = System.currentTimeMillis() - time;
			logger.warn("Fim do FM. Tempo gasto: " + time + " ms File: " + getGraph().getGraphFileName());
			result.printSets();
		} catch (Throwable e) {
			logger.error("Erro executando FM.", e);
		}
	}
}