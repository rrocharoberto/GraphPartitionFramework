package br.edu.unifei.mestrado.commons.graph;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphFileReader {

	private static Logger logger = LoggerFactory.getLogger(GraphFileReader.class);
		
	public void importGraph(GraphWrapper graph, String sourceFileName) {

		// abre o arquivo
		InputStream in = null;
		try {
			in = new FileInputStream(sourceFileName);
		} catch (FileNotFoundException e) {
			logger.error("Error reading file " + sourceFileName, e);
			throw new RuntimeException("Error reading file " + sourceFileName, e);
		}

		logger.debug("Lendo arquivo: " + sourceFileName);

		String delims = "[\\ ]+";

		long idEdge = 0;

		TransactionControl tc = new TransactionControl(graph);
		try {
			Scanner sc = new Scanner(in);
			String header = sc.nextLine();
	
			String[] headItens = header.split(delims);
	
			int sizeNodes = Integer.parseInt(headItens[0]);
			int sizeEdges = Integer.parseInt(headItens[1]);
	
			int format = 0;
			if (headItens.length > 2) {
				format = Integer.parseInt(headItens[2]);
			}
			boolean hasNodeWeight = false;
			boolean hasEdgeWeitht = false;
			if (format == 1) {
				hasEdgeWeitht = true;
			} else if (format == 10) {
				hasNodeWeight = true;
			} else if (format == 11) {
				hasNodeWeight = true;
				hasEdgeWeitht = true;
			}
	
			tc.beginTransaction();
			logger.info("Creating {} nodes.", sizeNodes);
			for (int i = 1; i <= sizeNodes ; i++) {
				graph.createNode(i, 1);
				tc.intermediateCommit();
			}
			tc.commit();
			long nodeId = 0;
			
			int nodeWeight = 1;
			int edgeHeight = 1;
	
			logger.info("Creating {} edges.", sizeEdges);
	
			tc.beginTransaction();
			while (nodeId < sizeNodes) {
				String line = sc.next() + sc.nextLine();
				if(line.startsWith("%") || line.startsWith("#")) { //comentarios
					continue;
				}
				nodeId++;
				String[] splited = line.split("\\ ");
				if (splited.length > 0) {
					int i = 0;
					if(hasNodeWeight) {
						nodeWeight = Integer.parseInt(splited[i++]);
						NodeWrapper node = graph.getNode(nodeId);
						node.setWeight(nodeWeight);
					}
					while (i < splited.length) {
						String otherNodeId = splited[i++];
						Long nodeB = Long.parseLong(otherNodeId);
						if(hasEdgeWeitht) {
							edgeHeight = Integer.parseInt(splited[i++]);
						}
						
						//indica que a aresta já foi criada de nodeId para nodeB
						if(nodeB < nodeId) {
							continue;
						}
	
						// UTIL: se existir uma aresta para o proprio vertice, ela
						// eh descartada.
						if (nodeId == nodeB) {
							continue;
						}
						idEdge++;
						graph.createEdge(idEdge, edgeHeight, nodeId, nodeB);
						tc.intermediateCommit();
						
						//just for log
						if(idEdge % ((int)sizeEdges / 100 + 1) == 0 ) {
							logger.info("Creating edge {} percent {}.", idEdge, (100 * idEdge / sizeEdges));
						}
					}
				}
			}
		} catch(Exception e) {
			logger.error("Error importing file " + sourceFileName, e);
		} finally {
			tc.commit();
		}
		logger.info("Amount of edges created: {}.", idEdge);
	}
	
//	private void oldGraphFileReaderFromGraphMem() {
//		InputStream in = System.in;
//		if (graphFileName != null) {
//			try {
//				in = new FileInputStream(graphFileName);
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//				throw new RuntimeException(e);
//			}
//		}
//
//		if (in == System.in) {
//			System.out.println("Digite os valores do grafo: ");
//		} else {
//			logger.debug("Lendo arquivo: " + graphFileName);
//		}
//		Scanner sc = new Scanner(in);
//		int size = sc.nextInt();
//
//		for (int n = 0; n < size; n++) {
//			String line = sc.next() + sc.nextLine();
//			String[] splited = line.split("\\ ");
//			if (splited.length > 0) {
//				String nodeId1 = splited[0];
//				Long nodeA = Long.parseLong(nodeId1);
//
//				for (int i = 1; i < splited.length; i++) {
//					String nodeId2 = splited[i];
//					Long nodeB = Long.parseLong(nodeId2);
//
//					// UTIL: se existir uma aresta para o proprio vertice, ela eh descartada.
//					if (nodeA == nodeB) {
//						continue;
//					}
//					createNode(nodeA, 1);
//					createNode(nodeB, 1);
//					createEdge(idEdge, 1, nodeA, nodeB);
//					idEdge++;
//				}
//			}
//		}
//	}
}
