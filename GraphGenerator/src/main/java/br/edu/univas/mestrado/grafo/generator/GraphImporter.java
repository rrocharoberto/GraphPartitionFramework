package br.edu.univas.mestrado.grafo.generator;

import br.edu.unifei.mestrado.commons.graph.GraphFileReader;
import br.edu.unifei.mestrado.commons.graph.GraphWrapper;
import br.edu.unifei.mestrado.commons.graph.db.GraphDB;

public class GraphImporter {

	public static final String dirImportados = "importados/";
	public static final String DB_PATH = "neo4j-";

	public void importGraph(String graphFileName, String sourceFileName) {

		// inicializa o grafo
		String databaseName = dirImportados + "/" + DB_PATH + graphFileName;
		GraphDB graphDb = new GraphDB(databaseName, GraphWrapper.NO_LEVEL, GraphDB.REUSE_DB_NO);

		GraphFileReader reader = new GraphFileReader();
		reader.importGraph(graphDb, sourceFileName);
	}
}
