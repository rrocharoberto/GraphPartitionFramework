package br.edu.unifei.mestrado.mn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.unifei.mestrado.commons.graph.GraphWrapper;
import br.edu.unifei.mestrado.commons.graph.mem.GraphMem;
import br.edu.unifei.mestrado.commons.partition.BestPartition;
import br.edu.unifei.mestrado.commons.partition.TwoWayPartition;
import br.edu.unifei.mestrado.commons.partition.index.CutIndex;
import br.edu.unifei.mestrado.commons.partition.index.PartitionIndex;
import br.edu.unifei.mestrado.kl.BKL;
import br.edu.unifei.mestrado.kl.KL;
import br.edu.unifei.mestrado.kl.util.BKLPartition;

public class MultinivelMemory extends TwoWayMultinivel {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private String graphFileName;

	public MultinivelMemory(GraphMem graph) {
		super(graph);//new GraphView());
		this.graphFileName = graph.getGraphFileName();
	}

	@Override
	protected GraphWrapper createNewGraph(int level) {
		GraphMem grafo = new GraphMem(graphFileName, level);
		return grafo;
	}
	
	@Override
	protected TwoWayPartition createNewPartition(final GraphWrapper graph, int level) {
		PartitionIndex partitionIdx = graph.getCurrentPartitionIndex();
		CutIndex cutIdx = graph.getCurrentCutIndex();
		BKLPartition partition = new BKLPartition(level, partitionIdx, cutIdx);
		return partition;
	}

	@Override
	protected BestPartition executePartition(GraphWrapper graph) {
		long delta = System.currentTimeMillis();
		KL kl = new KL(graph);

		BestPartition result = kl.executeKL();
		result.printSets();
		delta = System.currentTimeMillis() - delta;
		logger.warn("Tempo gasto no KL: " + delta + " ms");
		return result;
	}

	@Override
	protected BestPartition refinePartition(GraphWrapper graph, TwoWayPartition partition) {
		BKL bkl = new BKL(graph, (BKLPartition)partition);
		BestPartition part = bkl.executeBKL();
		return part;
	}
}
