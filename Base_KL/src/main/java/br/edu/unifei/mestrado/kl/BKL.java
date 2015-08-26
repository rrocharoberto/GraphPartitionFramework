package br.edu.unifei.mestrado.kl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.unifei.mestrado.commons.graph.GraphWrapper;
import br.edu.unifei.mestrado.commons.graph.NodeWrapper;
import br.edu.unifei.mestrado.commons.graph.TransactionControl;
import br.edu.unifei.mestrado.commons.partition.BestPartition;
import br.edu.unifei.mestrado.kl.util.BKLPartition;
import br.edu.unifei.mestrado.kl.util.DArray;
import br.edu.unifei.mestrado.kl.util.KLPartition;
import br.edu.unifei.mestrado.kl.util.NodePair;

public class BKL extends KL {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Number of iteration retries if there was no exchange in the iteration (G <= 0) 
	 */
	private static int MAX_NUMBER_OF_RETRIES = 5;

	public BKL(GraphWrapper graph, KLPartition partition) {
		super(graph, partition);
	}
	
	/**
	 * UTIL: Inclui os nós adjacentes de pair.i e pair.j devido sua troca, pois eles tornaram fronteira. E
	 * depois executa o calculo dos custos normalmente, usando {@link KL#recalculateCost(NodePair, DArray)}.
	 * Tem que ser nessa ordem pq os nós incluídos na fronteira precisam ter seus valores de D calculados,
	 * devido a troca do par.
	 * 
	 * Também faz o unlock dos nodes incluidos na fronteira, pois eles não foram destravados no início da iteração (devido ao método generateBoundaryPartition())
	 * 
	 */
	@Override
	protected void recalculateCost(NodePair pair) {
		NodeWrapper nodeI = getGraph().getNode(pair.getI().getNodeId());
		NodeWrapper nodeJ = getGraph().getNode(pair.getJ().getNodeId());

		TransactionControl tc = new TransactionControl(getGraph());
		try {
			tc.beginTransaction();

			List<NodeWrapper> justIncludedI = ((BKLPartition)basePartition).addNeighborsToFrontier(nodeI);
			List<NodeWrapper> justIncludedJ = ((BKLPartition)basePartition).addNeighborsToFrontier(nodeJ);
			tc.commit();
			tc.beginTransaction();
			// calcula o D para os vizinhos de I e J e faz unlock neles
			for (NodeWrapper node : justIncludedI) {
				calculateDForNode(node);
				node.unlock();
				tc.intermediateCommit();
			}
			for (NodeWrapper node : justIncludedJ) {
				calculateDForNode(node);
				node.unlock();
				tc.intermediateCommit();
			}
	
			if ((justIncludedI.size() + justIncludedJ.size()) > 0) {
				logger.debug("Quantidade de nós incluidos na fronteira: "
						+ (justIncludedI.size() + justIncludedJ.size()));
			}
		} finally {
			tc.commit();
		}
		basePartition.printSets();

		super.recalculateCost(pair);
	}

	/*
	 * Metodo BKL.
	 */
	public BestPartition executeBKL() {
		logger.warn("Executando BKL.");
		long delta = System.currentTimeMillis();
		
//		//TODO_OK: testando, apagar essas 2 linhas! -> a partição já vem gerada boundary 
//		BKLPartition basePartition2 = new BKLPartition(level, getPartitionIndex());
//		basePartition2.generateBoundaryPartition(partitionToRefine.getEdgeCut(), getGraph());

		// TODO_OK: não precisa mais, pq o código da frontier foi pro Partition
		// //UTIL: precisa incluir os nodes iniciais na fronteira também
		// frontier.includeInitialFrontier(basePartition.getAllNodes());

		int bestCut = basePartition.getCutWeight();
		logger.warn("Corte inicial BKL: " + bestCut);

		BestPartition bestPartition = basePartition.createBestPartition(getGraph(), getGraph());

		int it = 0;
		int bestIt = 0;
		int retries = 0;
		do {
			it++;
			unlockNodesOf((BKLPartition)basePartition);

			int cut = klIteration(it);

			// se não houve trocas
			if (cut == -1) {
				// se não houve melhoras, então não continua
				break;
			} else {
				// usa a mesma partição atual, como a partição inicial do passo seguinte
				if (cut < bestCut) {
					// armazena o melhor resultado
					bestIt = it;
					bestPartition = basePartition.createBestPartition(getGraph(), getGraph());
					bestCut = cut;
				} else {
					retries++;
				}
			}
			logger.warn("Melhor corte BKL: " + bestCut + " It: " + bestIt + " retries: " + retries);
		} while(retries < MAX_NUMBER_OF_RETRIES);
		
		// logger.warn("End of partitioning.");
		delta = System.currentTimeMillis() - delta;
		logger.warn("Fim do BKL. Tempo gasto: {} ms ", delta);
		return bestPartition;
	}

	private void unlockNodesOf(BKLPartition partition) {
		TransactionControl transaction = new TransactionControl(getGraph());
		try {
			transaction.beginTransaction();
			for (NodeWrapper node : partition.getAllNodesSet1()) {
				node.unlock();
				transaction.intermediateCommit();
			}
			for (NodeWrapper node : partition.getAllNodesSet2()) {
				node.unlock();
				transaction.intermediateCommit();
			}
		} finally {
			transaction.commit();
		}
	}
	
	/**
	 * O BKL não aceita ganhos negativos.
	 */
	@Override
	protected boolean acceptNegativeGain() {
		return false;
	}
}
