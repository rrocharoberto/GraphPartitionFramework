package br.edu.unifei.mestrado.commons.mn;

import java.util.HashMap;
import java.util.Map;

import br.edu.unifei.mestrado.commons.graph.NodeWrapper;

public class NodeContainement {

	// Node nivel mais detalhado | Id do node do nivel mais contraido
	private Map<NodeWrapper, Long> insideOf = new HashMap<NodeWrapper, Long>();

	public boolean hasInsideOf(NodeWrapper node) {
		return insideOf.containsKey(node);
	}

	public void setInsideOf(NodeWrapper node, Long coarsedNodeId) {
		insideOf.put(node, coarsedNodeId);
	}

	public Long getNodeInsideOf(NodeWrapper node) {
		return insideOf.get(node);
	}

	public Map<NodeWrapper, Long> getInsideOf() {
		return insideOf;
	}
}
