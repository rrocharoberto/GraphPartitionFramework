package br.edu.unifei.mestrado.generator;

import java.io.IOException;
import java.util.Collection;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.stream.file.FileSinkBase;

public class FileSinkJostle extends FileSinkBase {

	@Override
	protected void exportGraph(Graph graph) {
		long timeId = 0;

		try {
			output.write("" + graph.getNodeCount() + " " + graph.getEdgeCount() + "\n");
			output.write("#atributes:" + "\n");
			for (String key : graph.getAttributeKeySet()){
				output.write("#" + key + ": " + graph.getAttribute(key) + "\n");
			}

			for (Node node : graph) {
				String nodeId = node.getId();
				Collection<Edge> edges = node.getEdgeSet();
				for (Edge edge : edges) {
					if(edge.getSourceNode().getId() == nodeId) {
						output.write(edge.getTargetNode().getId() + " ");
					} else {
						output.write(edge.getSourceNode().getId() + " ");
					}
				}
				output.write("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void graphAttributeAdded(String sourceId, long timeId, String attribute, Object value) {
	}

	@Override
	public void graphAttributeChanged(String sourceId, long timeId, String attribute, Object oldValue, Object newValue) {
	}

	@Override
	public void graphAttributeRemoved(String sourceId, long timeId, String attribute) {
	}

	@Override
	public void nodeAttributeAdded(String sourceId, long timeId, String nodeId, String attribute, Object value) {
	}

	@Override
	public void nodeAttributeChanged(String sourceId, long timeId, String nodeId, String attribute, Object oldValue,
			Object newValue) {
	}

	@Override
	public void nodeAttributeRemoved(String sourceId, long timeId, String nodeId, String attribute) {
	}

	@Override
	public void edgeAttributeAdded(String sourceId, long timeId, String edgeId, String attribute, Object value) {
	}

	@Override
	public void edgeAttributeChanged(String sourceId, long timeId, String edgeId, String attribute, Object oldValue,
			Object newValue) {
	}

	@Override
	public void edgeAttributeRemoved(String sourceId, long timeId, String edgeId, String attribute) {		
	}

	@Override
	public void nodeAdded(String sourceId, long timeId, String nodeId) {
	}

	@Override
	public void nodeRemoved(String sourceId, long timeId, String nodeId) {
	}

	@Override
	public void edgeAdded(String sourceId, long timeId, String edgeId, String fromNodeId, String toNodeId,
			boolean directed) {
	}

	@Override
	public void edgeRemoved(String sourceId, long timeId, String edgeId) {
	}

	@Override
	public void graphCleared(String sourceId, long timeId) {
	}

	@Override
	public void stepBegins(String sourceId, long timeId, double step) {
	}

	@Override
	protected void outputHeader() throws IOException {
	}

	@Override
	protected void outputEndOfFile() throws IOException {
	}
	
	
}