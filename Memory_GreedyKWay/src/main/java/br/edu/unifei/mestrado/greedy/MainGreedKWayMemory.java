package br.edu.unifei.mestrado.greedy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.unifei.mestrado.commons.graph.mem.GraphMem;
import br.edu.unifei.mestrado.commons.partition.AbstractPartition;
import br.edu.unifei.mestrado.commons.partition.BestPartition;
import br.edu.unifei.mestrado.view.GraphView;

public class MainGreedKWayMemory {
	
	private static Logger logger = LoggerFactory.getLogger(MainGreedKWayMemory.class);

	public static void main(String[] args) {
		String graphFileName = null;
		Integer k = null;
		List<Integer> rawSeeds = new ArrayList<Integer>();

		// executar assim: main graphFileName k seed1 seed2 ... seedN
		if (args.length < 2) {
			logger.warn("Uso: MainGreedKWayMemory graphFileName k seed1 seed2 ... seedN");
			System.exit(2);
		}
		graphFileName = args[0];
		
		try {
			k = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			logger.warn("Valor de K inválido.");
			System.exit(3);
		}
		
		GraphMem graph = new GraphMem(graphFileName);
		
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
		
		logger.warn("Iniciando GreedyK-Way com memória... file: " + graphFileName);
		long delta = System.currentTimeMillis();

		try {
			
			GreedyKWay gkw = new GreedyKWay(graph, k, rawSeeds, new GraphView());
			BestPartition resultPartition = gkw.executeGreedyKWay();
			delta = System.currentTimeMillis() - delta;
			resultPartition.exportPartition("gkw-mem.out", graphFileName, delta);

		} catch(Throwable e) {
			logger.error("Erro no GreedyK-Way com memória with file: " + graphFileName, e);
		} finally {
			logger.warn("Finalizando GreedyK-Way com memória - Tempo gasto: " + delta + " ms");
		}
	}
	
	/*
	 public void execute(Integer sets, List<Integer> rawSeeds) {

		int k = -1;
		List<Integer> seeds = null;
		if (sets == null) {
			Scanner sc = new Scanner(System.in);
			while (k < 2 || k > getGraph().getSizeNodes() / 3) {
				System.out.println("Digite a quantidade de partições (Max " + getGraph().getSizeNodes() / 3
						+ "): ");
				k = sc.nextInt();
			}
			seeds = readSeeds(k);
		} else {
			k = sets;
			seeds = rawSeeds;
		}
		
			private List<Integer> readSeeds(int k) {
		List<Integer> seeds = new ArrayList<Integer>();
		Scanner sc = new Scanner(System.in);
		for (int i = 0; i < k; i++) {

			System.out.println("digite o seed " + (i + 1) + ": ");
			int verticeId = sc.nextInt();
			seeds.add(verticeId);
		}
		return seeds;
	}
	 */
}
