package br.edu.unifei.mestrado.fm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.unifei.mestrado.commons.graph.mem.GraphMem;
import br.edu.unifei.mestrado.commons.partition.BestPartition;

public class MainFMMemory {

	private static Logger logger = LoggerFactory.getLogger(MainFMMemory.class);

	public static void main(String[] args) {
		String graphFileName = null;
		double ratio = -1D;

		// executar assim: main ratio graphFileName
		if (args.length > 1) {
			ratio = Double.parseDouble(args[0]);
			graphFileName = args[1];
		} else {
			logger.error("Uso: MainFMMemory ratio graphFileName");
			System.exit(2);
		}

		logger.warn("Iniciando FM com memória... file: " + graphFileName);
		GraphMem graph = new GraphMem(graphFileName);
		long delta = System.currentTimeMillis();

		try {
			
			FM kl = new FM(graph, ratio);
			BestPartition resultPartition = kl.executeFM();
			delta = System.currentTimeMillis() - delta;
			resultPartition.exportPartition("fm-mem.out", graphFileName, delta);
	
		} catch(Throwable e) {
			logger.error("Erro no FM com memória with file: " + graphFileName, e);
		} finally {
			logger.warn("Finalizando FM com memória - Tempo gasto: " + delta + " ms");
		}
	}

}
