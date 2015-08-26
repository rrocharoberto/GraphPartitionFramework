package br.edu.unifei.mestrado.view;

import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import br.edu.unifei.mestrado.commons.graph.EdgeWrapper;
import br.edu.unifei.mestrado.commons.graph.GraphWrapper;
import br.edu.unifei.mestrado.commons.graph.NodeWrapper;

public class GraphJungConverter {

	public static UndirectedSparseGraph<NodeWrapper, EdgeWrapper> convertGraph(GraphWrapper graph) {
		UndirectedSparseGraph<NodeWrapper, EdgeWrapper> newGraph = new UndirectedSparseGraph<NodeWrapper, EdgeWrapper>();
		
		for (NodeWrapper node : graph.getAllNodes()) {
			newGraph.addVertex(node);
		}
		for (EdgeWrapper rel : graph.getAllEdges()) {
			newGraph.addEdge(rel, rel.getStartNode(), rel.getEndNode());
		}
		return newGraph;
	}
}
