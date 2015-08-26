package br.edu.unifei.mestrado.kl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.unifei.mestrado.commons.graph.mem.GraphMem;
import br.edu.unifei.mestrado.commons.partition.BestPartition;

public class MainKLMemory {

	private static Logger logger = LoggerFactory.getLogger(MainKLMemory.class);

	public static void main(String[] args) {
		String graphFileName = null;

		// executar assim: main dbFileName
		if (args.length == 1) {
			graphFileName = args[0];
		} else {
			logger.warn("Uso: MainKLMemory graphFileName");
			System.exit(2);
		}

//		GraphView view = new GraphView();
//		KL kl = new KL(graph, view);

		logger.warn("Iniciando KL com memória... file: " + graphFileName);
		GraphMem graph = new GraphMem(graphFileName);
		long delta = System.currentTimeMillis();

		try {

			KL kl = new KL(graph);
			BestPartition resultPartition = kl.executeKL();
			delta = System.currentTimeMillis() - delta;
			resultPartition.exportPartition("kl-mem.out", graphFileName, delta);

		} catch(Throwable e) {
			logger.error("Erro no KL com memória with file: " + graphFileName, e);
		} finally {
			logger.warn("Finalizando KL com memória - Tempo gasto: " + delta + " ms");
		}
	}
}
