package br.edu.unifei.mestrado.fm;

import br.edu.unifei.mestrado.commons.graph.NodeWrapper;

/**
 * Simple POJO class to represent a node move. *
 */
public class Move implements Comparable<Move>  {

	/** ID of the node. */
	private NodeWrapper node;
	
	/** Gain of the node. */
	private int gain;
	
	public Move(NodeWrapper node, int gain) {
		this.node = node;
		this.gain = gain;
	}
	
	@Override
	public int compareTo(Move o) {
		return o.gain - this.gain;
	}

	/**
	 * @return the node
	 */
	public NodeWrapper getNode() {
		return node;
	}

	/**
	 * @param node the node to set
	 */
	public void setNode(NodeWrapper node) {
		this.node = node;
	}

	/**
	 * @return the gain
	 */
	public int getGain() {
		return gain;
	}

	/**
	 * @param gain the gain to set
	 */
	public void setGain(int gain) {
		this.gain = gain;
	}
}