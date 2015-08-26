package br.edu.univas.mestrado.grafo.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GraphExporter {

	public static final String dirGerados = "gerados/";
	public static final String DB_PATH = "neo4j-";

	private final int maxNodes;
	private final long maxArestas;
	private final Random random;

	private Map<Long, Map<Long, Long>> edges = new HashMap<Long, Map<Long, Long>>();

	private long edgeCount = 0;

	public GraphExporter(int maxNodes, long maxArestas, Random random) {
		this.maxNodes = maxNodes;
		this.maxArestas = maxArestas;
		this.random = random;
	}

	public void generateGrafo(String databaseName, boolean exportFile, boolean exportDB) {

		Map<Long, Long> mapPercentual = new HashMap<Long, Long>();

		File gerados = new File(dirGerados);
		if (!gerados.exists()) {
			gerados.mkdirs();
		}
		databaseName = gerados + "/" + DB_PATH + databaseName;
		String fileName = databaseName + ".txt";
		GrafoNeoDB graphDb = null;

		if (exportDB) {
			graphDb = new GrafoNeoDB(databaseName);
			graphDb.initDB();
		}
		PrintWriter bos = null;
		if (exportFile) {
			File file = new File(fileName);
			file.delete();
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(fileName);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			bos = new PrintWriter(fos);
			bos.println(maxArestas);
		}
		if (exportDB) {
			graphDb.beginTransaction();

			for (int i = 0; i < maxNodes; i++) {
				// TODO: setar o peso do node, hoje esta 1
				graphDb.createNode(i, 1);
			}
		}

		// cria as arestas
		while (edgeCount < maxArestas) {
			long i = Math.abs(random.nextLong() % maxNodes);
			long j = Math.abs(random.nextLong() % maxNodes);

			if (i != j) { // nao deixa gerar auto-aresta
				Map<Long, Long> sm = edges.get(i);
				if (sm != null) {
					Long ij = sm.get(j);
					if (ij != null) {
						continue;// já existe aresta i - j
					}
					sm.put(j, j);
				} else {
					sm = edges.get(j);
					if (sm != null) {
						Long ji = sm.get(i);
						if (ji != null) {
							continue;// já existe aresta j - i
						}
						sm.put(i, i);
					} else {
						sm = new HashMap<Long, Long>();
						sm.put(j, j);
						edges.put(i, sm);
					}
				}
				edgeCount++;
				// Colocar o peso aleatorio.
				if (exportDB) {
					graphDb.createChain(i, j, 1, edgeCount, 1);
				}
				if (exportFile) {
					bos.println(i + " " + j);
				}

				// esquema para fazer commit a cada numero percentual da criação
				// do grafo

				long percentual = (long) (edgeCount * 100 / maxArestas);
				if (!mapPercentual.containsKey(percentual)) {
					mapPercentual.put(percentual, percentual);
					System.out.println("Commit " + percentual + "% edgeCount: " + edgeCount);
					if (exportDB && edgeCount > 0) {
						graphDb.endTransaction();
						graphDb.finish();
						graphDb.beginTransaction();
					}

					if (exportFile) {
						bos.flush();
					}
				}
			}
		}
		if (exportFile) {
			bos.flush();
		}
		if (exportDB) {
			graphDb.endTransaction();
			graphDb.finish();
		}
	}
}
