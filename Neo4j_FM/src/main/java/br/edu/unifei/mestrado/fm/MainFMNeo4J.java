package br.edu.unifei.mestrado.fm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.unifei.mestrado.commons.graph.db.GraphDB;
import br.edu.unifei.mestrado.commons.partition.BestPartition;

public class MainFMNeo4J {

	private static Logger logger = LoggerFactory.getLogger(MainFMNeo4J.class);

	public static void main(String[] args) {
		String graphFileName = null;
		double ratio = -1D;

		// executar assim: main ratio graphFileName
		if (args.length > 1) {
			ratio = Double.parseDouble(args[0]);
			graphFileName = args[1];
		} else {
			logger.error("Uso: MainFMNeo4J ratio graphFileName");
			System.exit(2);
		}

		logger.warn("Iniciando FM com Neo4J... file: " + graphFileName);
		long delta = System.currentTimeMillis();

		try {
			
			GraphDB graph = new GraphDB(graphFileName);
			FM kl = new FM(graph, ratio);
			BestPartition resultPartition = kl.executeFM();
			delta = System.currentTimeMillis() - delta;
			resultPartition.exportPartition("fm-db.out", graphFileName, delta);
			
		} catch(Throwable e) {
			logger.error("Erro no FM com Neo4J with file: " + graphFileName, e);
		} finally {
			logger.warn("Finalizando FM com Neo4J - Tempo gasto: " + delta + " ms");
		}
	}

}
