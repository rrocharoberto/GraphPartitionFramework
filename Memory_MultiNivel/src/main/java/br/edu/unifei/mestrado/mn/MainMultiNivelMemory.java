package br.edu.unifei.mestrado.mn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.unifei.mestrado.commons.graph.mem.GraphMem;
import br.edu.unifei.mestrado.commons.partition.BestPartition;


public class MainMultiNivelMemory {

	//TODO: ver como funciona: NodeWrapper, MetaModel, 7.11. Extra features for Lucene indexes

	private static Logger logger = LoggerFactory.getLogger(MainMultiNivelMemory.class);

	public static void main(String[] args) {
		String graphFileName = null;

		// executar assim: main graphFileName
		if (args.length == 1) {
			graphFileName = args[0];
		} else {
			logger.warn("Uso: MainMultiNivelMemory graphFileName");
			System.exit(2);
		}

		logger.warn("Iniciando Multinivel com memória... file: " + graphFileName);
		GraphMem graph = new GraphMem(graphFileName);
		long delta = System.currentTimeMillis();

		try {

			TwoWayMultinivel mn = new MultinivelMemory(graph);
			BestPartition resultPartition = mn.executeMultilevel();
			delta = System.currentTimeMillis() - delta;
			resultPartition.exportPartition("mn-mem.out", graphFileName, delta);

		} catch(Throwable e) {
			logger.error("Erro no Multinivel com memória with file: " + graphFileName, e);
		} finally {
			logger.warn("Finalizando Multinivel com memória - Tempo gasto: " + delta + " ms");
		}
	}
}
