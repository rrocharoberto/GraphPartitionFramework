package br.edu.unifei.mestrado.generator;

import java.util.HashMap;
import java.util.Map;

import org.graphstream.algorithm.generator.BaseGenerator;
import org.graphstream.graph.Node;

public class ClusterGeneratorOld extends BaseGenerator {

	private long nodeId = 0;
	private Map<Long, Node> nodeMap = new HashMap<Long, Node>();
	private long maxNodes;
	
	public ClusterGeneratorOld(long maxNodes) {
		this.maxNodes = maxNodes /2;
	}
	
	@Override
	public void begin() {
	}

	@Override
	public boolean nextEvents() {
		nodeId++;
		sendNodeAdded(sourceId, sourceTime.newEvent(), Long.toString(nodeId));
		return nodeId > maxNodes;
	}


}
