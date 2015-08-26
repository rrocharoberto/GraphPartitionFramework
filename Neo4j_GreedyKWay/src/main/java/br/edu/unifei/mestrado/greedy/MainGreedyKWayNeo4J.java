package br.edu.unifei.mestrado.greedy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.unifei.mestrado.commons.graph.db.GraphDB;
import br.edu.unifei.mestrado.commons.partition.AbstractPartition;
import br.edu.unifei.mestrado.commons.partition.BestPartition;

public class MainGreedyKWayNeo4J {

	private static Logger logger = LoggerFactory.getLogger(MainGreedyKWayNeo4J.class);

	public static void main(String[] args) {
		String graphFileName = null;
		Integer k = null;
		List<Integer> rawSeeds = new ArrayList<Integer>();

		// executar assim: main graphFileName k seed1 seed2 ... seedN
		if (args.length < 2) {
			logger.warn("Uso: MainGreedyKWayNeo4J graphFileName k seed1 seed2 ... seedN");
			System.exit(2);
		}
		graphFileName = args[0];
		
		try {
			k = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			logger.warn("Valor de K inválido.");
			System.exit(3);
		}
		
		GraphDB graph = new GraphDB(graphFileName);
		
		int sizeNodes = graph.getSizeNodes();

		Random rand = new Random(System.currentTimeMillis());
		
		if (args.length == 2) {
			int i = AbstractPartition.FIRST_PART;
			while (i <= k) {
				int value = Math.abs(rand.nextInt()) % sizeNodes + 1;
				if(!rawSeeds.contains(value)) {
					rawSeeds.add(value);
					i++;
				}
			}
			logger.info("Randon seeds generated: {}", rawSeeds);
		} else {
			if( args.length < k + 2) {
				logger.warn("Os valores de seed insuficientes: {}. Esperado: {}.", args.length - 2, k);
				System.exit(4);
			}
			for (int i = 2; i < args.length; i++) {
				try {
					rawSeeds.add(Integer.parseInt(args[i]));
				} catch (NumberFormatException e) {
					logger.warn("Valor de seed inválido: {}", args[i]);
					System.exit(5);
				}
			}
		}
		
		logger.warn("Iniciando GreedyK-Way com Neo4J... file: " + graphFileName);
		long delta = System.currentTimeMillis();

		try {
			
			GreedyKWay gkw = new GreedyKWay(graph, k, rawSeeds);
			BestPartition resultPartition = gkw.executeGreedyKWay();
			delta = System.currentTimeMillis() - delta;
			resultPartition.exportPartition("gkw-db.out", graphFileName, delta);
	
		} catch(Throwable e) {
			logger.error("Erro no GreedyK-Way com Neo4J with file: " + graphFileName, e);
		} finally {
			logger.warn("Finalizando GreedyK-Way com Neo4J - Tempo gasto: " + delta + " ms");
		}
	}
}
