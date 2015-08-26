package br.edu.unifei.mestrado.kl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.unifei.mestrado.commons.graph.db.GraphDB;
import br.edu.unifei.mestrado.commons.partition.BestPartition;

public class MainKLNeo4J {

	private static Logger logger = LoggerFactory.getLogger(MainKLNeo4J.class);

	public static void main(String[] args) {
		String graphFileName = null;

		// executar assim: main dbFileName
		if (args.length == 1) {
			graphFileName = args[0];
		} else {
			logger.warn("Uso: MainKLNeo4J graphFileName");
			System.exit(2);
		}
		logger.warn("Iniciando KL com Neo4J... file: " + graphFileName);
		long delta = System.currentTimeMillis();

		try {
			
			GraphDB graph = new GraphDB(graphFileName);
			KL kl = new KL(graph);
			BestPartition resultPartition = kl.executeKL();
			delta = System.currentTimeMillis() - delta;
			resultPartition.exportPartition("kl-db.out", graphFileName, delta);
			
		} catch(Throwable e) {
			logger.error("Erro no KL com Neo4J with file: " + graphFileName, e);
		} finally {
			logger.warn("Finalizando KL com Neo4J - Tempo gasto: " + delta + " ms");
		}
	}
}
