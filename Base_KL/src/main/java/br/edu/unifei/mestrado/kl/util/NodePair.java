package br.edu.unifei.mestrado.kl.util;


public class NodePair {

	private NodeDiff i;
	private NodeDiff j;
	private int gain;
	private int actualGain;

	public NodePair() {
	}

	public NodePair(NodePair newPair) {
		i = newPair.getI();
		j = newPair.getJ();
		gain = newPair.getGain();
		actualGain = newPair.getActualGain();
	}

	public NodeDiff getI() {
		return i;
	}

	public void setI(NodeDiff i) {
		this.i = i;
	}

	public NodeDiff getJ() {
		return j;
	}

	public void setJ(NodeDiff j) {
		this.j = j;
	}

	public int getGain() {
		return gain;
	}

	public void setGain(int gain) {
		this.gain = gain;
	}

	public int getActualGain() {
		return actualGain;
	}

	public void setActualGain(int actualGain) {
		this.actualGain = actualGain;
	}

	@Override
	public String toString() {
		return "NodePair [i=" + i.getNodeId() + ", j=" + j.getNodeId() + ", g=" + gain + ", ag=" + actualGain + "]";
	}

	public String toHumanString() {
		return getI().getNodeId() + ":" + getJ().getNodeId() + " gain: " + getGain() + "|" + getActualGain();
	}

}
