package br.edu.unifei.mestrado.commons.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GraphUtil {

	private static Logger logger = LoggerFactory.getLogger(GraphUtil.class);

	public static void printGraph(final GraphWrapper graph) {
		if (logger.isDebugEnabled()) {
			logger.debug("Informacoes do grafo: \n{} \n{}", new Object() {
				public String toString() {
					StringBuffer b = new StringBuffer();
					Iterable<EdgeWrapper> edges = graph.getAllEdgesStatic();
					b.append("Arestas: ");
					int count = 0;
					for (EdgeWrapper aresta : edges) {
						b.append(aresta.getId()).append(": ").append(aresta.getStartNode().getId()).append("-")
								.append(aresta.getEndNode().getId()).append(", ");
						count++;
						if (count > 100) {
							b.append("Arestas interrompidas maior que 100");
							break;
						}
					}
					return b.toString();
				}
			}, new Object() {
				public String toString() {
					StringBuffer b = new StringBuffer();
					Iterable<NodeWrapper> nodes = graph.getAllNodesStatic();
					b.append("Vertices: ");
					int count = 0;
					for (NodeWrapper node : nodes) {
						b.append(node.getId()).append(", ");
						count++;
						if (count > 100) {
							b.append("Nós interrompidos maior que 100");
							break;
						}
					}
					return b.toString();
				}
			});
		}
	}

}
