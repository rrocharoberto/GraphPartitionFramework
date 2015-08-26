package br.edu.univas.mestrado.grafo.generator;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainImporter {

	private static Logger logger = LoggerFactory.getLogger(MainImporter.class);

	public static void main(String[] args) {

		try {
			if (args.length < 2) {
				logger.info("Especifique o nome do grafo e do arquivo a ser importado.");
				System.exit(1);
			}
			String graphFileName = args[0];
			String sourceFileName = args[1];

			long delta = System.currentTimeMillis();
			logger.info("Inicio: " + new Date());

			GraphImporter importer = new GraphImporter();

			importer.importGraph(graphFileName, sourceFileName);

			delta = System.currentTimeMillis() - delta;

			logger.info("Termino importação - Tempo gasto: " + delta + "ms Hora: " + new Date());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
