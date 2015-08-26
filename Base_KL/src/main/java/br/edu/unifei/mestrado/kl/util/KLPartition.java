package br.edu.unifei.mestrado.kl.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.unifei.mestrado.commons.graph.GraphWrapper;
import br.edu.unifei.mestrado.commons.graph.NodeWrapper;
import br.edu.unifei.mestrado.commons.graph.TransactionControl;
import br.edu.unifei.mestrado.commons.partition.TwoWayPartition;
import br.edu.unifei.mestrado.commons.partition.index.CutIndex;
import br.edu.unifei.mestrado.commons.partition.index.PartitionIndex;

public class KLPartition extends TwoWayPartition {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public KLPartition(Integer level, PartitionIndex partitionIdx, CutIndex cutIdx) {
		super(level, partitionIdx, cutIdx);
	}

	/**
	 * Exchange the pairs of the initial partition
	 * 
	 * @param moves
	 *            Elements to be moved
	 * @param graph
	 *            Used to control the transaction.
	 */
	public void exchangePairs(List<NodePair> moves, GraphWrapper graph) {
		TransactionControl transaction = new TransactionControl(graph);
		try {
			logger.debug("Trocando os {} pares.", moves.size());
			transaction.beginTransaction();
			for (NodePair pair : moves) {

				logger.debug("Exchanging pair {}", pair);

				int pI = pair.getI().getPartition();
				int pJ = pair.getJ().getPartition();

				NodeWrapper nodeI = graph.getNode(pair.getI().getNodeId());
				nodeI.setPartition(pJ);
				super.updateNodePartition(nodeI, pI, pJ);

				NodeWrapper nodeJ = graph.getNode(pair.getJ().getNodeId());
				nodeJ.setPartition(pI);
				super.updateNodePartition(nodeJ, pJ, pI);

				transaction.intermediateCommit();
			}
		} catch (Exception e) {
			logger.error("Error exchanging " + moves.size() + " pairs.", e);
		} finally {
			transaction.commit();
		}
	}

}
