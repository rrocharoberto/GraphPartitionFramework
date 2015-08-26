package br.edu.univas.mestrado.grafo.generator;

import java.util.Date;
import java.util.Random;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainGenerator {

	private static Logger logger = LoggerFactory.getLogger(MainGenerator.class);

	// check: GraphEditorDemo, SimpleGraphDraw
	public static void main(String[] args) {

		try {
			boolean exportFile = false;
			boolean exportDB = false;
			Integer size = null;
			Integer percArestas = null;
			if (args.length > 0) {
				size = Integer.parseInt(args[0]);
				if (args.length > 1) {
					percArestas = Integer.parseInt(args[1]);
					if (args.length > 2) {
						exportFile = args[2].equals("true");
						if (args.length > 3) {
							exportDB = args[3].equals("true");
						}
					} else {
						logger.info("Especifique as opções exportFile e exportDB");
						System.exit(3);
					}
				}
			}
			if (size == null || percArestas == null) {
				logger.info("Entre com a quantidade de vertices, percentual de arestas: ");
				Scanner sc = new Scanner(System.in);
				size = sc.nextInt();
				percArestas = sc.nextInt();
			}
			if (!exportFile && !exportDB) {
				logger.info("Especifique as opções exportFile ou exportDB");
				System.exit(4);
			}
			long seed = Integer.MAX_VALUE;

			long delta = System.currentTimeMillis();
			logger.info("Inicio: " + new Date());

			int sizeMinusOne = size - 1;
			int sumGauss = (sizeMinusOne * (sizeMinusOne + 1)) / 2;

			long maxArestas = sumGauss * percArestas / 100;
			logger.info("Generating graph with " + size + " nodes and " + maxArestas + " edges...");

			
			GraphExporter exporter = new GraphExporter(size, maxArestas, new Random(seed));

			String fileName = "grafo_v" + size + "_p" + percArestas + "_a" + maxArestas;
			exporter.generateGrafo(fileName, exportFile, exportDB);

			delta = System.currentTimeMillis() - delta;

			logger.info("Termino - Tempo gasto: " + delta + "ms Hora: " + new Date());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
