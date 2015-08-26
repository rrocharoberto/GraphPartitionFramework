package br.edu.unifei.mestrado.commons.mn;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.unifei.mestrado.commons.graph.GraphWrapper;

/**
 * Armazena cada grafo correspondente a um nível.
 * 
 * @author roberto
 * 
 */
public class LevelInformation {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private int level = -1;

	private Map<Integer, GraphWrapper> gn = new HashMap<Integer, GraphWrapper>();

	// public void addGraph(GraphWrapper graph) {
	// this.addGraph(graph, null);
	// }

	public void addGraph(GraphWrapper graph) {
		level++;
		gn.put(level, graph);
	}

	public GraphWrapper getCurrentGraph() {
		if (!gn.containsKey(level)) {
			throw new RuntimeException("No graph for level " + level);
		}
		return gn.get(level);
	}

	public int getSizeNodesOfGraph() {
		return gn.get(level).getSizeNodes();
	}

	public boolean hasMoreLevels() {
		return level > 0;
	}

	public GraphWrapper removeGraph() {
		GraphWrapper graph = null;
		if (level > -1) {
			graph = gn.remove(level);
			level--;
		}
		return graph;
	}

	public GraphWrapper getPreviousGraph() {
		if (level == 0) {
			throw new RuntimeException("There is no graph above level zero.");
		}
		return gn.get(level - 1);
	}

	public int getCurrentLevel() {
		return level;
	}

	public Integer getNextLevel() {
		return level + 1;
	}

	public int getPreviousLevel() {
		if (level == 0) {
			throw new RuntimeException("The level is already zero.");
		}
		return level - 1;
	}

}
