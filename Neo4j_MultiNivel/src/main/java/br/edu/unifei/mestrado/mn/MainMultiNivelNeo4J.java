package br.edu.unifei.mestrado.mn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.unifei.mestrado.commons.graph.db.GraphDB;
import br.edu.unifei.mestrado.commons.partition.BestPartition;

public class MainMultiNivelNeo4J {

	private static Logger logger = LoggerFactory.getLogger(MainMultiNivelNeo4J.class);

	public static void main(String[] args) {
		String graphFileName = null;

		// executar assim: main graphFileName
		if (args.length == 1) {
			graphFileName = args[0];
		} else {
			logger.warn("Uso: MainMultiNivelNeo4J graphFileName");
			System.exit(2);
		}

		logger.warn("Iniciando Multinivel com Neo4J... file: " + graphFileName);
		long delta = System.currentTimeMillis();

		try {
			
			GraphDB graph = new GraphDB(graphFileName);
			TwoWayMultinivel mn = new MultinivelNeoDB(graph);
			BestPartition resultPartition = mn.executeMultilevel();
			delta = System.currentTimeMillis() - delta;
			resultPartition.exportPartition("mn-db.out", graphFileName, delta);
	
		} catch(Throwable e) {
			logger.error("Erro no Multinivel com Neo4J with file: " + graphFileName, e);
		} finally {
			logger.warn("Finalizando Multinivel com Neo4J - Tempo gasto: " + delta + " ms");
		}
	}
}
