package br.edu.unifei.mestrado.generator;

import org.graphstream.algorithm.generator.BaseGenerator;

public class CustomFlowerSnarkGenerator extends BaseGenerator {

	private int n;

	private int nextStarNumber = 1;

	public CustomFlowerSnarkGenerator(int n) {
		this.n = n;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#begin()
	 */
	public void begin() {
		addStar();
		addStar();
		addStar();

		addEdge(N.B, 1, N.B, 2);
		addEdge(N.B, 2, N.B, 3);
		addEdge(N.B, 3, N.B, 1);

		addEdge(N.C, 1, N.C, 2);
		addEdge(N.C, 2, N.C, 3);
		// addEdge(N.C, 3, N.D, 1);
		// addEdge(N.D, 1, N.D, 2);
		// addEdge(N.D, 2, N.D, 3);
		// addEdge(N.D, 3, N.C, 1);

//		flushCoords();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#nextEvents()
	 */
	public boolean nextEvents() {

		if(n > nextStarNumber * 3) {
			delEdge(N.B, nextStarNumber - 1, N.B, 1);
			// delEdge(N.C, nextStarNumber - 1, N.D, 1);
			// delEdge(N.D, nextStarNumber - 1, N.C, 1);
	
			addStar();
	
			addEdge(N.B, nextStarNumber - 2, N.B, nextStarNumber - 1);
			addEdge(N.B, nextStarNumber - 1, N.B, 1);
			addEdge(N.C, nextStarNumber - 2, N.C, nextStarNumber - 1);
			// addEdge(N.C, nextStarNumber - 1, N.D, 1);
			// addEdge(N.D, nextStarNumber - 2, N.D, nextStarNumber - 1);
			// addEdge(N.D, nextStarNumber - 1, N.C, 1);
	
//			flushCoords();
	
			return true;
		} else {
			return false;
		}
	}

	private void addStar() {
		int i = nextStarNumber++;

		addNode(N.A, i);
		addNode(N.B, i);
		addNode(N.C, i);
		// addNode(N.D, i);

		addEdge(N.A, i, N.B, i);
		addEdge(N.A, i, N.C, i);
		// addEdge(N.A, i, N.D, i);
	}

	protected static enum N {
		A, B, C
	}

	private void addNode(N n, int i) {
		addNode(getNodeId(n, i));
	}

	protected String getNodeId(N n, int i) {
		return String.format("%s%04d", n, i);
	}

	private void addEdge(N n1, int i1, N n2, int i2) {
		addEdge(getEdgeId(n1, i1, n2, i2), getNodeId(n1, i1), getNodeId(n2, i2));
	}

	private void delEdge(N n1, int i1, N n2, int i2) {
		delEdge(getEdgeId(n1, i1, n2, i2));
	}

	protected String getEdgeId(N n1, int i1, N n2, int i2) {
		return String.format("%s%s", getNodeId(n1, i1), getNodeId(n2, i2));
	}

	protected void flushCoords() {
		double d = 2 * Math.PI / (nextStarNumber - 1);

		for (int i = 1; i < nextStarNumber; i++) {
			sendNodeAttributeChanged(sourceId, getNodeId(N.B, i), "x", null, Math.cos((i - 1) * d));
			sendNodeAttributeChanged(sourceId, getNodeId(N.B, i), "y", null, Math.sin((i - 1) * d));

			sendNodeAttributeChanged(sourceId, getNodeId(N.A, i), "x", null, 1.5 * Math.cos((i - 1) * d));
			sendNodeAttributeChanged(sourceId, getNodeId(N.A, i), "y", null, 1.5 * Math.sin((i - 1) * d));

			sendNodeAttributeChanged(sourceId, getNodeId(N.C, i), "x", null, 2 * Math.cos((i - 1) * d - d / 4.0));
			sendNodeAttributeChanged(sourceId, getNodeId(N.C, i), "y", null, 2 * Math.sin((i - 1) * d - d / 4.0));

			// sendNodeAttributeChanged(sourceId, getNodeId(N.D, i), "x", null,
			// 2.5 * Math.cos((i - 1) * d + d / 4.0));
			// sendNodeAttributeChanged(sourceId, getNodeId(N.D, i), "y", null,
			// 2.5 * Math.sin((i - 1) * d + d / 4.0));
		}
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CustomFlowerSnarkGenerator [n=");
		builder.append(n);
		builder.append("]");
		return builder.toString();
	}
	
}
